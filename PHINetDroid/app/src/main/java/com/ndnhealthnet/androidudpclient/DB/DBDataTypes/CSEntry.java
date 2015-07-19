package com.ndnhealthnet.androidudpclient.DB.DBDataTypes;

import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Container for a ContentStore entry
 */
public class CSEntry {

    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String dataPayload;
    private int freshnessPeriod;

    /**
     * Constructor
     *
     * @param sensorID associated with CS Data
     * @param processID associated with CS Data
     * @param timeString associated with CS Data
     * @param userID associated with CS Data
     * @param dataPayload associated with CS Data
     * @param freshnessPeriod associated with CS Data
     */
    public CSEntry(String sensorID, String processID, String timeString,
                  String userID, String dataPayload, int freshnessPeriod) {
        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.dataPayload = dataPayload;
        this.freshnessPeriod = freshnessPeriod;
    }

    public CSEntry() {}

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

    public String getDataPayload() {
        return dataPayload;
    }

    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }

    public int getFreshnessPeriod() {
        return freshnessPeriod;
    }

    public void setFreshnessPeriod(int freshnessPeriod) {
        this.freshnessPeriod = freshnessPeriod;
    }
}
