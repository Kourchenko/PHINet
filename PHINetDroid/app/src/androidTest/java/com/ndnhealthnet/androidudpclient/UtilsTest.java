package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Tests the functionality of Utils.java
 */
public class UtilsTest extends TestCase {

    Context context;

    /**
     *@param context used to create Utils object used in testing
     */
    UtilsTest(Context context) {
        this.context = context;
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {
        testSaveToPrefs();
        testGetFromPrefs();
        testConvertDBRowTFloats();
        testGetCurrentTime();
        testValidIP();
        testCreateAnalyticTimeInterval();
        testCreateTimeStringInterval();
        testIsValidEmail();
        testFormatSynchData();
        testIsValidUserName();
        testIsValidPassword();
        testIsValidInterval();
        testIsValidSensorName();
        testGenerateTimeStringFromInts();
        testIsValidFreshnessPeriod();

        // reset user credentials after test
        assertTrue(Utils.saveToPrefs(context, ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, ""));
        assertTrue(Utils.saveToPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, ""));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testSaveToPrefs() throws Exception {
        // test bad input
        assertFalse(Utils.saveToPrefs(null, null, null));

        // test bad key input
        assertFalse(Utils.saveToPrefs(context, "bad key", "password_test"));

        // test good input
        assertTrue(Utils.saveToPrefs(context, ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "password_test"));
    }

    /**
     * Method stores USER_ID then tests retrieval, along with other functionality.
     *
     * @throws Exception for failed tests
     */
    public void testGetFromPrefs() throws Exception {

        String userID = "USER_TEST_ID";

        // store input and later test retrieval
        assertTrue(Utils.saveToPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, userID));

        // test bad input
        assertEquals(Utils.getFromPrefs(null, null, null), null);

        // test bad key
        assertEquals(Utils.getFromPrefs(context, "bad key", ""), null);

        // test real params
        assertEquals(Utils.getFromPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, ""), userID);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testConvertDBRowTFloats() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetCurrentTime() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testValidIP() throws Exception {

        // test bad IP input
        assertFalse(Utils.isValidIP("invalid"));
        assertFalse(Utils.isValidIP(null));
        assertFalse(Utils.isValidIP("11.11.11.500000"));

        // test good IP input
        assertTrue(Utils.isValidIP("10.10.10.10"));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testIsValidForTimeInterval() throws Exception {

        final String goodRequestInterval = "2012-05-04T08:08:08.888||2014-05-04T08:08:08.888";
        final String badRequestInterval = "2012-ERROR:08.888||2014-ERROR8:08.888";

        final String goodDataInterval1 = "2012-07-04T08:08:08.888"; // date is within goodRequestInterval
        final String goodDataInterval2 = "2012-01-04T08:08:08.888"; // date is before goodRequestInterval
        final String goodDataInterval3 = "2014-07-04T08:08:08.888"; // date is after goodRequestInterval

        final String testInterval1 = "2015-02-22T00:00:00.000||2015-04-22T00:00:00.000";
        final String dataTime1 = "2015-03-22T22:58:10.878";

        // --- test bad input ---
        assertFalse(Utils.isValidForTimeInterval(null, null)); // null entries

        // syntax error in request interval
        assertFalse(Utils.isValidForTimeInterval(badRequestInterval, goodDataInterval1));

        // two data intervals and no request interval
        assertFalse(Utils.isValidForTimeInterval(goodDataInterval1, goodDataInterval1));

        // --- test bad input ---

        // test input rejection if before interval
        assertFalse(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval2));

        // test input rejection if after interval
        assertFalse(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval3));

        // test input acceptance if during interval
        assertTrue(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval1));
        assertTrue(Utils.isValidForTimeInterval(testInterval1, dataTime1));
    }

    /**
     * @throws Exception
     */
    public void testCreateAnalyticTimeInterval() throws Exception {

        String goodOutput = "06/11/1990 - 09/10/2000";
        String goodInput = "1990-06-11T00.00.00.000||2000-09-10T00.00.00.000";

        String badInput = "2934asdfkj1;23";
        String badOutput = "fdfjjfjfjjda2332---";

        // test on good input
        assertTrue(Utils.createAnalyticTimeInterval(goodInput).equals(goodOutput));

        // test on bad input
        boolean exceptionCaught = false;
        try {
            assertFalse(Utils.createAnalyticTimeInterval(badInput).equals(badOutput));
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught); // assert that exception was thrown
    }

    /**
     * @throws Exception
     */
    public void testCreateTimeStringInterval() throws Exception {
        String goodInput = "06/11/1990 - 09/10/2000";
        String goodOutput = "1990-06-11T00.00.00.000||2000-09-10T00.00.00.000";

        String badInput = "2934asdfkj1;23";
        String badOutput = "fdfjjfjfjjda2332---";

        // test on good input
        assertTrue(Utils.createTimeStringInterval(goodInput).equals(goodOutput));

        // test on bad input
        boolean exceptionCaught = false;
        try {
            assertFalse(Utils.createTimeStringInterval(badInput).equals(badOutput));
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught); // assert that exception was thrown
    }

    /**
     * @throws Exception
     */
    public void testIsValidEmail() throws Exception {
        String validEmail1 = "me@mail.com";
        String validEmail2 = "this.person@co.de.edu";
        String validEmail3 = "another.person.here@cs.edu";
        String invalidEmail1 = "me@mmail@mail.com";
        String invalidEmail2 = "me@.mail.com";
        String invalidEmail3 = "@mail.com";

        // test valid emails
        assertTrue(Utils.isValidEmail(validEmail1));
        assertTrue(Utils.isValidEmail(validEmail2));
        assertTrue(Utils.isValidEmail(validEmail3));

        // test invalid emails
        assertFalse(Utils.isValidEmail(invalidEmail1));
        assertFalse(Utils.isValidEmail(invalidEmail2));
        assertFalse(Utils.isValidEmail(invalidEmail3));
    }

    /**
     * @throws Exception
     */
    public void testFormatSynchData() throws Exception {
        CSEntry goodData1 = new CSEntry("sensorID1", ConstVar.NULL_FIELD,
                "1990-11-06T00.00.00.000", "userID", "10", ConstVar.DEFAULT_FRESHNESS_PERIOD);
        CSEntry goodData2 = new CSEntry("sensorID1", ConstVar.NULL_FIELD,
                "1990-11-07T00.00.00.000", "userID", "15", ConstVar.DEFAULT_FRESHNESS_PERIOD);
        CSEntry goodData3 = new CSEntry("sensorID1", ConstVar.NULL_FIELD,
                "1990-11-08T00.00.00.000", "userID", "99", ConstVar.DEFAULT_FRESHNESS_PERIOD);
        CSEntry goodData4 = new CSEntry("sensorID2", ConstVar.NULL_FIELD,
                "1990-11-09T00.00.00.000", "userID", "100", ConstVar.DEFAULT_FRESHNESS_PERIOD);
        CSEntry badData1 = null;

        ArrayList<CSEntry> goodDataList = new ArrayList<>();
        goodDataList.add(goodData1);
        goodDataList.add(goodData2);
        goodDataList.add(goodData3);
        goodDataList.add(goodData4);

        ArrayList<CSEntry> badDataList = new ArrayList<>();
        badDataList.add(goodData1);
        badDataList.add(badData1);

        String formattedData = Utils.formatSynchData(goodDataList);
        String correctlyFormattedData = "sensorID2--100,1990-11-09T00.00.00.000::sensorID1--10,"
                + "1990-11-06T00.00.00.000;;15,1990-11-07T00.00.00.000;;99,1990-11-08T00.00.00.000";

        assertTrue(formattedData.equals(correctlyFormattedData));

        boolean exceptionThrown = false;
        try {
            Utils.formatSynchData(badDataList);
        } catch (Exception e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown); // assert that exception was thrown
    }

    /**
     * @throws Exception
     */
    public void testIsValidUserName() throws Exception {
        String validUserName1 = "username";
        String invalidUserName1 = "user,name";
        String invalidUserName2 = "user;name";
        String invalidUserName3 = "user-name";
        String invalidUserName4 = "user:name";
        String invalidUserName5 = "a"; // too short
        String invalidUserName6 = "aopsidjfapisjfapisdjfaisdjapsidfj"; // too long

        // test valid username
        assertTrue(Utils.isValidUserName(validUserName1));

        // test invalid usernames
        assertFalse(Utils.isValidUserName(invalidUserName1));
        assertFalse(Utils.isValidUserName(invalidUserName2));
        assertFalse(Utils.isValidUserName(invalidUserName3));
        assertFalse(Utils.isValidUserName(invalidUserName4));
        assertFalse(Utils.isValidUserName(invalidUserName5));
        assertFalse(Utils.isValidUserName(invalidUserName6));
        assertFalse(Utils.isValidUserName(null));
    }

    /**
     * @throws Exception
     */
    public void testIsValidPassword() throws Exception {
        String validPassword = "hunter2";
        String invalidPassword1 = "pw"; // too short
        String invalidPassword2 = "asdfjapsidjfapsidjfpaisjdfpaisdjfas"; // too long

        // test valid password
        assertTrue(Utils.isValidPassword(validPassword));

        // test invalid passwords
        assertFalse(Utils.isValidPassword(invalidPassword1));
        assertFalse(Utils.isValidPassword(invalidPassword2));
        assertFalse(Utils.isValidPassword(null));
    }

    /**
     * @throws Exception
     */
    public void testIsValidSensorName() throws Exception {
        String validSensorName = "HeartbeatSensor";
        String invalidSensorName1 = "pw"; // too short
        String invalidSensorName2 = "asdfjapsidjfapsidjfpaisjdfpaisdjfas"; // too long

        // test valid password
        assertTrue(Utils.isValidSensorName(validSensorName));

        // test invalid passwords
        assertFalse(Utils.isValidSensorName(invalidSensorName1));
        assertFalse(Utils.isValidSensorName(invalidSensorName2));
        assertFalse(Utils.isValidSensorName(null));
    }

    /**
     * @throws Exception
     */
    public void testIsValidInterval() throws Exception {

        // test valid intervals
        assertTrue(Utils.isValidInterval(2000, 4, 2, 2000, 4, 3));
        assertTrue(Utils.isValidInterval(1999, 4, 2, 2000, 4, 3));
        assertTrue(Utils.isValidInterval(2000, 3, 2, 2000, 4, 3));
        assertTrue(Utils.isValidInterval(2000, 4, 2, 2000, 4, 2)); // same day should pass

        // test invalid intervals
        assertFalse(Utils.isValidInterval(2000, 4, 3, 2000, 4, 2));
        assertFalse(Utils.isValidInterval(2000, 4, 2, 1999, 4, 2));
        assertFalse(Utils.isValidInterval(2000, 5, 2, 2000, 4, 2));
    }

    /**
     * @throws Exception
     */
    public void testGenerateTimeStringFromInts() throws Exception {

        int year = 2100;
        int month = 10;
        int day = 4;
        boolean isEndDate = false; // this parameter is disregarded until input is invalid

        Calendar now = Calendar.getInstance();

        int defaultMonth = now.get(Calendar.MONTH) + 1; // offset required (months are 0-indexed)
        int defaultDay = now.get(Calendar.DAY_OF_MONTH);
        int defaultYear = now.get(Calendar.YEAR);


        // Output Syntax: "yyyy-MM-ddTHH.mm.ss.SSS"
        String correctTimeString = "2100-10-4T00.00.00.000";

        // subtract 1 (definition of "start" time string)
        String correctStartTimeString = Integer.toString(defaultYear-1) + "-"
                + Integer.toString(defaultMonth) + "-" + Integer.toString(defaultDay) + "T00.00.00.000";

        // add 1 (definition of "end" time string)
        String correctEndTimeString = Integer.toString(defaultYear+1) + "-"
                + Integer.toString(defaultMonth) + "-" + Integer.toString(defaultDay) + "T00.00.00.000";

        String timeString = Utils.generateTimeStringFromInts(year, month, day, isEndDate);

        // since input is invalid, will result to default given final boolean param
        String startTimeString = Utils.generateTimeStringFromInts(0, 0, 0, isEndDate);
        String endTimeString = Utils.generateTimeStringFromInts(0, 0, 0, !isEndDate);

        // test output
        assertEquals(correctTimeString, timeString);
        assertEquals(correctStartTimeString, startTimeString);
        assertEquals(correctEndTimeString, endTimeString);
    }

    /**
     * @throws Exception
     */
    public void testIsValidFreshnessPeriod() throws Exception {
        String timeString1 = Utils.getPreviousSynchTime();
        String timeString2 = Utils.getCurrentTime();

        String interval1 = timeString1 + "||" + timeString2;

        int oneMinuteFreshnessPeriod = 1000 * 60;

        assertTrue(Utils.isValidFreshnessPeriod(oneMinuteFreshnessPeriod, timeString1));
        assertTrue(Utils.isValidFreshnessPeriod(oneMinuteFreshnessPeriod, interval1));
        assertFalse(Utils.isValidFreshnessPeriod(oneMinuteFreshnessPeriod, timeString2));
    }
}