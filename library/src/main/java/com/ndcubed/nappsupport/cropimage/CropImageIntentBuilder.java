package com.ndcubed.nappsupport.cropimage;

import android.app.Activity;
import android.content.Intent;

public class CropImageIntentBuilder {

    Intent intent;

    public CropImageIntentBuilder(Activity activity, float aspectX, float aspectY, String sourceImagePath, String saveImagePath) {

        intent = new Intent(activity, CropImage.class);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("sourceImage", sourceImagePath);
        intent.putExtra("saveImagePath", saveImagePath);
    }

    public void setCropOval(boolean b) {
        intent.putExtra("cropOval", b);
    }

    public void setMaxSize(int width, int height) {
        intent.putExtra("maxWidth", width);
        intent.putExtra("maxHeight", height);
    }

    public Intent getIntent() {
        return intent;
    }
}
