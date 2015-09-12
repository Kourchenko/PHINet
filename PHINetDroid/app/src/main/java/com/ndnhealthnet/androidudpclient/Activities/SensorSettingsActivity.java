package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.SensorDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Enables user to connect to a sensor then modify its settings (such as collection interval).
 */
public class SensorSettingsActivity extends Activity {

    Button backBtn, connectBtn, saveBtn, deleteSensorBtn;
    TextView loggedInText, sensorNameText, connectStatusText;
    EditText intervalEdit;
    String sensorName; // used-defined name
    String chosenSensorInfo; // info of sensor chosen by user in PairedSensorListActivity

    final int REQUEST_ENABLE_BT = 1; // used for getActivityResult
    final int SENSOR_SELECTION_CODE = 2; // used for getActivityResult
    static final String CHOSEN_SENSOR_INFO = "CHOSEN_SENSOR_INFO"; // used to pass data in intent
    private final String DEFAULT_INTERVAL = "24"; // initial, arbitrarily chosen, interval

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorsettings);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        sensorName = getIntent().getExtras().getString(SensorListActivity.SENSOR_NAME);
        SensorDBEntry specificSensor = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);

        intervalEdit = (EditText) findViewById(R.id.intervalEditText);

        if (specificSensor == null) {
            intervalEdit.setText(DEFAULT_INTERVAL);
        } else {
            intervalEdit.setText(Integer.toString(specificSensor.getSensorCollectionInterval()));
        }

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);  // place username on screen

        sensorNameText = (TextView) findViewById(R.id.specificSensorNameTextView);
        sensorNameText.setText(sensorName);

        connectStatusText = (TextView) findViewById(R.id.specificConnectionStatusTextView);
        connectStatusText.setText("DISCONNECTED");
        // TODO - set connection status

        connectBtn = (Button) findViewById(R.id.sensorConnectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Toast toast = Toast.makeText(getApplicationContext(), "Device does not support bluetooth", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        // if bluetooth isn't enabled, ask user to enable it here
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        connectToSensor();
                    }
                }
            }
        });

        saveBtn = (Button) findViewById(R.id.sensorDataSubmitBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveChanges();
            }
        });

        deleteSensorBtn = (Button) findViewById(R.id.deleteSensorBtn);
        deleteSensorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SensorDBEntry sensorQueryResult = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);

                if (sensorQueryResult != null) {
                    // sensor exists within db; delete it now

                    DBSingleton.getInstance(getApplicationContext()).getDB().deleteSensorEntry(sensorName);
                }

                Toast toast = Toast.makeText(getApplicationContext(), "Deletion successful", Toast.LENGTH_LONG);
                toast.show();

                finish();
            }
        });

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                saveChanges();
                                finish();

                            case DialogInterface.BUTTON_NEGATIVE:
                                finish();
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(SensorSettingsActivity.this);
                builder.setMessage("Save changes?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    /**
     * Should be invoked automatically OnActivityResult
     *
     * @param requestCode code of activity that has returned a result
     * @param resultCode status of activity return
     * @param data intent associated with returned activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // invoked after requesting BT
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToSensor();
            }
        }
        // invoked after sensor chosen
        else if (requestCode == SENSOR_SELECTION_CODE) {
            if (resultCode == RESULT_OK) {

                chosenSensorInfo = data.getExtras().getString(CHOSEN_SENSOR_INFO);

                // TODO - handle success (now connect to sensor and collect regularly)
            }
        }
    }

    /**
     * Starts Activity that allows sensor selection and discovery.
     */
    private void connectToSensor() {
        Intent intent = new Intent(SensorSettingsActivity.this, PairedSensorsListActivity.class);
        startActivityForResult(intent, SENSOR_SELECTION_CODE);
    }

    /**
     * Invoked when user elects to save sensor-setting changes.
     */
    private void saveChanges() {
        int chosenInterval = Integer.parseInt(intervalEdit.getText().toString());

        if (chosenInterval <= 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid interval", Toast.LENGTH_LONG);
            toast.show();
        } else {
            SensorDBEntry sensorQueryResult = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);

            if (sensorQueryResult == null) {
                // result was null; sensor hasn't been added yet - do so now

                SensorDBEntry newEntry = new SensorDBEntry(sensorName, chosenInterval);

                DBSingleton.getInstance(getApplicationContext()).getDB().addSensorData(newEntry);
            } else {
                // result wasn't null; sensor has already been added - just update it

                SensorDBEntry updatedEntry = new SensorDBEntry(sensorName, chosenInterval);

                DBSingleton.getInstance(getApplicationContext()).getDB().updateSensorData(updatedEntry);
            }

            Toast toast = Toast.makeText(getApplicationContext(), "Save successful", Toast.LENGTH_LONG);
            toast.show();

            // TODO - initiate collection interval (create thread to collect, etc)
        }
    }
}
