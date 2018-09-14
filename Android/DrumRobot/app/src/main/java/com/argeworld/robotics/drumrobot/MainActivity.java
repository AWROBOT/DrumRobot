package com.argeworld.robotics.drumrobot;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import info.hoang8f.widget.FButton;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String BT_NAME = "DRUMROBOT";

    private FButton btnPlayDrum;
    private FButton btnPlaySong;

    private MaterialDialog dialog;

    private int iBTSetup;

    private List<SongListItem> songList;

    private List<Rhythm> rhythms;

    private MediaPlayer mediaPlayer;

    public int play_index = 0;

    private static MainActivity instance;

    private MainApplication m_Main;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");

        m_Main = (MainApplication) this.getApplication();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        if(m_Main.bt == null)
        {
            m_Main.bt = new BluetoothSPP(this);

            if (!m_Main.bt.isBluetoothAvailable()) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Error")
                        .content("Bluetooth is not available !")
                        .negativeText("EXIT")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                System.exit(0);
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .show();

                return;
            }

            m_Main.bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                public void onDataReceived(byte[] data, String message) {
                    Log.i(TAG, "BT onDataReceived: " + message);
                }
            });

            m_Main.bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    Log.i(TAG, "BT onDeviceConnected: " + name + " " + address);

                    Toast.makeText(getApplicationContext()
                            , "Connected to " + name
                            , Toast.LENGTH_SHORT).show();

                    SendCommand("I");

                    iBTSetup = 1;

                    SharedPreferences settings = getApplicationContext().getSharedPreferences("DRUM_APP", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("BT_SETUP", iBTSetup);
                    editor.apply();
                }

                public void onDeviceDisconnected() {
                    Toast.makeText(getApplicationContext()
                            , "Connection lost", Toast.LENGTH_SHORT).show();
                }

                public void onDeviceConnectionFailed() {
                    Toast.makeText(getApplicationContext()
                            , "Unable to connect", Toast.LENGTH_SHORT).show();
                }
            });

            m_Main.bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
                public void onServiceStateChanged(int state) {
                    if (state == BluetoothState.STATE_CONNECTED) {
                        Log.i(TAG, "BT onServiceStateChanged to CONNECTED");

                        if (dialog != null)
                            dialog.dismiss();

                        Toast.makeText(getApplicationContext()
                                , "Connected to Drum Robot"
                                , Toast.LENGTH_SHORT).show();

                        SendCommand("I");
                    } else if (state == BluetoothState.STATE_CONNECTING) {
                        Log.i(TAG, "BT onServiceStateChanged to CONNECTING");
                    } else if (state == BluetoothState.STATE_LISTEN) {
                        Log.i(TAG, "BT onServiceStateChanged to LISTEN");
                    } else if (state == BluetoothState.STATE_NONE) {
                        Log.i(TAG, "BT onServiceStateChanged to STATE_NONE");
                    }
                }
            });

            m_Main.bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
                public void onNewConnection(String name, String address) {
                    Log.i(TAG, "New Connection - " + name + " - " + address);

                    if (!name.equals(BT_NAME)) {
                        if (dialog != null)
                            dialog.dismiss();

                        iBTSetup = 0;

                        SharedPreferences settings = getApplicationContext().getSharedPreferences("DRUM_APP", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("BT_SETUP", iBTSetup);
                        editor.apply();

                        InitBT();
                    }
                }

                public void onAutoConnectionStarted() {
                    Log.i(TAG, "Auto menu_connection started");
                }
            });

            InitBT();
        }

        SetupButtons();
    }

    public static MainActivity getInstance()
    {
        return instance;
    }

    @Override
    public void onStart()
    {
        Log.i(TAG, "onStart");

        super.onStart();

        if(!m_Main.bInited)
        {
            if (!m_Main.bt.isBluetoothEnabled()) {
                Log.i(TAG, "BT disabled");

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            } else {
                Log.i(TAG, "BT enabled");

                if (!m_Main.bt.isServiceAvailable()) {
                    StartBTService();
                } else {
                    SendCommand("I");
                }
            }
        }
    }

    public void onDestroy()
    {
        Log.i(TAG, "onDestroy");

        super.onDestroy();
        //bt.stopService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG, "onActivityResult: " + requestCode);

        if(!m_Main.bInited)
        {
            if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
                if (resultCode == Activity.RESULT_OK)
                    m_Main.bt.connect(data);
            } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
                if (resultCode == Activity.RESULT_OK) {
                    StartBTService();
                } else {
                    Toast.makeText(getApplicationContext()
                            , "Bluetooth was not enabled."
                            , Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    public void StartBTService()
    {
        Log.i(TAG,"StartBTService");

        dialog = new MaterialDialog.Builder(this)
                .title("Please Wait")
                .content("Connecting to Drum Robot...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();

        m_Main.bt.setupService();
        m_Main.bt.startService(BluetoothState.DEVICE_OTHER);

        if(iBTSetup == 1)
        {
            Log.i(TAG,"AutoConnect");

            m_Main.bt.autoConnect(BT_NAME);
        }
    }

    public void InitBT()
    {
        if(!m_Main.bInited)
        {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("DRUM_APP", 0);
            iBTSetup = settings.getInt("BT_SETUP", 0);

            if (iBTSetup == 0) {
                if (m_Main.bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    m_Main.bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        }
    }

    public void SetupButtons()
    {
        btnPlayDrum = (FButton) findViewById(R.id.drum_button);
        btnPlayDrum.setButtonColor(getResources().getColor(R.color.fbutton_color_orange));
        btnPlayDrum.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), DrumScreen.class);
                startActivity(intent);
            }
        });

        btnPlaySong = (FButton) findViewById(R.id.song_button);
        btnPlaySong.setButtonColor(getResources().getColor(R.color.fbutton_color_alizarin));
        btnPlaySong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), SongListScreen.class);
                startActivity(intent);
            }
        });
    }

    public void SendCommand(String cmd)
    {
        Log.i(TAG, "SendCommand: " + cmd);

        if(m_Main.bt != null)
        {
            m_Main.bt.send(cmd, true);

            if (cmd.equals("I"))
            {
                m_Main.bInited = true;
            }
        }
    }
}
