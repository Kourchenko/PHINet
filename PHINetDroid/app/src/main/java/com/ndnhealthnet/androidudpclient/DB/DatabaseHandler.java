package com.ndnhealthnet.androidudpclient.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.ndnhealthnet.androidudpclient.Activities.MainActivity;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PacketDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.SensorDBEntry;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

import java.util.ArrayList;

/**
 * Class handles logic associated with three DB tables
 * the PIT, CS, FIB (whose contents are specified by NDN)
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NDNHealthNet10";
    private static final int DATABASE_VERSION = 10;

    private static final String KEY_USER_ID = "_userID";
    private static final String KEY_SENSOR_ID = "sensorID";
    private static final String KEY_TIME_STRING = "timeString";
    private static final String KEY_PROCESS_ID = "processID";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DATA_CONTENTS = "dataContents";
    private static final String KEY_DATA_FRESHNESS_PERIOD = "dataFreshnessPeriod";
    private static final String KEY_COLLECTION_INTERVAL = "collectionInterval";
    private static final String KEY_PACKET_NAME = "_packetName";
    private static final String KEY_PACKET_CONTENT = "packetContent";

    // used to denote if entry in FIB is patient of client
    private static final String KEY_IS_MY_PATIENT = "isMyPatient";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // keys are USER_ID, TIME_STRING, PROCESS_ID - only one piece of data per user per process per time instant
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.CS_DB + "("
                + KEY_USER_ID + " TEXT ," + KEY_DATA_FRESHNESS_PERIOD + " INTEGER ," + KEY_SENSOR_ID + " TEXT," +
                KEY_TIME_STRING + " TEXT ," + KEY_PROCESS_ID + " TEXT," +KEY_DATA_CONTENTS +
                " TEXT, " + "PRIMARY KEY(" + KEY_USER_ID + ", " + KEY_TIME_STRING + ","
                + KEY_PROCESS_ID +"))";

        db.execSQL(CREATE_DATABASE_TABLE); // create CS

        // keys are USER_ID, TIME_STRING, PROCESS_ID, and IP_ADDRESS
                    // - only one piece of requested data per user per time instant
        CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.PIT_DB + "("
                + KEY_USER_ID + " TEXT ," + KEY_SENSOR_ID + " TEXT," +
               KEY_TIME_STRING + " TEXT," +KEY_PROCESS_ID + " TEXT," +KEY_IP_ADDRESS + " TEXT,"
                + "PRIMARY KEY(" + KEY_USER_ID + "," + KEY_TIME_STRING + ", "
                + KEY_IP_ADDRESS + "," + KEY_PROCESS_ID + "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create PIT

        // keys are USER_ID - only location per user
        CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.FIB_DB + "("
                +KEY_USER_ID + " TEXT PRIMARY KEY," + KEY_TIME_STRING +
                " TEXT, " +KEY_IP_ADDRESS + " TEXT, " + KEY_IS_MY_PATIENT +" BOOLEAN)";

        db.execSQL(CREATE_DATABASE_TABLE); // create FIB

        // key is sensor_name
        CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.SENSOR_DB + "(" +
                KEY_SENSOR_ID + " TEXT PRIMARY KEY," + KEY_COLLECTION_INTERVAL + " BIGINT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create sensor table

        // key is packet name
        CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.PACKET_DB + "(" +
                KEY_PACKET_NAME + " TEXT PRIMARY KEY, " + KEY_PACKET_CONTENT + " TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create db to hold packets (only for viewing)
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.PIT_DB);
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.FIB_DB);
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.CS_DB);
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.SENSOR_DB);
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.PACKET_DB);

        // Create tables again
        onCreate(db);
    }

    /**
     * Performs insertion into Sensor database
     *
     * @param data of sensor to be inserted
     * @return true if insertion was successful, false otherwise
     */
    public boolean addSensorData(SensorDBEntry data) {
        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.SENSOR_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * Performs insertion into Packet database
     *
     * @param data of Packet to be inserted
     * @return true if insertion was successful, false otherwise
     */
    public boolean addPacketData(PacketDBEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_PACKET_NAME, data.getPacketName());
        values.put(KEY_PACKET_CONTENT, data.getPacketContent());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.PACKET_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * @param data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addPITData(PITEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, data.getUserID());
        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_PROCESS_ID, data.getProcessID());
        values.put(KEY_IP_ADDRESS, data.getIpAddr());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.PIT_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;

        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    /**
     * @param data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addCSData(CSEntry data) {
        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, data.getUserID());
        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_PROCESS_ID, data.getProcessID());
        values.put(KEY_DATA_CONTENTS, data.getDataPayload());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.CS_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * @param data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addFIBData(FIBEntry data) {

        // don't add the device's own data to its FIB
        if (data == null || data.getIpAddr().equals(MainActivity.deviceIP)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, data.getUserID());
        values.put(KEY_IP_ADDRESS, data.getIpAddr());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_IS_MY_PATIENT, data.isMyPatient());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.FIB_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * Data is queried without ipAddr specification; multiple entries may be found.
     *
     * @param userID specifies which PIT entries should be returned
     * @return ArrayList of data for userID param
     */
    public ArrayList<PITEntry> getGeneralPITData(String userID) {

        if (userID == null) {
            return null; // return empty array list
        }

        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_USER_ID + "=\"" + userID + "\"";

        Cursor cursor = db.query(ConstVar.PIT_DB, new String[] {KEY_USER_ID,
                        KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                whereSelection, null, null, null, null);

        ArrayList<PITEntry> allValidPITEntries = new ArrayList<>();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PITEntry data = new PITEntry();

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setIpAddr(cursor.getString(4));
                allValidPITEntries.add(data);
            }

            cursor.close();

        } else {
            allValidPITEntries = null; // query found nothing, set return object to null
        }
        
        return allValidPITEntries;
    }

    /**
     * Entry is queried with userID, processID, and timeString
     *
     * @param userID of queried PITEntry
     * @param timeString of queried PITEntry
     * @param processID of queried PITEntry
     * @return single db entry associated with input params
     */
    public PITEntry getSpecificPITEntry(String userID, String timeString, String processID) {

        if (userID == null || timeString == null || processID == null) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_USER_ID + "= \"" + userID + "\" AND " + KEY_TIME_STRING
                + "= \"" + timeString + "\" AND " + KEY_PROCESS_ID + "= \"" + processID + "\"";

        Cursor cursor;
        try {
            cursor = db.query(ConstVar.PIT_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // bad query apparently

        }

        PITEntry data = new PITEntry();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setTimeString(cursor.getString(2));
            data.setProcessID(cursor.getString(3));
            data.setIpAddr(cursor.getString(4));
            cursor.close();
        } else {
            data = null; // query found nothing, set return object to null
        }

        return data;
    }

    /**
     * Entry is queried with userID, processID, and timeString
     *
     * @param userID of queried PITEntry
     * @param processID of queried PITEntry
     * @return single db entry associated with input params
     */
    public ArrayList<PITEntry> getPITEntryGivenPID(String userID, String processID) {

        if (userID == null || processID == null) {
           return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_USER_ID + "= \"" + userID + "\" AND "
                + KEY_PROCESS_ID + "= \"" + processID + "\"";

        Cursor cursor;
        try {
            cursor = db.query(ConstVar.PIT_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // bad query apparently

        }

        ArrayList<PITEntry> pitArray = new ArrayList<>();

        // return each row
        while (cursor.moveToNext()) {
            PITEntry data = new PITEntry();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setTimeString(cursor.getString(2));
            data.setProcessID(cursor.getString(3));
            data.setIpAddr(cursor.getString(4));

            pitArray.add(data);
        }
        cursor.close();

        return pitArray;
    }

    /**
     * Method queries and returns entire Sensor DB
     *
     * @return an ArrayList of type DBData containing entire Sensor database; null if db empty
     */
    public ArrayList<SensorDBEntry> getAllSensorData() {

        ArrayList<SensorDBEntry> allSensorData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + ConstVar.SENSOR_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                SensorDBEntry data = new SensorDBEntry();

                data.setSensorID(cursor.getString(0));
                data.setSensorCollectionInterval(cursor.getInt(1));
                allSensorData.add(data);
            }
            cursor.close();
        }

        return allSensorData;
    }

    /**
     * Queries and returns a specific Sensor database entry
     *
     * @return sensor entry if found; otherwise, null
     */
    public SensorDBEntry getSpecificSensorData(String sensorID) {

        SensorDBEntry sensorData = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_SENSOR_ID + " = \"" + sensorID + "\"";
        Cursor cursor = db.query(ConstVar.SENSOR_DB, new String[]{KEY_SENSOR_ID,
                KEY_COLLECTION_INTERVAL}, whereSelection, null, null, null, null);

        if (cursor != null && cursor.getCount() == 1) {

            cursor.moveToFirst();

            sensorData = new SensorDBEntry();
            sensorData.setSensorID(cursor.getString(0));
            sensorData.setSensorCollectionInterval(cursor.getInt(1));
            cursor.close();
        }

        return sensorData;
    }

    /**
     * Method queries and returns entire Packet DB
     *
     * @return an ArrayList of type DBData containing entire Packet database; null if db empty
     */
    public ArrayList<PacketDBEntry> getAllPacketData() {

        ArrayList<PacketDBEntry> allPacketData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + ConstVar.PACKET_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                PacketDBEntry data = new PacketDBEntry();

                data.setPacketName(cursor.getString(0));
                data.setPacketContent(cursor.getString(1));

                // Syntax used so that both Interests and Data can be stored: PACKET_TYPE packet_name
                    // we only care about the packet_name, PACKET_TYPE only is used in storage
                data.setPacketName(data.getPacketName().split(" ")[1]);

                allPacketData.add(data);
            }
            cursor.close();
        }

        return allPacketData;
    }

    /**
     * Method returns specific, single FIB entry.
     *
     * @param userID associated with entry to be returned
     * @return entry if found, otherwise null returned
     */
    public FIBEntry getFIBData(String userID) {

        if (userID == null) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String whereSelection = KEY_USER_ID + "=\"" + userID + "\"";
        Cursor cursor = db.query(ConstVar.FIB_DB, new String[]{KEY_USER_ID,
                        KEY_TIME_STRING, KEY_IP_ADDRESS, KEY_IS_MY_PATIENT},
                whereSelection, null, null, null, null);

        FIBEntry data = null;

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            data = new FIBEntry();
            data.setUserID(cursor.getString(0));
            data.setTimeString(cursor.getString(1));
            data.setIpAddr(cursor.getString(2));
            data.setIsMyPatient(cursor.getInt(3)>0);

            cursor.close();
        }

        return data;
    }

    /**
     * Data is queried without timestring specification; multiple entries may be found.
     *
     * @param userID associated with entries to be returned
     * @return entries if found, otherwise null returned
     */
    public ArrayList<CSEntry> getGeneralCSData(String userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String whereSelection = KEY_USER_ID + "=\"" + userID + "\"";
        Cursor cursor;

        try {
            cursor = db.query(ConstVar.CS_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_DATA_CONTENTS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // presumably bad request
        }

        ArrayList<CSEntry> allValidCSEntries = new ArrayList<>();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                CSEntry data = new CSEntry();

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setDataPayload(cursor.getString(4));
                allValidCSEntries.add(data);
            }

            cursor.close();
        } else {
            allValidCSEntries = null; // query found nothing, set return object to null
        }
        
        return allValidCSEntries;
    }

    /**
     * Method used to query entire FIB table; useful when multi-casting interests
     *
     * @return entries if any exist, otherwise null returned
     */
    public ArrayList<FIBEntry> getAllFIBData() {
        ArrayList<FIBEntry> allFIBData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + ConstVar.FIB_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                FIBEntry data = new FIBEntry();

                data.setUserID(cursor.getString(0));
                data.setTimeString(cursor.getString(1));
                data.setIpAddr(cursor.getString(2));
                data.setIsMyPatient(cursor.getInt(3)>0);
                allFIBData.add(data);
            }
            cursor.close();
        }

        return allFIBData;
    }

    /**
     * Method returns a single, specific CS entry if it exists.
     *
     * @param userID associated with entry to be returned
     * @param timeString associated with entry to be returned
     * @return entry if found, otherwise null returned
     */
    public CSEntry getSpecificCSData(String userID, String timeString, String processID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_USER_ID + "=\"" + userID + "\" AND " + KEY_TIME_STRING + "=\"" + timeString
                + "\" AND " + KEY_PROCESS_ID + "=\"" + processID + "\"";

        Cursor cursor = db.query(ConstVar.CS_DB, new String[]{KEY_USER_ID,
                        KEY_SENSOR_ID, KEY_TIME_STRING, KEY_PROCESS_ID, KEY_DATA_CONTENTS},
                whereSelection, null, null, null, null);

        CSEntry data = new CSEntry();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setTimeString(cursor.getString(2));
            data.setProcessID(cursor.getString(3));
            data.setDataPayload(cursor.getString(4));

            cursor.close();
        } else {
            data = null; // query found nothing, set return object to null
        }
        
        return data;
    }

    /**
     * Method updates a single Sensor database row
     *
     * @param data allows for identification of row and update
     * @return true of update was successful, false otherwise
     */
    public boolean updateSensorData(SensorDBEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());

        // updating row
        return db.update(ConstVar.SENSOR_DB, values, KEY_SENSOR_ID + " = ?",
                new String[]{data.getSensorID()}) > 0;
    }

    /**
     * Method updates a single, specific FIB entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updateFIBData(FIBEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, data.getUserID());
        values.put(KEY_IP_ADDRESS, data.getIpAddr());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_IS_MY_PATIENT, data.isMyPatient());

        // updating row
        return db.update(ConstVar.FIB_DB, values, KEY_USER_ID + " = ?",
                new String[]{data.getUserID()}) > 0;
    }

    /**
     * Method updates a single, specific PIT entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updatePITData(PITEntry data) {
        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_PROCESS_ID, data.getProcessID());
        values.put(KEY_IP_ADDRESS, data.getIpAddr());

        // updating row
        return db.update(ConstVar.PIT_DB, values, KEY_USER_ID + " = ?",
                new String[]{data.getUserID()}) > 0;
    }

    /**
     * Method updates a single, specific CS entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updateCSData(CSEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_TIME_STRING, data.getTimeString());
        values.put(KEY_PROCESS_ID, data.getProcessID());
        values.put(KEY_DATA_CONTENTS, data.getDataPayload());

        // updating row
        return db.update(ConstVar.CS_DB, values, KEY_USER_ID + " = ?",
                new String[]{data.getUserID()}) > 0;
    }

    /**
     * Method deletes a single, specific PIT entry.
     *
     * @param userID associated with entry to be deleted
     * @param timeString associated with entry to be deleted
     * @param ipAddr associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deletePITEntry(String userID, String timeString, String ipAddr) {

        if (userID == null || timeString == null || ipAddr == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_USER_ID + "= \"" + userID + "\"" + "AND " + KEY_TIME_STRING
                + " =\"" + timeString + "\" AND " + KEY_IP_ADDRESS + "= \"" + ipAddr + "\"";

        return db.delete(ConstVar.PIT_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single, specific FIB entry.
     *
     * @param userID associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deleteFIBEntry(String userID) {

        if (userID == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_USER_ID + "= \"" + userID + "\"";

        return db.delete(ConstVar.FIB_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single Sensor database entry
     *
     * @param sensorID of entry to be deleted
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteSensorEntry(String sensorID) {

        if (sensorID == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_SENSOR_ID + "= \"" + sensorID + "\"";

        return db.delete(ConstVar.SENSOR_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single, specific CS entry.
     *
     * @param userID associated with entry to be deleted
     * @param timeString associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deleteCSEntry(String userID, String timeString) {

        if (userID == null || timeString == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_USER_ID + "= \"" + userID + "\" AND " + KEY_TIME_STRING + "=\""
                + timeString + "\"";

        return db.delete(ConstVar.CS_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes all entries in PIT.
     */
    public void deleteEntirePIT() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ConstVar.PIT_DB, null, null);
    }

    /**
     * Method deletes all entries in CS.
     */
    public void deleteEntireCS() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ConstVar.CS_DB, null, null);
    }

    /**
     * Method deletes all entries in FIB.
     */
    public void deleteEntireFIB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ConstVar.FIB_DB, null, null);
    }

    /**
     * Method deletes all entries in the Packet DB.
     */
    public void deleteEntirePacketDB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ConstVar.PACKET_DB, null, null);
    }

    /**
     * Method deletes all entries in the Sensor DB
     */
    public void deleteEntireSensorDB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ConstVar.SENSOR_DB, null, null);
    }
}