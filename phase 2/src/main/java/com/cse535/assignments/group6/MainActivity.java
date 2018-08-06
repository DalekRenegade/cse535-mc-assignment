package com.cse535.assignments.group6;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements GraphUpdateCallback, SensorManagerCallback {

    public static SensorManagerCallback sensorManagerCallback;
    private final Handler handler = new Handler();
    private GraphView gvGraph;
    private LineGraphSeries<DataPoint> dataSeriesX, dataSeriesY, dataSeriesZ;
    private Runnable graphThread;
    private Deque queue;
    private Button uploadButton;
    private AccelerometerService accelerometerService;
    private AccelerometerBroadcastReceiver receiver;
    private LocalBroadcastManager localBroadcastManager;
    private int currentIndex = 0;
    private boolean serviceInvoked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // -> By Rishabh
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        setGvGraphProperties();
        registerReceiver();

        graphThread = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, Constants.DELAY);
            }
        };
    }

    private void initialize() {
        queue = new LinkedList<AccelerometerData>();
        gvGraph = findViewById(R.id.graph);
        uploadButton = findViewById(R.id.buttonUploadDb);
        sensorManagerCallback = this;
        Constants.DATABASE_NAME = HelperClass.getApplicationResource(this, R.string.app_name);
    }

    private PatientInfo verifyPatientInfo() {
        PatientInfo info = null;
        try {
            boolean valid = true;
            EditText etPatientId = findViewById(R.id.editTextPatientId);
            EditText etPatientName = findViewById(R.id.editTextPatientName);
            EditText etPatientAge = findViewById(R.id.editTextAge);
            RadioGroup grp = findViewById(R.id.radioSex);
            RadioButton etPatientSex = findViewById(grp.getCheckedRadioButtonId());
            String name = etPatientName.getText().toString().trim();
            String id = etPatientId.getText().toString().trim();
            String sex = etPatientSex.getText().toString().trim();
            String age = etPatientAge.getText().toString().trim();
            if (TextUtils.isEmpty(id) || !id.matches("[a-zA-Z0-9]+")) {
                etPatientId.setError("Valid input required...");
                valid = false;
            }
            if (TextUtils.isEmpty(name) || !name.matches("[a-zA-Z]+")) {
                etPatientName.setError("Valid input required...");
                valid = false;
            }
            if (TextUtils.isEmpty(age) || !TextUtils.isDigitsOnly(age)) {
                etPatientAge.setError("Valid input required...");
                valid = false;
            }
            if (valid)
                info = new PatientInfo(name, id, sex, Integer.parseInt(age));
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            Toast.makeText(getApplicationContext(), "Error in patient data...", Toast.LENGTH_SHORT).show();
        } finally {
            return info;
        }
    }

    private void registerReceiver() {
        receiver = new AccelerometerBroadcastReceiver(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION);
            registerReceiver(receiver, intentFilter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void setGvGraphProperties() {
        dataSeriesX = new LineGraphSeries<>();
        dataSeriesY = new LineGraphSeries<>();
        dataSeriesZ = new LineGraphSeries<>();
        dataSeriesX.setColor(Color.GREEN);
        dataSeriesY.setColor(Color.BLUE);
        dataSeriesZ.setColor(Color.RED);

        gvGraph.addSeries(dataSeriesX);
        gvGraph.addSeries(dataSeriesY);
        gvGraph.addSeries(dataSeriesZ);

        dataSeriesX.setTitle("X");
        dataSeriesY.setTitle("Y");
        dataSeriesZ.setTitle("Z");
        gvGraph.getLegendRenderer().setVisible(true);
        gvGraph.getLegendRenderer().setBackgroundColor(Color.LTGRAY);
        gvGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        // set viewport properties      -> By Manish
        gvGraph.getViewport().setXAxisBoundsManual(true);
        gvGraph.getViewport().setYAxisBoundsManual(true);
        gvGraph.getViewport().setMinX(0);
        gvGraph.getViewport().setMaxX(Constants.GRAPH_HOR_SIZE);
        gvGraph.getViewport().setMinY(-1 * Constants.GRAPH_VER_LIMIT);
        gvGraph.getViewport().setMaxY(Constants.GRAPH_VER_LIMIT);
        gvGraph.getViewport().setBackgroundColor(Color.BLACK);
        gvGraph.getViewport().setBorderColor(Color.WHITE);
        gvGraph.getViewport().setDrawBorder(true);

        // set grid & label properties
        gvGraph.getGridLabelRenderer().setHorizontalAxisTitle(Constants.HOR_AXIS_TITLE);
        gvGraph.getGridLabelRenderer().setVerticalAxisTitle(Constants.VER_AXIS_TITLE);
        gvGraph.getGridLabelRenderer().setHighlightZeroLines(true);
        gvGraph.getGridLabelRenderer().setLabelsSpace(5);
        gvGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        gvGraph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setGridColor(Color.WHITE);

        gvGraph.getGridLabelRenderer().reloadStyles();
    }

    // By Manish
    public void onRun(View v) {
        PatientInfo info = verifyPatientInfo();
        uploadButton.setEnabled(false);
        v.setEnabled(false);
        if (info != null) {
            try {
                HelperClass.setDb(this, info);
                Intent startIntent = new Intent(MainActivity.this, AccelerometerService.class);
                startService(startIntent);
                serviceInvoked = true;
            } catch (Exception ex) {
                Log.e(this.getClass().getName(), ex.getMessage());
            }
            Toast.makeText(getApplicationContext(), "RUNNING", Toast.LENGTH_SHORT).show();
        }
    }

    //By Varun
    public void onStop(View v) {
        try {
            Intent stopIntent = new Intent(MainActivity.this, AccelerometerService.class);
            stopService(stopIntent);
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage());
        } finally {
            if (HelperClass.getDb() != null)
                HelperClass.getDb().safeCloseDatabase();
            uploadButton.setEnabled(serviceInvoked);
            Button runButton = findViewById(R.id.buttonRun);
            runButton.setEnabled(true);
        }
        Toast.makeText(getApplicationContext(), "STOPPED", Toast.LENGTH_SHORT).show();
        controlGraph(false, null);
    }

    public void onUploadClick(View v) {
        uploadButton.setEnabled(false);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("UPLOAD");
        progress.setMessage("Uploading database. Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... dbFilePath) {
                return HelperClass.uploadFile(dbFilePath[0]);
            }

            @Override
            protected void onPostExecute(String status) {
                progress.dismiss();
                uploadButton.setEnabled(serviceInvoked);
                Toast.makeText(getApplicationContext(), "Status: UPLOAD " + status.toUpperCase(), Toast.LENGTH_SHORT).show();
            }
        }.execute(HelperClass.getDb().getDbFilePath());
    }

    public void onDownloadClick(View v) {

        final PatientInfo info = verifyPatientInfo();
        if (info != null) {

            final String targetDbFileUrl = Constants.SERVER_URL
                    + (Constants.SERVER_URL.endsWith(File.separator) ? "" : File.separator)
                    + URLEncoder.encode(Constants.DATABASE_NAME + (Constants.DATABASE_NAME.endsWith(".db") ? "" : ".db"));

            String rootPath = getApplicationInfo().dataDir;
            File sdcard = Environment.getExternalStorageDirectory();
            if (sdcard != null)
                rootPath = sdcard.getAbsolutePath();
            String downloadedDbFilePath = rootPath + File.separator + Constants.DATABASE_DOWNLOAD_PATH_FROM_ROOT;

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("DOWNLOAD");
            progress.setMessage("Downloading database. Please wait...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            new AsyncTask<String, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(String... targetUrlAndSaveDest) {
                    return HelperClass.downloadFile(targetUrlAndSaveDest[0], targetUrlAndSaveDest[1]);
                }

                @Override
                protected void onPostExecute(Boolean status) {
                    handler.removeCallbacks(graphThread);
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Status: " + (status ? "DOWNLOADED" : "FILE NOT AVAILABLE"), Toast.LENGTH_SHORT).show();
                    if (status) {
                        //todo:
                        DatabaseHelperClass downloadDbObj = new DatabaseHelperClass(getApplicationContext(),
                                Constants.DATABASE_PATH_FROM_ROOT, Constants.DATABASE_NAME, info);
                        ArrayList<AccelerometerData> accDataList = downloadDbObj.getAccelerometerDataFromDb(Constants.GRAPH_HOR_SIZE);
                        if (accDataList != null) {
                            DataPoint[][] dp = new DataPoint[3][accDataList.size()];
                            for (int i = 0; i < accDataList.size(); i++) {
                                dp[0][i] = new DataPoint(i, accDataList.get(i).getX());
                                dp[1][i] = new DataPoint(i, accDataList.get(i).getY());
                                dp[2][i] = new DataPoint(i, accDataList.get(i).getZ());
                            }
//                            dp = HelperClass.resetGraphData(queue, currentIndex);
                            dataSeriesX.resetData(dp[0]);
                            dataSeriesY.resetData(dp[1]);
                            dataSeriesZ.resetData(dp[2]);
                            gvGraph.getGridLabelRenderer().reloadStyles();
                        } else
                            Toast.makeText(getApplicationContext(),
                                    "No records found for the patient in the database. Please enter correct patient info.", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute(targetDbFileUrl, downloadedDbFilePath);
        }
    }

    // -> By Amit
    // function to plot random data points
    @Override
    public void controlGraph(boolean execution, AccelerometerData data) {

        if (execution) {
            if (queue.size() == Constants.GRAPH_HOR_SIZE)
                queue.poll();
            queue.addLast(data);
            currentIndex++;

            DataPoint[][] dp = HelperClass.resetGraphData(queue, currentIndex);
            dataSeriesX.resetData(dp[0]);
            dataSeriesY.resetData(dp[1]);
            dataSeriesZ.resetData(dp[2]);
            if (queue.size() < Constants.GRAPH_HOR_SIZE)
                gvGraph.getViewport().setMaxX(queue.size());
            else {
                gvGraph.getViewport().setMinX(0);
                gvGraph.getViewport().setMaxX(Constants.GRAPH_HOR_SIZE - 1);
            }
            dataSeriesX.appendData(new DataPoint(currentIndex, data.getX()), true, Constants.GRAPH_HOR_SIZE, false);
            dataSeriesY.appendData(new DataPoint(currentIndex, data.getY()), true, Constants.GRAPH_HOR_SIZE, false);
            dataSeriesZ.appendData(new DataPoint(currentIndex, data.getZ()), true, Constants.GRAPH_HOR_SIZE, false);
            handler.postDelayed(graphThread, Constants.DELAY);
            Log.d("State", "started");
//            }
        } else {
            handler.removeCallbacks(graphThread);
            DataPoint[] dataPoints = new DataPoint[1];
            dataPoints[0] = new DataPoint(0, 0);
            dataSeriesX.resetData(dataPoints);
            dataSeriesY.resetData(dataPoints);
            dataSeriesZ.resetData(dataPoints);
            Log.d("State", "stopped");
        }

        gvGraph.getGridLabelRenderer().reloadStyles();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerService != null) {
            accelerometerService.pauseSensor();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerService != null) {
            accelerometerService.resumeSensor();
        }
    }

    @Override
    public Object getSystemServiceCallback(String serviceName) {
        return this.getSystemService(serviceName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
