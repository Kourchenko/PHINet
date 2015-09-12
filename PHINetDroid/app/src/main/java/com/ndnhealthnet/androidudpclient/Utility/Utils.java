package com.ndnhealthnet.androidudpclient.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;

import java.net.InetAddress;

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
}