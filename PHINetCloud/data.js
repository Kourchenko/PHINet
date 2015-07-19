/** 
 * File contains code for the data-object used to handle patient information (i.e., the Patient Class)
 */

var StringConst = require('./string_const').StringConst;
var Utils = require('./utils').Utils;

/**
 * Returns object that holds/manipulates Packet/DB Data.
 */
exports.DATA = function () { 

    return {

        // --- member variables that may be manipulated ---
        sensorID: null,
        processID: null,
        timeString: null,
        userID: null,
        dataPayload: null,
        ipAddr: null,
        // --- member variables that may be manipulated ---

        /**
         * constructor for CS
         *
         * @param sensorID associated with CS data
         * @param processID associated with CS data
         * @param timeString associated with CS data
         * @param userID associated with CS data
         * @param dataPayload contents associated with CS data
         */
    	csData: function (userID, sensorID, processID, timeString, dataPayload) {

             // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            } 

            this.sensorID = sensorID;
            this.processID = processID;
            this.timeString = timeString;
            this.userID = userID;
            this.dataPayload = dataPayload;
        },

        /**
         * constructor for PIT
         *
         * @param sensorID associated with PIT data
         * @param processID associated with PIT data
         * @param timeString associated with PIT data
         * @param userID associated with PIT data
         * @param ipAddr associated with request
         */
        pitData: function (userID, sensorID, processID, timeString, ipAddr) {

            // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            }

            this.sensorID = sensorID;
            this.processID = processID;
            this.timeString = timeString;
            this.userID = userID;
            this.ipAddr = ipAddr;
        },

        /**
         * constructor for FIB
         *
         * @param userID associated with FIB data
         * @param timeString associated with FIB data
         * @param ipAddr associated with FIB data
         */
        fibData: function(userID, timeString, ipAddr) {

            // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            }
            
            this.userID = userID;
            this.timeString = timeString;
            this.ipAddr = ipAddr;
        },

        getSensorID: function () {
            return this.sensorID;
        },

        setSensorID: function (sensorID) {
            this.sensorID = sensorID;
        },

        getProcessID: function () {
            return this.processID;
        },

        setProcessID: function (processID) {
            this.processID = processID;
        },

        getTimeString: function () {
            return this.timeString;
        },

        /**
         * Method allows setting of object's timeString. If CURRENT_TIME chosen, set using Utils method.
         */
        setTimeString: function (timeString) {

            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            }

            this.timeString = timeString;
        },

        getUserID: function () {
            return this.userID;
        },

        setUserID: function (userID) {
            this.userID = userID;
        },

        getIpAddr: function () {
            return this.ipAddr;
        },

        setIpAddr: function (ipAddr) {
            this.ipAddr = ipAddr;
        },

        getDataPayload: function () {
            return this.dataPayload;
        },

        setDataPayload: function (dataPayload) {
            this.dataPayload = dataPayload;
        }
    }
};