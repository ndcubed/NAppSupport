package com.ndcubed.nappsupport.views.RobotoTextView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewThin extends TextView {

    public TextViewThin(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public TextViewThin(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public TextViewThin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public void createFont() {
        if(!isInEditMode()) {
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Thin.ttf");
            setTypeface(font);
        }
    }
}
