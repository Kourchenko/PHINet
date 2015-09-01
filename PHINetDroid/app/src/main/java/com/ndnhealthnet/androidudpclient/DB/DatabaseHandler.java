package com.ndnhealthnet.androidudpclient.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.SensorDBEntry;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

import java.util.ArrayList;

/**
 * Class handles logic associated with three DB tables
 * the PIT, CS, FIB (whose contents are specified by NDN)
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TODO";
    private static final int DATABASE_VERSION = 1;

    private static final String KEY_USER_ID = "_userID";
    private static final String KEY_SENSOR_ID = "sensorID";
    private static final String KEY_TIME_STRING = "timeString";
    private static final String KEY_PROCESS_ID = "processID";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DATA_CONTENTS = "dataContents";
    private static final String KEY_DATA_FRESHNESS_PERIOD = "dataFreshnessPeriod";
    private static final String KEY_COLLECTION_INTERVAL = "collectionInterval";
    private static final String KEY_PACKET_NAME = "_packetName";
    private static final String KEY_PACKET_CONTENT = "packetContent";

    // used to denote if entry in FIB is patient of client
    private static final String KEY_IS_MY_PATIENT = "isMyPatient";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {


        // key is sensor_name
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + ConstVar.SENSOR_DB + "(" +
                KEY_SENSOR_ID + " TEXT PRIMARY KEY," + KEY_COLLECTION_INTERVAL + " BIGINT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create sensor table
}

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ConstVar.SENSOR_DB);

        // Create tables again
        onCreate(db);
    }

    /**
     * Performs insertion into Sensor database
     *
     * @param data of sensor to be inserted
     * @return true if insertion was successful, false otherwise
     */
    public boolean addSensorData(SensorDBEntry data) {
        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());

        try {

            // Inserting Row
            return db.insertWithOnConflict(ConstVar.SENSOR_DB, null, values, SQLiteDatabase.CONFLICT_FAIL) > 0;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * Method queries and returns entire Sensor DB
     *
     * @return an ArrayList of type DBData containing entire Sensor database; null if db empty
     */
    public ArrayList<SensorDBEntry> getAllSensorData() {

        ArrayList<SensorDBEntry> allSensorData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + ConstVar.SENSOR_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                SensorDBEntry data = new SensorDBEntry();

                data.setSensorID(cursor.getString(0));
                data.setSensorCollectionInterval(cursor.getInt(1));
                allSensorData.add(data);
            }
            cursor.close();
        }

        return allSensorData;
    }

    /**
     * Queries and returns a specific Sensor database entry
     *
     * @return sensor entry if found; otherwise, null
     */
    public SensorDBEntry getSpecificSensorData(String sensorID) {

        SensorDBEntry sensorData = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_SENSOR_ID + " = \"" + sensorID + "\"";
        Cursor cursor = db.query(ConstVar.SENSOR_DB, new String[]{KEY_SENSOR_ID,
                KEY_COLLECTION_INTERVAL}, whereSelection, null, null, null, null);

        if (cursor != null && cursor.getCount() == 1) {

            cursor.moveToFirst();

            sensorData = new SensorDBEntry();
            sensorData.setSensorID(cursor.getString(0));
            sensorData.setSensorCollectionInterval(cursor.getInt(1));
            cursor.close();
        }

        return sensorData;
    }

    /**
     * Method updates a single Sensor database row
     *
     * @param data allows for identification of row and update
     * @return true of update was successful, false otherwise
     */
    public boolean updateSensorData(SensorDBEntry data) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put(KEY_SENSOR_ID, data.getSensorID());
        values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());

        // updating row
        return db.update(ConstVar.SENSOR_DB, values, KEY_SENSOR_ID + " = ?",
                new String[]{data.getSensorID()}) > 0;
    }

    /**
     * Method deletes a single Sensor database entry
     *
     * @param sensorID of entry to be deleted
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteSensorEntry(String sensorID) {

        if (sensorID == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_SENSOR_ID + "= \"" + sensorID + "\"";

        return db.delete(ConstVar.SENSOR_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }
}