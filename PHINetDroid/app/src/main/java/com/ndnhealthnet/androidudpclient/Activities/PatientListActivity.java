package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Activity displays list of current patients and allows selection patient and 
 * move to activity where modification/data-requests are possible
 */
public class PatientListActivity extends ListActivity {

    Button backBtn;
    TextView emptyListTextView, loggedInText;
    ProgressBar progressBar;
    PatientAdapter adapter;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    // used to identify intent-data
    final static String PATIENT_USER_ID = "PATIENT_USER_ID";

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload (to display new patients)
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientlist);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        progressBar = (ProgressBar) findViewById(R.id.patientProgressBar);
        progressBar.setVisibility(View.GONE); // hide until query made

        // call to getPatients() will populate; pass empty ArrayList now
        adapter = new PatientAdapter(this, new ArrayList<String>());
        setListAdapter(adapter);

        getPatients(currentUserID);// query server to populate ListView

        emptyListTextView = (TextView) findViewById(R.id.emptyListTextView);

        if (adapter.getCount() > 0) {
            // hide "empty patient list" text when patients actually do exist
            emptyListTextView.setVisibility(View.GONE);
        }

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Queries server for all patients and waits for response.
     *
     * @param userID of client using this device
     */
    private void getPatients(final String userID) {

        progressBar.setVisibility(View.VISIBLE); // query begins, show progressbar

        final String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.PATIENT_LIST);
        Interest interest = JNDNUtils.createInterestPacket(packetName);

        // add entry into PIT
        PITEntry pitEntry = new PITEntry(userID, ConstVar.PATIENT_LIST,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(pitEntry);

        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

        // store received packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        // create thread to check for Interest from server
        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 8; // check for SLEEP_TIME*8 = 2 seconds (somewhat arbitrary)
                int loopCount = 0;
                CSEntry candidateEntry = null;

                while (loopCount++ < maxLoopCount) {

                    candidateEntry = DBSingleton
                            .getInstance(getApplicationContext()).getDB()
                            .getSpecificCSData(ConstVar.SERVER_ID, currentTime,
                                    ConstVar.PATIENT_LIST);

                    if (candidateEntry != null) {
                        break; // result found; break from
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final CSEntry patientList = candidateEntry;

                // delete PATIENT_LIST Interest from PIT
                DBSingleton.getInstance(getApplicationContext())
                        .getDB().deletePITEntry(ConstVar.SERVER_ID,
                        currentTime, ConstVar.SERVER_IP);

                if (candidateEntry != null) {

                    // update UI with query result
                    PatientListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // query over, hide progress bar

                            // only update if data exists
                            if (!patientList.getDataPayload().isEmpty()) {
                                // update ListView adapter
                                adapter.listData.clear();
                                adapter.listData.addAll(new ArrayList<>(Arrays.asList(patientList.getDataPayload().split(","))));
                                adapter.notifyDataSetChanged();

                                emptyListTextView.setVisibility(View.GONE); // hide empty list text
                            }
                        }
                    });

                } else {

                    // update UI with query result
                    PatientListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // query over, hide progress bar

                            Toast toast = Toast.makeText(PatientListActivity.this,
                                    "Error: could not get patient list from server.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }

            }
        }).start();
    }

    /**
     * Used by patient list view.
     */
    private class PatientAdapter extends ArrayAdapter<String> {

        Activity activity = null;
        ArrayList<String> listData;

        public PatientAdapter(ListActivity li, ArrayList<String> patientList)
        {
            super(li, 0, patientList);
            listData = patientList;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_patient, null);
            }

            final String patientName = listData.get(position);

            // creates individual button in ListView for each patient
            Button patientButton = (Button)convertView.findViewById(R.id.listPatientButton);
            patientButton.setText("Name: "+ patientName);
            patientButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(PatientListActivity.this);
                    builder.setTitle("Go to patient page?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(PatientListActivity.this, PatientActivity.class);

                            // through intent, pass patient information to activity
                            intent.putExtra(PATIENT_USER_ID, patientName);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });
            return convertView;
        }
    }
}