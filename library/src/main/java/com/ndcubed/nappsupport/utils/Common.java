package com.ndcubed.nappsupport.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Nathan on 7/20/2015.
 */
public class Common {

    public static final int PURPLE_VOTE_COLOR = Color.rgb(153, 0, 208);
    public static final int BLUE_VOTE_COLOR = Color.rgb(27, 180, 255);
    public static final int PINK_VOTE_COLOR = Color.rgb(235, 0, 101);
    public static final int YELLOW_VOTE_COLOR = Color.rgb(230, 188, 0);

    public static final int NATURAL_LIGHT_COLOR = Color.rgb(254, 189, 145);
    public static final int BLUE_COLOR = Color.rgb(27, 180, 255);
    public static final int ORANGE_COLOR = Color.rgb(244, 132, 45);
    public static final int GRAY_COLOR = Color.rgb(98, 98, 98);
    public static final int GREEN_COLOR = Color.rgb(150, 170, 57);
    public static final int FOREST_GREEN_COLOR = Color.rgb(129, 146, 49);
    public static final int BLUE_TITLE_COLOR = Color.rgb(63, 155, 224);

    public static void showSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public static void showSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static float dpToPx(Context context, float dp){

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static int getSoftButtonHeight(Activity context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            context.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
}
