package com.argeworld.robotics.drumrobot;

import android.app.Application;

import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class MainApplication extends Application
{
    public List<SongListItem> songList;

    public List<Rhythm> rhythms;

    public int iSelectedSong;

    public String strSelectedSongName;

    public boolean bInited = false;

    public BluetoothSPP bt;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }
}
