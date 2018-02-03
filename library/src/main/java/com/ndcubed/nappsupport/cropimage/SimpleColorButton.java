package com.ndcubed.nappsupport.cropimage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class SimpleColorButton extends TextView {

    public static final int FILL_STYLE_SOLID = 0;
    public static final int FILL_STYLE_TRANSPARENT = 1;

    private int fillStyle = FILL_STYLE_SOLID;

    private int buttonColor = Common.ORANGE_COLOR;
    private int onPressTextColor = Color.rgb(255, 255, 255);
    private int textColor = Color.rgb(255, 255, 255);
    private boolean isPressed = false;
    private boolean enabled = true;
    private boolean didCancel = false;

    private Camera camera;
    private Matrix matrix;
    private float animationPercent = 0f;
    private Paint paint;
    private ValueAnimator animation;

    private float pressY = 0f;
    private float pressX = 0f;
    private boolean didBuild = false;

    ArrayList<SimpleButtonListener> listeners = new ArrayList<SimpleButtonListener>();

    public SimpleColorButton(Context context) {
        super(context);
        init();
    }

    public SimpleColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleColorButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        setLayerType(LAYER_TYPE_SOFTWARE, paint);

        setDrawingCacheEnabled(true);

        matrix = new Matrix();
        camera = new Camera();

        setGravity(Gravity.CENTER);
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Regular.ttf");
        setTypeface(font);
        setAllCaps(true);
        setTextSize(13);
        setTextColor(textColor);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                didBuild = false;
                setDrawingCacheEnabled(false);
                setDrawingCacheEnabled(true);
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(enabled) {
                    switch(motionEvent.getAction()) {

                        case MotionEvent.ACTION_CANCEL:
                            if(animation != null) animation.cancel();
                            isPressed = false;
                            didCancel = true;
                            animationPercent = 0f;

                            invalidate();
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isPressed = true;
                            didCancel = false;
                            pressY = motionEvent.getY();
                            pressX = motionEvent.getX();

                            startPressAnimation();

                            break;
                        case MotionEvent.ACTION_UP:
                            isPressed = false;
                            invalidate();

                            if(!animation.isRunning()) {
                                startReleaseAnimation();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            pressY = motionEvent.getY();
                            pressX = motionEvent.getX();
                            invalidate();
                            break;
                    }
                }
                return true;
            }
        });
    }

    public void setButtonText(String text) {
        setText(text);

        setDrawingCacheEnabled(false);
        setDrawingCacheEnabled(true);
        didBuild = false;

        invalidate();
    }

    private void startPressAnimation() {
        if(animation != null) animation.cancel();
        animation = ValueAnimator.ofFloat(animationPercent, 1f);
        animation.setDuration(100);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isPressed && !didCancel) {
                    startReleaseAnimation();
                }
            }
        });
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                animationPercent = (Float)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animation.start();
    }

    private void startReleaseAnimation() {
        if(animation != null) animation.cancel();
        animation = ValueAnimator.ofFloat(animationPercent, 0f);
        animation.setDuration(100);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fireButtonClick();
            }
        });
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                animationPercent = (Float)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animation.start();
    }

    public void setFillStyle(int fillStyle) {
        this.fillStyle = fillStyle;

        if(fillStyle == FILL_STYLE_SOLID) {
            setTextColor(Color.WHITE);
            setAllCaps(true);
            setTextSize(13f);
        } else {
            setTextColor(Common.ORANGE_COLOR);
            setAllCaps(false);
            setTextSize(15f);
        }

        invalidate();
    }

    public int getFillStyle() {
        return fillStyle;
    }

    public void setButtonEnabled(boolean b) {
        enabled = b;

        setTextColor(enabled ? textColor : Color.rgb(255, 255, 255));
        invalidate();
    }

    public void addSimpleButtonListener(SimpleButtonListener l) {
        listeners.add(l);
    }

    public void removeSimpleButtonListener(SimpleButtonListener l) {
        listeners.remove(l);
    }

    public void clearAllListeners() {
        listeners.clear();
    }

    public void fireButtonClick() {

        for(SimpleButtonListener l : listeners) {
            l.onClick(this);
        }
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public void setButtonColor(int color) {
        buttonColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(animationPercent != 0f) {

            float rotation = ((getHeight() / 2) - pressY) * animationPercent;
            float rotationY = ((getWidth() / 2) - pressX) * animationPercent;
            float z = 30f * animationPercent;

            if(rotation > 0) {
                rotation = Math.min(rotation, 35f);
            } else {
                rotation = Math.max(rotation, -35f);
            }

            if(rotationY > 0) {
                rotationY = Math.min(rotationY, 10f);
            } else {
                rotationY = Math.max(rotationY, -10f);
            }

            camera.save();
            camera.translate(0, 0, z);
            camera.rotate(rotation, 0, 0);
            camera.getMatrix(matrix);
            matrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
            matrix.postTranslate(getWidth() / 2, getHeight() / 2);
            //canvas.concat(matrix);
        }

        if(animationPercent == 0f) {

            if(fillStyle == FILL_STYLE_SOLID) {
                Paint paint = new Paint();
                paint.setColor(buttonColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            } else {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(210, 210, 210));
                canvas.drawRect(0, 0, getWidth(), 1, paint);
            }

            super.onDraw(canvas);
            if(!didBuild) {
                didBuild = true;
                buildDrawingCache();
            }
        } else {
            Bitmap cache = getDrawingCache();
            canvas.drawBitmap(cache, matrix, paint);
        }

        if(animationPercent != 0f) camera.restore();
    }

    public interface SimpleButtonListener {
        public void onClick(SimpleColorButton view);
    }
}
