package com.ndcubed.nappsupport.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import com.ndcubed.nappsupport.utils.Common;

public class ColorDot extends View {

    private int color = Common.NATURAL_LIGHT_COLOR;
    private SweepGradient sweepGradient;
    private boolean isMultiColored = false;

    public ColorDot(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ColorDot(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ColorDot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setDotColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setDotColors(int[] colors) {
        isMultiColored = true;
        sweepGradient = new SweepGradient(getMeasuredWidth()/2, getMeasuredHeight()/2, colors, null);
        invalidate();
    }

    public int getDotColor() {
        return color;
    }

    public void setMultiColored(boolean b) {
        isMultiColored = b;
        invalidate();
    }

    public boolean isMultiColored() {
        return isMultiColored;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        if(!isMultiColored || sweepGradient == null) {
            paint.setColor(getDotColor());
            canvas.drawOval(new RectF(0, 0, getWidth(), getHeight()), paint);
        } else {
            paint.setShader(sweepGradient);
            canvas.drawOval(new RectF(0, 0, getWidth(), getHeight()), paint);
        }
    }
}
