package com.cse535.assignments.group6;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import java.util.Deque;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements GraphUpdateCallback, SensorManagerCallback {

    public static SensorManagerCallback sensorManagerCallback;
    private final Handler handler = new Handler();
    boolean allPermissionStatus, internetPermissionStatus, readStoragePermissionStatus, writeStoragePermissionStatus;
    private GraphView gvGraph;
    private LineGraphSeries<DataPoint> dataSeriesX, dataSeriesY, dataSeriesZ;
    private Runnable graphThread;
    private Deque queue;
    private Button uploadButton, downloadButton, runButton, stopButton;
    private AccelerometerBroadcastReceiver receiver;
    private int currentIndex = 0;
    private boolean serviceInvoked = false;
    private DatabaseHelperClass dbHelper;

    //By Amit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.PERMISSION_WRITE_EXTERNAL_STORAGE)
                && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Constants.PERMISSION_READ_EXTERNAL_STORAGE)
                && checkPermission(Manifest.permission.INTERNET, Constants.PERMISSION_INTERNET)) {
            performStorageInitializations();
            enableUiControls();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_WRITE_EXTERNAL_STORAGE: {
                writeStoragePermissionStatus = HelperClass.evaluatePermissionRequestResponse(grantResults);
            }
            case Constants.PERMISSION_READ_EXTERNAL_STORAGE: {
                readStoragePermissionStatus = HelperClass.evaluatePermissionRequestResponse(grantResults);
            }
            case Constants.PERMISSION_INTERNET: {
                internetPermissionStatus = HelperClass.evaluatePermissionRequestResponse(grantResults);
            }
            case Constants.PERMISSION_ALL: {
                allPermissionStatus = HelperClass.evaluatePermissionRequestResponse(grantResults);
            }
        }
        if (readStoragePermissionStatus && writeStoragePermissionStatus) {
            performStorageInitializations();
            enableUiControls();
        }
    }

    public boolean checkPermission(String permissionString, int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, permissionString) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permissionString}, requestCode);
            return false;
        }
        return true;
    }

    private void performStorageInitializations() {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            if (sdcard == null)
                displayAlert("SD card not detected.");
            else {
                dbHelper = new DatabaseHelperClass();
                dbHelper.initializeDatabase(Constants.DATABASE_PATH_FROM_ROOT);
            }
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage());
            displayAlert("Issue creating database because of Permissions, API level and/or SD card availability.\r\nTry restarting the app.");
        }
    }

    private void setButtonState(Button button, boolean state) {
        button.setEnabled(state);
        button.setAlpha(state ? 1.0f : 0.45f);
    }

    private void enableUiControls() {
        setButtonState(runButton, true);
        setButtonState(stopButton, true);
        setButtonState(downloadButton, true);
    }

    private void displayAlert(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Exit");
        builder.setMessage(message + "\r\nThe app will now exit.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initialize() {
        queue = new LinkedList<AccelerometerData>();
        gvGraph = findViewById(R.id.graph);

        uploadButton = findViewById(R.id.buttonUploadDb);
        downloadButton = findViewById(R.id.buttonDownloadDb);
        runButton = findViewById(R.id.buttonRun);
        stopButton = findViewById(R.id.buttonStop);

        setButtonState(runButton, false);
        setButtonState(stopButton, false);
        setButtonState(uploadButton, false);
        setButtonState(downloadButton, false);

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
        gvGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        gvGraph.getLegendRenderer().setPadding(10);
        gvGraph.getLegendRenderer().setTextSize(25);
        gvGraph.getLegendRenderer().setSpacing(5);

        // set viewport properties
        gvGraph.getViewport().setXAxisBoundsManual(true);
        gvGraph.getViewport().setYAxisBoundsManual(true);
        gvGraph.getViewport().setMinX(0);
        gvGraph.getViewport().setMaxX(Constants.GRAPH_HOR_SIZE - 1);
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
        }.execute(dbHelper.getDbPath());
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
                        try {
                            DatabaseHelperClass downloadDbHelper = new DatabaseHelperClass();
                            downloadDbHelper.initializeDatabase(Constants.DATABASE_DOWNLOAD_PATH_FROM_ROOT);
                            Deque<AccelerometerData> accDataList = downloadDbHelper.getAccelerometerDataFromDb(info.toString(), Constants.GRAPH_HOR_SIZE);
                            if (accDataList != null) {
                                DataPoint[][] dp = HelperClass.resetGraphData(accDataList, Math.max(currentIndex, Constants.GRAPH_HOR_SIZE));
                                dataSeriesX.resetData(dp[0]);
                                dataSeriesY.resetData(dp[1]);
                                dataSeriesZ.resetData(dp[2]);
                                gvGraph.getGridLabelRenderer().reloadStyles();
                            } else
                                Toast.makeText(getApplicationContext(),
                                        "No records found for this patient. Please enter correct patient info.", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Log.e(this.getClass().getName(), ex.getMessage());
                        }
                    }
                }
            }.execute(targetDbFileUrl, downloadedDbFilePath);
        }
    }

    //By Rishabh
    public void onRun(View v) {
        String toastMsg = "RUNNING";
        PatientInfo info = verifyPatientInfo();
        if (info != null) {
            try {
                setButtonState(uploadButton, false);
                setButtonState(downloadButton, false);
                setButtonState(runButton, false);
                dbHelper.createPatientTable(info.toString());
                Toast.makeText(getApplicationContext(), dbHelper.getDbPath(), Toast.LENGTH_LONG).show();
                Intent startIntent = new Intent(MainActivity.this, AccelerometerService.class);
                startService(startIntent);
                serviceInvoked = true;
            } catch (Exception ex) {
                Log.e(this.getClass().getName(), ex.getMessage());
                toastMsg = "Run failed";
            } finally {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onStop(View v) {
        String toastMsg = "STOPPED";
        try {
            Intent stopIntent = new Intent(MainActivity.this, AccelerometerService.class);
            stopService(stopIntent);
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), ex.getMessage());
            toastMsg = "Error'ed stop";
        } finally {
            setButtonState(uploadButton, serviceInvoked);
            setButtonState(downloadButton, true);
            setButtonState(runButton, true);
            Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
        }
        controlGraph(false, null);
    }

    @Override
    public void controlGraph(boolean execution, AccelerometerData data) {

        if (execution) {
            if (queue.size() == Constants.GRAPH_HOR_SIZE)
                queue.poll();
            queue.addLast(data);
            currentIndex++;
            dbHelper.addAccelerometerDataToDb(data);

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
            gvGraph.getGridLabelRenderer().reloadStyles();
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
            gvGraph.getGridLabelRenderer().reloadStyles();
            Log.d("State", "stopped");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AccelerometerService.ServiceObject != null) {
            AccelerometerService.ServiceObject.pauseSensor();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccelerometerService.ServiceObject != null) {
            AccelerometerService.ServiceObject.resumeSensor();
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
