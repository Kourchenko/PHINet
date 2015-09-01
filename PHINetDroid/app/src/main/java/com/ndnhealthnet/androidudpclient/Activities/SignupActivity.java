package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

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

                if (Utils.isValidUserName(currentUserID) && Utils.isValidPassword(currentPassword)) {
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

                   // TODO
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
}

