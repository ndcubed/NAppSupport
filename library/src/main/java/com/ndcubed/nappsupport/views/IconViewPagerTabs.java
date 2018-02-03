package com.ndcubed.nappsupport.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ndcubed.nappsupport.utils.Common;
import com.ndcubed.nappsupport.views.RobotoTextView.TextViewThick;

import java.util.ArrayList;


public class IconViewPagerTabs extends ViewGroup {

    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    private int[] colors = {Color.argb(200, 153, 0, 208), Color.argb(200, 27, 180, 255), Color.argb(200, 235, 0, 101), Color.argb(200, 230, 188, 0)};

    int tabUnderlineColor = Common.ORANGE_COLOR;
    int tabLabelColor = Color.rgb(100, 100, 100);
    int iconSize = 35;
    float scrollX = 0f;
    float totalWidth = 0f;
    float tabUnderlineOffset = 0f;
    float tabUnderLineWidth = 200f;
    Paint tabUnderline = new Paint();

    float pagerWidth = 0f;

    ArrayList<TextViewThick> tabs = new ArrayList<TextViewThick>();
    int index = 0;

    ViewPager pager;
    PagerAdapter adapter;

    private ScrollListener scrollListener;

    public IconViewPagerTabs(Context context) {
        super(context);

        init();
    }

    public IconViewPagerTabs(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public IconViewPagerTabs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public void setScrollListener(ScrollListener l) {
        scrollListener = l;
    }

    public void setPercent(float p) {
        float t = (float)(getHeight()) * p;
        setTranslationY(-t);
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    protected void onLayout(boolean b, int i1, int i2, int i3, int i4) {

        float childWidth = (float)getWidth() / (float)getChildCount();
        int size = (int)dpToPx(getIconSize());

        int x = (int)((childWidth / 2) - (size / 2));
        int y = (getHeight() / 2) - (size / 2);

        for(int i = 0; i < getChildCount(); i++) {

            View view = getChildAt(i);

            view.layout(x, y, x + size, y + size);
            x += childWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float childWidth = (float)getWidth() / (float)getChildCount();
        totalWidth = 0f;

        for(int i = 0; i < getChildCount(); i++) {

            View view = getChildAt(i);
            final int childWidthSpec = MeasureSpec.makeMeasureSpec((int)dpToPx(40f),  MeasureSpec.EXACTLY);
            view.measure(childWidthSpec, heightMeasureSpec);

            int width = view.getMeasuredWidth();
            totalWidth += childWidth;
        }

        totalWidth += getPaddingLeft() + getPaddingRight();
    }

    public void init() {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)dpToPx(50));
        setLayoutParams(params);

        //setGravity(Gravity.CENTER);
        setWillNotDraw(false);
        //setOrientation(HORIZONTAL);

        tabUnderline = new Paint();
        tabUnderline.setColor(tabUnderlineColor);
        tabUnderline.setStyle(Paint.Style.FILL);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {

                if(!tabs.isEmpty()) {
                    TextViewThick tab = tabs.get(index);

                    tabUnderlineOffset = tab.getX();
                    tabUnderLineWidth = tab.getWidth();
                }
            }
        });
    }

    public void setViewPager(ViewPager viewPager) {

        pager = viewPager;
        adapter = pager.getAdapter();

        pagerWidth = pager.getWidth();
        pager.setOnPageChangeListener(new PageListener());
        pager.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                pagerWidth = pager.getWidth();
            }
        });

        updateTabs();
    }

    public String getTabTitle() {
        return (String)pager.getAdapter().getPageTitle(index);
    }

    public void setTabUnderlineColor(int color) {
        tabUnderlineColor = color;
        tabUnderline.setColor(color);

        invalidate();
    }

    public void setTabLabelColor(int color) {
        tabLabelColor = color;

        for(int i = 0; i < getChildCount(); i++) {

            View v = getChildAt(i);
            if(v instanceof  TextViewThick) {
                ((TextViewThick)v).setTextColor(color);
            }
        }
    }

    public void updateTabs() {

        if(pager != null) {

            /*
            if(icons.size() < adapter.getCount()) {
                //throw new ArrayIndexOutOfBoundsException("Number of icons must be greater than or equal to number of ViewPager tabs.");
            }

            int pages = adapter.getCount();
            removeAllViews();

            System.out.println(pages + "page count");
            for(int i = 0; i < pages; i++) {

                System.out.println(adapter.getPageTitle(i).toString());
                tabs.add(addTab(adapter.getPageTitle(i).toString()));
            }
            */

            if(!tabs.isEmpty() && index < tabs.size()) {
                TextViewThick tab = tabs.get(index);
                tabUnderlineOffset = tab.getX();
                tabUnderLineWidth = tab.getWidth();

                invalidate();
                requestLayout();
            }
        }
    }

    public TextViewThick addTab(int drawableID) {
        TextViewThick view = new TextViewThick(getContext());

        view.setPadding((int) dpToPx(50), (int)dpToPx(5), (int) dpToPx(50), (int) dpToPx(5));
        view.setTextColor(tabLabelColor);
        view.setTextSize(13);
        view.setGravity(Gravity.CENTER);
        view.setAllCaps(true);
        view.setOnTouchListener(new TabTouchListener());

        try {
            if(Build.VERSION.SDK_INT >= 16) {
                view.setBackground(getResources().getDrawable(drawableID));
            } else {
                view.setBackgroundDrawable(getResources().getDrawable(drawableID));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        addView(view, params);

        tabs.add(view);

        return view;
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        System.out.println("PAINT TABS");

        Paint paint = new Paint();
        paint.setColor(Color.argb(80, 110, 110, 110));
        paint.setStyle(Paint.Style.FILL);
        //canvas.drawRect(0, getHeight() - 1, Math.max(totalWidth, getWidth() + getPaddingRight() + getPaddingLeft()), getHeight(), paint);

        float x = tabUnderlineOffset;
        float top = getHeight() - dpToPx(5);

        canvas.drawRect(x, top, x + tabUnderLineWidth, getHeight(), tabUnderline);

        /*
        tabUnderline.setColor(colors[0]);
        canvas.drawRect(x, top, x + (tabUnderLineWidth * 0.4f), getHeight(), tabUnderline);

        x += (tabUnderLineWidth * 0.4f);
        tabUnderline.setColor(colors[1]);
        canvas.drawRect(x, top, x + (tabUnderLineWidth * 0.3f), getHeight(), tabUnderline);

        x += (tabUnderLineWidth * 0.3f);
        tabUnderline.setColor(colors[2]);
        canvas.drawRect(x, top, x + (tabUnderLineWidth * 0.2f), getHeight(), tabUnderline);

        x += (tabUnderLineWidth * 0.2f);
        tabUnderline.setColor(colors[3]);
        canvas.drawRect(x, top, x + (tabUnderLineWidth * 0.1f), getHeight(), tabUnderline);
        */

        //canvas.drawRect(tabUnderlineOffset, getHeight() - dpToPx(5), tabUnderlineOffset + tabUnderLineWidth, getHeight(), tabUnderline);

        //canvas.drawRect(0, getHeight() - dpToPx(2), Math.max(totalWidth, getWidth()), getHeight(), paint);
    }

    class TabTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            TextViewThick textViewThick = (TextViewThick)view;

            switch(motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //view.setBackgroundColor(tabUnderlineColor);
                    break;
                case MotionEvent.ACTION_UP:
                    //view.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    pager.setCurrentItem(tabs.indexOf(textViewThick), true);
                    break;
            }

            return true;
        }
    }

    class PageListener implements ViewPager.OnPageChangeListener {

        int oldPosition = pager.getCurrentItem();
        int newPosition = pager.getCurrentItem();

        int scrollNewPosition = pager.getCurrentItem();

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            if(position != scrollNewPosition) {
                scrollNewPosition = position;
                if(scrollListener != null) {
                    scrollListener.onPositionChanged(scrollNewPosition, (String)pager.getAdapter().getPageTitle(scrollNewPosition));
                }
            }

            System.out.println("PP: " + positionOffset + "   " + positionOffsetPixels + "  " + position);

            if(!tabs.isEmpty() && position < tabs.size()) {

                float percent = (float)positionOffsetPixels / pagerWidth;
                float nextTabOffset = tabs.get(Math.min(tabs.size()-1, position + 1)).getX();
                float offset = nextTabOffset - tabs.get(position).getX();

                float nextTabWidth = tabs.get(Math.min(tabs.size()-1, position + 1)).getWidth();
                float widthDelta = nextTabWidth - tabs.get(position).getWidth();

                tabUnderlineOffset = tabs.get(position).getX() + (offset * percent);
                tabUnderLineWidth = tabs.get(position).getWidth() + (widthDelta * percent);

                if(totalWidth > getWidth()) {
                    float tabX = (tabUnderlineOffset + tabUnderLineWidth) + (getWidth()/2);
                    scrollX = Math.max(0, tabX - getWidth());
                    scrollX = Math.min(scrollX, ((totalWidth - getWidth())));
                   // setScrollX((int)(scrollX));
                } else {
                    //setScrollX(0);
                }

                index = position;
                invalidate();

                if(scrollListener != null) {
                    if(position < oldPosition) {
                        float p = 1f - positionOffset;
                        scrollListener.onScroll(p, position, DIRECTION_RIGHT);
                    } else if(position == oldPosition) {
                        scrollListener.onScroll(positionOffset, position, DIRECTION_LEFT);
                    }
                }
            }
        }

        @Override
        public void onPageSelected(int i) {
            newPosition = i;
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            if(i == ViewPager.SCROLL_STATE_IDLE && oldPosition != newPosition) {
                oldPosition = newPosition;
                if(scrollListener != null) scrollListener.onTabSelected(newPosition, (String)pager.getAdapter().getPageTitle(index));
            }
        }
    }

    public interface ScrollListener {
        public void onPositionChanged(int position, String title);
        public void onScroll(float percent, int position, int direction);
        public void onTabSelected(int position, String title);
    }
}
