/**
 * File contains code for the LoginCredential database
 * that allows user-information manipulation and storage.
 */

var StringConst = require('./string_const').StringConst;
var postgresDB = require('pg'); // postgres database module
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
var UserClass = require('./user');

var dbName = StringConst.LOGIN_DB;

/**
 * Returns object that allows manipulation of LoginCredential database.
 *
 * @param tableName specifies if table or test-table will be used (separate to avoid data corruption during testing)
 */
exports.LoginCredentials = function (tableName) {

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
         * Method inserts new user.
         * 
         * @param userID of new user
         * @param password of new user
         * @param email of new user
         * @param entityType of new user
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        insertNewUser: function(userID, password, email, entityType, insCallback) {

            try {
                if (!userID || !password  || !email || !entityType || !insCallback) {
                    return false;
                } else {

                    if (entityType !== StringConst.PATIENT_USER_TYPE
                                        && entityType !== StringConst.DOCTOR_USER_TYPE) {
                        console.log("!! Error in user insertion: entity is of invalid type \'" + entityType + "\' .");
                        return false;
                    }

                    this.getUserByEmail(email, function(rowsTouched, queryResults) {
                        // user with that email already exists AND the email isn't the default NULL_FIELD, return null
                        if ((rowsTouched > 0 || queryResults.length > 0) && email != StringConst.NULL_FIELD) {
                            insCallback(0); // do not insert user if another user has same email
                        } else {

                            var doctorList = "", patientList = ""; // set of initial lists to empty string

                            // no other user has this email, add now
                            client.query("INSERT INTO " + dbName + "(" + StringConst.KEY_USER_ID + ", "
                                + StringConst.KEY_EMAIL + ", " + StringConst.KEY_ENTITY_TYPE + ", "
                                + StringConst.KEY_PASSWORD + ", " + StringConst.KEY_DOCTOR_LIST 
                                + ") values($1, $2, $3, $4, $5)",
                                [userID, email, entityType, password, doctorList],

                                function(err, result) {

                                    if (err) {
                                        console.log("err: " + err);

                                        insCallback(0);  // error occurred - 0 rows modified; return
                                    } else {

                                        insCallback(result.rowCount);
                                    }
                                });
                        }

                    });
                }

                return true;
            } catch (err) {
                console.log("!!Error in LoginCredentials.insertNewUser(): " + err);
                return false;
            }
        },

        /**
         * Method returns user if found in DB.
         * 
         * @param userID of requested user
         * @param getCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        getUserByID: function(userID, getCallback) {

            try {
                if (!userID || !getCallback) {
                    return false;
                } else {
                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'"
                            + userID + "\'",

                        function(err, result) {
                            if (err) {

                                getCallback(0, null);  // error occurred - 0 rows modified; return
                            } else {

                                if (result.rowCount > 0) {
                                    var queriedRow = UserClass.User();

                                    queriedRow.setUserID(result.rows[0]._userid);
                                    queriedRow.setEntityType(result.rows[0].entitytype);
                                    queriedRow.setPassword(result.rows[0].password);
                                    queriedRow.setEmail(result.rows[0].email);
                                    queriedRow.setDoctorList(result.rows[0].doctorlist);
                                    queriedRow.setPatientList(result.rows[0].patientlist);

                                    getCallback(result.rowCount, queriedRow);
                                } else {

                                    getCallback(0, null);
                                }
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.getUserByID(): " + err);
                return false;
            }
        },

        /**
         * Method returns user if found in DB.
         *
         * @param email of requested user
         * @param getCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        getUserByEmail: function(email, getCallback) {

            try {
                if (!email || !getCallback) {
                    return false;
                } else {
                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_EMAIL + " = \'"
                        + email + "\'",

                        function(err, result) {

                            if (err || !result || result.rowCount == 0) {

                                getCallback(0, []);  // error occurred - 0 rows modified; return
                            } else {

                                var users = [];
                                for (var i = 0; i < result.rows.length; i++) {

                                    var queriedRow = UserClass.User();

                                    queriedRow.setUserID(result.rows[i]._userid);
                                    queriedRow.setEntityType(result.rows[i].entitytype);
                                    queriedRow.setPassword(result.rows[i].password);
                                    queriedRow.setEmail(result.rows[i].email);
                                    queriedRow.setDoctorList(result.rows[i].doctorlist);
                                    queriedRow.setPatientList(result.rows[i].patientlist);

                                    users.push(queriedRow);
                                }

                                getCallback(result.rowCount, users);
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.getUserByID(): " + err);
                return false;
            }
        },

        /**
         * Method updates a users password, email, and entityType
         * 
         * @param userID used to find user
         * @param password used to update user information
         * @param email used to update user information
         * @param entityType used to update user information
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @param doctorList used to update user information
         * @return boolean - true if valid query, false otherwise
         */
        updateUser: function(userID, password, email, entityType, doctorList, updateCallback) {

            try {
                if (!userID || !password || !email || !entityType || !updateCallback) {
                    return false;
                } else {

                    if (entityType !== StringConst.DOCTOR_USER_TYPE
                                        && entityType !== StringConst.PATIENT_USER_TYPE) {
                        console.log("!! Error in user update: entity is of invalid type \'" + entityType + "\' .");

                        return false;
                    }

                    client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_EMAIL + " = \'"
                        + email + "\', " + StringConst.KEY_PASSWORD + " = \'" + password + "\', "
                        + StringConst.KEY_ENTITY_TYPE + " = \'" + entityType + "\' WHERE "
                        + StringConst.KEY_USER_ID + " = \'" + userID + "\', " 
                        + StringConst.DOCTOR_USER_TYPE + " = \'" + doctorList + "\'",

                        function(err, result) {
                            if (err) {

                                updateCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                updateCallback(result.rowCount);
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.updateUser(): " + err);
                return false;
            }
        },

        /**
         * Returns list of user's doctors
         *
         * @param userID requesting doctorList
         * @param getDrCallback used to pass back the doctorList
         * @returns {boolean} used during testing to denote valid/invalid input
         */
        getDoctors: function(userID, getDrCallback) {
            try {
                if (!userID || !getDrCallback) {
                    return false;
                } else {
                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'"
                            + userID + "\'",

                        function(err, result) {

                            if (err) {

                                getDrCallback(0, []);  // error occurred - 0 rows modified; return
                            } else {

                                if (result.rowCount > 0) {
                                    var doctorList = result.rows[0].doctorlist;

                                    if (doctorList) {
                                        // doctor list syntax: "doctor_1,...,doctor_n"; thus split on comma
                                        getDrCallback(result.rowCount, doctorList.split(","));
                                    } else {
                                        getDrCallback(0, []); // return nothing, no doctors found
                                    }

                                } else {
                                    getDrCallback(0, []);
                                }
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.getDoctors(): " + err);
                return false;
            }
        },

        /**
         * Returns list of user's patients
         *
         * @param userID requesting patientList
         * @param getPatientsCallback used to pass back the patientList
         * @returns {boolean} used during testing to denote valid/invalid input
         */
        getPatients: function(userID, getPatientsCallback) {
            try {
                if (!userID || !getPatientsCallback) {
                    return false;
                } else {

                    this.getUserByID(userID, function(rowsTouched, queryResult) {

                        if (rowsTouched === 1 && queryResult 
                            && queryResult.getEntityType() === StringConst.DOCTOR_USER_TYPE) {

                            var patientList = queryResult.getPatientList();
                            getPatientsCallback(patientList.length, patientList);

                        } else {
                            getPatientsCallback(0, []);
                        }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.getPatients(): " + err);
                return false;
            }
        },

        /**
         * Adds doctor to a given userID's doctorList column
         *
         * @param userID of user requesting new doctor
         * @param doctor - name of new doctor
         * @param addDrCallback used to pass back result
         * @returns {boolean} used during testing to denote valid/invalid input
         */
        addDoctor: function(userID, doctor, addDrCallback) {
            try {
                if (!userID || !doctor || !addDrCallback) {
                    return false;
                } else {

                    var getDoctors = this.getDoctors;

                    this.addPatient(doctor, userID, function(rowCount) {
                        if (rowCount === 1) {
                            getDoctors(userID, function(rowCount, queryResult) {

                                var doctorArray = queryResult;
                                var doctorList = queryResult.join();

                                if (doctorArray.indexOf(doctor) === -1) {
                                    doctorList += "," + doctor; // append doctor to list

                                    client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_DOCTOR_LIST + " = \'"
                                        +  doctorList + "\' WHERE " + StringConst.KEY_USER_ID + " = \'" + userID + "\' ",

                                        function(err, result) {
                                            if (err) {

                                                addDrCallback(0);  // error occurred - 0 rows modified; return
                                            } else {
                                                addDrCallback(result.rowCount);
                                            }
                                    });
                                }  else {
                                     addDrCallback(0); // doctor already in list; do nothing
                                 }   
                            });
                        } else {
                            addDrCallback(0); // no patient was added implies patient already added
                        }
                    });
                return true;
                }   
            } catch (err) {
                console.log("!!Error in LoginCredentials.addDoctor(): " + err);
                return false;
            }
        },

        /**
         * Adds doctor to a given userID's doctorList column
         *
         * @param userID of user requesting new doctor
         * @param patient - name of new doctor
         * @param addPatientCallback used to pass back result
         * @returns {boolean} used during testing to denote valid/invalid input
         */
        addPatient: function(userID, patient, addPatientCallback) {
            try {
                if (!userID || !patient || !addPatientCallback) {
                    return false;
                } else {

                     this.getPatients(userID, function(rowCount, queryResult) {

                        var patientArray = queryResult;
                        var patientList = queryResult.join();

                        if (patientArray.indexOf(patient) === -1) {
                            patientList += "," + patient; // append doctor to list

                            client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_PATIENT_LIST + " = \'"
                                +  patientList + "\' WHERE " + StringConst.KEY_USER_ID + " = \'" + userID + "\' ",

                                function(err, result) {

                                    if (err) {

                                        addPatientCallback(0);  // error occurred - 0 rows modified; return
                                    } else {
                                        addPatientCallback(result.rowCount);
                                    }
                            });
                        } else {
                            addPatientCallback(0); // doctor already in list; do nothing
                        }
                });


                    return true;       
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.addPatient(): " + err);
                return false;
            }
        },

        /**
         * Method deletes user from DB
         *
         * @param userID used to find and delete user
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        deleteUser: function(userID, delCallback) {

            try {
                if (!userID || !delCallback) {
                    return false;
                } else {
                    client.query( "DELETE FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID
                            + " = \'" + userID + "\'",

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
                console.log("!!Error in LoginCredentials.deleteUser(): " + err);
                return false;
            }
        }
    }
};