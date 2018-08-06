package com.cse535.assignments.group6;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HelperClass {

    private static DatabaseHelperClass db;

    public static DatabaseHelperClass getDb() {
        return db;
    }

    public static void setDb(Context context, PatientInfo info) {
        if (db == null && context != null && info != null) {
            db = new DatabaseHelperClass(context, Constants.DATABASE_PATH_FROM_ROOT, Constants.DATABASE_NAME, info);
        }
    }

    public static String getApplicationResource(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static String uploadFile(String dbFilePath) {

        String status = "FAILED";

        String boundary = Long.toHexString(System.currentTimeMillis());
        final String CRLF = "\r\n";
        final String charset = "UTF-8";
        final String contentMimeType = "text";//"application/x-sqlite3";

        FileInputStream dbFileInputStream = null;
        OutputStream requestOutputStream = null;
        BufferedWriter requestWriter = null;
        HttpURLConnection connection = null;

        try {
            File dbFile = new File(dbFilePath);
            String s = Constants.SERVER_URL + "UploadToServer.php";
            connection = (HttpURLConnection) new URL(s).openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


            requestOutputStream = connection.getOutputStream();
            requestWriter = new BufferedWriter(new OutputStreamWriter(requestOutputStream, charset));

            requestWriter.append("--" + boundary).append(CRLF);
            requestWriter.append("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + URLEncoder.encode(dbFile.getName()) + "\"").append(CRLF);
//            requestWriter.append("Content-Type: " + contentMimeType + "; charset=" + charset).append(CRLF);

            dbFileInputStream = new FileInputStream(dbFile);
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = dbFileInputStream.read(buffer)) != -1) {
                requestOutputStream.write(buffer, 0, bytesRead);
            }
            requestOutputStream.flush();

            requestWriter.append(CRLF).flush();
            requestWriter.append("--" + boundary + "--").append(CRLF).flush();

            requestOutputStream.close();
            requestWriter.close();

            BufferedReader httpResponseReader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lineRead;
            StringBuilder sb = new StringBuilder();
            while ((lineRead = httpResponseReader.readLine()) != null)
                sb.append(lineRead);

            if (connection.getResponseCode() == 200)
                status = sb.toString();
        } catch (Exception ex) {
            Log.e(HelperClass.class.getSimpleName(), ex.getMessage());
        } finally {
            try {
                if (dbFileInputStream != null)
                    dbFileInputStream.close();
            } catch (Exception e) {
            }
            try {
                if (requestOutputStream != null)
                    requestOutputStream.close();
            } catch (Exception e) {
            }
            try {
                if (requestWriter != null)
                    requestWriter.close();
            } catch (Exception e) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return status;
    }

    public static boolean downloadFile(String fileURL, String destDir) {
        boolean downloaded = false;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            fileURL = "http://impact.asu.edu/CSE535Spring18Folder/Assignment2_Group10.db";
            URL url = new URL(fileURL);
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = connection.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
                }

                // opens input stream from the HTTP connection
                inputStream = connection.getInputStream();
                String saveFilePath = destDir + File.separator + URLDecoder.decode(fileName);

                File result = new File(saveFilePath);
                if (!result.getParentFile().exists())
                    result.getParentFile().mkdirs();

                // opens an output stream to save into file
                outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, bytesRead);


                downloaded = true;
            } else
                Log.i(HelperClass.class.getSimpleName(), "Target file not found on server...");
        } catch (Exception ex) {
            Log.e(HelperClass.class.getSimpleName(), ex.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
            }
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
        return downloaded;
    }

    //By Karthik
    public static DataPoint[] generateRandomData(List<DataPoint> dataPointList) {
        Random mRandom = new Random();
        int count = Constants.GRAPH_HOR_SIZE;
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double y = mRandom.nextDouble() * 50.0;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        for (int i = 0; i < count; i++) {
            dataPointList.add(values[i]);
        }
        return values;
    }

    //By Varun
    public static DataPoint[] regenerateRandomData(List<DataPoint> dataPointList) {

        DataPoint[] values = new DataPoint[dataPointList.size()];
        for (int i = 0; i < dataPointList.size(); i++) {
            values[i] = dataPointList.get(i);
        }
        return values;
    }

    public static DataPoint[][] resetGraphData(Deque queue, int currentIndex) {
        DataPoint[][] dataPoints = new DataPoint[3][Constants.GRAPH_HOR_SIZE > queue.size() ? queue.size() : Constants.GRAPH_HOR_SIZE];
        int i = 0;
        Iterator queueIterator = queue.iterator();
        while (queueIterator.hasNext()) {
            AccelerometerData data = (AccelerometerData) queueIterator.next();
            dataPoints[0][i] = new DataPoint(currentIndex - queue.size() + i, data.getX());
            dataPoints[1][i] = new DataPoint(currentIndex - queue.size() + i, data.getY());
            dataPoints[2][i] = new DataPoint(currentIndex - queue.size() + i, data.getZ());

            i++;
        }
        return dataPoints;
    }

    public static DataPoint[][] generateGraphDataPoint(Deque queue) {
        DataPoint[][] dataPoints = new DataPoint[3][];
        for (int k = 0; k < 3; k++)
            dataPoints[k] = new DataPoint[queue.size()];
        Iterator queueIterator = queue.iterator();
        int i = 0;
        while (queueIterator.hasNext()) {
            AccelerometerData data = (AccelerometerData) queueIterator.next();
            dataPoints[0][i] = new DataPoint(i, data.getX());
            dataPoints[1][i] = new DataPoint(i, data.getY());
            dataPoints[2][i] = new DataPoint(i, data.getZ());
            i++;
        }
        return dataPoints;
    }

    public void checkPermission(Activity activity) {
        Context context = activity.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 21) {
            String readPermStr = Manifest.permission.READ_EXTERNAL_STORAGE;
            String writePermStr = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            int readPerm = ActivityCompat.checkSelfPermission(context, readPermStr);
            int writePerm = ActivityCompat.checkSelfPermission(context, writePermStr);
            if (readPerm == PackageManager.PERMISSION_GRANTED) {
                //Do something
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{readPermStr}, 1024);
            }
            if (writePerm == PackageManager.PERMISSION_GRANTED) {
                //Do something
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{writePermStr}, 1024);
            }
        } else {
            Toast.makeText(context,
                    "Minimum SDK version required = 21 | Current = " + Build.VERSION.SDK_INT,
                    Toast.LENGTH_SHORT).show();
            Log.d(this.getClass().getName(),
                    "Minimum SDK version required = 21 | Current = " + Build.VERSION.SDK_INT);
        }
    }

}
