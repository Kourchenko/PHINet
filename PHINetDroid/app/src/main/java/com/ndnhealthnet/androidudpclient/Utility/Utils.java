package com.ndnhealthnet.androidudpclient.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PacketDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.DB.DatabaseHandler;
import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * Class facilitates user credential storage, which is necessary for NDN
 * communication - as well as numerous other helpful, miscellaneous features.
 */
public class Utils {

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to save supplied value in shared preferences against given key.
     * @param context Context of caller activity
     * @param key Key of value to save against
     * @param value Value to save
     */
    public static boolean saveToPrefs(Context context, String key, String value) {

        if (context == null || key == null || value == null
                || (!key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY) // key must equal valid key
                && !key.equals(ConstVar.PREFS_LOGIN_USER_ID_KEY)
                && !key.equals(ConstVar.PREFS_USER_TYPE_KEY))) { // otherwise, it's invalid
            return false;
        }

        if (key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY)) {
            value = BCrypt.hashpw(key, BCrypt.gensalt()); // hash if password
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();

        return true;
    }

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned if no value found or if error occurred.
     * 
     * @param context Context of caller activity
     * @param key Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue) {

        if (context == null || key == null || defaultValue == null
                || (!key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY) // key must equal valid key
                && !key.equals(ConstVar.PREFS_LOGIN_USER_ID_KEY)
                && !key.equals(ConstVar.PREFS_USER_TYPE_KEY))) { // otherwise, it's invalid
            return null;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Forwards Interest to several nodes from FIB
     *
     * @param interest to be forwarded
     * @param context used to access DB
     */
    public static void forwardInterestPacket(Interest interest, Context context) {

        ArrayList<FIBEntry> fibEntries = DBSingleton.getInstance(context).getDB().getAllFIBData();

        // TODO - improve forwarding strategy

        boolean forwardedToServer = false;

        // only forward to ceil(sqrt(FIB.size()))
        for (int i = 0; i < Math.ceil(Math.sqrt(fibEntries.size())); i++) {

            forwardedToServer |= fibEntries.get(i).getIpAddr().equals(ConstVar.SERVER_IP);

            new UDPSocket(ConstVar.PHINET_PORT, fibEntries.get(i).getIpAddr())
                    .execute(interest.wireEncode().getImmutableArray()); // send Interest now
        }

        if (!forwardedToServer) {
            // always forward Interest to server
            new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP)
                    .execute(interest.wireEncode().getImmutableArray()); // send Interest now
        }
    }

    /**
     * Determines whether packet is valid given timeString and freshnessPeriod.
     *
     * NOTE: this method is still speculative.
     * TODO - How to better determine validity?
     *
     * @param freshnessPeriod of packet in question in milliseconds
     * @param timeString of packet in question
     * @return boolean denoting whether packet is still valid given params
     */
    public static boolean isValidFreshnessPeriod(int freshnessPeriod, String timeString) {

        try {
            String suspectInterval = timeString;

            if (timeString.contains("||")) {
                // assumed Interval (it has the interval parsing characters '||')
                // application-convention: we'll check freshnessPeriod against start interval

                suspectInterval = timeString.split("\\|\\|")[0]; // set to start interval
            }

            Calendar currentTime = Calendar.getInstance();
            Calendar timeStringPlusFreshnessTime = Calendar.getInstance();

            // suspect Interval syntax: "yyyy-MM-ddTHH.mm.ss.SSS"
            String[] suspectIntervalComponents = suspectInterval.split("T");
            String[] suspectIntervalShortComponents = suspectIntervalComponents[1].split("\\.");
            String[] suspectIntervalLongComponents = suspectIntervalComponents[0].split("-");

            // indexes chosen based on syntax "yyyy-MM-ddTHH.mm.ss.SSS"
            int packetYear = Integer.parseInt(suspectIntervalLongComponents[0]);
            int packetMonth = Integer.parseInt(suspectIntervalLongComponents[1]) - 1; // reverse offset
            int packetDay = Integer.parseInt(suspectIntervalLongComponents[2]);
            int packetHour = Integer.parseInt(suspectIntervalShortComponents[0]);
            int packetMinute = Integer.parseInt(suspectIntervalShortComponents[1]);
            int packetSecond = Integer.parseInt(suspectIntervalShortComponents[2]);
            int packetMillisecond = Integer.parseInt(suspectIntervalShortComponents[3]);

            // update second given millisecond from timeString and freshnessPeriod
            packetSecond += Math.round((packetMillisecond + freshnessPeriod)/1000);

            timeStringPlusFreshnessTime.set(packetYear, packetMonth, packetDay,
                    packetHour, packetMinute, packetSecond);

            // if the (freshnessPeriod + timeString) isn't before currentTime, then packet is valid
            return timeStringPlusFreshnessTime.before(currentTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error in utils.isValidFreshnessPeriod(): " + e);
        }
    }

    /**
     * Method takes integer date inputs and returns the application-specific time string. The last
     * parameter is disregarded unless input is invalid; then it's used to generate default.
     *
     * If an input is invalid, method returns default timeString. As timeStrings are often
     * combined to form intervals, the final parameter determines whether the timeString
     * will be used for the start (previous year) or end (next year) of an interval.
     *
     * Output Syntax: "yyyy-MM-ddTHH.mm.ss.SSS"
     *
     * @param year to be converted
     * @param month to be converted
     * @param day to be converted
     * @param isEndDate whether output is for start (previous year) or end (next year) of an interval
     * @return the application-specific timeString given input
     */
    public static String generateTimeStringFromInts(int year, int month, int day, boolean isEndDate) {

        int selectedYear = year;
        int selectedMonth = month;
        int selectedDay = day;

        // attempts to determine whether an interval has been selected
        if (year <= 0 || month < 0 || day < 0) {
            // an input is invalid, set

            Calendar now = Calendar.getInstance();

            selectedMonth = now.get(Calendar.MONTH) + 1; // offset required (months are 0-indexed)
            selectedDay = now.get(Calendar.DAY_OF_MONTH);

            if (isEndDate) {
                selectedYear = now.get(Calendar.YEAR) + 1;
            } else {
                // if not endDate, must be start date
                selectedYear = now.get(Calendar.YEAR) - 1;
            }
        }
        return Integer.toString(selectedYear) + "-" + Integer.toString(selectedMonth)
                + "-" + Integer.toString(selectedDay) + "T00.00.00.000"; // append zeros at end
    }

    /**
     * Method takes query results, filters based upon input parameters,
     * and then converts to a format that can be presented via graph
     *
     * @param myData array list of database data
     * @param sensor the name of selected sensor
     * @param startDate of requested interval
     * @param endDate of requested interval
     * @return data from input in graphable format
     */
    public static ArrayList<Float> convertDBRowToFloats(ArrayList<CSEntry> myData, String sensor,
                    String startDate, String endDate) {

        ArrayList<Float> myFloatData = new ArrayList<>();

        // syntax for interval: startDate,||endDate
        String requestInterval = startDate + "||" + endDate;

        for (int i = 0; i < myData.size(); i++) {

            // only get if the sensorID matches, is valid for time interval,
                // and not analytic data (only sensor data)
            if (myData.get(i).getSensorID().equals(sensor)
                    && isValidForTimeInterval(requestInterval, myData.get(i).getTimeString())
                    && !Utils.isAnalyticProcessID(myData.get(i).getProcessID())) {

                String [] floatArray = myData.get(i).getDataPayload().trim().split(",");
                for (int j = 0; j < floatArray.length; j++) {

                    try {
                        myFloatData.add(Float.parseFloat(floatArray[j].trim()));
                    } catch (NumberFormatException e) {
                        // disregard this for now

                        // TODO - handle the error
                    }
                }
            }
        }

        return myFloatData;
    }

    /**
     * Converts the date format displayed to the user to one that better
     * captures the correct time and is used elsewhere in the program.
     *
     * Input Syntax: "MM/DD/YYYY - MM/DD/YYYY"
     * Output Syntax: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
     *
     * @param analyticTimeInterval to be converted to alternative syntax
     * @return the syntactically-converted interval
     */
    public static String createTimeStringInterval(String analyticTimeInterval)  {

        try {
            analyticTimeInterval = analyticTimeInterval.replace(" ", ""); // remove spaces

            String [] intervals = analyticTimeInterval.split("-");

            String [] startInterval = intervals[0].split("/");
            String [] endInterval = intervals[1].split("/");

            // set hours,minutes,seconds,millis all to 0 as default
            return startInterval[2] + "-" + startInterval[0] + "-" +
                    startInterval[1] + "T00.00.00.000||" + endInterval[2] + "-" + endInterval[0] +
                    "-" + endInterval[1] + "T00.00.00.000";
        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error occurred in Utils.createTimeStringInterval(): \" + e");
        }
    }

    /**
     * Converts the date format (timeString) used to internally represent time
     * to one that is more easily understood by the user.
     *
     * Method is inverse of Utils.createTimeStringInterval(String chosenInterval)
     *
     * Output Syntax: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
     * Input Syntax: "MM/DD/YYYY - MM/DD/YYYY"
     *
     * @param timeStringInterval to be converted to alternative syntax
     * @return the syntactically-converted interval
     */
    public static String createAnalyticTimeInterval(String timeStringInterval) {

        try {
            String [] intervals = timeStringInterval.split("\\|\\|");
            String [] startInterval = intervals[0].split("T")[0].split("-"); // disregard hours, etc
            String [] endInterval = intervals[1].split("T")[0].split("-"); // disregard hours, etc

            return startInterval[1] + "/" + startInterval[2] + "/" + startInterval[0] + " - "
                    + endInterval[1] + "/" + endInterval[2] + "/" + endInterval[0];

        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error occurred in Utils.createTimeStringInterval(): \" + e");
        }
    }

    /**
     * @return UTC-compliant current time
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        formatUTC.setTimeZone(TimeZone.getDefault());

        // replace space with T; change makes parsing easier
        return formatUTC.format(new Date()).replace(" ", "T").replace(":", ".");
    }

    /**
     * Used to create start for synchronization time interval.
     *
     * @return timeString for previous hour
     */
    public static String getPreviousSynchTime() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        formatUTC.setTimeZone(TimeZone.getDefault());

        Date date = new Date();
        date.setTime(System.currentTimeMillis() - ConstVar.SYNCH_INTERVAL_MILLIS); // previous hour

        // replace space with T; change makes parsing easier
        return formatUTC.format(date).replace(" ", "T").replace(":", ".");
    }

    /**
     * Helper method that simplifies the code elsewhere.
     *
     * @param processID under question
     * @return true if processID is denotes an analytic task, false otherwise
     */
    public static boolean isAnalyticProcessID(String processID) {
        return processID.equals(ConstVar.MODE_ANALYTIC)
                || processID.equals(ConstVar.MEAN_ANALYTIC)
                || processID.equals(ConstVar.MEDIAN_ANALYTIC);

        // TODO - add more analytic process ids here once they are implemented
    }

    /**
     * Verifies validity of userType
     *
     * @param userType selected by user
     * @return validity of userType selection
     */
    public static boolean isValidUserType(String userType) {
        return userType.equals(ConstVar.PATIENT_USER_TYPE)
                || userType.equals(ConstVar.DOCTOR_USER_TYPE);
    }

    /**
     * tests validity of IP input
     *
     * @param ip input to be validated
     * @return validity status of input IP
     */
    public static boolean isValidIP(String ip) {

        boolean validIP;

        try {
            InetAddress.getByName(ip);
            validIP = true;
        } catch (Exception e) {
            validIP = false;
        }

        return ip != null && validIP;
    }

    /**
     * Method returns true if the data interval is within request interval
     *
     * @param requestInterval a request interval; necessarily must contain two times (start and end)
     * @param dataTimeString the time stamp on specific data
     * @return determination of whether dataTimeString is within requestInterval
     */
    static public boolean isValidForTimeInterval(String requestInterval, String dataTimeString) {

        if (requestInterval == null || dataTimeString == null) {
            return false; // reject bad input
        }

        String [] requestIntervals = requestInterval.split("\\|\\|"); // split interval into start/end

        // TIME_STRING FORMAT: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
        // the former is start interval, latter is end interval

        boolean beforeStartDate, afterEndDate;
        Date startDate, endDate, dataDate;

        try {

            // replace "T" with empty char "", so that comparison is easier
            requestIntervals[0] = requestIntervals[0].replace("T", " ");
            requestIntervals[1] = requestIntervals[1].replace("T", " ");
            dataTimeString = dataTimeString.replace("T", " ");

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
            df.setTimeZone(TimeZone.getDefault());

            startDate = df.parse(requestIntervals[0]);
            endDate = df.parse(requestIntervals[1]);
            dataDate = df.parse(dataTimeString);

            beforeStartDate = dataDate.before(startDate);
            afterEndDate = dataDate.after(endDate);

        } catch (Exception e) {
            e.printStackTrace();

            return false; // some problem occurred, default return is false
        }

        // if dataTimeString is not before start and not after end, then its with interval
        return (!beforeStartDate && !afterEndDate) || requestIntervals[0].equals(dataTimeString)
                || requestIntervals[1].equals(dataTimeString);
    }

    /**
     * Each user-defined synch interval, this method is invoked and converts all data collected
     * during past interval into a string (syntax below) for easy placement in NDN Data packet.
     *
     * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
     *
     * @param data to be converted
     * @return converted data
     */
    public static String formatSynchData(ArrayList<CSEntry> data) {

        try {
            Hashtable<String, ArrayList<CSEntry>> hashedBySensors = new Hashtable<>();
            String formattedSyncData = "";

            // first separate data based upon sensor
            for (int i = 0; i < data.size(); i++) {
                // sensor hasn't been stored yet, create ArrayList for its data and store now
                if (!hashedBySensors.containsKey(data.get(i).getSensorID())) {

                    ArrayList<CSEntry> dataForSensor = new ArrayList<>();
                    dataForSensor.add(data.get(i));

                    hashedBySensors.put(data.get(i).getSensorID(), dataForSensor);
                }
                // sensor has been seen, append data to its ArrayList now
                else {

                    hashedBySensors.get(data.get(i).getSensorID()).add(data.get(i));
                }
            }

            // now format data for each sensor
            for (String key : hashedBySensors.keySet()) {

                formattedSyncData += key + "--"; // '--' separates sensor's name from its data

                for (int i = 0; i < hashedBySensors.get(key).size(); i++) {
                    CSEntry sensorData = hashedBySensors.get(key).get(i);

                    formattedSyncData += sensorData.getDataPayload() + "," + sensorData.getTimeString();
                    formattedSyncData += ";;"; // ';;' separates each data piece for sensor
                }

                // remove last two chars, ';;', because they proceed no data
                formattedSyncData = formattedSyncData.substring(0, formattedSyncData.length() - 2);

                formattedSyncData += "::"; // '::' separates each sensor
            }

            // remove last two chars, '::', because they proceed no sensor
            formattedSyncData = formattedSyncData.substring(0, formattedSyncData.length() - 2);

            return formattedSyncData;
        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error in utils.formatSynchData(): " + e);
        }
    }

    /**
     * Used to parse (syntax below) data formatted as shown below
     *
     * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
     *
     * @param userID of sender
     * @param dataContents currently formatted
     * @return ArrayList containing parsed data if input valid; otherwise returns empty ArrayList
     */
    public static ArrayList<CSEntry> parseFormattedData(String userID, String dataContents) {

        if (!dataContents.isEmpty() && !userID.isEmpty()) {
            ArrayList<CSEntry> parsedData = new ArrayList<>();
            String [] splitBySensor = dataContents.split("::"); // '::' separates by sensor

            for (int i = 0; i < splitBySensor.length; i++) {
                String[] sensor = splitBySensor[i].split("--"); // '--' separates sensor's name from its data

                if (sensor.length > 1) {
                    String sensorName = sensor[0];
                    String [] sensorData = sensor[1].split(";;"); // ';;' separates sensor data pieces

                    for (int j = 0; j < sensorData.length; j++) {

                        String [] dataPiece = sensorData[j].split(","); // ',' separates (data,time) tuple

                        CSEntry sensorEntry = new CSEntry();
                        sensorEntry.setDataPayload(dataPiece[0]);
                        sensorEntry.setSensorID(sensorName);
                        sensorEntry.setTimeString(dataPiece[1]);
                        sensorEntry.setUserID(userID);
                        sensorEntry.setProcessID(ConstVar.NULL_FIELD);

                        parsedData.add(sensorEntry);
                    }
                } else {
                    // input was bad; do nothing
                }
            }

            return parsedData;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Attempts to determine whether userID input is valid
     *
     * Usernames may be between 3-15 characters and contain alpha-numeric characters and underscore.
     *
     * @param userID input to have validity assessed
     * @return boolean regarding validity of input
     */
    public static boolean isValidUserName(String userID) {

        // NOTE: keep username validation separate from password; they may change

        return userID != null && userID.matches("^[a-zA-Z0-9._]{3,15}$");
      }

    /**
     * Attempts to determine whether password is valid
     *
     * Passwords may be between 3-15 characters and contain alpha-numeric characters and underscore.
     *
     * @param password input to have validity assessed
     * @return boolean regarding validity of input
     */
    public static boolean isValidPassword(String password) {

        return password != null && password.matches("^[a-zA-Z0-9._]{3,15}$");
    }

    /**
     * Attempts to determine whether sensorname is valid
     *
     * Passwords may be between 3-20 characters and contain alpha-numeric characters and underscore.
     *
     * @param name input to have validity assessed
     * @return boolean regarding validity of input
     */
    public static boolean isValidSensorName(String name) {

        return name != null && name.matches("^[a-zA-Z0-9._]{3,20}$");
    }

    /**
     * Uses Java Mail module to determine validity of email address.
     *
     * @param email specified by user
     * @return true if valid; otherwise, false
     */
    public static boolean isValidEmail(String email) {
        boolean result = true;

        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Verifies that start interval selection is before (or is) end interval.
     *
     * @param startYear input by user
     * @param startMonth input by user
     * @param startDay input by user
     * @param endYear input by user
     * @param endMonth input by user
     * @param endDay input by user
     * @return boolean determining whether start interval is prior to end interval
     */
    public static boolean isValidInterval(int startYear, int startMonth, int startDay, int endYear,
                                          int endMonth, int endDay) {

        if (startYear < endYear) {
            return true;
        } else if (startYear == endYear) {
            if (startMonth < endMonth) {
                return true;
            } else if (startMonth == endMonth) {
                if (startDay <= endDay) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * On signup/login, server replies with FIB entries
     * so to provide client with inter-network connections.
     *
     * Syntax: "userId,ipAddr" and "||" separates entries
     *
     * @param serverFIBEntries that are to be placed into FIB
     * @param timeString denoting when FIBEntries were sent to the client
     * @param context used to access the client's database
     */
    public static void insertServerFIBEntries(String serverFIBEntries, String timeString,
                                              Context context) {

        if (!serverFIBEntries.isEmpty()) {
            String[] individualFIBEntries = serverFIBEntries.split("\\|\\|");

            for (int i = 0; i < individualFIBEntries.length; i++) {
                //Syntax: "userId,ipAddr"
                String [] individualEntry = individualFIBEntries[i].split(",");

                String userID = individualEntry[0].trim();
                String userIP = individualEntry[1].trim();

                // assume false until (if and when) user exists in FIB; then look to FIB for result
                boolean isMyPatient = false;

                FIBEntry fibEntry = new FIBEntry(userID, timeString, userIP, isMyPatient);

                DatabaseHandler dbHandler = DBSingleton.getInstance(context).getDB();

                FIBEntry fibQueryResult = dbHandler.getFIBData(fibEntry.getUserID());

                if (fibQueryResult == null) {
                    // entry does not exist; add now

                    dbHandler.addFIBData(fibEntry);
                } else {

                    // set isMyPatient to previously known value
                    fibEntry.setIsMyPatient(fibQueryResult.isMyPatient());

                    // entry already existed; update now
                    dbHandler.updateFIBData(fibEntry);
                }
            }
        }
    }

    /**
     * Converts an NDN Name component to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/name.html
     *
     * @param name - an NDN name component
     * @return input param converted to string
     */
    public static String convertNameToString(Name name) {

        String hashComponent = "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + name.hashCode()
                + " TLV-LENGTH " + Integer.toString(name.hashCode()).length();

        //decode the parsing characters "||"
        String nameContent = name.toUri().replace("%7C%7C", "||");

        String genericNameComponent = "NAME-COMPONENT-TYPE " + nameContent + " TLV-LENGTH "
                + nameContent.length();

        return  "NAME-TYPE TLV-LENGTH " + (hashComponent.length() + genericNameComponent.length())
                + genericNameComponent + " " + hashComponent;
    }

    /**
     * Converts an Data packet's MetaInfo to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data used during conversion
     * @return string containing Data's MetaInfo
     */
    public static String convertMetaInfoToString(Data data) {

        String contentType = "CONTENT-TYPE-TYPE TLV-LENGTH " + data.getMetaInfo().getType();
        String freshnessPeriod = "FRESHNESS-PERIOD-TLV TLV-LENGTH " + data.getMetaInfo().getFreshnessPeriod();
        String finalBlockID = "FINAL-BLOCK-ID-TLV TLV-LENGTH " + data.getMetaInfo().getFinalBlockId().hashCode();

        int length = contentType.length() + freshnessPeriod.length() + finalBlockID.length();

        return "META-INFO-TYPE TLV-LENGTH " + length + " " + contentType + " " + freshnessPeriod
                + " " + finalBlockID;
    }

    /**
     * Converts an Data packet's Content to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data used during conversion
     * @return string containing Data's Content
     */
    public static String convertContentToString(Data data) {

        String contentType = "CONTENT-TYPE-TYPE TLV-LENGTH " + data.getMetaInfo().getType();
        String content = data.getContent().toString();

        int length = contentType.length() + content.length();

        return "CONTENT-TYPE " + content + " TLV-LENGTH " + length + " " + contentType;
    }

    /**
     * Converts an Data packet's Signature to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     * @param data used during conversion
     * @return string containing Data's Signature
     */
    public static String convertSignatureToString(Data data) {

        // TODO - create correct signature

        String signatureInfo = "SIGNATURE-INFO-TYPE TLV-LENGTH "; // + data.getSignature().hashCode();
        String signatureBits = "SIGNATURE-VALUE-TYPE TLV-LENGTH " + data.getSignature().hashCode();

       return signatureInfo + " " + signatureBits;
    }

    /**
     * Converts an Data packet to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data - a data packet
     * @return input param converted to string
     */
    public static String convertDataToString(Data data) {

        String name = convertNameToString(data.getName());
        String metaInfo = convertMetaInfoToString(data);
        String content = convertContentToString(data);
        String signature = convertSignatureToString(data);

        int length = name.length() + metaInfo.length() + content.length() + signature.length();

        return "DATA-TLV TLV-LENGTH " + length + " " + name + " " + metaInfo + " " + content
                + " " + signature;
    }

    /**
     * Converts an Interest packet's selectors to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/interest.html
     *
     * @param interest used during conversion
     * @return string containing Interest's selectors
     */
    public static String convertSelectorsToString(Interest interest) {

        String minSuffixComponents = "MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH " + interest.getMinSuffixComponents();
        String maxSuffixComponents = "MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH " + interest.getMaxSuffixComponents();
        String publisherPublicKeyLocator = interest.getKeyLocator().getKeyName().toUri();
        String exclude = "EXCLUDE-TYPE TLV-LENGTH ANY-TYPE TLV-LENGTH(=0)";
        String childSelector = "CHILD-SELECTOR-TYPE TLV-LENGTH " + interest.getChildSelector();
        String mustBeFresh = "MUST-BE-FRESH-TYPE TLV-LENGTH(=0)";

        int length = minSuffixComponents.length() + maxSuffixComponents.length()
                + publisherPublicKeyLocator.length() + exclude.length() + childSelector.length()
                + mustBeFresh.length();

        return "SELECTORS-TYPE TLV-LENGTH " + length + " " + minSuffixComponents
                + " " + maxSuffixComponents + " " + publisherPublicKeyLocator + " " + exclude
                + " " + childSelector + " " + mustBeFresh;
    }

    /**
     * Converts an Interest packet to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/interest.html
     *
     * @param interest - an Interest packet
     * @return input param converted to string
     */
    public static String convertInterestToString(Interest interest) {

        String name = convertNameToString(interest.getName());
        String selectors = convertSelectorsToString(interest);
        String nonce = "NONCE-TYPE " + interest.getNonce().hashCode() + " TLV-LENGTH 4";
        String scope = "SCOPE-TYPE TLV-LENGTH " + interest.getScope();
        String interestLifetime = "INTEREST-LIFETIME-TYPE TLV-LENGTH " + interest.getInterestLifetimeMilliseconds();

        int length = name.length() + selectors.length() + nonce.length() + scope.length()
                + interestLifetime.length();

        return "INTEREST-TYPE TLV-LENGTH " + length + " " + name + " " + selectors + " "
                + nonce + " " + scope + " " + interestLifetime;
    }

    /**
     * Simplifies the insertion of Interest packet into the database.
     *
     * @param context used to access DBSingleton
     * @param interest packet to store in DB
     */
    public static void storeInterestPacket(Context context, Interest interest) {

        // NOTE: we append INTEREST to the front so that both the Interest and the Data reply
                    // can be stored; otherwise only one would be possible
        String packetName = "INTEREST " + interest.getName().toUri().replace("%7C%7C", "||");

        DBSingleton.getInstance(context).getDB()
                .addPacketData(new PacketDBEntry(packetName, convertInterestToString(interest)));
    }

    /**
     * Simplifies the insertion of a Data packet into the database.
     *
     * @param context used to access DBSingleton
     * @param data packet to store in DB
     */
    public static void storeDataPacket(Context context, Data data) {

        // NOTE: we append DATA to the front so that both the Interest and the Data reply
                 // can be stored; otherwise only one would be possible
        String packetName = "DATA " + data.getName().toUri().replace("%7C%7C", "||");

        DBSingleton.getInstance(context).getDB()
                .addPacketData(new PacketDBEntry(packetName, convertDataToString(data)));
    }
}