
/**
 * File contains constant strings as well as methods for generating DB Schema.
 */

exports.StringConst = {

    DB_CONNECTION_STRING: "TODO",

    // database tables
    USER_DB: "Users",
    USER_TEST_DB: "Users",

    // database columns
    USER_ID : "userID",
    FIRST_NAME : "firstName",
    LAST_NAME : "lastName",
    EMAIL: "email",
    PASSWORD: "password",
    GENDER: "gender",
    WEIGHT_POUNDS: "weight",
    HEIGHT_INCHES: "height",
    DOB: "dateOfBirth",
    LAST_LOGIN_TIME: "lastLoginTime",
    LAST_LOGIN_TYPE: "lastLoginType",

    // TODO - healthConditions, location, emergencyContact, secretQuestion/Answer
    // TODO - data-related fields

    createUserDB : function(dbName) {

        return "CREATE TABLE " + dbName + "("
            + this.USER_ID + " TEXT, "
            + this.FIRST_NAME + " TEXT, "
            + this.LAST_NAME + " TEXT, "
            + this.EMAIL + " TEXT, "
            + this.PASSWORD + " TEXT, "
            + this.GENDER + " TEXT, "
            + this.WEIGHT_POUNDS + " REAL, "
            + this.HEIGHT_INCHES + " REAL, "
            + this.DOB + " DATE, "
            + this.LAST_LOGIN_TIME + " TIMESTAMP, "
            + this.LAST_LOGIN_TYPE + " TEXT, "
            + "PRIMARY KEY( " + this.USER_ID + " ))"
    }
};
