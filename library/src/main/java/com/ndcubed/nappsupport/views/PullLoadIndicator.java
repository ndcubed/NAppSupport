package com.ndcubed.nappsupport.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.ndcubed.nappsupport.R;

import java.util.ArrayList;

public class PullLoadIndicator extends FrameLayout {

    private float percent = 0f;
    private float translateY = 0f;
    private boolean didCancel = false;
    private VisibilityListener visibilityListener = null;
    private boolean didTrigger = false;

    Paint linePaint;
    int lineColor = Color.rgb(230, 120, 0);
    ValueAnimator animation;
    RecyclerView listView;

    ArrayList<OnRefreshListener> listeners = new ArrayList<OnRefreshListener>();
    ArrayList<OnPullListener> pullListeners = new ArrayList<OnPullListener>();

    View root, refreshArrow;

    public PullLoadIndicator(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public PullLoadIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public PullLoadIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public void init() {

        if(!isInEditMode()) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            root = inflater.inflate(R.layout.pull_load_indicator_layout, null);

            refreshArrow = root.findViewById(R.id.refresh_arrow);

            //setTextColor(Color.rgb(80,80,80));
            //setBackgroundColor(Color.rgb(235,235,235));
            //setPadding(0, 0, 0, (int) dpToPx(4));
            setAlpha(0f);

            linePaint = new Paint();
            linePaint.setColor(lineColor);
            linePaint.setStyle(Paint.Style.FILL);

            addView(root);
        }
    }

    public boolean isAttached() {
        return listView != null;
    }

    public void setVisibilityListener(VisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    public void setOnPullListener(OnPullListener l) {
        pullListeners.clear();
        pullListeners.add(l);
    }

    public void addOnPullListener(OnPullListener l) {
        pullListeners.add(l);
    }

    public void removeOnPullListener(OnPullListener l) {
        pullListeners.remove(l);
    }

    private void fireOnPullEvent(RecyclerView listView, float distance, float percent) {

        for(OnPullListener l : pullListeners) {
            l.onPull(listView, distance, percent);
        }
    }

    public void registerListView(RecyclerView listView) {
        unregisterListView();

        this.listView = listView;
        listView.setOnTouchListener(new PullLoadListener(listView));

        System.out.println("[reg]REGISTER: " + listView);
    }

    public void unregisterListView() {
        System.out.println("[reg]UNREGISTER: " + listView);

        if(listView != null) {
            listView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            listView = null;
        }
    }

    public void setLineColor(int color) {
        lineColor = color;
        invalidate();
    }

    public void fadeOut() {
        ValueAnimator animation = ValueAnimator.ofFloat(getAlpha(), 0f);
        animation.setDuration(300);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
               // setText("Pull to Refresh");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                if(visibilityListener != null) visibilityListener.onVisibilityChanged(false);
            }
        });

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float p = (Float) valueAnimator.getAnimatedValue();
                setAlpha(p);

                System.out.println("SETTING ALPHA");
            }
        });
        animation.start();
    }

    public void fadeIn() {
        if(visibilityListener != null) visibilityListener.onVisibilityChanged(true);
        setVisibility(View.VISIBLE);

        setAlpha(0f);
        ValueAnimator animation = ValueAnimator.ofFloat(getAlpha(), 1f);
        animation.setDuration(300);

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float p = (Float) valueAnimator.getAnimatedValue();
                setAlpha(p);
            }
        });
        animation.start();
    }

    public void release() {

        didCancel = false;
        animation = ValueAnimator.ofFloat(percent, 0f);
        animation.setInterpolator(new DecelerateInterpolator(5f));
        animation.setDuration(405);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                didCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //setLayerType(LAYER_TYPE_SOFTWARE, null);
                if(didTrigger) {
                    fireOnRefreshEvent();
                }
            }
        });
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(!didCancel) {
                    percent = (Float) valueAnimator.getAnimatedValue();
                    fireOnPullEvent(listView, 0f, percent);

                    translateY = dpToPx(100f) * percent;
                    float rotation = Math.min(180f * percent, 180f);
                    refreshArrow.setRotation(rotation);
                    refreshArrow.setTranslationY(translateY);

                    invalidate();
                }
            }
        });
        animation.start();
    }

    public void clear() {
        if(animation != null) animation.cancel();
        clearAnimation();
    }

    public void setPercent(float p) {
        if(animation != null) animation.cancel();
        clearAnimation();

        translateY = dpToPx(150f) * p;

        float rotation = Math.min(180f * p, 180f);
        refreshArrow.setRotation(rotation);
        refreshArrow.setTranslationY(translateY);
        //refreshArrowBlue.setRotation(rotation);


        percent = p;
        invalidate();
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float lineWidth = (float)getWidth() * percent;
        float startX = getWidth() / 2 - lineWidth / 2;

        //Paint paint = new Paint();
        //paint.setColor(Color.rgb(230, 230, 230));
        //canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

       // linePaint.setColor(percent >= 1f ? Color.rgb(27,180,255) : lineColor);
        //canvas.drawRect(startX, getHeight() - dpToPx(1), startX + lineWidth, getHeight(), linePaint);
    }

    public class PullLoadListener implements OnTouchListener {

        float pressY = 0f;
        float distance = 0f;
        boolean wasAtTop = false;
        boolean atTop = false;
        boolean released = false;

        RecyclerView listView;

        public PullLoadListener(RecyclerView listView) {
            this.listView = listView;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //setLayerType(LAYER_TYPE_HARDWARE, null);

                clear();
                pressY = motionEvent.getY();
                distance = 0f;

            } else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                if(!didTrigger && !released) {
                    distance = motionEvent.getY() - pressY;

                    /* CHECK IF LIST AT TOP */
                    if(distance > dpToPx(15)) {
                        if(!atTop) {
                            if(listView.getChildCount() == 0) {
                                atTop = true;
                            } else if(listView.getChildAt(0).getTop() - listView.getPaddingTop() == 0 && listView.getChildAdapterPosition(listView.getChildAt(0)) == 0) {
                                atTop = true;
                            } else {
                                atTop = false;
                            }
                        } else {
                            if(listView.getChildCount() == 0) {
                                atTop = true;
                            } else if(listView.getChildAt(0).getTop() - listView.getPaddingTop() > -dpToPx(5f) && listView.getChildAdapterPosition(listView.getChildAt(0)) == 0) {
                                atTop = true;
                                System.out.println("POSITION: " + (listView.getChildAt(0).getTop() - listView.getPaddingTop()));
                            } else {
                                atTop = false;
                            }
                        }

                        if(atTop && getVisibility() == View.GONE) {
                            setVisibility(View.VISIBLE);
                        }
                    }

                    if(atTop) {
                        wasAtTop = true;

                        float percent = (distance - dpToPx(15f)) / dpToPx(200);
                        float alphaPercent = (distance - dpToPx(15f)) / dpToPx(80f);
                        setAlpha(1f * Math.min(1f, alphaPercent));

                        System.out.println("ALPHA: " + getAlpha() + "   " + getVisibility());

                        if(percent >= 1f) {
                            released = true;
                            didTrigger = true;
                            release();
                            fadeOut();
                        } else {
                            setPercent(percent);
                            fireOnPullEvent(listView, distance, percent);
                        }
                    } else {
                        if(wasAtTop) {
                            pressY = motionEvent.getY();
                            release();
                            fadeOut();

                            System.out.println("FADE----------");
                        }
                        wasAtTop = false;
                    }
                }

                System.out.println("DISTANCE: " + distance + "  " + wasAtTop + "  " + atTop + -dpToPx(5) + " : " + (distance < -dpToPx(5) && wasAtTop));

            } else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                atTop = false;
                wasAtTop = false;
                released = false;
                if(!didTrigger) {
                    release();
                    fadeOut();
                }
            }

            return false;
        }
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        listeners.clear();
        listeners.add(l);
    }

    public void removeOnRefreshListener(OnRefreshListener l) {
        listeners.remove(l);
    }

    private void fireOnRefreshEvent() {
        didTrigger = false;
        for(OnRefreshListener l : listeners) {
            l.onRefresh();
        }
    }

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public interface OnPullListener {
        public void onPull(RecyclerView listView, float distance, float percent);
    }

    public interface VisibilityListener {
        public void onVisibilityChanged(boolean visible);
    }
}
