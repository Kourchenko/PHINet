package com.ndnhealthnet.androidudpclient.DB.DBDataTypes;

import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Container for a ForwardingInformationBase entry
 */
public class FIBEntry {

    private String timeString;
    private String userID;
    private String ipAddr;
    boolean isMyPatient;

    /**
     * Constructor
     *
     * @param userID associated with FIB data
     * @param timeString associated with FIB data
     * @param ipAddr associated with FIB data
     */
    public FIBEntry(String userID, String timeString, String ipAddr, boolean isMyPatient) {

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.userID = userID;
        this.timeString = timeString;
        this.ipAddr = ipAddr;
        this.isMyPatient = isMyPatient;
    }

    public FIBEntry() {}

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

    public boolean isMyPatient() {
        return isMyPatient;
    }

    public void setIsMyPatient(boolean isMyPatient) {
        this.isMyPatient = isMyPatient;
    }
}
