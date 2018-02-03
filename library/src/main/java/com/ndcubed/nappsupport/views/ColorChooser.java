package com.ndcubed.nappsupport.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ndcubed.nappsupport.R;
import com.ndcubed.nappsupport.utils.Common;

public class ColorChooser extends View {

    private Bitmap colorWheel;
    private ColorChooseListener listener;
    private int color = 0;
    private float eX = 0f;
    private float eY = 0f;

    float sampleX = 0f;
    float sampleY = 0f;

    boolean run = false;

    public ColorChooser(Context context) {
        super(context);
        init();
    }

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {

        setClickable(true);
        setWillNotDraw(false);

        colorWheel = BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {

                    float x = event.getX();
                    float y = event.getY();

                    eX = x;
                    eY = y;

                    x = Math.max(0, x);
                    y = Math.max(0, y);

                    float scaleX = (float)colorWheel.getWidth() / (float)getWidth();
                    float scaleY = (float)colorWheel.getHeight() / (float)getHeight();

                    x = x * scaleX;
                    y = y * scaleY;

                    sampleX = Math.min(x, colorWheel.getWidth() - 1);
                    sampleY = Math.min(y, colorWheel.getHeight() - 1);
                    color = colorWheel.getPixel((int)sampleX, (int)sampleY);


                    if(!run) {
                        run = true;
                        new ColorChooseThread().start();
                    }

                    fireOnColorChangeEvent(color);

                    invalidate();

                    if(event.getAction() == MotionEvent.ACTION_DOWN) fireInteractEvent(color);
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    run = false;
                    fireColorChooseEvent(eX, eY);
                    fireOnDoneEvent(color, eX, eY);
                }

                return true;
            }
        });
    }

    public void clearColor() {
        color = 0;
        invalidate();
    }

    public void setSamplePoint(float x, float y) {

        System.out.println("X: " + x +  " Y: " + y);

        eX = x;
        eY = y;

        x = Math.max(0, x);
        y = Math.max(0, y);

        float scaleX = (float)colorWheel.getWidth() / (float)getWidth();
        float scaleY = (float)colorWheel.getHeight() / (float)getHeight();

        x = x * scaleX;
        y = y * scaleY;

        sampleX = Math.min(x, colorWheel.getWidth() - 1);
        sampleY = Math.min(y, colorWheel.getHeight() - 1);
        color = colorWheel.getPixel((int)sampleX, (int)sampleY);

        invalidate();
    }

    private void fireInteractEvent(int color) {
        if(listener != null) {
            listener.onInteract(color);
        }
    }

    private void fireOnDoneEvent(int color, float x, float y) {
        if(listener != null) {
            listener.onDone(color, x, y);
        }
    }

    private void fireColorChooseEvent(float x, float y) {

        if(listener != null) {
            listener.onColorChosen(color);
        }
    }

    @Deprecated
    /**
     * Does not follow one second call limit. To be Removed...
     * Use OnColorChoose() instead.
     */
    private void fireOnColorChangeEvent(int color) {
        if(listener != null) {
            listener.onColorChange(color);
        }
    }

    public void setColorChooseListener(ColorChooseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(colorWheel, new Rect(0, 0, colorWheel.getWidth(), colorWheel.getHeight()), new Rect(0, 0, getWidth(), getHeight()), null);

        if(color != 0) {
            Paint p = new Paint();
            p.setColor(color);
            p.setStyle(Paint.Style.FILL);
            p.setAntiAlias(true);

            float circleWidth = Common.dpToPx(getContext(), 70f);
            float x = eX - (circleWidth / 2f);
            float y = eY - (circleWidth / 2f);

            canvas.drawOval(new RectF(x, y, x + circleWidth, y + circleWidth), p);
        }
    }

    public interface ColorChooseListener {
        void onColorChosen(int color);
        void onInteract(int color);
        void onDone(int color, float x, float y);

        /**
         * Does not follow one second call limit. To be Removed...
         * Use OnColorChosen(int color) instead.
         */
        @Deprecated
        void onColorChange(int color);
    }

    public class ColorChooseThread extends Thread {

        @Override
        public void run() {

            while(run) {
                fireColorChooseEvent(eX, eY);
                try {
                    Thread.sleep(1000);
                } catch(Exception err){}
            }
        }
    }
}
