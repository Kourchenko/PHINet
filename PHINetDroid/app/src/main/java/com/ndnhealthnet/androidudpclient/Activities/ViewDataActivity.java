package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ndnhealthnet.androidudpclient.R;

/**
 * Activity deals specifically with interacting with user's own data.
 */
public class ViewDataActivity extends Activity {

    Button backBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO

        backBtn = (Button) findViewById(R.id.userDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}