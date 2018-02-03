package com.ndcubed.nappsupport.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebImageUploader {

    private String filePath;
    private String postURL;
    private long userID = -1;

    private ArrayList<ImageLoadListener> listeners = new ArrayList<ImageLoadListener>();

    private Activity activity;

    public WebImageUploader(Activity activity, String postURL, String filePath) {
        this.activity = activity;
        this.postURL = postURL;
        this.filePath = filePath;
    }

    public void addImageLoadListener(ImageLoadListener l) {
        listeners.add(l);
    }

    public void removeImageLoadListener(ImageLoadListener l) {
        listeners.remove(l);
    }

    private void fireImageLoadEvent(final boolean success) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(ImageLoadListener l : listeners) {
                    l.onImageLoad(success);
                }
            }
        });
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;


                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                try {
                    // ------------------ CLIENT REQUEST

                    //Log.e(Tag, "Inside second Method");

                    FileInputStream fileInputStream = new FileInputStream(new File(filePath));

                    // open a URL connection to the Servlet

                    URL url = new URL(postURL);

                    // Open a HTTP connection to the URL

                    conn = (HttpURLConnection) url.openConnection();

                    // Allow Inputs
                    conn.setDoInput(true);

                    // Allow Outputs
                    conn.setDoOutput(true);

                    // Don't use a cached copy.
                    conn.setUseCaches(false);

                    // Use a post method.
                    conn.setRequestMethod("POST");

                    conn.setRequestProperty("Connection", "Keep-Alive");

                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: post-data; name=uploadedfile;filename=" + filePath + "" + lineEnd);
                    dos.writeBytes(lineEnd);

                    //Log.e(Tag, "Headers are written");

                    // create a buffer of maximum size

                    int bytesAvailable = fileInputStream.available();
                    int maxBufferSize = 1000;
                    // int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bytesAvailable];

                    // read file and write it into form...

                    int bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bytesAvailable);
                        bytesAvailable = fileInputStream.available();
                        bytesAvailable = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);
                    }

                    // send multipart form data necesssary after file data...

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // close streams
                    //Log.e(Tag, "File is written");
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {
                    //Log.e(Tag, "error: " + ex.getMessage(), ex);
                }

                catch (IOException ioe) {
                   // Log.e(Tag, "error: " + ioe.getMessage(), ioe);
                }

                try {
                    if(conn != null) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line = in.readLine();

                        if(line != null) {
                            fireImageLoadEvent(line.equals("true"));
                        } else {
                            fireImageLoadEvent(true);
                        }

                        in.close();
                    } else {
                        fireImageLoadEvent(false);
                    }
                } catch (IOException ioex) {
                    fireImageLoadEvent(false);
                }
            }
        }).start();
    }

    public interface ImageLoadListener {
        public void onImageLoad(boolean success);
    }
}
