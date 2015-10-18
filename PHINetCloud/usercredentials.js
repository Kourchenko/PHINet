/**
 * File contains code for the User database
 */

var StringConst = require('./string_const').StringConst;
var User = require('./user').User;

var postgresDB = require('pg'); // postgres database module
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);

var dbName = StringConst.USER_DB;

/**
 * Returns object that allows manipulation of LoginCredential database.
 *
 * @param tableName specifies if table or test-table will be used (separate to avoid data corruption during testing)
 */
exports.UserCredentials = function (tableName) {

    dbName = tableName; // set dbName (may be table or test-table name)

    /**
     * Function invocation connects to DB
     */
    (function connectClient () {
        client.connect(function(err) {
            if(err) {
                console.error('could not connect to postgres', err);
            }
        });
    })();

    return {

        insertNewUser: function(user, callback) {

            try {
                if (user) {

                    client.query("INSERT INTO " + dbName + " ("
                        + StringConst.USER_ID + ", "
                        + StringConst.FIRST_NAME + ", "
                        + StringConst.LAST_NAME + ", "
                        + StringConst.EMAIL + ", "
                        + StringConst.PASSWORD + ", "
                        + StringConst.GENDER + ", "
                        + StringConst.WEIGHT_POUNDS + ", "
                        + StringConst.HEIGHT_INCHES + ", "
                        + StringConst.DOB + ", "
                        + StringConst.LAST_LOGIN_TIME + ", "
                        + StringConst.LAST_LOGIN_TYPE

                        + ") values($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)",
                        [user.userID, user.firstName, user.lastName, user.email, user.password,
                            user.gender, user.weightInPounds, user.heightInInches, user.dateOfBirth,
                            user.lastLoginTime, user.lastLoginType],

                        function(err, result) {
                            if (err) {
                                console.log("Error in UserCredentials.insertNewUser(): " + err);

                                callback(0);
                            } else {
                                callback(result.rowCount);
                            }
                        });
                }
            } catch (err) {
                console.log("Error in UserCredentials.insertNewUser(): " + err);
                callback(0); // 0 rows touched
            }
        },

        getUserByID: function(userID, callback) {

            try {
                if (userID) {
                    client.query( "SELECT * FROM " + dbName + " WHERE "
                        + StringConst.USER_ID + " = \'" + userID + "\'",

                        function(err, result) {
                            if (err) {
                                console.log("Error in UserCredentials.getUserByID(): " + err);
                                callback(null);
                            } else {

                                var user = User();

                                if (result.rowCount == 1) {

                                    user.userID = result.rows[0].userID;
                                    user.firstName = result.rows[0].firstName;
                                    user.lastName = result.rows[0].lastName;
                                    user.email = result.rows[0].email;
                                    user.password = result.rows[0].password;
                                    user.gender = result.rows[0].gender;
                                    user.weightInPounds = result.rows[0].weightInPounds;
                                    user.heightInInches =result.rows[0].heightInInches;
                                    user.dateOfBirth = result.rows[0].dateOfBirth;
                                    user.lastLoginTime = result.rows[0].lastLoginTime;
                                    user.lastLoginType = result.rows[0].lastLoginType;
                                }

                                callback(user);
                            }
                        });
                }
            } catch (err) {
                console.log("Error in UserCredentials.getUserByID(): " + err);
                callback(null);
            }
        },

        updateUser: function(user, callback) {

            try {
                if (user) {

                    client.query( "UPDATE " + dbName + " SET "
                        + StringConst.FIRST_NAME + " = \'" + user.firstName + "\', "
                        + StringConst.LAST_NAME + " = \'" + user.lastName + "\', "
                        + StringConst.EMAIL + " = \'" + user.email + "\',"
                        + StringConst.PASSWORD + " = \'" + user.password + "\',"
                        + StringConst.GENDER + " = \'" + user.gender + "\',"
                        + StringConst.WEIGHT_POUNDS + " = \'" + user.weightInPounds + "\',"
                        + StringConst.HEIGHT_INCHES + " = \'" + user.heightInInches + "\',"
                        + StringConst.DOB + " = \'" + user.dateOfBirth + "\',"
                        + StringConst.LAST_LOGIN_TIME + " = \'" + user.lastLoginTime + "\',"
                        + StringConst.LAST_LOGIN_TYPE + " = \'" + user.lastLoginType + "\'"
                        + " WHERE " + StringConst.USER_ID + " = \'" + userID + "\' ",

                        function(err, result) {
                            if (err) {
                                console.log("Error in UserCredentials.updateUser(): " + err);
                                callback(0);
                            } else {

                                callback(result.rowCount);
                            }
                        });
                }
            } catch (err) {
                console.log("Error in UserCredentials.updateUser(): " + err);
                callback(0);
            }
        },

        deleteUser: function(userID, callback) {

            try {
                if (!userID) {
                    client.query( "DELETE FROM " + dbName + " WHERE " + StringConst.USER_ID
                        + " = \'" + userID + "\'",

                        function(err, result) {
                            if (err) {
                                console.log("!!Error in UserCredentials.deleteUser(): " + err);
                                callback(0);
                            } else {

                                callback(result.rowCount);
                            }
                        });
                }
            } catch (err) {
                console.log("Error in UserCredentials.deleteUser(): " + err);
                callback(0);
            }
        }
    }
};
