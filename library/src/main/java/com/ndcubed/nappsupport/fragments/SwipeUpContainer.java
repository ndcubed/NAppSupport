package com.ndcubed.nappsupport.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ndcubed.nappsupport.R;

public abstract class SwipeUpContainer extends Fragment {

    LinearLayout rootView;
    TextView dragHandleLabel;
    View dragHandle, fadeView;
    float shadowSize;

    boolean isShowing = false;
    boolean showShadow = true;

    View contentView;
    View whiteFadeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (LinearLayout)inflater.inflate(R.layout.basic_slidable_view_layout, null);
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                refreshPosition();
            }
        });

        whiteFadeView = rootView.findViewById(R.id.whiteFadeView);
        whiteFadeView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        dragHandle = rootView.findViewById(R.id.dragHandle);
        dragHandleLabel = (TextView)rootView.findViewById(R.id.dragHandleLabel);

        dragHandle.setOnTouchListener(new DragHandleListener());

        View content = createContentView(inflater);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ((LinearLayout)rootView.findViewById(R.id.contentView)).addView(content, params);

        shadowSize = showShadow ? dpToPx(3) : 0f;
        rootView.findViewById(R.id.shadow).setVisibility(showShadow ? View.VISIBLE : View.GONE);

        return rootView;
    }

    public abstract View createContentView(LayoutInflater inflater);

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

    public void setText(String text) {
        dragHandleLabel.setText(text);
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    private void refreshPosition() {

        if(!isShowing) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), rootView.getHeight() - (dragHandle.getHeight() + shadowSize));

            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                    float alpha = 1f - percent;

                    if(fadeView != null) {
                        fadeView.setAlpha(alpha);
                    }

                    // whiteFadeView.setAlpha(percent);
                }
            });

            anim.setInterpolator(new DecelerateInterpolator(4f));
            anim.setDuration(500);
            anim.start();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), 0f);
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

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), rootView.getHeight() - (dragHandle.getHeight() + shadowSize));

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

    public void show() {
        isShowing = true;

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getTranslationY(), 0f);
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

    public void setFadeView(View view) {
        fadeView = view;
    }

    class DragHandleListener implements View.OnTouchListener {

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

                rootView.setTranslationY(rootView.getTranslationY() + delta);

                float percent = rootView.getTranslationY() / (float)(rootView.getHeight() - (dragHandle.getHeight() + shadowSize));
                float alpha = 1f - percent;

                if(fadeView != null) {
                    fadeView.setAlpha(alpha);
                }

                //whiteFadeView.setAlpha(percent);

                if(!didDrag) {
                    if(Math.abs(delta) > dpToPx(10)) {
                        didDrag = true;
                    }
                }
            } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                if(didDrag) {
                    if(!isShowing) {
                        if(rootView.getTranslationY() > rootView.getHeight() - dpToPx(100f)) {
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
}

