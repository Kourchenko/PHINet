package com.ndnhealthnet.androidudpclient.Utility;

/**
 * File contains constants that are sent withing
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful constants.
 */
public class ConstVar {

    // sent to server to initiate synchronization request
    static public final String INITIATE_SYNCH_REQUEST = "INITIATE_SYNCH_REQUEST";

    // send from server to request synchronization data
    static public final String SYNCH_DATA_REQUEST = "SYNCH_DATA_REQUEST";

    // notify recipient that content in data packet is CacheData
    static public final String DATA_CACHE = "DATA_CACHE";

    // notify the recipient (in this case the server) of client login request
    static public final String LOGIN_REQUEST = "LOGIN_REQUEST";

    // notify the recipient (in this case the server) of client register request
    static public final String REGISTER_REQUEST = "REGISTER_REQUEST";

    // notify the recipient (in this case a client) of the sender requesting credentials
    static public final String CREDENTIAL_REQUEST = "CREDENTIAL_REQUEST";

    // notify recipient (in this case the server) that content in data packet is user login credentials
    static public final String LOGIN_CREDENTIAL_DATA = "LOGIN_CREDENTIAL_DATA";

    // notify recipient (in this case the server) that content in data packet is user signup credentials
    static public final String REGISTER_CREDENTIAL_DATA = "REGISTER_CREDENTIAL_DATA";

    //  used by client to request and server to respond to register login request
    static public final String LOGIN_RESULT = "LOGIN_RESULT";

    // used by client to request and server to respond to register result request
    static public final String REGISTER_RESULT = "REGISTER_RESULT";

    // placeholder in name when a field isn't needed for specific packet
    public static final String NULL_FIELD = "NULL_FIELD";

    // used as Data packet content when login/register have failed respective
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";

    // denotes a user who has no associated IP
    public static final String NULL_IP = "NULL_IP";

    // notifies current time should be given
    static public final String CURRENT_TIME = "CURRENT_TIME";

    // the names of database tables
    public static final String CS_DB = "ContentStore";
    public static final String PIT_DB = "PendingInterestTable";
    public static final String FIB_DB = "ForwardingInformationBase";
    public static final String SENSOR_DB = "Sensors";
    public static final String PACKET_DB = "Packets";

    // used to specify which analytic task to be completed; TODO - add more later
    public static final String MODE_ANALYTIC = "MODE_ANALYTIC";
    public static final String MEDIAN_ANALYTIC = "MEDIAN_ANALYTIC";
    public static final String MEAN_ANALYTIC = "MEAN_ANALYTIC";

    // use to store/retrieve/identify user login credentials
    public static final String PREFS_LOGIN_USER_ID_KEY = "__USER_ID__" ;
    public static final String PREFS_LOGIN_PASSWORD_ID_KEY = "__PASSWORD_ID__" ;
    public static final String PREFS_USER_TYPE_KEY = "__USER_TYPE__";

    // denotes user type of patient (i.e. doctor or patient)
    public static final String DOCTOR_USER_TYPE = "DOCTOR_USER_TYPE";
    public static final String PATIENT_USER_TYPE = "PATIENT_USER_TYPE";

    // denotes the heartbeat sensor in SensorListActivity
    public static final String HEARTBEAT_SENSOR = "HeartbeatSensor";

    // passed within intent to ViewDataActivity to determine proper entity
    public static final String ENTITY_NAME = "ENTITY_NAME";

    // information used to contact the server
    public static final String SERVER_IP = "52.26.209.179";
    public static final String SERVER_ID = "CLOUD-SERVER";

    // used to query patients from server
    public static final String ADD_DOCTOR = "ADD_DOCTOR";
    public static final String CLIENT_DOCTOR_SELECTION = "CLIENT_DOCTOR_SELECTION";
    public static final String DOCTOR_LIST = "DOCTOR_LIST";

    // used to query patients from server
    public static final String CLIENT_PATIENT_SELECTION = "CLIENT_PATIENT_SELECTION";
    public static final String PATIENT_LIST = "PATIENT_LIST";

    // port used by all PHINet applications
    public static final int PHINET_PORT = 50056;

    // synchronization requests are initiated every hour (arbitrarily chosen)
    public static final int SYNCH_INTERVAL_MILLIS = 1000 * 60 * 60;

    // for now, the default freshness period is set to SYNCH_INTERVAL_MILLIS
    public static final int DEFAULT_FRESHNESS_PERIOD = SYNCH_INTERVAL_MILLIS;

    // title of dialog that allows user to select interval
    public static final String INTERVAL_TITLE_START = "Choose the start interval.";
    public static final String INTERVAL_TITLE_END = "Choose the end interval.";
}
