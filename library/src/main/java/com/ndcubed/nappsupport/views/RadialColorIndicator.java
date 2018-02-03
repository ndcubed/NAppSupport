package com.ndcubed.nappsupport.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ndcubed.nappsupport.utils.Common;

/**
 * Created by Nathan on 12/14/2016.
 */

public class RadialColorIndicator extends View {

    RadialGradient radialGradient;
    SweepGradient sweepGradient;
    Paint paint;

    PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    Bitmap bitmap;

    public RadialColorIndicator(Context context) {
        super(context);

        init();
    }

    public RadialColorIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadialColorIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        paint = new Paint();
        paint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, paint);

        /*
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                System.out.println("GO");

                int[] colors = {Color.argb(0, 0, 0, 0), Color.argb(80, 0, 0, 0)};
                float[] stops = {0.75f, 1f};
               // radialGradient = new RadialGradient(getMeasuredWidth() / 2, getMeasuredHeight()/2, getMeasuredWidth() / 2, colors, stops, Shader.TileMode.CLAMP);

                int[] gradientColors = {Color.CYAN, Color.BLUE, Color.MAGENTA, Color.CYAN};
                sweepGradient = new SweepGradient(getMeasuredWidth()/2, getMeasuredHeight()/2, gradientColors, null);
            }
        });
         */
    }

    public void update() {

        if(getMeasuredWidth() > 0) {
            int[] colors = {Color.argb(200, 0, 0, 0), Color.argb(0, 0, 0, 0)};
            float[] stops = {0.95f, 1f};
            radialGradient = new RadialGradient(getMeasuredWidth() / 2, getMeasuredHeight()/2, getMeasuredWidth() / 2, colors, stops, Shader.TileMode.CLAMP);

            int[] gradientColors = {Color.CYAN, Color.BLUE, Color.MAGENTA, Color.CYAN};
            sweepGradient = new SweepGradient(getMeasuredWidth()/2, getMeasuredHeight()/2, gradientColors, null);

            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(sweepGradient == null) {
            int[] colors = {Color.argb(200, 0, 0, 0), Color.argb(0, 0, 0, 0)};
            float[] stops = {0.95f, 1f};
            radialGradient = new RadialGradient(getMeasuredWidth() / 2, getMeasuredHeight()/2, getMeasuredWidth() / 2, colors, stops, Shader.TileMode.CLAMP);

            int[] gradientColors = {Color.CYAN, Color.BLUE, Color.MAGENTA, Color.CYAN};
            sweepGradient = new SweepGradient(getMeasuredWidth()/2, getMeasuredHeight()/2, gradientColors, null);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        System.out.println("HARDWARE: " + canvas.isHardwareAccelerated());

        float stroke = Common.dpToPx(getContext(), 4);
        float padding = Common.dpToPx(getContext(), 4f);

        if(sweepGradient != null) {
            /** DRAW GRAPHICS **/
            paint.setShader(radialGradient);
            canvas.drawOval(0, 0, getWidth(), getHeight(), paint);

            paint.setShader(sweepGradient);
            //paint.setXfermode(porterDuffXfermode);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            canvas.drawOval(padding, padding, getWidth() - padding, getHeight() - padding, paint);

           // paint.setXfermode(null);
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawOval(padding, padding, getWidth() - padding, getHeight() - padding, paint);

            /** ------------- **/
        }
    }
}
