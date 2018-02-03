package com.ndcubed.nappsupport.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;


public class DotPageIndicator extends View {

    private ViewPager pager;
    private int count = 0;
    private int selected = 0;

    private Paint paint = new Paint();
    private int color = Color.argb(60, 255, 255, 255);
    private int highlightColor = Color.argb(255, 255, 255, 255);

    public DotPageIndicator(Context context) {
        super(context);
        init();
    }

    public DotPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DotPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        paint.setAntiAlias(true);
        paint.setColor(color);
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
        invalidate();
    }

    public void setViewPager(ViewPager pager) {
        this.pager = pager;
        count = pager.getAdapter().getCount();

        pager.clearOnPageChangeListeners();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                selected = i;
                invalidate();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        invalidate();
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isInEditMode()) {
            paint.setAntiAlias(true);
            count = 5;
            selected = 2;
        }

        float radius = getHeight();
        float gap = dpToPx(10f);
        float totalWidth = (radius * (count)) + (gap * (count - 1));
        float x = (getWidth() / 2) - (totalWidth / 2);

        for(int i = 0; i < count; i++) {

            if(i == selected) {
                paint.setColor(highlightColor);
            } else {
                paint.setColor(color);
            }

            canvas.drawOval(new RectF(x, 0, x + radius, getHeight()), paint);
            x += (radius + dpToPx(10f));
        }
    }
}
