package com.argeworld.robotics.drumrobot;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import info.hoang8f.widget.FButton;

public class DrumScreen extends AppCompatActivity implements View.OnTouchListener
{
    private static final String TAG = DrumScreen.class.getSimpleName();

    private FButton btnLeft;
    private FButton btnRight;
    private FButton btnBoth;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drum);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SetupButtons();
    }

    public void SetupButtons()
    {
        btnLeft = (FButton) findViewById(R.id.left_button);
        btnLeft.setButtonColor(getResources().getColor(R.color.fbutton_color_orange));
        btnLeft.setOnTouchListener(this);

        btnRight = (FButton) findViewById(R.id.right_button);
        btnRight.setButtonColor(getResources().getColor(R.color.fbutton_color_orange));
        btnRight.setOnTouchListener(this);

        btnBoth = (FButton) findViewById(R.id.both_button);
        btnBoth.setButtonColor(getResources().getColor(R.color.fbutton_color_alizarin));
        btnBoth.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(v.getId() == R.id.both_button)
            {
                Log.i(TAG,"Both Button Down");

                btnBoth.onTouch(v, event);

                MainActivity.getInstance().SendCommand("B");
            }
            else if(v.getId() == R.id.left_button)
            {
                Log.i(TAG,"Left Button Down");

                btnLeft.onTouch(v, event);

                MainActivity.getInstance().SendCommand("L");
            }
            else if(v.getId() == R.id.right_button)
            {
                Log.i(TAG,"Right Button Down");

                btnRight.onTouch(v, event);

                MainActivity.getInstance().SendCommand("R");
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if(v.getId() == R.id.both_button)
            {
                Log.i(TAG,"Both Button Up");

                btnBoth.onTouch(v, event);
            }
            else if(v.getId() == R.id.left_button)
            {
                Log.i(TAG,"Left Button Up");

                btnLeft.onTouch(v, event);
            }
            else if(v.getId() == R.id.right_button)
            {
                Log.i(TAG,"Right Button Up");

                btnRight.onTouch(v, event);
            }
        }

        return true;
    }
}
