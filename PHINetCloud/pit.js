/** 
 * File contains code for the Pending Interest Table specified in the NDN documentation
 **/

var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;
var postgresDB = require('pg');  // the postgres Node.js module
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);

var dbName = StringConst.PIT_DB;

/**
 * Returns object that allows manipulation of PIT.
 *
 * @param tableName specifies if table or test-table will be used (separate to avoid data corruption during testing)
 */
exports.PIT = function (tableName) {

    dbName = tableName; // set dbName (may be table or test-table name)

    /**
     * Function invocation connects to DB
     */
    (function connectClient () {
        client.connect(function(err) {
            if(err) {
                return console.error('could not connect to postgres', err);
            }
        });
    })();

	return {

        /**
         * Method deletes a single, specific PIT entry.
         *
         * @param userID associated with entry to be deleted
         * @param timeString associated with entry to be deleted
         * @param ipAddr associated with entry to be deleted
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        deletePITData: function (userID, timeString, ipAddr, delCallback) {

            try {
                if (!userID || !timeString || !ipAddr || !delCallback) {
                    return false;
                } else {
                    client.query( "DELETE FROM " + dbName + " WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  userID + "\' AND " + StringConst.KEY_TIME_STRING + " = \'"
                     + timeString + "\' AND " + StringConst.KEY_IP_ADDRESS + " = \'" + ipAddr + "\'",

                        function(err, result) {

                            if (err) {
                                delCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                delCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.deletePITData(): " + err);
                return false;
            }
		},

        /**
         * Method updates a single, specific PIT entry.
         *
         * @param dbDataObject object containing updated row contents
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
		updatePITData: function (dbDataObject, updateCallback) {

            try {
                if (!dbDataObject || !updateCallback) {

                    return false;
                } else {

                    client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_TIME_STRING + " = \'"
                    + dbDataObject.getTimeString() + "\' WHERE "+ StringConst.KEY_PROCESS_ID + " = \'"
                        + dbDataObject.getProcessID() + "\' AND " + StringConst.KEY_IP_ADDRESS + " = \'"
                        + dbDataObject.getIpAddr() + "\' AND " + StringConst.KEY_SENSOR_ID + " = \'"
                        + dbDataObject.getSensorID() + "\' AND " + StringConst.KEY_USER_ID + " = \'"
                        + dbDataObject.getUserID()+ "\'" ,

                        function(err, result) {

                            if (err) {
                                updateCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                updateCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            }
            catch (err) {

                console.log("!!Error in PendingInterestTable.updatePITData(): " + err);
                return false;
            }
		},

        /**
         * Data is queried without ipAddr specification; multiple entries may be found.
         *
         * @param userID specifies which PIT entries should be returned, together with ipAddr
         * @param ipAddr specifies which PIT entries should be returned
         * @param getGenCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
		getGeneralPITData: function (userID, ipAddr, getGenCallback) {

            try {
                if (!userID || !ipAddr || !getGenCallback) {

                    return false;
                } else {

                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID +
                        " = \'" + userID + "\' AND " + StringConst.KEY_IP_ADDRESS + " = \'" + ipAddr + "\'"
                        , function(err, result) {

                            if (err) {
                                // table doesn't exist
                                getGenCallback(0);  // error occurred - 0 rows modified; return

                            } else {

                                if (result.rowCount > 0) {
                                    var queryResults = [];

                                    for (var i = 0; i < result.rows.length; i++) {

                                        var queriedRow = DBDataClass.DATA();
                                        queriedRow.setUserID(result.rows[i]._userid);
                                        queriedRow.setSensorID(result.rows[i].sensorid);
                                        queriedRow.setTimeString(result.rows[i].timestring);
                                        queriedRow.setProcessID(result.rows[i].processid);
                                        queriedRow.setIpAddr(result.rows[i].ipaddress);

                                        queryResults.push(queriedRow);
                                    }

                                    getGenCallback(result.rowCount, queryResults);
                                } else {

                                    getGenCallback(0, null);
                                }
                            }
                    });

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.getGeneralPITData(): " + err);
                return false;
            }
		},

        /**
         * Method returns a single, specific PIT entry if it exists.
         *
         * @param userID associated with entry to be returned
         * @param ipAddr associated with entry to be returned
         * @param timeString associated with entry to be returned
         * @param getSpecCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        getSpecificPITData: function(userID, timeString, ipAddr, getSpecCallback) {

            try {
                if (!userID || !timeString || !ipAddr || !getSpecCallback) {
                    return false;
                } else {

                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'" +
                    userID + "\' AND " + StringConst.KEY_TIME_STRING + " = \'" + timeString + "\' AND "
                    + StringConst.KEY_IP_ADDRESS + " = \'" + ipAddr + "\'",

                        function(err, result) {

                        if (err) {

                            getSpecCallback(0); // error occurred - 0 rows modified; return
                        } else {

                            if (result.rowCount > 0) {

                                var queriedRow = DBDataClass.DATA();
                                queriedRow.setUserID(result.rows[0]._userid);
                                queriedRow.setSensorID(result.rows[0].sensorid);
                                queriedRow.setTimeString(result.rows[0].timestring);
                                queriedRow.setProcessID(result.rows[0].processid);
                                queriedRow.setIpAddr(result.rows[0].ipaddress);
                                getSpecCallback(result.rowCount, queriedRow);
                            } else {

                                getSpecCallback(0, null);
                            }
                        }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in PendingInterestTable.getSpecificPITData(): " + err);
                return false;
            }
        },

        /**
         * Method allows insertion of valid data into pending interest table.
         *
         * @param dbDataObject data object to be entered
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
		insertPITData: function(dbDataObject, insCallback)  {

            try {
                if (!dbDataObject || !dbDataObject.getUserID() || !insCallback) {
                    return false;
                } else {
                    client.query("INSERT INTO " + dbName + "(" + StringConst.KEY_USER_ID
                        + "," + StringConst.KEY_SENSOR_ID + "," +StringConst. KEY_TIME_STRING + ","
                        + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_IP_ADDRESS
                        +") values($1, $2, $3, $4, $5)",
                        [dbDataObject.getUserID(), dbDataObject.getSensorID(), dbDataObject.getTimeString(),
                            dbDataObject.getProcessID(), dbDataObject.getIpAddr()],

                        function(err, result) {
                            if (err) {
                                insCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                insCallback(result.rowCount);
                            }
                        });

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.insertPITData(): " + err);
                return false;
            }
		}
	}
};