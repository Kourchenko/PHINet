package com.ndnhealthnet.androidudpclient.DB.DBDataTypes;

/**
 * Container for a Sensor entry
 */
public class SensorDBEntry {

    private String sensorID;
    private int sensorCollectionInterval;

    /**
     * Constructor
     *
     * @param sensorID of given sensor
     * @param collectionInterval of given sensor
     */
    public SensorDBEntry(String sensorID, int collectionInterval) {
        this.sensorID = sensorID;
        this.sensorCollectionInterval = collectionInterval;
    }

    public SensorDBEntry() {}

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public int getSensorCollectionInterval() {
        return sensorCollectionInterval;
    }

    public void setSensorCollectionInterval(int sensorCollectionInterval) {
        this.sensorCollectionInterval = sensorCollectionInterval;
    }
}
