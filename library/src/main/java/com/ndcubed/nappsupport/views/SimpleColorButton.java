package com.ndcubed.nappsupport.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ndcubed.nappsupport.R;
import com.ndcubed.nappsupport.utils.Common;

import java.util.ArrayList;

public class SimpleColorButton extends TextView {

    public static final int FILL_STYLE_SOLID = 0;
    public static final int FILL_STYLE_TRANSPARENT = 1;

    private int fillStyle = FILL_STYLE_SOLID;

    private int buttonColor = Common.ORANGE_COLOR;
    private int disabledColor = Common.ORANGE_COLOR;
    private boolean disabledColorSet = false;
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
    private boolean paintForAnimation = false;
    private boolean isRounded = true;
    private boolean isHalfRounded = false;
    private boolean animate = true;

    ArrayList<SimpleButtonListener> listeners = new ArrayList<SimpleButtonListener>();

    public SimpleColorButton(Context context) {
        super(context);
        init();
    }

    public SimpleColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleColorButton);
        for (int i = 0; i < a.getIndexCount(); i++)
        {
            int attr = a.getIndex(i);

            if(attr == R.styleable.SimpleColorButton_buttonColor) {
                buttonColor = a.getColor(attr, Common.ORANGE_COLOR);
            } else if(attr == R.styleable.SimpleColorButton_disabledColor) {
                disabledColor = a.getColor(attr, Common.ORANGE_COLOR);
                disabledColorSet = true;
            } else if(attr == R.styleable.SimpleColorButton_textColor) {
                textColor = a.getColor(attr, Color.WHITE);
            } else if(attr == R.styleable.SimpleColorButton_isRoundedStyle) {
                isRounded = a.getBoolean(attr, true);
            } else if(attr == R.styleable.SimpleColorButton_isHalfRoundedStyle) {
                if(a.getBoolean(attr, false)) {
                    isHalfRounded = true;
                    isRounded = false;
                }
            } else if(attr == R.styleable.SimpleColorButton_isAnimated) {
                animate = a.getBoolean(attr, false);
            }

            System.out.println(attr + "ATTRI");
        }
        a.recycle();

        init();
    }

    public SimpleColorButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleColorButton);
        for (int i = 0; i < a.getIndexCount(); i++)
        {
            int attr = a.getIndex(i);
            if(attr == R.styleable.SimpleColorButton_buttonColor) {
                buttonColor = a.getColor(attr, Common.ORANGE_COLOR);
            } else if(attr == R.styleable.SimpleColorButton_disabledColor) {
                disabledColor = a.getColor(attr, Common.ORANGE_COLOR);
                disabledColorSet = true;
            } else if(attr == R.styleable.SimpleColorButton_textColor) {
                textColor = a.getColor(attr, Color.WHITE);
            } else if(attr == R.styleable.SimpleColorButton_isRoundedStyle) {
                isRounded = a.getBoolean(attr, true);
            } else if(attr == R.styleable.SimpleColorButton_isHalfRoundedStyle) {
                if(a.getBoolean(attr, false)) {
                    isHalfRounded = true;
                    isRounded = false;
                }
            }
        }
        a.recycle();

        init();
    }


    private void init() {

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        setDrawingCacheEnabled(true);
        setWillNotDraw(false);

        matrix = new Matrix();
        camera = new Camera();

        setGravity(Gravity.CENTER);
        //Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Regular.ttf");
        //setTypeface(font);
        //setAllCaps(true);
        setTextColor(textColor);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oLeft, int oTop, int oRight, int oBottom) {

                int newDim = left + top + right + bottom;
                int oldDim = oLeft + oTop + oRight + oBottom;

                if(newDim != oldDim) {
                    didBuild = false;
                    setDrawingCacheEnabled(false);
                    setDrawingCacheEnabled(true);

                    invalidate();
                }
            }
        });


        setOnTouchListener(new OnTouchListener() {

            float oldY, oldX;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(enabled && animate) {
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
                            oldX = pressX;
                            oldY = pressY;

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
                            pressX = motionEvent.getX();
                            pressY = motionEvent.getY();

                            float dX = Math.abs(pressX - oldX);
                            float dY = Math.abs(pressY - oldY);

                            if(dX > dpToPx(30f) && isPressed) {
                                isPressed = false;
                                startReleaseAnimation();
                            }

                            invalidate();

                            break;
                    }
                } else if(!animate && enabled) {

                    switch(motionEvent.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            isPressed = true;
                            break;
                        case MotionEvent.ACTION_UP:
                            isPressed = false;
                            fireButtonClick();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            isPressed = false;
                            break;
                    }
                    invalidate();
                }
                return true;
            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        if(visibility == View.VISIBLE) {
            setDrawingCacheEnabled(false);
            setDrawingCacheEnabled(true);
            didBuild = false;
        }

        super.setVisibility(visibility);
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

    public void setDisabledColor(int color) {
        disabledColorSet = true;
        disabledColor = color;
        invalidate();
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

    public void setSimpleButtonListener(SimpleButtonListener l) {
        listeners.clear();
        addSimpleButtonListener(l);
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

    public boolean hasListener() {
        return !listeners.isEmpty();
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
        setDrawingCacheEnabled(false);
        setDrawingCacheEnabled(true);
        didBuild = false;
        invalidate();
    }

    public int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public int test() {
        return 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(enabled) {
            if(animationPercent != 0f) {

                float rotation = ((getHeight() / 2) - pressY) * animationPercent;
                float z = 30f * animationPercent;

                if(rotation > 0) {
                    rotation = Math.min(rotation, 35f);
                } else {
                    rotation = Math.max(rotation, -35f);
                }

                camera.save();
                camera.translate(0, 0, z);
                camera.rotate(rotation, 0, 0);
                camera.getMatrix(matrix);
                matrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
                matrix.postTranslate(getWidth() / 2, getHeight() / 2);
            }

            if(animationPercent == 0f) {

                if(fillStyle == FILL_STYLE_SOLID) {
                    Paint paint = new Paint();

                    if(animate) {
                        paint.setColor(buttonColor);
                    } else {
                        if(isPressed) {
                            paint.setColor(manipulateColor(buttonColor, 0.7f));
                        } else {
                            paint.setColor(buttonColor);
                        }
                    }

                    paint.setStyle(Paint.Style.FILL);
                    if(isRounded) {
                        paint.setAntiAlias(true);
                        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), dpToPx(3f), dpToPx(3f), paint);
                    } else if(isHalfRounded) {
                        paint.setAntiAlias(true);
                        canvas.drawRect(new RectF(0, 0, getWidth(), getHeight() / 2f), paint);
                        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), dpToPx(3f), dpToPx(3f), paint);
                    } else {
                        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                    }
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
        } else {
            Paint paint = new Paint();
            paint.setColor(disabledColorSet ? disabledColor : buttonColor);
            paint.setStyle(Paint.Style.FILL);
            if(isRounded) {
                paint.setAntiAlias(true);
                canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), dpToPx(3f), dpToPx(3f), paint);
            } else if(isHalfRounded) {
                paint.setAntiAlias(true);
                canvas.drawRect(new RectF(0, 0, getWidth(), getHeight() / 2f), paint);
                canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), dpToPx(3f), dpToPx(3f), paint);
            } else {
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            }

            super.onDraw(canvas);
        }
    }

    public interface SimpleButtonListener {
        public void onClick(SimpleColorButton view);
    }
}
