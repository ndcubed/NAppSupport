package com.ndcubed.nappsupport.cropimage;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;

import com.ndcubed.nappsupport.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class CropImage extends Activity {

    private Uri imageUri;
    private String imageFile = "";
    private CropImageView cropImageView;

    private String saveImagePath = "";
    private float aspectX, aspectY;
    boolean cropOval = false;

    int maxWidth = 500;
    int maxHeight = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_layout);

        cropImageView = (CropImageView)findViewById(R.id.cropImageView);

        SimpleColorButton doneButton = (SimpleColorButton)findViewById(R.id.doneButton);
        doneButton.setButtonColor(Color.rgb(2, 146, 190));
        doneButton.addSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {
                cropImageView.saveImage(saveImagePath);

                Intent intent = getIntent();
                intent.putExtra("saveImagePath", saveImagePath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null) {
            try {
                saveImagePath = extras.getString("saveImagePath", "");
                cropOval = extras.getBoolean("cropOval", false);
                imageFile = extras.getString("sourceImage");
                imageUri = Uri.parse(imageFile);

                System.out.println("IMAGE PATH: " + imageUri.getPath());
                ExifInterface exifInterface = new ExifInterface(getRealPathFromURI_API19(this, imageUri));
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                System.out.println("ORIENTATION:" + orientation);

                cropImageView.setCropOval(cropOval);

                aspectX = extras.getFloat("aspectX", 1f);
                aspectY = extras.getFloat("aspectY", 1f);
                maxHeight = extras.getInt("maxHeight", 500);
                maxWidth = extras.getInt("maxWidth", 500);

                ContentResolver cr = getContentResolver();
                InputStream in = cr.openInputStream(imageUri);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);

                System.out.println(options.outHeight + "  " + options.outWidth);

                in.close();
                in = cr.openInputStream(imageUri);
                options.inSampleSize = Common.calculateInSampleSize(options, 500, 500);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
                cropImageView.setImage(bitmap, new RectF(0, 0, aspectX, aspectY));
                cropImageView.setMaxSize(maxWidth, maxHeight);

                System.out.println("CROP VIEW: " + bitmap.getWidth() + "   " + bitmap.getHeight());
            } catch(Exception err) {
                System.out.println("ERRORRR");
                err.printStackTrace();
            }
        }
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cropImageView != null) {
            cropImageView.recycle();
        }
    }
}
