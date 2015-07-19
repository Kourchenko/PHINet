package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

/**
 * Enables user to join to PHINet; request is sent to server for validation, notification sent back.
 */
public class SignupActivity extends Activity {

    Button backBtn, signupBtn;
    EditText userNameEdit, pwEdit, verifyPWEdit, emailEdit;
    ProgressBar progressBar;
    RadioButton patientRadioBtn, doctorRadioBtn;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userNameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);
        verifyPWEdit = (EditText) findViewById(R.id.verifyPasswordEditText);
        emailEdit = (EditText) findViewById(R.id.email_editText);
        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);
        patientRadioBtn = (RadioButton) findViewById(R.id.patientRadioButton);
        doctorRadioBtn = (RadioButton) findViewById(R.id.doctorRadioButton);

        patientRadioBtn.toggle(); // let Patient be the default choice

        progressBar.setVisibility(View.GONE); // hide progress bar until signup pressed

        backBtn = (Button) findViewById(R.id.signupBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // NOTE: credentials were only stored if signup was valid

                // get current user credentials and determine whether valid
                String currentPassword = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
                String userType = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_USER_TYPE_KEY, "");

                if (Utils.isValidUserName(currentUserID) && Utils.isValidUserType(userType)
                        && Utils.isValidPassword(currentPassword)) {
                    setResult(RESULT_OK, new Intent()); // notifies MainActivity of success
                } else {
                    setResult(RESULT_CANCELED, new Intent()); // notifies MainActivity of failure
                }

                finish();
            }
        });

        signupBtn = (Button) findViewById(R.id.signupSubmitBtn);
        signupBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
               
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connManager.getActiveNetworkInfo();

                // a network connection is required to signup
                if (netInfo != null) {

                    /**
                     * Due to the nature of NDN, the client must first send signup Interest to
                     * server (because the server is the only one who can validate such requests). The
                     * server will an Interest requesting signup credentials, and the client will
                     * then reply with a Data packet containing them. The client then sends an
                     * Interest to the server querying for the result, to which the server replies
                     * with a Data packet. If the results are positive, the client has signed up;
                     * otherwise, signup failed.
                     */

                    final String userID = userNameEdit.getText().toString().trim();
                    final String password = pwEdit.getText().toString().trim();
                    final String email = emailEdit.getText().toString().isEmpty()
                            ? ConstVar.NULL_FIELD : emailEdit.getText().toString().trim();
                    final String userType = patientRadioBtn.isChecked()
                            ? ConstVar.PATIENT_USER_TYPE : ConstVar.DOCTOR_USER_TYPE;

                    final boolean pwMatch = password.equals(verifyPWEdit.getText().toString());

                    // all inputs are valid, now query server for signup
                    if (pwMatch && Utils.isValidUserName(userID) && Utils.isValidPassword(password)
                            && (email.equals(ConstVar.NULL_FIELD) || Utils.isValidEmail(email))
                            && Utils.isValidUserType(userType)) {

                        // store userID temporarily (until signup result); UDPListener logic accesses it
                        Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);

                        progressBar.setVisibility(View.VISIBLE); // show progress bar now

                        String currentTime = Utils.getCurrentTime();

                        /**
                         * Here client userID is stored in sensorID position. We needed to send userID 
                         * but had no place to do so and sensorID would have otherwise been null. 
                         */

                        // send Interest to initiate signup
                        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                                currentTime, ConstVar.REGISTER_REQUEST);

                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        // NOTE: we don't store this Interest because it is never satisfied
                            // it merely initiates the signup process

                        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

                        // store received packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        // invoke method that handles the server's reply
                        initialServerReplyHandler(userID, password, email, userType, currentTime);
                    }
                    // passwords don't match; only notify is PW was valid in first place
                    else if (Utils.isValidPassword(password) && !pwMatch) {

                        Toast toast = Toast.makeText(SignupActivity.this,
                                "Error: passwords don't match.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    // one input (or both) were invalid, notify user
                    else {
                        Toast toast = Toast.makeText(SignupActivity.this,
                                "Error: input syntactically incorrect.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                // network connection invalid; notify user
                else {
                    Toast toast = Toast.makeText(SignupActivity.this,
                            "Error: Network connection is required.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    /**
     * Method creates thread, after initial signup request, and checks
     * for a reply from the server before responding appropriately.
     *
     * @param userID entered to signup
     * @param password entered to signup
     * @param email entered to signup
     * @param userType entered to signup
     * @param packetTime of initial Interest packet (used to delete from PIT if result not found)
     */
    private void initialServerReplyHandler(final String userID, final String password,
                                           final String email, final String userType,
                                           final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 4; // check for SLEEP_TIME*4 = 1 second (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;

                // each loop, check for reply for server
                while (loopCount++ < maxLoopCount) {

                    // check to see if server has sent an Interest asking for client's credentials
                    PITEntry pitEntry = DBSingleton.getInstance(getApplicationContext())
                            .getDB().getSpecificPITEntry(userID, packetTime, ConstVar.REGISTER_CREDENTIAL_DATA);

                    // valid interest found
                    if (pitEntry != null) {

                        // reply with credentials to satisfy the interest
                            // syntax: "userID,password,email"
                        String credentialContent = userID + "," + password + "," + email + "," + userType;

                        CSEntry credentialData = new CSEntry(ConstVar.NULL_FIELD,
                                ConstVar.REGISTER_CREDENTIAL_DATA, pitEntry.getTimeString(),
                                userID, credentialContent, ConstVar.DEFAULT_FRESHNESS_PERIOD);

                        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                pitEntry.getTimeString(), ConstVar.REGISTER_CREDENTIAL_DATA);

                        Data data = JNDNUtils.createDataPacket(credentialContent, packetName);

                        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP)
                                .execute(data.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                        // delete Interest requesting signup credentials from PIT; it has been satisfied
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .deletePITEntry(pitEntry.getUserID(), pitEntry.getTimeString(),
                                        pitEntry.getIpAddr());

                        // place packet into CACHE
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .addCSData(credentialData);

                        // delete initial Interest placed in PIT to initiate signup
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                        // store packet in database for further review
                        Utils.storeDataPacket(getApplicationContext(), data);

                        SignupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        // wait 1 second for server to process results before checking result
                                        credentialQueryHandler(userID, password, userType);
                                    }
                                }, 1000); // chosen somewhat arbitrarily
                            }
                        });

                        serverResponseFound = true;
                        break; // result was found, break from loop
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean serverResponseFoundFinal = serverResponseFound;

                SignupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!serverResponseFoundFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            Toast toast = Toast.makeText(SignupActivity.this,
                                    "Signup failed.\nCould not reach server.", Toast.LENGTH_LONG);
                            toast.show();
                            
                            progressBar.setVisibility(View.GONE); // hide progress; signup failed

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);
                        }
                    }
                });
            }

        }).start();
    }

    /**
     * Method invoked via handler, after register credentials sent, and
     * sends an Interest to the server to inquire whether register was valid.
     *
     * @param userID entered to signup
     * @param password entered to signup
     * @param userType entered to signup
     */
    private void credentialQueryHandler(final String userID, final String password,
                                        final String userType) {

        String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        // send interest requesting result of signup
        Name packetNameInner = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.REGISTER_RESULT);
        Interest interest = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        PITEntry data = new PITEntry(userID, ConstVar.REGISTER_RESULT,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(data);

        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

        // store packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        // after sending query result, now handle its response
        credentialQueryResultHandler(userID, password, userType, currentTime);
    }

    /**
     * Method creates thread, after query for result of register send,
     * and checks the content store for the result before deleting it.
     *
     * @param userID entered to signup
     * @param password entered to signup
     * @param userType entered to signup
     * @param packetTime of Interest packet sent to query result (used to delete from PIT if result not found)
     */
    private void credentialQueryResultHandler(final String userID, final String password,
                                              final String userType, final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 8; // check for SLEEP_TIME*8 = 2 seconds (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;
                boolean accountMayExist = false;

                // each loop, check for reply for server
                while (loopCount++ < maxLoopCount) {

                    // query the ContentStore for the signup result
                    CSEntry csEntry = DBSingleton.getInstance(getApplicationContext()).getDB()
                            .getSpecificCSData(ConstVar.SERVER_ID, packetTime, ConstVar.REGISTER_RESULT);

                    if (csEntry != null) {

                        // signup response yields success
                        if (!csEntry.getDataPayload().equals(ConstVar.REGISTER_FAILED)) {

                            // on signup, server replies with FIB entries - place into FIB now
                            Utils.insertServerFIBEntries(csEntry.getDataPayload(),
                                    csEntry.getTimeString(), getApplicationContext());

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(csEntry.getUserID(), csEntry.getTimeString());

                            // signup was successful; store values now
                            String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_USER_TYPE_KEY, userType);

                            serverResponseFound = true; // response found
                            accountMayExist = false; // result success; account doesn't exist

                            break; // break from loop, response found

                        } else if (csEntry.getDataPayload().equals(ConstVar.REGISTER_FAILED)) {

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(csEntry.getUserID(), csEntry.getTimeString());

                            serverResponseFound = true; // response found
                            accountMayExist = true; // result failure; account does exist

                            break; // break from loop, response found
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean accountMayExistFinal = accountMayExist;
                final boolean serverResponseFoundFinal = serverResponseFound;

                SignupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE); // hide progress; signup process over

                        if (!serverResponseFoundFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                            Toast toast = Toast.makeText(SignupActivity.this,
                                    "Signup failed.\nCould not reach server.", Toast.LENGTH_LONG);
                            toast.show();

                        } else if (serverResponseFoundFinal && accountMayExistFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            Toast toast = Toast.makeText(SignupActivity.this,
                                    "Signup failed.\nAccount may already exist.", Toast.LENGTH_LONG);
                            toast.show();;

                        } else if (serverResponseFoundFinal && !accountMayExistFinal) {

                            // go to main page; signup was successful
                            setResult(RESULT_OK, new Intent());
                            finish();
                        }
                    }
                });

            }
        }).start();
    }
}

