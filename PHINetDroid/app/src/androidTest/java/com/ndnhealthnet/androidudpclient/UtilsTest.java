package com.ndnhealthnet.androidudpclient;

import android.content.Context;

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
        testIsValidEmail();
        testIsValidUserName();
        testIsValidPassword();
        testIsValidSensorName();

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
}