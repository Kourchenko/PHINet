package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.CSEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.util.ArrayList;

/**
 * Activity deals specifically with interacting with user's own data.
 */
public class ViewDataActivity extends Activity {

    Button backBtn, intervalSelectionBtn, analyticsBtn;
    TextView dataStatusText, loggedInText, entityNameText, analyticsResultText;
    Spinner sensorSelectionSpinner;
    GraphView graph;
    String entityName, currentSensorSelected, mostRecentlySelectedTask;
    ProgressBar analyticsWait;

    final int SLEEP_TIME = 250;

    // --- used by the interval selector ---
    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;
    // --- used by the interval selector ---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdata);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        // get name of entity to determine whose data to display
        entityName = getIntent().getExtras().getString(ConstVar.ENTITY_NAME);

        graph = (GraphView) findViewById(R.id.graph); // reset graph when updating

        analyticsResultText = (TextView) findViewById(R.id.analyticsResultTextView);

        analyticsWait = (ProgressBar) findViewById(R.id.analyticsProgressBar);
        analyticsWait.setVisibility(View.GONE); // analytics not requested yet; hide it

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);  // display username on screen

        entityNameText = (TextView) findViewById(R.id.entityNameView);
        entityNameText.setText(entityName + "'s data");

        // TextView used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatusTextView);

        ArrayList<CSEntry> myData = DBSingleton.getInstance(getApplicationContext()).getDB()
                .getGeneralCSData(entityName);

        // used to store all sensor names; then displayed to user
        ArrayList<String> sensors = new ArrayList<>();

        if (myData != null) {
            // get all sensors in order to allow user to choose
            for (int i = 0; i < myData.size(); i++) {
                // check for un-added sensor and verify that it isn't NULL_FIELD
                if (!sensors.contains(myData.get(i).getSensorID())
                        && !myData.get(i).getSensorID().equals(ConstVar.NULL_FIELD)) {
                    sensors.add(myData.get(i).getSensorID()); // new sensor detected; store now
                }
            }
        }

        if (sensors.size() == 0) {
            sensors.add("No data available");
        }

        sensorSelectionSpinner = (Spinner) findViewById(R.id.sensorSelectorSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this,
                            android.R.layout.simple_spinner_item, sensors);

        sensorSelectionSpinner.setAdapter(adapter);

        currentSensorSelected = sensorSelectionSpinner.getSelectedItem().toString();

        sensorSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // reload if different sensor chosen
                if (!currentSensorSelected.equals(adapter.getItem(position).toString())) {

                    // update the currentSensorSelected then reload the graph
                    currentSensorSelected = adapter.getItem(position).toString();
                    updateGraph();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        updateGraph(); // provides the initial rendering of the graph

        intervalSelectionBtn = (Button) findViewById(R.id.intervalSelectionBtn);
        intervalSelectionBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                AlertDialog.Builder initialInterval = generateIntervalSelector(
                        ConstVar.INTERVAL_TITLE_START);
                initialInterval.show();
            }
        });

        analyticsBtn = (Button) findViewById(R.id.analyticsBtn);
        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connManager.getActiveNetworkInfo();

                // only attempt analytics if connected to Network
                if (netInfo != null) {
                    AlertDialog.Builder analyticDialog = generateAnalyticsSelector();
                    analyticDialog.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Error: Network connection required", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        backBtn = (Button) findViewById(R.id.userDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Allows users to select analytics function to be perform on current data set
     *
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateAnalyticsSelector() {
        final Spinner analyticSelector = new Spinner(ViewDataActivity.this);

        final ArrayList<String> analyticTasks = new ArrayList<>();
        analyticTasks.add("Mean");
        analyticTasks.add("Mode");
        analyticTasks.add("Median");
        // TODO - create more analytic tasks

        final ArrayAdapter<String> adapter = new ArrayAdapter(ViewDataActivity.this,
                R.layout.spinner_item, R.id.textview, analyticTasks);

        analyticSelector.setAdapter(adapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewDataActivity.this);
        builder.setTitle("Choose analytic task");
        builder.setView(analyticSelector);

        analyticSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // update chosen task; then if "Select" is chosen in dialog, this task will be invoked
                mostRecentlySelectedTask = analyticTasks.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String chosenTime = Utils.createTimeStringInterval(
                        dataStatusText.getText().toString());
                final String processID = selectAnalyticProcessID(mostRecentlySelectedTask);

                CSEntry queryResult = DBSingleton.getInstance(getApplicationContext()).getDB()
                        .getSpecificCSData(entityName, chosenTime, processID);

                // first check if Analytic request isn't already in ContentStore and  still valid
                if (queryResult != null
                        && Utils.isValidFreshnessPeriod(queryResult.getFreshnessPeriod(),
                        queryResult.getTimeString())) {

                    analyticsResultText.setText(mostRecentlySelectedTask + " is " +
                            queryResult.getDataPayload());
                } else {
                    // query server for analytic task
                    Name packetName = JNDNUtils.createName(entityName, currentSensorSelected,
                            chosenTime, processID);
                    Interest interest = JNDNUtils.createInterestPacket(packetName);

                    // add entry into PIT
                    final PITEntry data = new PITEntry(currentSensorSelected, processID,
                            chosenTime, entityName, ConstVar.SERVER_IP);

                    DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(data);

                    Utils.forwardInterestPacket(interest, getApplicationContext()); // forward now

                    // store received packet in database for further review
                    Utils.storeInterestPacket(getApplicationContext(), interest);

                    analyticsWait.setVisibility(View.VISIBLE); // request sent; display progress bar
                    analyticsBtn.setVisibility(View.GONE); // prevent user from resending request

                    // create thread to check for result
                    new Thread(new Runnable() {
                        public void run() {

                            int maxLoopCount = 12; // SLEEP_TIME*12 = 3 seconds (somewhat arbitrary)
                            int loopCount = 0;
                            CSEntry candidateData = null;

                            while (loopCount++ < maxLoopCount) {

                                candidateData = DBSingleton
                                        .getInstance(getApplicationContext()).getDB()
                                        .getSpecificCSData(entityName, chosenTime, processID);

                                if (candidateData != null
                                        && Utils.isValidFreshnessPeriod(
                                        candidateData.getFreshnessPeriod(),
                                        candidateData.getTimeString())) {

                                    break; // result found; break from
                                }

                                SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                            }

                            final CSEntry analyticsResultFinal = candidateData;

                            ViewDataActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    // delete analytic request Interest from PIT
                                    DBSingleton.getInstance(getApplicationContext()).getDB()
                                            .deletePITEntry(data.getUserID(), data.getTimeString(),
                                                    data.getIpAddr());

                                    analyticsBtn.setVisibility(View.VISIBLE); // place button back on view
                                    analyticsWait.setVisibility(View.GONE); // hide; result checked now

                                    if (analyticsResultFinal != null) {

                                        analyticsResultText.setText(mostRecentlySelectedTask + " is "
                                                +analyticsResultFinal.getDataPayload());
                                    } else {
                                        // nothing found; display error

                                        analyticsResultText.setText("Error processing request.");
                                    }
                                }
                            });

                        }
                    }).start();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetIntervalParams(); // clear so that future updates may occur
                dialog.cancel();
            }
        });

        return builder;
    }

    /**
     * Allows users to select date regarding interval of requested data
     *
     * @param title used to set title of dialog
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateIntervalSelector(String title) {
        final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewDataActivity.this);
        builder.setTitle(title);
        builder.setView(intervalSelector);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // get user input
                int day = intervalSelector.getDayOfMonth();
                int month = intervalSelector.getMonth() + 1; // offset required
                int year = intervalSelector.getYear();

                if (startYear == 0) {
                    // startYear == 0 means this is the first input (nothing has been set)
                    // store now and request again

                    startYear = year;
                    startMonth = month;
                    startDay = day;

                    // call again to get end interval
                    final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
                    AlertDialog.Builder secondInterval = generateIntervalSelector(ConstVar.INTERVAL_TITLE_END);
                    secondInterval.setView(intervalSelector);

                    secondInterval.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // start input already set, now store end input
                            endYear = intervalSelector.getYear();
                            endMonth = intervalSelector.getMonth() + 1; // offset required
                            endDay = intervalSelector.getDayOfMonth();

                            if (Utils.isValidInterval(startYear, startMonth, startDay, endYear, endMonth, endDay)) {
                                // now that interval has been entered, update the graph

                                analyticsResultText.setText("none requested"); // reset text for new data
                                updateGraph();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Invalid interval: start must be before end.", Toast.LENGTH_LONG);
                                toast.show();

                                resetIntervalParams(); // clear so that future updates may occur
                            }
                        }
                    });
                    secondInterval.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetIntervalParams(); // clear so that future updates may occur
                            dialog.cancel();
                        }
                    });

                    secondInterval.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetIntervalParams(); // clear so that future updates may occur
                dialog.cancel();
            }
        });

        return builder;
    }

    /**
     * Method takes an analyticTask and maps it to the appropriate process id.
     *
     * @param analyticTask used to determine process id
     * @return process id mapped to analyticTask
     */
    public String selectAnalyticProcessID(String analyticTask) {

        // TODO - expand available analytic tasks

        if (analyticTask.equals("Mode")) {
            return ConstVar.MODE_ANALYTIC;
        } else if (analyticTask.equals("Median")) {
            return ConstVar.MEDIAN_ANALYTIC;
        } else {
            return ConstVar.MEAN_ANALYTIC; // mean is only task that remains
        }
    }

    /**
     * Should be invoked when graphing params have been
     * updated (i.e., new sensor or interval selection).
     *
     * Method redraws the graph based upon the updated criterion.
     */
    public void updateGraph() {

        if (graph != null) {
            graph.removeAllSeries(); // graph exists, wipe all its data before adding new data
        }

        // holds data before conversion
        ArrayList<CSEntry> myData = DBSingleton.getInstance(getApplicationContext()).getDB()
                .getGeneralCSData(entityName);

        // holds data after conversion
        ArrayList<Float> myFloatData;

        // the final boolean parameter denotes whether timeString is endDate
        String startDate = Utils.generateTimeStringFromInts(startYear, startMonth, startDay, false);
        String endDate = Utils.generateTimeStringFromInts(endYear, endMonth, endDay, true);

        // db query unsuccessful: no user data found in cache
        if (myData == null || myData.size() == 0) {

            myFloatData = new ArrayList<>(); // create empty ArrayList; no data exists for it
        }
        // db query was successful; populate ArrayList that will be used to generate the graph
        else {

            // convert valid data to a format that can be displayed
            myFloatData = Utils.convertDBRowToFloats(myData, currentSensorSelected, startDate, endDate);
        }

        // generate interval text to display to user
        String intervalText = Utils.createAnalyticTimeInterval(startDate + "||" + endDate);

        // database query returned valid data, display it now
        if (myFloatData.size() > 0) {

            graph.setTitle("Sensor Values / Chronological Data Points");
            dataStatusText.setText(intervalText); // notify user of chosen interval

            // TODO - improve presentation
            DataPoint[] dataPoints = new DataPoint[myFloatData.size()];
            for (int i = 0; i < myFloatData.size(); i++) {
                dataPoints[i] = new DataPoint(i, myFloatData.get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graph.addSeries(series); // update graph with new data
        }
        // database query returned nothing , notify the user
        else {
            graph.setTitle(""); // no data; remove title
            dataStatusText.setText("Nothing during " + intervalText);
        }

        resetIntervalParams(); // after using the parameters, clear so that future updates may occur
    }

    /**
     * Resets the interval params so that future requests may start fresh.
     */
    public void resetIntervalParams() {
        startDay = 0;
        endDay = 0;
        startMonth = 0;
        endMonth = 0;
        startYear = 0;
        endYear = 0;
    }
}