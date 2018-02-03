package com.ndcubed.nappsupport.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ndcubed.nappsupport.R;

import java.util.ArrayList;

public class SwipeDownView extends LinearLayout {

    LinearLayout rootView;
    TextView dragHandleLabel;
    View dragHandle, fadeView;
    float shadowSize;

    boolean isShowing = false;
    boolean showShadow = true;

    LinearLayout contentContainer;
    View contentView;
    View whiteFadeView;

    ArrayList<OnAnimateFrameListener> listeners = new ArrayList<OnAnimateFrameListener>();

    public SwipeDownView(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public SwipeDownView(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public SwipeDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    private void init() {

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();

        rootView = (LinearLayout)inflater.inflate(R.layout.swipe_down_container_layout, null);
        rootView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oLeft, int oTop, int oRight, int oBottom) {

                int nLayout = left + top + right + bottom;
                int oLayout = oLeft + oTop + oRight + oBottom;

                System.out.println("NEW HEIGHT: " + (bottom - top) + "   O: " + rootView.getHeight() + " dd: " + dragHandle.getHeight());

                if(nLayout != oLayout) {
                    if(!isShowing) {
                        rootView.setTranslationY(-(rootView.getHeight() - (dragHandle.getHeight() + shadowSize)));
                    } else {
                        rootView.setTranslationY(-shadowSize);
                    }
                }
            }
        });

        whiteFadeView = rootView.findViewById(R.id.whiteFadeView);
        whiteFadeView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        dragHandle = rootView.findViewById(R.id.dragHandle);
        dragHandleLabel = (TextView)rootView.findViewById(R.id.dragHandleLabel);

        dragHandle.setOnTouchListener(new DragHandleListener());

        contentContainer = (LinearLayout)rootView.findViewById(R.id.contentView);

        View content = createContentView(inflater);
        if(content != null) {
            contentView = content;
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            contentContainer.addView(content, params);
        }

        shadowSize = showShadow ? dpToPx(3) : 0f;
        rootView.findViewById(R.id.shadow).setVisibility(showShadow ? View.VISIBLE : View.GONE);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(rootView, params);
    }

    //Override...
    public View createContentView(LayoutInflater inflater) {
        return null;
    }

    public View getDragHandle() {
        return dragHandle;
    }

    public void setContentView(View contentView) {

        this.contentView = contentView;
        contentContainer.removeAllViews();

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentContainer.addView(contentView, params);
    }

    public View getContentView() {
        return contentView;
    }

    public void setShadowVisible(boolean b) {
        showShadow = b;
        shadowSize = b ? dpToPx(3) : 0f;

        if(rootView != null) {
            if(!b) {
                rootView.findViewById(R.id.shadow).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.shadow).setVisibility(View.VISIBLE);
            }
        }
    }

    public void addOnAnimateFrameListener(OnAnimateFrameListener l) {
        listeners.add(l);
    }

    public void removeOnAnimateFrameListener(OnAnimateFrameListener l) {
        listeners.remove(l);
    }

    private void fireOnAnimateFrameEvent() {

        float visible = (rootView.getHeight() + rootView.getTranslationY()) - (dragHandle.getHeight() + shadowSize);
        float percent = visible / (rootView.getHeight() - (dragHandle.getHeight() + shadowSize));

        for(OnAnimateFrameListener l : listeners) {
            l.onFrame(visible, percent);
        }
    }

    public void setText(String text) {
        dragHandleLabel.setText(text);
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    private void refreshPosition() {

        System.out.println("REFERSH: " + shadowSize);

        if(!isShowing) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), -(rootView.getHeight() - (dragHandle.getHeight() + shadowSize)));
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                    float alpha = 1f - percent;

                    if(fadeView != null) {
                        fadeView.setAlpha(alpha);
                    }

                    fireOnAnimateFrameEvent();
                    //whiteFadeView.setAlpha(percent);
                }
            });

            anim.setInterpolator(new DecelerateInterpolator(4f));
            anim.setDuration(500);
            anim.start();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), -shadowSize);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                    float alpha = 1f - percent;

                    if(fadeView != null) {
                        fadeView.setAlpha(alpha);
                    }

                    //whiteFadeView.setAlpha(percent);
                }
            });
            anim.setInterpolator(new DecelerateInterpolator(4f));
            anim.setDuration(500);
            anim.start();
        }
    }

    public void hide() {
        isShowing = false;

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), -(rootView.getHeight() - (dragHandle.getHeight() + shadowSize)));

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                float alpha = 1f - percent;

                if(fadeView != null) {
                    fadeView.setAlpha(alpha);
                }

                fireOnAnimateFrameEvent();
                //whiteFadeView.setAlpha(percent);
            }
        });

        anim.setInterpolator(new DecelerateInterpolator(4f));
        anim.setDuration(500);
        anim.start();
    }

    public void show() {
        isShowing = true;

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), -shadowSize);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                float alpha = 1f - percent;

                if(fadeView != null) {
                    fadeView.setAlpha(alpha);
                }

                fireOnAnimateFrameEvent();
                //whiteFadeView.setAlpha(percent);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator(4f));
        anim.setDuration(500);
        anim.start();
    }

    public void setFadeView(View view) {
        fadeView = view;
    }

    class DragHandleListener implements OnTouchListener {

        float pressY = 0f;
        float y = 0f;

        boolean didDrag = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                didDrag = false;
                rootView.clearAnimation();
                pressY = motionEvent.getRawY();
                y = pressY;
                return true;
            } else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                float delta = motionEvent.getRawY() - y;
                y = motionEvent.getRawY();

                float translation = rootView.getTranslationY() + delta;
                if(translation > -shadowSize) {
                    translation = -shadowSize;
                }
                rootView.setTranslationY(translation);

                float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                float alpha = 1f - percent;

                if(fadeView != null) {
                    fadeView.setAlpha(alpha);
                }

                if(!didDrag) {
                    didDrag = (delta >= 5);
                }

                fireOnAnimateFrameEvent();
            } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                if(didDrag) {
                    if(!isShowing) {
                        if(rootView.getTranslationY() < -(rootView.getHeight() - (dragHandle.getHeight() + dpToPx(50)))) {
                            hide();
                        } else {
                            show();
                        }
                    } else {
                        if(rootView.getTranslationY() > dpToPx(100f)) {
                            hide();
                        } else {
                            show();
                        }
                    }
                } else {
                    if(isShowing) {
                        hide();
                    } else {
                        show();
                    }
                }
                return true;
            }

            return false;
        }
    }

    public interface OnAnimateFrameListener {
        public void onFrame(float visibleHeight, float percent);
    }
}
