package com.ndnhealthnet.androidudpclient.DB.DBDataTypes;

import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Container for a PendingInterestTable entry
 */
public class PITEntry {

    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String ipAddr;
    private int freshnessPeriod;

    /**
     * Constructor for PIT
     *
     * @param sensorID associated with PIT data
     * @param processID associated with PIT data
     * @param timeString associated with PIT data
     * @param userID associated with PIT data
     * @param ipAddr associated with PIT data
     */
    public PITEntry(String sensorID, String processID, String timeString,
                  String userID, String ipAddr) {

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    }

    public PITEntry() {}

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getTimeString() {
        return timeString;
    }

    /**
     * Method sets object's timeString. If CURRENT_TIME chose, invoke Utils method.
     */
    public void setTimeString(String timeString) {

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.timeString = timeString;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getFreshnessPeriod() {
        return freshnessPeriod;
    }

    public void setFreshnessPeriod(int freshnessPeriod) {
        this.freshnessPeriod = freshnessPeriod;
    }
}
