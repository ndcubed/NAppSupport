package com.ndcubed.nappsupport.cropimage;

import android.graphics.BitmapFactory;
import android.graphics.Color;

public class Common {

    public static final int BLUE_COLOR = Color.rgb(27, 180, 255);
    public static final int ORANGE_COLOR = Color.rgb(244, 132, 45);
    public static final int GRAY_COLOR = Color.rgb(98, 98, 98);
    public static final int GREEN_COLOR = Color.rgb(150, 170, 57);
    public static final int FOREST_GREEN_COLOR = Color.rgb(129, 146, 49);
    public static final int BLUE_TITLE_COLOR = Color.rgb(63, 155, 224);

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
        }

        return inSampleSize;
    }
}
