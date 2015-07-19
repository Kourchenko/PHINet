/** 
 * File contains code for the Content Store specified in the NDN documentation
 */

var postgresDB = require('pg'); // postgres database module
var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);

var dbName = StringConst.CS_DB; // name of database manipulated within this file

/**
 * Returns object that allows manipulation of ContentStore.
 *
 * @param tableName specifies if table or test-table will be used (separate to avoid data corruption during testing)
 */
exports.CS = function (tableName) {

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
         * Method deletes a single, specific CS entry.
         *
         * @param userID associated with entry to be deleted
         * @param timeString associated with entry to be deleted
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if entry successfully deleted, false otherwise
         */
		deleteCSData: function (userID, timeString, delCallback) {

            try {
                if (!userID || !timeString || !delCallback) {
                    return false;
                } else {
                    client.query( "DELETE FROM " + dbName + " WHERE "
                        + StringConst.KEY_USER_ID + " = \'" +  userID + "\' AND " +
                        StringConst.KEY_TIME_STRING + " = \'" + timeString + "\'",

                        function(err, result) {
                            if (err) {

                                delCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                delCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ContentStore.deleteCSData(): " + err);
                return false;
            }
        },

        /**
         * Method updates a single, specific CS entry.
         *
         * @param dbDataObject object containing updated row contents
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if entry successfully updated, false otherwise
         */
		updateCSData: function (dbDataObject, updateCallback) {

            try {
                // perform minimal input validation
                if (!dbDataObject || !dbDataObject.getUserID() || !updateCallback) {
                    return false;
                } else {

                    client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_PROCESS_ID + " = \'"
                        + dbDataObject.getProcessID() + "\', " + StringConst.KEY_DATA_CONTENTS + " = \'"
                        + dbDataObject.getDataPayload() + "\', " + StringConst.KEY_SENSOR_ID + " = \'"
                        + dbDataObject.getSensorID() + "\' WHERE " + StringConst.KEY_USER_ID + " = \'"
                        + dbDataObject.getUserID() + "\' AND " + StringConst.KEY_TIME_STRING + "= \'"
                        + dbDataObject.getTimeString() + "\'",

                        function(err, result) {

                            if (err) {

                                updateCallback(0); // error occurred - 0 rows modified; return
                            } else {

                                updateCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ContentStore.updateCSData(): " + err);
                return false;
            }
        },

        /**
         * Data is queried without timeString specification; multiple entries may be found.
         *
         * @param userID associated with entries to be returned
         * @param getGenCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if found valid query, otherwise false returned
         */
		getGeneralCSData: function (userID, getGenCallback) {

            try {
                if (!userID || !getGenCallback) {
                    return false;
                } else {

                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'" + userID + "\'",
                        function(err, result) {

                            if (err) {
                                // table doesn't exist

                                getGenCallback(0, null); // error occurred - 0 rows modified; return
                            } else {

                                if (result.rowCount > 0) {
                                    var queriedEntries = [];

                                    for (var i = 0; i < result.rows.length; i++) {

                                        var queriedRow = DBData.DATA();
                                        queriedRow.setUserID(result.rows[i]._userid);
                                        queriedRow.setSensorID(result.rows[i].sensorid);
                                        queriedRow.setTimeString(result.rows[i].timestring);
                                        queriedRow.setProcessID(result.rows[i].processid);
                                        queriedRow.setDataPayload(result.rows[i].datacontents);

                                        queriedEntries.push(queriedRow);
                                    }

                                    getGenCallback(result.rowCount, queriedEntries);
                                } else {

                                    getGenCallback(0, null);
                                }

                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ContentStore.getGeneralCSData(): " + err);
                return false;
            }
		},

        /**
         * Method returns a single, specific CS entry if it exists.
         *
         * @param userID associated with entry to be returned
         * @param timeString associated with entry to be returned
         * @param getSpecCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, otherwise false
         */
		getSpecificCSData: function (userID, timeString, getSpecCallback) {

            try {
                if (!userID || !timeString || !getSpecCallback) {
                    return false;
                } else {

                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'" +
                        userID + "\' AND " + StringConst.KEY_TIME_STRING + "= \'" + timeString + "\'",
                        function(err, result) {

                            if (err) {

                                getSpecCallback(0); // error occurred - 0 rows modified; return
                            } else {

                                if (result.rowCount > 0) {
                                    var queriedRow = DBData.DATA();
                                    queriedRow.setUserID(result.rows[0]._userid);
                                    queriedRow.setSensorID(result.rows[0].sensorid);
                                    queriedRow.setTimeString(result.rows[0].timestring);
                                    queriedRow.setProcessID(result.rows[0].processid);
                                    queriedRow.setDataPayload(result.rows[0].datacontents);

                                    getSpecCallback(result.rowCount, queriedRow);
                                } else {

                                    getSpecCallback(0, null);
                                }
                            }
                    });
                }
            } catch (err) {
                console.log("!!Error in ContentStore.getSpecificCSData(): " + err);
                return false;
            }
        },

        /**
         * Method allows insertion of valid data into content store database.
         *
         * @param dbDataObject data object to be entered
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if data was successfully entered into DB, false otherwise
         */
		insertCSData: function(dbDataObject, insCallback)  {

           try {
               if (!dbDataObject || !dbDataObject.getUserID() || !insCallback) {
                   return false;
               } else {

                   client.query("INSERT INTO " + dbName + "(" + StringConst.KEY_USER_ID
                       + "," + StringConst.KEY_SENSOR_ID + "," + StringConst.KEY_TIME_STRING + ","
                       + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_DATA_CONTENTS
                       +") values($1, $2, $3, $4, $5)", [dbDataObject.getUserID(), dbDataObject.getSensorID(),
                           dbDataObject.getTimeString(),dbDataObject.getProcessID(), dbDataObject.getDataPayload()],

                       function(err, result) {

                           if (err) {
                               insCallback(0); // error occurred - 0 rows modified; return
                           } else {
                               insCallback(result.rowCount);
                           }
                   });

                   return true;
               }
           } catch (err) {
               console.log("!!Error in ContentStore.addCSData(): " + err);
               return false;
           }
        }
	}
};

