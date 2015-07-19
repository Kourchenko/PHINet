package com.ndnhealthnet.androidudpclient.DB;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.SensorDBEntry;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

/**
 * Singleton that contains DatabaseHandler object; all manipulations
 * must be done through accessing this singleton.
 */
public class DBSingleton {
    private static DBSingleton instance = null;

    private static DatabaseHandler datasource;

    protected DBSingleton() {
        // Exists only to prevent multiple instantiations
    }

    public static DBSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new DBSingleton();
            datasource = new DatabaseHandler(context);

            // the heartbeat sensor should always be in the DB; any device will possess it
            datasource.addSensorData(new SensorDBEntry(ConstVar.HEARTBEAT_SENSOR, 1));

            // add cloud-server to FIB
            FIBEntry dbData = new FIBEntry(ConstVar.SERVER_ID, ConstVar.CURRENT_TIME,
                    ConstVar.SERVER_IP, false);

            datasource.addFIBData(dbData);
        }
        return instance;
    }

    public static DatabaseHandler getDB() {
        return datasource;
    }
}
