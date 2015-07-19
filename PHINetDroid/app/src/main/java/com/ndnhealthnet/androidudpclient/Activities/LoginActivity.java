package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
 * Enables user to login to PHINet; request is sent to server for validation, notification sent back.
 */
public class LoginActivity extends Activity {

    Button backBtn, loginBtn;
    EditText userNameEdit, pwEdit;
    ProgressBar progressBar;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);

        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.GONE); // hide progress bar until login pressed

        loginBtn = (Button) findViewById(R.id.loginSubmitBtn);
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connManager.getActiveNetworkInfo();

                // a network connection is required to login
                if (netInfo != null) {

                    /**
                     * Due to the nature of NDN, the client must first send login Interest to
                     * server (because the server is the only one who can validate such requests). The
                     * server will then reply with  an Interest requesting login credentials,
                     * and the client will then reply with a Data packet containing them. The client
                     * then sends an Interest to the server querying for the result,
                     * to which the server replies with a Data packet. If the results are positive,
                     * the client has logged in; otherwise, login failed.
                     */

                    final String userID = userNameEdit.getText().toString().trim();
                    final String password = pwEdit.getText().toString().trim();

                    // both inputs are valid, now query server for login
                    if (Utils.isValidUserName(userID) && Utils.isValidPassword(password)) {

                        progressBar.setVisibility(View.VISIBLE); // show progress bar now

                        // store userID temporarily (until login result); UDPListener logic accesses it
                        Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);

                        String currentTime = Utils.getCurrentTime();

                        /**
                         * Here client userID is stored in sensorID position. We needed to send userID 
                         * but had no place to do so and sensorID would have otherwise been null. 
                         */

                        // send Interest to initiate login
                        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                                currentTime, ConstVar.LOGIN_REQUEST);
                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        // NOTE: we don't store this Interest because it is never satisfied
                        // it merely initiates the login process

                        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

                        // store sent packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        // invoke method that handles the server's reply
                        initialServerReplyHandler(userID, password, currentTime);
                    }
                    // one input (or both) were invalid, notify user
                    else {

                        Toast toast = Toast.makeText(LoginActivity.this,
                                "Error: input syntactically incorrect.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                // network connection invalid; notify user
                else {
                    Toast toast = Toast.makeText(LoginActivity.this,
                            "Error: Network connection required.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        backBtn = (Button) findViewById(R.id.loginBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // NOTE: credentials were only stored if the login was valid

                // get current user credentials and determine whether valid
                String currentPassword = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                Intent returnIntent = new Intent();

                if (Utils.isValidUserName(currentUserID)
                        && Utils.isValidPassword(currentPassword)) {
                    setResult(RESULT_OK, returnIntent); // notifies MainActivity of success
                } else {
                    setResult(RESULT_CANCELED, returnIntent); // notifies MainActivity of failure
                }

                finish();
            }
        });
    }

    /**
     * Method creates thread, after initial login request, and checks
     * for a reply from the server before responding appropriately.
     *
     * @param userID entered to login
     * @param password entered to login
     * @param packetTime of initial Interest packet (used to delete from PIT if result not found)
     */
    private void initialServerReplyHandler(final String userID, final String password, final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 4; // check for SLEEP_TIME*4 = 1 second (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;

                // each loop, check for reply for server
                while (loopCount++ < maxLoopCount) {

                    // check to see if server has sent an Interest asking for client's credentials
                    PITEntry pitEntry = DBSingleton.getInstance(getApplicationContext())
                            .getDB().getSpecificPITEntry(userID, packetTime, ConstVar.LOGIN_CREDENTIAL_DATA);

                    // valid Interest found
                    if (pitEntry != null) {

                        // reply with credentials to satisfy the interest
                        // syntax: "userID,password"
                        String credentialContent = userID + "," + password;

                        CSEntry credentialData = new CSEntry(ConstVar.NULL_FIELD,
                                ConstVar.REGISTER_CREDENTIAL_DATA, pitEntry.getTimeString(),
                                userID, credentialContent, ConstVar.DEFAULT_FRESHNESS_PERIOD);

                        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                pitEntry.getTimeString(), ConstVar.LOGIN_CREDENTIAL_DATA);

                        Data data = JNDNUtils.createDataPacket(credentialContent, packetName);

                        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP)
                                .execute(data.wireEncode().getImmutableArray()); // send credentials now

                        // delete initial Interest placed in PIT to initiate login
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                        // delete Interest requesting login credentials from PIT; it has been satisfied
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .deletePITEntry(pitEntry.getUserID(), pitEntry.getTimeString(),
                                        pitEntry.getIpAddr());

                        // place credential-packet into CACHE
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .addCSData(credentialData);

                        // store packet in database for further review
                        Utils.storeDataPacket(getApplicationContext(), data);

                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        // wait .5 second for server to process results before checking result
                                        credentialQueryHandler(userID, password);
                                    }
                                }, 500); // chosen somewhat arbitrarily
                            }
                        });

                        serverResponseFound = true;
                        break; // result was found, break from loop
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean serverResponseFoundFinal = serverResponseFound;

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!serverResponseFoundFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            Toast toast = Toast.makeText(LoginActivity.this,
                                    "Login failed.\nCould not reach server.", Toast.LENGTH_LONG);
                            toast.show();

                            progressBar.setVisibility(View.GONE); // hide progress; login failed

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
     * Method invoked via handler, after login credentials sent, and
     * sends an Interest to the server to inquire whether login was valid.
     *
     * @param userID entered to login
     * @param password entered to login
     */
    private void credentialQueryHandler(final String userID, final String password) {
        String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        // send interest requesting result of login
        Name packetNameInner = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.LOGIN_RESULT);
        Interest interest = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        PITEntry pitEntry = new PITEntry(userID, ConstVar.LOGIN_RESULT,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(pitEntry);

        Utils.forwardInterestPacket(interest, getApplicationContext()); // forward Interest now

        // store packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        // after sending query result, now handle its response
        credentialQueryResultHandler(userID, password, currentTime);
    }

    /**
     * Method creates thread, after query for result of login sent,
     * and checks the content store for the result before deleting it.
     *
     * @param userID entered to login
     * @param password entered to login
     * @param packetTime of Interest packet sent to query result (used to delete from PIT if result not found)
     */
    private void credentialQueryResultHandler(final String userID, final String password,
                                              final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 8; // check for SLEEP_TIME*8 = 2 seconds (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;
                boolean accountMayExist = false;

                // each loop, check for reply for server
                while (loopCount++ < maxLoopCount) {

                    // query the ContentStore for the login result
                    CSEntry csEntry = DBSingleton.getInstance(getApplicationContext()).getDB()
                            .getSpecificCSData(ConstVar.SERVER_ID, packetTime, ConstVar.LOGIN_RESULT);

                    if (csEntry != null) {
                        if (!csEntry.getDataPayload().equals(ConstVar.LOGIN_FAILED)) {

                            // payload syntax: userType;;userid_1,ipaddr_1||...||userid_n,ipaddr_n"
                            String [] payloadSplit = csEntry.getDataPayload().split(";;");

                            String userType = payloadSplit[0];

                            if (payloadSplit.length == 2) { // only access latter part if it exists


                                // on login, server replies with FIB entries - place into FIB now
                                // AND only send second portion of the string to the fib
                                Utils.insertServerFIBEntries(payloadSplit[1],
                                        csEntry.getTimeString(), getApplicationContext());
                            }

                            // delete LOGIN_RESULT Interest from PIT (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deletePITEntry(ConstVar.SERVER_ID,
                                    csEntry.getTimeString(), ConstVar.SERVER_IP);

                            // login was successful; store values now
                            String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_USER_TYPE_KEY, userType);

                            serverResponseFound = true; // response found
                            accountMayExist = false; // result success; account doesn't exist

                            break; // break from loop, response found

                        } else if (csEntry.getDataPayload().equals(ConstVar.LOGIN_FAILED)) {

                            // delete LOGIN_RESULT Interest from PIT (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deletePITEntry(ConstVar.SERVER_ID,
                                    csEntry.getTimeString(), ConstVar.SERVER_IP);

                            serverResponseFound = true; // response found
                            accountMayExist = true; // result failure; account does exist

                            break; // break from loop, response found
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean accountMayExistFinal = accountMayExist;
                final boolean serverResponseFoundFinal = serverResponseFound;

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE); // hide progress; login process over

                        if (!serverResponseFoundFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                            Toast toast = Toast.makeText(LoginActivity.this,
                                    "Login failed.\nCould not reach server.", Toast.LENGTH_LONG);
                            toast.show();

                        } else if (serverResponseFoundFinal && accountMayExistFinal) {

                            // remove from storage now
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                            Toast toast = Toast.makeText(LoginActivity.this,
                                    "Login failed.\nInput combination does not seem to exist.", Toast.LENGTH_LONG);
                            toast.show();

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
