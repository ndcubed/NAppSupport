package com.ndcubed.nappsupport.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ndcubed.nappsupport.utils.Common;


public class RoundedSwitch extends View {

    private RoundedSwitchListener listener;

    public static final int STATE_ON = 1;
    public static final int STATE_OFF = 0;

    private int state = STATE_OFF;
    private ValueAnimator animation, iconAnimator;

    Paint paint = new Paint();
    RectF bounds = new RectF(0, 0, 0, 0);
    float handleX = dpToPx(3f);
    float onIconAlpha = 0f;
    float offIconAlpha = 1f;

    boolean dragging = false;

    public RoundedSwitch(Context context) {
        super(context);
        init();
    }

    public RoundedSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundedSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public void setSwitchState(int state) {
        this.state = state;
        fireOnStateChangeEvent();
        startSwitchAnimation();
    }

    public void setSwitchStateWithoutAnimation(int state) {
        this.state = state;

        if(state == STATE_OFF) {
            handleX = dpToPx(3f);
            onIconAlpha = 0f;
        } else {
            float radius = ((float) getHeight()) - dpToPx(6f);
            handleX = (getWidth() - (radius + dpToPx(3f)));
            onIconAlpha = 1f;
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("HEIGHT SPEC: " + heightMeasureSpec);

        if(getContext() != null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int)Common.dpToPx(getContext(), 26f));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float radius = ((float) getHeight()) - dpToPx(6f);
        float width = right - left;

        handleX = state == STATE_ON ? (width - (radius + dpToPx(3f))) : dpToPx(3f);
        onIconAlpha = state == STATE_ON ? 1f : 0f;

        invalidate();
    }

    private void startSwitchAnimation() {
        if(isLaidOut()) {
            if(animation != null) animation.cancel();
            if(iconAnimator != null) iconAnimator.cancel();

            float radius = ((float) getHeight()) - dpToPx(6f);
            float dX = state == STATE_ON ? (getWidth() - (radius + dpToPx(3f))) : dpToPx(3f);

            animation = ValueAnimator.ofFloat(handleX, dX);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(200);

            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    handleX = (Float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });


            if(state == STATE_ON) {
                iconAnimator = ValueAnimator.ofFloat(onIconAlpha, 1f);
                iconAnimator.setDuration(150);
                iconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        onIconAlpha = (Float)valueAnimator.getAnimatedValue();
                        offIconAlpha = 1f - onIconAlpha;
                        invalidate();
                    }
                });
            } else {
                iconAnimator = ValueAnimator.ofFloat(offIconAlpha, 1f);
                iconAnimator.setDuration(150);
                iconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        offIconAlpha = (Float)valueAnimator.getAnimatedValue();
                        onIconAlpha = 1f - offIconAlpha;
                        invalidate();
                    }
                });
            }
            iconAnimator.start();
            animation.start();
        }
    }

    private void init() {

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    fireOnPressEvent();

                    RectF handle = getHandleBounds();
                    dragging = handle.contains(motionEvent.getX(), motionEvent.getY());
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    /*
                    if (dragging) {
                        float radius = ((float) getHeight()) - dpToPx(6f);
                        float x = motionEvent.getX() - (radius / 2);

                        if(x > dpToPx(3f) && x < (getWidth() - (radius + dpToPx(3f)))) {
                            handleX = x;
                        } else if(x < dpToPx(3f)) {
                            handleX = dpToPx(3f);
                        } else if(x > (getWidth() - (radius + dpToPx(3f)))) {
                            handleX = (getWidth() - (radius + dpToPx(3f)));
                        }
                    }
                     */
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(state == STATE_ON) {
                        setSwitchState(STATE_OFF);
                    } else {
                        setSwitchState(STATE_ON);
                    }
                    fireOnReleaseEvent();
                }

                invalidate();
                return true;
            }
        });

    }

    public void setRoundedSwitchListener(RoundedSwitchListener listener) {
        this.listener = listener;
    }

    private void fireOnStateChangeEvent() {
        if(listener != null) {
            listener.onStateChange(state);
        }
    }

    private void fireOnPressEvent() {
        if(listener != null) {
            listener.onPress();
        }
    }

    private void fireOnReleaseEvent() {
        if(listener != null) {
            listener.onRelease();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private RectF getHandleBounds() {

        float radius = ((float)getHeight()) - dpToPx(6f);
        float y = (getHeight() / 2) - (radius / 2);
        float x = handleX;

        return new RectF(x, y, x + radius, y + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setAntiAlias(true);

        //draw back
        paint.setColor(Color.argb(45, 0, 0, 0));
        bounds.left = 0;
        bounds.top = 0;
        bounds.right = getWidth();
        bounds.bottom = getHeight();
        canvas.drawRoundRect(bounds, dpToPx(13f), dpToPx(13f), paint);

        //draw handle
        float radius = ((float)getHeight()) - dpToPx(6f);
        bounds.left = handleX;
        bounds.top = (getHeight() / 2) - (radius / 2);
        bounds.right = bounds.left + radius;
        bounds.bottom = bounds.top + radius;

        paint.setColor(Color.argb(230, 255, 255, 255));
        canvas.drawOval(bounds, paint);

        //draw status icons
        float x = getWidth() / 2.9f;
        paint.setColor(Color.argb((int)(255f * onIconAlpha), 255, 255, 255));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dpToPx(3f));
        canvas.drawLine(x, dpToPx(6f), x, getHeight() - dpToPx(6f), paint);
    }

    public interface RoundedSwitchListener {
        void onPress();
        void onRelease();
        void onStateChange(int switchState);
    }
}
