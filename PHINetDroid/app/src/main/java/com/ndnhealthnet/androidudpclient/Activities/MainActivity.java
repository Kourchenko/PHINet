package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.Comm.BTSensorComm;
import com.ndnhealthnet.androidudpclient.Comm.UDPListener;
import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

/**
 *  The "main loop" of the application; Activity should always be on stack.
 */
public class MainActivity extends Activity {

    final int CREDENTIAL_RESULT_CODE = 1; // used to identify the result of Login/Signup Activities

	Button logoutBtn, myDataBtn, patientsBtn, loginBtn, signupBtn,
            sensorBtn, viewPacketsBtn, doctorsBtn;
	TextView credentialWarningText, loggedInText;
	UDPListener receiverThread;
    BTSensorComm btSensorComm;
    Thread serverSynchWorker;

    // used to specify when listener "receiverThread" should actively listen for packets
    public static boolean continueReceiverExecution = true;

    public static String deviceIP; // the IP of this particular client

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get this device's ip
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        receiverThread = new UDPListener(getApplicationContext());
        receiverThread.start(); // begin listening for packets

        serverSynchWorker = createServerSynchRequestWorker();
        serverSynchWorker.start(); // begin worker who initiates Synch requests ever given interval

        /*
        TODO - initiate bluetooth comm. component
        btSensorComm = new BTSensorComm(getApplicationContext());
        btSensorComm.start(); // initiate bluetooth sensor communication thread
        */

        onCreateHelper();
    }

    /**
     * Method exists so that layout can easily be reset after certain actions (i.e., login/signup)
     */
    private void onCreateHelper()
    {
        setContentView(R.layout.activity_main);

        final String myUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
        final String userType = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_USER_TYPE_KEY, "");

        if (!myUserID.isEmpty() && !userType.isEmpty()) {
            // place userID on screen if user has logged in
            loggedInText = (TextView) findViewById(R.id.loggedInTextView);
            loggedInText.setText(myUserID);
        }

        credentialWarningText = (TextView) findViewById(R.id.credentialWarningTextView);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), CREDENTIAL_RESULT_CODE);
            }
        });

        signupBtn = (Button) findViewById(R.id.signupBtn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SignupActivity.class), CREDENTIAL_RESULT_CODE);
            }
        });

        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // clear userID, userType, and password on logout
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_USER_TYPE_KEY, "");

                onCreateHelper();
            }
        });

        viewPacketsBtn = (Button) findViewById(R.id.packetsBtn);
        viewPacketsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PacketListActivity.class));
            }
        });

        sensorBtn = (Button) findViewById(R.id.sensorSettingsBtn);
        sensorBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SensorListActivity.class));
            }
        });

        myDataBtn = (Button) findViewById(R.id.myDataBtn);
        myDataBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ViewDataActivity.class);

                // to view client's data, pass their user id
                intent.putExtra(ConstVar.ENTITY_NAME, myUserID);
                startActivity(intent);
            }
        });

        patientsBtn = (Button) findViewById(R.id.patientsBtn);
        patientsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PatientListActivity.class));
            }
        });

        doctorsBtn = (Button) findViewById(R.id.doctorsBtn);
        doctorsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DoctorListActivity.class));
            }
        });

        // check to see if userID has been entered (denotes Logged-in user)
        if (myUserID.isEmpty() && userType.isEmpty()) {

            //destroy all buttons until user enters credentials
            patientsBtn.setVisibility(View.GONE);
            doctorsBtn.setVisibility(View.GONE);
            myDataBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.GONE);
            sensorBtn.setVisibility(View.GONE);
            viewPacketsBtn.setVisibility(View.GONE);

        } else {

            // user has entered credentials, remove warning and buttons
            credentialWarningText.setVisibility(View.GONE);
            loginBtn.setVisibility(View.GONE);
            signupBtn.setVisibility(View.GONE);

            if (userType.equals(ConstVar.PATIENT_USER_TYPE)) {
                patientsBtn.setVisibility(View.GONE); // a patient entity can't have its own patients
            } else {
                doctorsBtn.setVisibility(View.GONE); // a doctor entity can't have its own doctors
            }
        }
    }

    /**
     * Should be invoked automatically after user enters credentials
     *
     * @param requestCode code of activity that has returned a result
     * @param resultCode status of activity return
     * @param data intent associated with returned activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CREDENTIAL_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                onCreateHelper(); // reset the layout after user has entered credentials
            }
        }
    }

    /**
     * Creates thread that initiates server-synch request every SYNC_INTERVAL_MILLIS period of time.
     */
    private Thread createServerSynchRequestWorker() {
        return new Thread(new Runnable() {
            public void run() {

                // each loop sends server-synch requests then sleeps for SYNC_INTERVAL_MILLIS
                while (true) {

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = connManager.getActiveNetworkInfo();

                    // a network connection is required to synch
                    if (netInfo != null) {

                        requestSynch(getApplicationContext());
                    }

                    SystemClock.sleep(ConstVar.SYNCH_INTERVAL_MILLIS); // sleep until next synch
                }
            }
        });
    }

    /**
     * Invoked to request Synch from server.
     */
    static void requestSynch(Context context) {

        // interval syntax: start||end
        String currentTime = Utils.getPreviousSynchTime() + "||" + Utils.getCurrentTime();

        String myUserID = Utils.getFromPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        // query server for initialization of synch request
        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, myUserID,
                currentTime, ConstVar.INITIATE_SYNCH_REQUEST);
        Interest interest = JNDNUtils.createInterestPacket(packetName);

        // add entry into PIT (we expect a SYNCH_DATA_REQUEST packet back)
        PITEntry data = new PITEntry(myUserID, ConstVar.SYNCH_DATA_REQUEST,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(context).getDB().addPITData(data);

        Utils.forwardInterestPacket(interest, context); // forward Interest now

        // store sent packet in database for further review
        Utils.storeInterestPacket(context, interest);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        continueReceiverExecution = false;  // notify receiver to terminate
    }
}