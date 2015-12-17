package com.ndnhealthnet.androidudpclient.Utility;

/**
 * File contains constants that are sent within
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful constants.
 */
public class ConstVar {

    // placeholder in name when a field isn't needed for specific packet
    public static final String NULL_FIELD = "NULL_FIELD";

    public static final String SENSOR_DB = "Sensors";

    // notifies current time should be given
    static public final String CURRENT_TIME = "CURRENT_TIME";

    // used to specify which analytic task to be completed; TODO - add more later
    public static final String MODE_ANALYTIC = "MODE_ANALYTIC";
    public static final String MEDIAN_ANALYTIC = "MEDIAN_ANALYTIC";
    public static final String MEAN_ANALYTIC = "MEAN_ANALYTIC";

    // use to store/retrieve/identify user login credentials
    public static final String PREFS_LOGIN_USER_ID_KEY = "__USER_ID__" ;
    public static final String PREFS_LOGIN_PASSWORD_ID_KEY = "__PASSWORD_ID__" ;
    public static final String PREFS_USER_TYPE_KEY = "__USER_TYPE__";

    // denotes the heartbeat sensor in SensorListActivity
    public static final String HEARTBEAT_SENSOR = "HeartbeatSensor";

    // passed within intent to ViewDataActivity to determine proper entity
    public static final String ENTITY_NAME = "ENTITY_NAME";

    // information used to contact the server
    public static final String SERVER_IP = "52.26.209.179";
    public static final String SERVER_ID = "CLOUD-SERVER";

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
