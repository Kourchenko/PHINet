
/**
 * File contains constant strings as well as methods for generating DB Schema.
 */

exports.StringConst = {

    // the id used by the server
    SERVER_ID : "CLOUD-SERVER",

    // sent to server to initiate synchronization request
    INITIATE_SYNCH_REQUEST :"INITIATE_SYNCH_REQUEST",

    // send from server to request synchronization data
    SYNCH_DATA_REQUEST : "SYNCH_DATA_REQUEST",

    // notify the recipient (in this case server) of client login request
    LOGIN_REQUEST : "LOGIN_REQUEST",

    // notify the recipient (in this case server) of client register request
    REGISTER_REQUEST : "REGISTER_REQUEST",

    // notify the recipient (in this case a client) of the sender requesting credentials
    CREDENTIAL_REQUEST : "CREDENTIAL_REQUEST",

    // notify recipient (in this case the server) that content in data packet is user login credentials
    LOGIN_CREDENTIAL_DATA : "LOGIN_CREDENTIAL_DATA",

    // notify recipient (in this case the server) that content in data packet is user signup credentials
    REGISTER_CREDENTIAL_DATA : "REGISTER_CREDENTIAL_DATA",

    //  used by client to request and server to respond to register login request
    LOGIN_RESULT : "LOGIN_RESULT",

    // used by client to request and server to respond to register result request
    REGISTER_RESULT : "REGISTER_RESULT",

    // notify recipient that content in data packet is CacheData
    DATA_CACHE : "DATA_CACHE",

    // placeholder in name when a field isn't needed for specific packet
    NULL_FIELD : "NULL_FIELD",

    // denotes a user who has no associated IP
    NULL_IP : "NULL_IP",

    // used as Data packet content when login/register have failed respective
    LOGIN_FAILED: "LOGIN_FAILED",
    REGISTER_FAILED: "REGISTER_FAILED",

    // used to denote the two types of packets: interest and data
    INTEREST_TYPE : "INTEREST-TYPE",
    DATA_TYPE : "DATA-TLV",

    // notifies current time should be given
    CURRENT_TIME : "CURRENT_TIME",

    // allows node.js postgres module to connect to db
    DB_CONNECTION_STRING: "pg://troy_admin:8n0o2z0a6@ndnhealthdb.cunk36nc1e3u.us-west-2.rds.amazonaws.com:5432/ndnhealth",

    // db variables
    CS_DB : "ContentStore",
    CS_TEST_DB: "TestContentStore",

    PIT_DB : "PendingInterestTable",
    PIT_TEST_DB: "TestPendingInterestTable",

    FIB_DB : "ForwardingInformationBase",
    FIB_TEST_DB: "TestForwardingInformationBase",

    LOGIN_DB: "LoginCredentials",
    LOGIN_TEST_DB: "TestLoginCredentials",

    KEY_USER_ID : "_userID",
    KEY_SENSOR_ID : "sensorID",
    KEY_TIME_STRING : "timeString",
    KEY_PROCESS_ID : "processID",
    KEY_IP_ADDRESS : "ipAddress",
    KEY_DATA_CONTENTS : "dataContents",
    KEY_DOCTOR_LIST : "doctorList",  // a list of doctors for given user
    KEY_PATIENT_LIST: "patientList", // a list of patients for given user
    KEY_EMAIL: "email",
    KEY_PASSWORD: "password",
    KEY_ENTITY_TYPE: "entityType",  // this key represents doctor or patient status (DOCTOR or PATIENT, respectively)

    // used to specify which analytic task to be completed; TODO - add more later
    MODE_ANALYTIC : "MODE_ANALYTIC",
    MEDIAN_ANALYTIC : "MEDIAN_ANALYTIC",
    MEAN_ANALYTIC : "MEAN_ANALYTIC",

    // denotes user type of patient (i.e. doctor or patient)
    DOCTOR_USER_TYPE : "DOCTOR_USER_TYPE",
    PATIENT_USER_TYPE : "PATIENT_USER_TYPE",

    // used to query doctors from server
    ADD_DOCTOR : "ADD_DOCTOR",
    CLIENT_DOCTOR_SELECTION: "CLIENT_DOCTOR_SELECTION",
    DOCTOR_LIST: "DOCTOR_LIST",

    // used to query patients from server
    ADD_PATIENT : "ADD_PATIENT",
    CLIENT_PATIENT_SELECTION: "CLIENT_PATIENT_SELECTION",
    PATIENT_LIST: "PATIENT_LIST",

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createPITQuery : function (dbName) {
      return "CREATE TABLE " + dbName + "("
          +this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
          this.KEY_TIME_STRING + " TEXT," +this.KEY_PROCESS_ID + " TEXT," +this.KEY_IP_ADDRESS + " TEXT,"
          + "PRIMARY KEY(" + this.KEY_USER_ID + "," + this.KEY_TIME_STRING + ", "
          + this.KEY_IP_ADDRESS+ "))";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createFIBQuery : function(dbName) {
      return "CREATE TABLE " + dbName + "("
          +this.KEY_USER_ID + " TEXT PRIMARY KEY," + this.KEY_TIME_STRING +
          " TEXT, " +this.KEY_IP_ADDRESS + " TEXT)";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createCSQuery : function(dbName) {
        return "CREATE TABLE " + dbName + "("
        + this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
        this.KEY_TIME_STRING + " TEXT ," + this.KEY_PROCESS_ID + " TEXT," +this.KEY_DATA_CONTENTS +
        " TEXT, " + "PRIMARY KEY(" + this.KEY_USER_ID + ", " + this.KEY_TIME_STRING + "))";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createLoginDBQuery : function(dbName) {

      // TODO - improve upon this schema; have a single field for doctor may not scale well 

        return "CREATE TABLE " + dbName + "("
        + this.KEY_USER_ID + " TEXT ," + this.KEY_EMAIL + " TEXT," + this.KEY_DOCTOR_LIST + " TEXT," + 
        this.KEY_PATIENT_LIST  + " TEXT ," + this.KEY_PASSWORD + " TEXT ," + this.KEY_ENTITY_TYPE 
        + " TEXT, PRIMARY KEY( " + this.KEY_USER_ID + " ))"
    }
};