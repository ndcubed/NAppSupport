package com.ndcubed.nappsupport.cropimage;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CropImageView extends View {

    Bitmap image, overlayImage;
    Canvas overlayCanvas;
    Paint selectionPaint, overlayPaint, darkOverlay;
    RectF selectionBounds = new RectF();
    RectF aspectRatio = new RectF();

    private boolean isRecycled = false;

    float aspectX = 1f;
    float aspectY = 1f;

    private float pressX = 0f;
    private float pressY = 0f;
    private float originX = 0f;
    private float originY = 0f;

    private boolean adjustSize = false;

    private boolean cropOval = false;

    private int maxWidth = -1;
    private int maxHeight = -1;

    public CropImageView(Context context) {
        super(context);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);    //To change body of overridden methods use File | Settings | File Templates.
        init();
    }

    public void init() {

        selectionPaint = new Paint();
        selectionPaint.setAntiAlias(true);
        selectionPaint.setColor(Common.BLUE_COLOR);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(dpToPx(3f));

        overlayPaint = new Paint();
        overlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        overlayPaint.setColor(Color.rgb(255, 255, 255));
        overlayPaint.setAntiAlias(true);

        darkOverlay = new Paint();
        darkOverlay.setColor(Color.rgb(0, 0, 0));
        darkOverlay.setAntiAlias(true);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(!isRecycled) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                        pressX = motionEvent.getX();
                        pressY = motionEvent.getY();

                        originX = motionEvent.getX();
                        originY = motionEvent.getY();

                        float handleSize = dpToPx(18);
                        float left = (selectionBounds.left + (selectionBounds.width() / 2)) - (handleSize / 2);
                        float top = selectionBounds.top - (handleSize / 2);
                        RectF handle = new RectF(left, top, left + handleSize, top + handleSize);

                        adjustSize = false;
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                        float deltaX = motionEvent.getX() - pressX;
                        float deltaY = motionEvent.getY() - pressY;
                        pressX = motionEvent.getX();
                        pressY = motionEvent.getY();

                        float scale = Math.min((float) getWidth() / (float) overlayImage.getWidth(), (float) getHeight() / (float) overlayImage.getHeight());
                        float width = (float) overlayImage.getWidth() * scale;
                        float height = (float) overlayImage.getHeight() * scale;
                        float x = (getWidth() / 2) - (width / 2);
                        float y = (getHeight() / 2) - (height / 2);

                        if(adjustSize) {
                            selectionBounds.top = selectionBounds.top + deltaY;
                            selectionBounds.left = selectionBounds.left + deltaY;

                            overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);
                            overlayCanvas.translate(-x, -y);
                            if (!cropOval) {
                                overlayCanvas.drawRect(selectionBounds, overlayPaint);
                            } else {
                                overlayCanvas.drawOval(selectionBounds, overlayPaint);
                            }
                            overlayCanvas.translate(x, y);
                        } else {

                            float nX = Math.max(x, selectionBounds.left + deltaX);
                            float nY = Math.max(y, selectionBounds.top + deltaY);

                            if (nX + selectionBounds.width() > (x + width)) {
                                nX = (x + width) - selectionBounds.width();
                            }

                            if (nY + selectionBounds.height() > (y + height)) {
                                nY = (y + height) - selectionBounds.height();
                            }

                            selectionBounds = new RectF(nX, nY, nX + selectionBounds.width(), nY + selectionBounds.height());

                            overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);
                            overlayCanvas.translate(-x, -y);
                            if (!cropOval) {
                                overlayCanvas.drawRect(selectionBounds, overlayPaint);
                            } else {
                                overlayCanvas.drawOval(selectionBounds, overlayPaint);
                            }
                            overlayCanvas.translate(x, y);
                        }
                    }
                }

                invalidate();
                return true;
            }
        });

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {

                if(image != null) {
                    float scale = Math.min((float) getWidth() / (float) image.getWidth(), (float) getHeight() / (float) image.getHeight());
                    float width = (float) image.getWidth() * scale;
                    float height = (float) image.getHeight() * scale;
                    float x = (getWidth() / 2) - (width / 2);
                    float y = (getHeight() / 2) - (height / 2);

                    float aspectScale = Math.min(width / aspectRatio.width(), height / aspectRatio.height());
                    float aspectWidth = aspectRatio.width() * aspectScale;
                    float aspectHeight = aspectRatio.height() * aspectScale;

                    float selectionX = (x + (width / 2)) - (aspectWidth / 2);
                    float selectionY = (y + (height / 2)) - (aspectHeight / 2);

                    selectionBounds = new RectF(selectionX, selectionY, selectionX + aspectWidth, selectionY + aspectHeight);

                    overlayImage = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                    overlayCanvas = new Canvas(overlayImage);

                    overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);
                    overlayCanvas.drawRect(selectionBounds, overlayPaint);
                    overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);

                    overlayCanvas.translate(-x, -y);
                    if(!cropOval) {
                        overlayCanvas.drawRect(selectionBounds, overlayPaint);
                    } else {
                        overlayCanvas.drawOval(selectionBounds, overlayPaint);
                    }
                    overlayCanvas.translate(x, y);

                    invalidate();
                }
            }
        });
    }

    public void recycle() {
        isRecycled = true;

        if(image != null) {
            image.recycle();
            image = null;
        }

        if(overlayImage != null) {
            overlayImage.recycle();
            overlayImage = null;
        }

        invalidate();
    }

    public void setMaxSize(int width, int height) {
        maxWidth = width;
        maxHeight = height;
    }

    public void setCropOval(boolean b) {
        cropOval = b;

        if(getWidth() != 0 && getHeight() != 0) {
            float scale = Math.min((float) getWidth() / (float) overlayImage.getWidth(), (float) getHeight() / (float) overlayImage.getHeight());
            float width = (float) overlayImage.getWidth() * scale;
            float height = (float) overlayImage.getHeight() * scale;
            float x = (getWidth() / 2) - (width / 2);
            float y = (getHeight() / 2) - (height / 2);

            overlayImage = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
            overlayCanvas = new Canvas(overlayImage);

            overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);
            overlayCanvas.translate(-x, -y);
            if(!cropOval) {
                overlayCanvas.drawRect(selectionBounds, overlayPaint);
            } else {
                overlayCanvas.drawOval(selectionBounds, overlayPaint);
            }
            overlayCanvas.translate(x, y);
        }

        invalidate();
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public void setImage(Bitmap image, RectF aspectRatio) {
        this.image = image;
        this.aspectRatio = aspectRatio;

        overlayImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayImage);

        overlayCanvas.drawRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight(), darkOverlay);
        overlayCanvas.drawRect(selectionBounds, overlayPaint);

        float scale = Math.min((float) getWidth() / (float) image.getWidth(), (float) getHeight() / (float) image.getHeight());
        float width = (float) image.getWidth() * scale;
        float height = (float) image.getHeight() * scale;
        float x = (getWidth() / 2) - (width / 2);
        float y = (getHeight() / 2) - (height / 2);

        float aspectScale = Math.min(width / aspectRatio.width(), height / aspectRatio.height());
        float aspectWidth = aspectRatio.width() * aspectScale;
        float aspectHeight = aspectRatio.height() * aspectScale;

        float selectionX = (x + (width / 2)) - (aspectWidth / 2);
        float selectionY = (y + (height / 2)) - (aspectHeight / 2);

        selectionBounds = new RectF(selectionX, selectionY, selectionX + aspectWidth, selectionY + aspectHeight);

        invalidate();
    }

    public String saveImage(String saveImagePath) {

        float scale = Math.min((float) getWidth() / (float) image.getWidth(), (float) getHeight() / (float) image.getHeight());
        float width = (float) image.getWidth() * scale;
        float height = (float) image.getHeight() * scale;
        float x = (getWidth() / 2) - (width / 2);
        float y = (getHeight() / 2) - (height / 2);

        float dstScaleX = (float)image.getWidth() / width;
        float dstScaleY = (float)image.getHeight() / height;

        float srcX = (selectionBounds.left - x) * dstScaleX;
        float srcY = (selectionBounds.top - y) * dstScaleY;
        float srcWidth = selectionBounds.width() * dstScaleX;
        float srcHeight = selectionBounds.height() * dstScaleY;

        if(!cropOval) {
            float bWidth = srcWidth;
            float bHeight = srcHeight;

            if(maxWidth != -1 && maxHeight != -1) {
                float bitmapScale = Math.min((float)maxWidth / srcWidth, (float)maxHeight / srcHeight);
                bWidth = srcWidth * bitmapScale;
                bHeight = srcHeight * bitmapScale;
            }

            Bitmap bitmap = Bitmap.createBitmap((int)bWidth, (int)bHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);

            Rect srcRect = new Rect((int)srcX, (int)srcY, (int)(srcX + srcWidth), (int)(srcY + srcHeight));
            canvas.drawBitmap(image, srcRect, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), null);

            try {
                FileOutputStream out = new FileOutputStream(saveImagePath);
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

                if(compressed) {
                    System.out.println("COMPRESSED");
                }
                bitmap.recycle();
            } catch(Exception err) {
                err.printStackTrace();
            }

            System.out.println("DIM: " + bitmap.getWidth() + " x " + bitmap.getHeight() + "  " + selectionBounds + "  |  " + image.getWidth() + " x " + image.getHeight());
        } else {
            float bWidth = srcWidth;
            float bHeight = srcHeight;

            if(maxWidth != -1 && maxHeight != -1) {
                float bitmapScale = Math.min((float)maxWidth / srcWidth, (float)maxHeight / srcHeight);
                bWidth = srcWidth * bitmapScale;
                bHeight = srcHeight * bitmapScale;
            }

            Bitmap mask = Bitmap.createBitmap((int)bWidth, (int)bHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mask);

            Paint clearPaint = new Paint();
            clearPaint.setAntiAlias(true);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            canvas.drawRect(new RectF(0, 0, mask.getWidth(), mask.getHeight()), darkOverlay);
            canvas.drawOval(new RectF(0, 0, mask.getWidth(), mask.getHeight()), clearPaint);

            Bitmap bitmap = Bitmap.createBitmap((int)bWidth, (int)bHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            Rect srcRect = new Rect((int)srcX, (int)srcY, (int)(srcX + srcWidth), (int)(srcY + srcHeight));
            canvas.drawBitmap(image, srcRect, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), null);
            canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), clearPaint);

            System.out.println("DIM: " + bitmap.getWidth() + " x " + bitmap.getHeight() + "  " + selectionBounds);

            try {
                FileOutputStream out = new FileOutputStream(saveImagePath);
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                if(compressed) {
                    System.out.println("COMPRESSED");
                }
                bitmap.recycle();
                mask.recycle();
            } catch(Exception err) {
                err.printStackTrace();
            }
        }

        return saveImagePath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(image != null) {

            float scale = Math.min((float)getWidth() / (float)image.getWidth(), (float)getHeight() / (float)image.getHeight());
            float width = (float)image.getWidth() * scale;
            float height = (float)image.getHeight() * scale;
            float x = (getWidth() / 2) - (width / 2);
            float y = (getHeight() / 2) - (height / 2);

            canvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new RectF(x, y, x + width, y + height), null);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setAlpha(120);

            canvas.drawBitmap(overlayImage, x, y, paint);

            if(!cropOval) {
                Paint borderPaint = new Paint();
                borderPaint.setAntiAlias(true);
                borderPaint.setColor(Color.rgb(2, 146, 190));
                borderPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(selectionBounds, borderPaint);

                /*
                //draw handles
                Paint handlePaint = new Paint();
                handlePaint.setAntiAlias(true);
                handlePaint.setColor(Common.BLUE_COLOR);

                float handleSize = dpToPx(16);
                float left = (selectionBounds.left + (selectionBounds.width() / 2)) - (handleSize / 2);
                float top = selectionBounds.top - (handleSize / 2);

                canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                left = selectionBounds.left - (handleSize / 2);
                top = (selectionBounds.top + (selectionBounds.height() / 2)) - (handleSize / 2);
                canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                left = (selectionBounds.left + (selectionBounds.width() / 2)) - (handleSize / 2);
                top = (selectionBounds.top + selectionBounds.height()) - (handleSize / 2);
                canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                left = (selectionBounds.left + selectionBounds.width()) - (handleSize / 2);
                top = (selectionBounds.top + (selectionBounds.height() / 2)) - (handleSize / 2);
                canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);
                */
            } else {
                //Paint handlePaint = new Paint();
                //handlePaint.setAntiAlias(true);
                //handlePaint.setColor(Common.BLUE_COLOR);

               // float handleSize = dpToPx(18);
                //float left = (selectionBounds.left + (selectionBounds.width() / 2)) - (handleSize / 2);
               // float top = selectionBounds.top - (handleSize / 2);

                //canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                //left = selectionBounds.left - (handleSize / 2);
                //top = (selectionBounds.top + (selectionBounds.height() / 2)) - (handleSize / 2);
                //canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                //left = (selectionBounds.left + (selectionBounds.width() / 2)) - (handleSize / 2);
                //top = (selectionBounds.top + selectionBounds.height()) - (handleSize / 2);
                //canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);

                //left = (selectionBounds.left + selectionBounds.width()) - (handleSize / 2);
                //top = (selectionBounds.top + (selectionBounds.height() / 2)) - (handleSize / 2);
                //canvas.drawOval(new RectF(left, top, left + handleSize, top + handleSize), handlePaint);
            }
        }
    }
}
