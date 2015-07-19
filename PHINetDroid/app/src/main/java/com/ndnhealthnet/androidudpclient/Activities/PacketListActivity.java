package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PacketDBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;

/**
 * Lowers the level of abstraction by providing the user with
 * the content of all NDN-compliant packets that have been sent
 */
public class PacketListActivity extends ListActivity {

    Button backBtn;
    TextView loggedInText, emptyListTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packetlist);

        ArrayList<PacketDBEntry> packetList = DBSingleton.getInstance(getApplicationContext()).getDB().getAllPacketData();

        if (packetList == null) {
            packetList = new ArrayList<>();
        }

        emptyListTextView = (TextView) findViewById(R.id.emptyListTextView);

        final PacketAdapter adapter = new PacketAdapter(this, packetList);
        setListAdapter(adapter);

        if (adapter.getCount() > 0) {
            // hide "empty packet list" text when packets actually do exist
            emptyListTextView.setVisibility(View.GONE);
        }

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Used by packet list view.
     */
    private class PacketAdapter extends ArrayAdapter<PacketDBEntry> {

        Activity activity = null;
        ArrayList<PacketDBEntry> listData;

        public PacketAdapter(ListActivity li, ArrayList<PacketDBEntry> allPackets)
        {
            super(li, 0, allPackets);
            listData = allPackets;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_packet, null);
            }

            final String packetContent = listData.get(position).getPacketContent();

            final int methodScopePosition = position;

            // creates individual button in ListView for each patient
            Button packetButton = (Button)convertView.findViewById(R.id.listPacketButton);
            packetButton.setText(packetContent);
            packetButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(PacketListActivity.this);

                    builder.setTitle("Packet Name: " + listData.get(methodScopePosition).getPacketName());

                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
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
