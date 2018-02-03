package com.ndcubed.nappsupport.views.RobotoTextView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewLight extends TextView {

    public TextViewLight(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public TextViewLight(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public TextViewLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
        createFont();
    }

    public void createFont() {
        if(!isInEditMode()) {
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf");
            setTypeface(font);
        }
    }
}
