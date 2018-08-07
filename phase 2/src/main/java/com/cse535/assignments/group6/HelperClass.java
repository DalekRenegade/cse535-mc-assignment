package com.cse535.assignments.group6;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.Deque;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HelperClass {


    public static String getApplicationResource(Context context, int id) {
        return context.getResources().getString(id);
    }

    // Basis idea defined in the function 'uploadFileOld'.
    // This implementation was taken from https://github.com/saurabhjagdhane/
    // The function has been modified a little to accommodate our requirement
    public static String uploadFile(String sourceFileUri) {
        String msg = "FAIL";
        String fileName = sourceFileUri;
        HttpURLConnection comm = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int serverResponseCode;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10240;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.e("UploadFile", "Source File not Exist: " + sourceFileUri);
            msg = "FAIL";
        } else {
            try {
                FileInputStream fis = new FileInputStream(sourceFile);
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                URL url = new URL(Constants.SERVER_URL + "UploadToServer.php");

                comm = (HttpURLConnection) url.openConnection();
                comm.setDoInput(true);
                comm.setDoOutput(true);
                comm.setUseCaches(false);
                comm.setRequestMethod("POST");
                comm.setRequestProperty("Connection", "Keep-Alive");
                comm.setRequestProperty("ENCTYPE", "multipart/form-data");
                comm.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                comm.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(comm.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fis.read(buffer, 0, bufferSize);

                while (bytesRead > 0)

                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fis.read(buffer, 0, bufferSize);

                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = comm.getResponseCode();
                String serverResponseMessage = comm.getResponseMessage();
                if (serverResponseCode == 200) {

                    BufferedReader httpResponseReader = new BufferedReader(new InputStreamReader(comm.getInputStream()));
                    String lineRead;
                    StringBuilder sb = new StringBuilder();
                    while ((lineRead = httpResponseReader.readLine()) != null)
                        sb.append(lineRead);
                    msg = sb.toString();
                }

                fis.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException e) {

                e.printStackTrace();

                Log.e("Upload file to server", "error: " + e.getMessage(), e);
            } catch (Exception e) {

                e.printStackTrace();

                Log.e("Upload file to server", "error: " + e.getMessage(), e);
            }
        }
        return msg;
    }

    public static String uploadFileOld(String dbFilePath) {

        String status = "FAILED";

        String boundary = "*****";//Long.toHexString(System.currentTimeMillis());
        final String CRLF = "\r\n";
        final String charset = "UTF-8";
        final String contentMimeType = "text";//"application/x-sqlite3";
        String fileName = "";

        FileInputStream dbFileInputStream = null;
        OutputStream requestOutputStream = null;
        BufferedWriter requestWriter = null;
        HttpURLConnection connection = null;

        try {
            File dbFile = new File(dbFilePath);
            fileName = dbFile.getName();
            String s = Constants.SERVER_URL + "UploadToServer.php";
            connection = (HttpURLConnection) new URL(s).openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", fileName);


            requestOutputStream = new DataOutputStream(connection.getOutputStream());
            requestWriter = new BufferedWriter(new OutputStreamWriter(requestOutputStream, charset));

            // URLEncoder.encode(dbFile.getName())

            requestWriter.append("--" + boundary).append(CRLF);
            requestWriter.append("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"").append(CRLF);
//            requestWriter.append("Content-Type: " + contentMimeType + "; charset=" + charset).append(CRLF);

            dbFileInputStream = new FileInputStream(dbFilePath);
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = dbFileInputStream.read(buffer)) != -1) {
                requestOutputStream.write(buffer, 0, bytesRead);
            }
            requestOutputStream.flush();

            requestWriter.append(CRLF).flush();
            requestWriter.append("--" + boundary + "--").append(CRLF).flush();


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
//            fileURL = "http://impact.asu.edu/CSE535Spring18Folder/Assignment2_Group10.db";
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

    public static boolean evaluatePermissionRequestResponse(int[] grantResults) {
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

}
