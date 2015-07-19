package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Activity displays list of current doctors and allows addition of more doctors.
 */
public class DoctorListActivity extends ListActivity {

    Button backBtn;
    Button addNewDoctorBtn;
    TextView emptyListTextView, loggedInText;
    DoctorAdapter adapter;
    ProgressBar progressBar;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload (to display new doctors)
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctorlist);

        final String userID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(userID); // place username on screen

        progressBar = (ProgressBar) findViewById(R.id.doctorProgressBar);
        progressBar.setVisibility(View.GONE); // hide until query made

        // call to getDoctors() will populate; pass empty ArrayList now
        adapter = new DoctorAdapter(this, new ArrayList<String>());
        setListAdapter(adapter);

        getDoctors(userID); // query server to populate ListView

        emptyListTextView = (TextView) findViewById(R.id.emptyListTextView);

        if (adapter.getCount() > 0) {
            // hide "empty doctor list" text when doctors actually do exist
            emptyListTextView.setVisibility(View.GONE);
        }

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        addNewDoctorBtn = (Button) findViewById(R.id.addNewDoctorBtn);
        addNewDoctorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final EditText doctorInput = new EditText(DoctorListActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(DoctorListActivity.this);
                builder.setTitle("Input Name");
                builder.setView(doctorInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String doctorName = doctorInput.getText().toString().trim();

                        if (Utils.isValidUserName(doctorName) && !adapter.listData.contains(doctorName)) {
                            initiateAddDoctorQuery(doctorName, userID);
                        } else if (Utils.isValidUserName(doctorName) && adapter.listData.contains(doctorName)) {
                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Already your doctor", Toast.LENGTH_LONG);
                            toast.show();
                        } else{
                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Error: name syntactically invalid.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    /**
     * Invoked when user enters new doctor; method polls server to initiate exchange.
     * Server then replies with an Interest, and the client replies with a Data packet
     * containing the doctor name then queries server for entire list of doctors.
     *
     * @param doctorName to be added
     * @param userID requesting new doctor
     */
    private void initiateAddDoctorQuery(final String doctorName, final String userID) {

        final String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */
        
        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.ADD_DOCTOR);
        Interest interest = JNDNUtils.createInterestPacket(packetName);

        // NOTE: don't add Interest into PIT (it shouldn't be satisfied); only initiates the process

        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

        // store received packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        progressBar.setVisibility(View.VISIBLE); // query made, now display
        
        // create thread to check for Interest from server
        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 8; // check for SLEEP_TIME*8 = 2 seconds (somewhat arbitrary)
                int loopCount = 0;
                PITEntry candidateEntry = null;

                while (loopCount++ < maxLoopCount) {

                    candidateEntry = DBSingleton
                            .getInstance(getApplicationContext()).getDB()
                            .getSpecificPITEntry(userID, currentTime,
                                    ConstVar.CLIENT_DOCTOR_SELECTION);

                    if (candidateEntry != null) {

                        break; // result found; break from
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                if (candidateEntry != null) {

                    // Interest found; reply with doctor's name
                    Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                            candidateEntry.getTimeString(), ConstVar.CLIENT_DOCTOR_SELECTION);

                    Data data = JNDNUtils.createDataPacket(doctorName, packetName);

                    new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP)
                            .execute(data.wireEncode().getImmutableArray()); // send Data now

                    // store received packet in database for further review
                    Utils.storeDataPacket(getApplicationContext(), data);

                    // delete CLIENT_DOCTOR_SELECTION Interest from PIT
                    DBSingleton.getInstance(getApplicationContext())
                            .getDB().deletePITEntry(ConstVar.SERVER_ID,
                            candidateEntry.getTimeString(), ConstVar.SERVER_IP);

                    DoctorListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    // wait .5 second for server to process results before checking result
                                    queryServerForResult(doctorName, userID);
                                }
                            }, 500); // chosen somewhat arbitrarily
                        }
                    });
                } else {

                    DoctorListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // query process over; hide

                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Error: could not reach server (maybe due to an invalid input).", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Invoked after sending to-be-added doctor to server; checks for result.
     *
     * @param doctorName that was sent to server
     * @param userID of client using this device
     */
    private void queryServerForResult(final String doctorName, String userID) {

        getDoctors(userID); // query server for update doctor list

        DoctorListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // wait for 1.5 seconds before displaying error toast (give server time to respond)
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (!adapter.listData.contains(doctorName)) {
                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Error: doctor could not be added.", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Success: Doctor was be added.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }, 1500); // chosen somewhat arbitrarily
            }
        });
    }

    /**
     * Queries server for all doctors and waits for response.
     *
     * @param userID of client using this device
     */
    private void getDoctors(final String userID) {

        progressBar.setVisibility(View.VISIBLE); // query begins, show progressbar

        final String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.DOCTOR_LIST);
        Interest interest = JNDNUtils.createInterestPacket(packetName);

        // add entry into PIT
        PITEntry pitEntry = new PITEntry(userID, ConstVar.DOCTOR_LIST,
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
                                    ConstVar.DOCTOR_LIST);

                    if (candidateEntry != null) {
                        break; // result found; break from
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final CSEntry doctorList = candidateEntry;

                // delete DOCTOR_LIST Interest from PIT
                DBSingleton.getInstance(getApplicationContext())
                        .getDB().deletePITEntry(ConstVar.SERVER_ID,
                        currentTime, ConstVar.SERVER_IP);

                if (candidateEntry != null) {

                    // update UI with query result
                    DoctorListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // query over, hide progress bar

                            // only update if data exists
                            if (!doctorList.getDataPayload().isEmpty()) {
                                // update ListView adapter
                                adapter.listData.clear();
                                adapter.listData.addAll(new ArrayList<>(Arrays.asList(doctorList.getDataPayload().split(","))));
                                adapter.notifyDataSetChanged();

                                emptyListTextView.setVisibility(View.GONE); // hide empty list text
                            }
                        }
                    });

                } else {

                    // update UI with query result
                    DoctorListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // query over, hide progress bar

                            Toast toast = Toast.makeText(DoctorListActivity.this,
                                    "Error: could not get doctor list from server.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }

            }
        }).start();
    }

    /**
     * Used by doctor list view.
     */
    private class DoctorAdapter extends ArrayAdapter<String> {

        Activity activity = null;
        ArrayList<String> listData;

        public DoctorAdapter(ListActivity li, ArrayList<String> allFIBData)
        {
            super(li, 0, allFIBData);
            listData = allFIBData;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_doctor, null);
            }

            final String drName = listData.get(position);

            // creates individual button in ListView for each patient
            TextView doctorText = (TextView) convertView.findViewById(R.id.listDoctorText);
            doctorText.setText(drName);

            return convertView;
        }
    }
}