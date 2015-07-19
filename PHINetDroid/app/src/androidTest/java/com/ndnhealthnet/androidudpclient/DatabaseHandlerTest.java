package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PacketDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.SensorDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 *  Tests the functionality of DatabaseHandler.java
 */
public class DatabaseHandlerTest extends TestCase {

    Context context;
    
    // --- test PIT data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    PITEntry pitData1 = new PITEntry("SENSOR1", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "11.11.11.11");

    PITEntry pitData2 = new PITEntry("SENSOR2", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER2", "12.12.12.12");

    PITEntry emptyPIT = new PITEntry();

    PITEntry pitData3 = new PITEntry("SENSOR3", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "13.13.13.13");

    // --- test PIT data ---

    // --- test CS data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    CSEntry csData1 = new CSEntry("SENSOR1", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "11,11,11,11", ConstVar.DEFAULT_FRESHNESS_PERIOD);

    CSEntry csData2 = new CSEntry("SENSOR2", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER2", "12,12,12,12", ConstVar.DEFAULT_FRESHNESS_PERIOD);

    CSEntry csData3 = new CSEntry("SENSOR1", ConstVar.DATA_CACHE,
            "2015-04-27T00:00:00.000", "USER1", "12", ConstVar.DEFAULT_FRESHNESS_PERIOD);

    // --- test CS data ---

    // --- test FIB data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    FIBEntry fibData1 = new FIBEntry("USER1", ConstVar.CURRENT_TIME, "11.11.11.11", false);
    
    FIBEntry fibData2 = new FIBEntry("USER2", ConstVar.CURRENT_TIME, "11.11.11.11", false);

    // --- test FIB data ---

    // -- test Sensor Data ---

    SensorDBEntry sensorData1 = new SensorDBEntry("SENSOR1", 890);

    SensorDBEntry sensorData2 = new SensorDBEntry("SENSOR2", 4);

    // -- test Sensor Data ---

    // -- test Packet Data ---

    PacketDBEntry packetData1 = new PacketDBEntry("INTEREST packetName1", "ContentContentContentContent");

    PacketDBEntry packetData2 = new PacketDBEntry("INTEREST packetName2", "packetcontentpacketcontent");

    // -- test Packet Data ---

    /**
     * @param context used to create DatabaseHandler object used in testing
     */
    public DatabaseHandlerTest(Context context) {
        this.context = context;
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {

        testAddPITData();
        testAddCSData();
        testAddFIBData();
        testAddSensorData();
        testAddPacketData();
        testGetGeneralPITData();
        testGetSpecificPITEntry();
        testGetGeneralCSData();
        testGetAllFIBData();
        testGetAllSensorData();
        testGetAllPacketData();
        testGetSpecificCSData();
        testGetSpecificSensorData();
        testUpdateFIBData();
        testUpdatePITData();
        testUpdateCSData();
        testUpdateSensorEntry();
        testDeletePITEntry();
        testDeleteAllPackets();
        testDeleteSensorEntry();
        testDeleteFIBEntry();
        testDeleteCSEntry();
        testDeleteEntirePIT();
        testDeleteEntireCS();
        testDeleteEntireFIB();

        // delete test entries
        DBSingleton.getInstance(context).getDB().deleteEntireCS();
        DBSingleton.getInstance(context).getDB().deleteEntirePIT();
        DBSingleton.getInstance(context).getDB().deleteEntireFIB();
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB();
        DBSingleton.getInstance(context).getDB().deleteEntireSensorDB();
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddPITData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        // test addition of null data
        assertFalse(DBSingleton.getInstance(context).getDB().addPITData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addPITData(pitData1)); // test addition of duplicate entry

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT after testing insertion
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddCSData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));

        // test addition of null and/or duplicate data
        assertFalse(DBSingleton.getInstance(context).getDB().addCSData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addCSData(csData1));

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS after testing insertion
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        // test addition of null and/or duplicate data
        assertFalse(DBSingleton.getInstance(context).getDB().addFIBData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB after testing
    }

    /**
     * @throws Exception
     */
    public void testAddSensorData() throws Exception {

        // for safety, delete prior to test
        DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID());
        DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID());

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // test adding bad data (null and/or duplicate entry)
        assertFalse(DBSingleton.getInstance(context).getDB().addSensorData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception
     */
    public void testAddPacketData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // clear DB before testing

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        // test adding bad data (null and/or duplicate entry)
        assertFalse(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertFalse(DBSingleton.getInstance(context).getDB().addPacketData(null));

        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing
    }

    /**
     * method wipes pit, then adds two entries and checks that both are returned
     *
     * @throws Exception for failed tests
     */
    public void testGetGeneralPITData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing

        // check that null is returned for empty PIT
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralPITData("id1"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        // check that both will be returned (due to same userID)
        assertEquals(pitData1.getUserID(), pitData3.getUserID());

        // check that both have different IP, so that we can verify 2 distinct entries
        assertTrue(!pitData1.getIpAddr().equals(pitData3.getIpAddr()));

        ArrayList<PITEntry> generalPITData = DBSingleton.getInstance(context).getDB().getGeneralPITData(pitData1.getUserID());

        assertEquals(generalPITData.size(), 2); // check that 2 entries have been added

        int foundCount = 0;
        for (int i = 0; i < generalPITData.size(); i++) {
            if (generalPITData.get(i).getIpAddr().equals(pitData1.getIpAddr())) {
                foundCount++;
            } else if (generalPITData.get(i).getIpAddr().equals(pitData3.getIpAddr())) {
                foundCount++;
            }
        }

        assertEquals(foundCount, 2); // assert that both were found

        // test null request
        ArrayList<PITEntry> nullPITData = DBSingleton.getInstance(context).getDB().getGeneralPITData(null);

        assertEquals(nullPITData, null); // verify that no entries were returned

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT after testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificPITEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing

        // check null is returned for empty PIT
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificPITEntry("id1", "time1", "ip1"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        PITEntry pitSpecific1 = DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getProcessID());
        PITEntry pitSpecific3 = DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData3.getUserID(), pitData3.getTimeString(), pitData3.getProcessID());

        // test that pitData1 was returned correctly
        assertEquals(pitSpecific1.getIpAddr(), pitData1.getIpAddr());
        assertEquals(pitSpecific1.getUserID(), pitData1.getUserID());

        // test that pitData3 was returned correctly
        assertEquals(pitSpecific3.getIpAddr(), pitData3.getIpAddr());
        assertEquals(pitSpecific3.getUserID(), pitData3.getUserID());

        // test bad input gets a null return
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData(null, null, null), null);
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData("validUSERID", null, null), null);

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT after testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing

        // check null returned for empty FIB
        assertEquals(DBSingleton.getInstance(context).getDB().getAllFIBData(), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        FIBEntry fibReturn1 = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        FIBEntry fibReturn2 = DBSingleton.getInstance(context).getDB().getFIBData(fibData2.getUserID());

        // test valid return
        assertEquals(fibReturn1.getUserID(), fibData1.getUserID());
        assertEquals(fibReturn2.getUserID(), fibData2.getUserID());

        // test bad input
        assertEquals(DBSingleton.getInstance(context).getDB().getFIBData(null), null);

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetGeneralCSData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing

        // check null returned for empty CS
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralCSData("userid"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData3));

        ArrayList<CSEntry> csReturn1 = DBSingleton.getInstance(context).getDB().getGeneralCSData(csData1.getUserID());
        ArrayList<CSEntry> csReturn2 = DBSingleton.getInstance(context).getDB().getGeneralCSData(csData2.getUserID());

        // test valid data
        int foundCount = 0;
        for (int i = 0; i < csReturn1.size(); i++) {
            if (csReturn1.get(i).getTimeString().equals(csData1.getTimeString())) {
                foundCount ++;
            } else if (csReturn1.get(i).getTimeString().equals(csData3.getTimeString())) {
                foundCount ++;
            }
        }

        // should have found three entries (only three were inserted under this userID)
        assertEquals(foundCount, 2);

        // should only return one entry (only one test case inserted under this userID)
        assertEquals(csReturn2.get(0).getDataPayload(), csData2.getDataPayload());

        // test bad data
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralCSData(null), null);

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetAllFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing

        assertEquals(DBSingleton.getInstance(context).getDB().getAllFIBData(), null); // check that empty FIB returns null

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        ArrayList<FIBEntry> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

        assertEquals(allFIBData.size(), 2);

        int foundCount = 0;
        for (int i = 0; i < allFIBData.size(); i++) {
            if (allFIBData.get(i).getUserID().equals(fibData1.getUserID())) {
                foundCount++;
            } else if (allFIBData.get(i).getUserID().equals(fibData2.getUserID())) {
                foundCount++;
            }
        }

        assertEquals(foundCount, 2);

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing
    }

    /**
     * @throws Exception
     */
    public void testGetAllSensorData() throws Exception {

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        ArrayList<SensorDBEntry> sensorData = DBSingleton.getInstance(context).getDB().getAllSensorData();

        // both entries should have been returned (plus the HeartbeatSensor which is always present)
        assertTrue(sensorData.size() == 2 + 1);

        boolean entry1Found = false;
        boolean entry2Found = false;

        for (int i = 0; i < sensorData.size(); i++) {

            if (sensorData.get(i).getSensorID().equals(sensorData1.getSensorID())) {
                entry1Found = true;
            }

            if (sensorData.get(i).getSensorID().equals(sensorData2.getSensorID())) {
                entry2Found = true;
            }
        }

        assertTrue(entry1Found && entry2Found); // verify that both were detected

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception
     */
    public void testGetAllPacketData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete before testing

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        ArrayList<PacketDBEntry> packetData = DBSingleton.getInstance(context).getDB().getAllPacketData();

        boolean entry1Found = false;
        boolean entry2Found = false;

        for (int i = 0; i < packetData.size(); i++) {

            // only use the "real" portion of the name, not the appended INTEREST or DATA
            if (packetData.get(i).getPacketName().equals(packetData1.getPacketName().split(" ")[1])) {
                entry1Found = true;
            }

            // only use the "real" portion of the name, not the appended INTEREST or DATA
            if (packetData.get(i).getPacketName().equals(packetData2.getPacketName().split(" ")[1])) {
                entry2Found = true;
            }
        }

        assertTrue(entry1Found && entry2Found); // verify that both were detected

        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificCSData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing

        // check null returned for empty CS
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData("userid", "timestring", "processid"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData3));

        // test that, although both have same userID, two different entries are returned
                    // due to their respective time stamps
        CSEntry csReturn1 = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(),
                csData1.getTimeString(), csData1.getProcessID());
        CSEntry csReturn3 = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData3.getUserID(),
                csData3.getTimeString(), csData3.getProcessID());

        assertEquals(csReturn1.getDataPayload(), csData1.getDataPayload());
        assertEquals(csReturn3.getDataPayload(), csData3.getDataPayload());

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS after testing
    }

    /**
     * @throws Exception
     */
    public void testGetSpecificSensorData() throws Exception {

        DBSingleton.getInstance(context).getDB().getAllSensorData(); // delete before testing

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // test against actual entries
        SensorDBEntry sensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getSensorID());
        SensorDBEntry sensor2 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData2.getSensorID());

        assertTrue(sensor1.getSensorID().equals(sensorData1.getSensorID()));
        assertTrue(sensor2.getSensorID().equals(sensorData2.getSensorID()));

        // test against false entries
        SensorDBEntry sensor3 = DBSingleton.getInstance(context).getDB().getSpecificSensorData("asdpfoiamsdf");
        assertTrue(sensor3 == null);

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));

        DBSingleton.getInstance(context).getDB().getAllSensorData(); // delete after testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        // check original return
        FIBEntry originalReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // now, modify and check return
        fibData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updateFIBData(fibData1));

        FIBEntry updatedReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(updatedReturn.getIpAddr(), fibData1.getIpAddr());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updateFIBData(null));

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB after testing insertion
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdatePITData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));

        // check original return
        PITEntry originalReturn = DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getProcessID());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // now, modify and check return
        pitData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updatePITData(pitData1));

        PITEntry updatedReturn = DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getProcessID());
        assertEquals(updatedReturn.getIpAddr(), pitData1.getIpAddr());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updatePITData(null));

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete FIB after testing insertion
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateCSData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete before testing

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));

        // check original return
        CSEntry originalReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(),
                csData1.getTimeString(), csData1.getProcessID());
        assertEquals(originalReturn.getDataPayload(), csData1.getDataPayload());

        // now, modify and check return
        csData1.setDataPayload("99,99,99,99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updateCSData(csData1));

        CSEntry updatedReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(),
                csData1.getTimeString(), csData1.getProcessID());
        assertEquals(updatedReturn.getDataPayload(), csData1.getDataPayload());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updateCSData(null));

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete after testing
    }

    /**
     * @throws Exception
     */
    public void testUpdateSensorEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireSensorDB(); // delete before testing

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // update the interval to a new, arbitrarily chosen, value
        sensorData1.setSensorCollectionInterval(10 + sensorData1.getSensorCollectionInterval() * 10);

        assertTrue(DBSingleton.getInstance(context).getDB().updateSensorData(sensorData1));

        SensorDBEntry updatedSensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getSensorID());

        assertTrue(updatedSensor1.getSensorCollectionInterval() == sensorData1.getSensorCollectionInterval());

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeletePITEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete before testing

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));

        // check original return
        PITEntry originalReturn = DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getProcessID());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // check deletePITEntry rejects bad data
        assertFalse(DBSingleton.getInstance(context).getDB().deletePITEntry(null, pitData1.getTimeString(), pitData1.getIpAddr()));

        // check deletePITEntry accepts valid deletion
        assertTrue(DBSingleton.getInstance(context).getDB().deletePITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getIpAddr()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB()
                .getSpecificPITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getProcessID()), null);
    }

    /**
     * @throws Exception
     */
    public void testDeleteAllPackets() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete before testing

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        // test return is valid object
        ArrayList<PacketDBEntry> allPackets = DBSingleton.getInstance(context).getDB().getAllPacketData();
        assertTrue(allPackets != null);

        // delete
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing

        // now, after deletion, test return is a null object
        ArrayList<PacketDBEntry> allPacketsAfterDeletion = DBSingleton.getInstance(context).getDB().getAllPacketData();

        assertTrue(allPacketsAfterDeletion == null);
    }

    /**
     * @throws Exception
     */
    public void testDeleteSensorEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireSensorDB(); // delete before testing

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // delete
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));

        // now, after deletion, test return is a null object
        SensorDBEntry afterDeleteSensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getSensorID());
        SensorDBEntry afterDeleteSensor2 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData2.getSensorID());
        assertTrue(afterDeleteSensor1 == null);
        assertTrue(afterDeleteSensor2 == null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteFIBEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        // check original return
        FIBEntry originalReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // check null data rejected
        assertFalse(DBSingleton.getInstance(context).getDB().deleteFIBEntry(null));

        // check valid data accepted
        assertTrue(DBSingleton.getInstance(context).getDB().deleteFIBEntry(fibData1.getUserID()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteCSEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete before testing

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));

        // check original return
        CSEntry originalReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(),
                csData1.getTimeString(), csData1.getProcessID());
        assertEquals(originalReturn.getDataPayload(), csData1.getDataPayload());

        // check null data rejected
        assertFalse(DBSingleton.getInstance(context).getDB().deleteCSEntry(null, null));

        // check valid data accepted
        assertTrue(DBSingleton.getInstance(context).getDB().deleteCSEntry(csData1.getUserID(), csData1.getTimeString()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(),
                csData1.getTimeString(), csData1.getProcessID()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntirePIT() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntireCS() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntireFIB() throws Exception {
        // TODO
    }
}