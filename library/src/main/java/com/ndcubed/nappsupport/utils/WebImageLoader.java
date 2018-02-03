package com.ndcubed.nappsupport.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class WebImageLoader {

    private ArrayList<ImageLoadListener> listeners = new ArrayList<ImageLoadListener>();
    private String imageURL;
    private String cacheDir = "";
    private boolean cacheImages = false;
    private int width = 0;
    private int height = 0;

    public WebImageLoader(String imageURL) {
        this.imageURL = imageURL;
    }

    public void addImageLoadListener(ImageLoadListener l) {
        listeners.add(l);
    }

    public void removeImageLoadListener(ImageLoadListener l) {
        listeners.remove(l);
    }

    private void fireImageLoadEvent(Bitmap bitmap) {

        for(ImageLoadListener l : listeners) {
            l.onImageLoad(bitmap);
        }
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        cacheImages = true;
    }

    public void setCache(String cacheDir) {
        this.cacheDir = cacheDir;
        cacheImages = true;
    }

    public void setCacheImages(boolean b) {
        cacheImages = b;
    }

    public void setSampleSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isImageCached() {
        File file = new File(cacheDir + "/" + getFileName(imageURL));
        return file.exists();
    }

    public String getFileName(String url) {
        if(url.contains("/")) {
            return url.substring(url.lastIndexOf("/", url.length()));
        } else {
            return url;
        }
    }

    public Bitmap startOnMainThread() {
        System.out.println("START LOAD BITMAP: " + imageURL);


        if(!isImageCached()) {
            System.out.println("NOT CACHED");
            try {
                URL url = new URL(imageURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);

                System.out.println(options.outHeight + "  " + options.outWidth);

                if(width != 0 && height != 0)options.inSampleSize = calculateInSampleSize(options, width, height);
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inScaled = false;
                options.inJustDecodeBounds = false;

                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                input.close();
                input = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                if(cacheImages) cache(bitmap);

                input.close();
                connection.disconnect();

                System.out.println("DONE LOAD BITMAP");

                return bitmap;
            } catch(Exception err) {
                System.out.println("ERRRRR");
                err.printStackTrace();
                return null;
            }
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(cacheDir + "/" + getFileName(imageURL), options);

            System.out.println(options.outHeight + "  " + options.outWidth);

            if(width != 0 && height != 0)options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inScaled = false;
            options.inJustDecodeBounds = false;

            Bitmap b = BitmapFactory.decodeFile(cacheDir + "/" + getFileName(imageURL), options);
            System.out.println("IS CACHED: " + b);
            return b;
        }
    }

    public void start() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!isImageCached()) {
                    try {
                        URL url = new URL(imageURL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();

                        InputStream input = connection.getInputStream();

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(input, null, options);

                        System.out.println(options.outHeight + "  " + options.outWidth);

                        if(width != 0 && height != 0)options.inSampleSize = calculateInSampleSize(options, width, height);
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inScaled = false;
                        options.inJustDecodeBounds = false;

                        connection.disconnect();
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();

                        input.close();
                        input = connection.getInputStream();

                        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                        if(cacheImages) cache(bitmap);

                        input.close();
                        connection.disconnect();

                        fireImageLoadEvent(bitmap);
                    } catch(Exception err) {
                        err.printStackTrace();
                    }
                } else {
                    System.out.println("IS CACHED");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(cacheDir + "/" + getFileName(imageURL), options);

                    System.out.println(options.outHeight + "  " + options.outWidth);

                    if(width != 0 && height != 0)options.inSampleSize = calculateInSampleSize(options, width, height);
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inScaled = false;
                    options.inJustDecodeBounds = false;

                    Bitmap b = BitmapFactory.decodeFile(cacheDir + "/" + getFileName(imageURL), options);
                    fireImageLoadEvent(b);
                    System.out.println("DONE");
                }
            }
        }).start();
    }

    private void cache(Bitmap bitmap) {
        boolean didTrimCache = true;

        File[] fileList = new File(cacheDir).listFiles();
        if(fileList != null) {
            Arrays.sort(fileList);

            if(fileList.length > 50) {
                didTrimCache = fileList[0].delete();
            }
        }

        if(didTrimCache) {
            try {
                String cacheFile = cacheDir + "/" + getFileName(imageURL);
                System.out.println(new File(cacheDir).exists() + "CAHCE EXISTS");
                FileOutputStream out = new FileOutputStream(cacheFile);
                bitmap.compress(imageURL.endsWith(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                System.out.println("DONE CACHE: " + cacheFile);
            } catch(Exception err) {
                err.printStackTrace();
            }
        } else {
            System.out.println("FAILED TO TRIM" + fileList[0]);
        }
    }

    public interface ImageLoadListener {
        public void onImageLoad(Bitmap bitmap);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            System.out.println("IN SAMPLE SIZE: " + inSampleSize);
        }

        return inSampleSize;
    }
}
