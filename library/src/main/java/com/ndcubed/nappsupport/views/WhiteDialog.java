package com.ndcubed.nappsupport.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndcubed.nappsupport.R;
import com.ndcubed.nappsupport.utils.Common;

import java.util.ArrayList;

public class WhiteDialog {

    public static final int CANCEL_OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    public static final int OK_OPTION = 2;

    public static final int INPUT_AUTO_CORRECT = 0;
    public static final int INPUT_PLAIN = 1;

    private Activity activity;

    private int dialogType = CANCEL_OK_OPTION;
    private String messageText = "";
    private String dismissButtonText = "";
    private String positiveButtonText = "";

    private ViewGroup dialogRoot;
    private View dialogView, divider, decor, loadingWheel, verticalDivider, buttonContainer, loadingWheelContainer, iconViewContainer;
    private TextView messageTextView, okButton, dismissButton;
    private EditText inputField;
    private ImageView iconView;

    private ArrayList<DialogListener> dialogListeners = new ArrayList<DialogListener>();
    private boolean hideOnClick = true;
    private boolean acceptsInput = false;
    private boolean loadingDialog = false;
    private boolean wasShown = false;

    private Sensor magnetometer, accelerometer;
    private SensorListener sensorListener;
    private float maxRange = 0f;

    private float[] lastMagnetometer = new float[3];
    private float[] lastAccelerometer = new float[3];
    private boolean magnetometerSet = false;
    private boolean accelerometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];


    public WhiteDialog(Activity activity) {
        this.activity = activity;

        dialogRoot = new FrameLayout(activity);
        dialogView = activity.getLayoutInflater().inflate(R.layout.white_dialog_layout, null);
        decor = dialogView.findViewById(R.id.dialogDecor);

        iconView = (ImageView)dialogView.findViewById(R.id.iconView);
        iconViewContainer = dialogView.findViewById(R.id.iconViewContainer);
        inputField = (EditText)dialogView.findViewById(R.id.inputField);
        loadingWheelContainer = dialogView.findViewById(R.id.loadingWheelContainer);
        loadingWheel = dialogView.findViewById(R.id.loadingWheel);
        verticalDivider = dialogView.findViewById(R.id.verticalDivider);
        buttonContainer = dialogView.findViewById(R.id.buttonContainer);

        messageTextView = (TextView)dialogView.findViewById(R.id.messageText);
        okButton = (TextView)dialogView.findViewById(R.id.okButton);
        dismissButton = (TextView)dialogView.findViewById(R.id.dismissButton);
        divider = dialogView.findViewById(R.id.divider);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firePositiveButtonPressed();
            }
        });
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireDismissButtonPressed();
            }
        });

       // sensorListener = new SensorListener();

        //SensorManager sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        //magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
       // accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

       // if(magnetometer != null && accelerometer != null) {
           // sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        //}
    }

    public void setInputType(int inputType) {
        if(inputType == INPUT_AUTO_CORRECT) {
            inputField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        } else {
            inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    public void setMaxInputLength(int length) {
    }

    public void addDialogListener(DialogListener l) {
        dialogListeners.add(l);
    }

    public void removeDialogListener(DialogListener l) {
        dialogListeners.remove(l);
    }

    private void fireDismissButtonPressed() {

        for(DialogListener l : dialogListeners) {
            l.dismissButtonClicked();
        }

        if(hideOnClick) hide();
    }

    private void firePositiveButtonPressed() {

        for(DialogListener l : dialogListeners) {
            l.positiveButtonClicked();
        }

        if(hideOnClick) hide();
    }

    public void setIcon(int id) {
        iconView.setImageResource(id);
    }

    public void setIconVisible(boolean b) {
        iconViewContainer.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setIsLoadingDialog(boolean b) {
        loadingDialog = b;

        if(b) {
            buttonContainer.setVisibility(View.GONE);
            verticalDivider.setVisibility(View.GONE);
            loadingWheel.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.loading_wheel_animation));
            loadingWheelContainer.setVisibility(View.VISIBLE);
        } else {
            loadingWheel.clearAnimation();
            loadingWheelContainer.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.VISIBLE);
            verticalDivider.setVisibility(View.VISIBLE);
        }
    }

    public void setAcceptsInput(boolean b) {
        acceptsInput = b;

        if(b) {
            inputField.setVisibility(View.VISIBLE);
        } else {
            inputField.setVisibility(View.GONE);
        }
    }

    public String getInput() {
        return inputField.getText().toString();
    }

    public EditText getInputField() {
        return inputField;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;

        if(messageText.equals("")) {
            divider.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.GONE);
            messageTextView.setVisibility(View.GONE);
        } else if(!loadingDialog) {
            divider.setVisibility(View.VISIBLE);
            buttonContainer.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.VISIBLE);
        }

        messageTextView.setText(messageText);
    }

    public void setMessageInputText(String text) {
        inputField.setText(text);
    }

    public void setDismissButtonText(String dismissButtonText) {
        this.dismissButtonText = dismissButtonText;
        dismissButton.setText(dismissButtonText);
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
        okButton.setText(positiveButtonText);
    }

    public void setDismissButtonVisible(boolean b) {
        if(b) {
            dismissButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            dismissButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }

    public void setPositiveButtonVisible(boolean b) {
        if(b) {
            okButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            okButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }

    public void setLayout(int dialogType) {
        this.dialogType = dialogType;

        if(dialogType == CANCEL_OPTION) {
            dismissButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else if(dialogType == CANCEL_OK_OPTION) {
            dismissButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else if(dialogType == OK_OPTION) {
            dismissButton.setVisibility(View.GONE);
            okButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.GONE);
        }
    }

    public void show(boolean showKeyboard) {
        wasShown = true;

        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.CENTER;
        mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        // mWindowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(dialogRoot, mWindowParams);

        dialogRoot.addView(dialogView);

        if(acceptsInput && showKeyboard) {
            inputField.selectAll();
            Common.showSoftKeyboard(activity);
        }
    }

    public void show() {
        wasShown = true;

        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.CENTER;
        mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.horizontalMargin =
       // mWindowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        } else {
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(dialogView, mWindowParams);

        //dialogRoot.addView(dialogView);

        if(acceptsInput) {
            inputField.selectAll();
            Common.showSoftKeyboard(activity);
        }

        decor.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.white_dialog_show));
        //decor.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_from_top));
    }

    public void hide() {
        loadingWheel.clearAnimation();

        try {
            if(wasShown) {
                if(acceptsInput) {
                    Common.hideSoftKeyboard(activity, getInputField());
                }

                WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
                windowManager.removeView(dialogView);
            }
        } catch(IllegalStateException e) {
            e.printStackTrace();
        }

        dialogListeners.clear();
        activity = null;
    }

    public void setHideOnClick(boolean b) {
        this.hideOnClick = b;
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            System.out.println("MAX: " + maxRange);

            float[] values = sensorEvent.values;
            float offsetY = values[1] * 2f;


            if (sensorEvent.sensor == accelerometer) {

                float[] g = new float[3];
                g = sensorEvent.values.clone();

                double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

// Normalize the accelerometer vector
                g[0] = g[0] / (float)norm_Of_g;
                g[1] = g[1] / (float)norm_Of_g;
                g[2] = g[2] / (float)norm_Of_g;

                double inclination = Math.toDegrees(Math.acos(g[2]));
                float translation = 150f * ((float)(inclination) / 180f);

                float decorTranslation = (translation + (decor.getTranslationY() * 15)) / 16;

                //System.out.println("INCLINE: " + inclination);
                decor.setTranslationY(decorTranslation);

            } else if (sensorEvent.sensor == magnetometer) {
                System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
                magnetometerSet = true;
            }
            if (magnetometerSet && accelerometerSet) {
                SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public interface DialogListener {
        public void dismissButtonClicked();
        public void positiveButtonClicked();
    }
}
