package com.google.android.exoplayer2.demo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import omlBasePackage.OMLBase;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import omlBasePackage.OmlMP;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;

public class LogService extends Service {

    public static OMLBase oml;
    public static OmlMP video_mp;
    public static OmlMP audio_mp;
    public static ArrayList<OmlMP> measure_points;
    public static boolean isOmlRunning = false;

    public static boolean logServiceIsRunning = false;

    static SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyyHH.mm.ss.SSS");
    private static String timestamp;
    private static String video_format;
    private static String resolution = "";
    IBinder mBinder = new LocalBinder();
    private String audio_format;

    public static void TestMsg(String test) {
        Log.d("[LogService]", test);
    }

    public static void logVideoSizeChange(int w, int h) {
        Log.d("[LogService]", "logVideoSizeChange");
        timestamp = formatter.format(System.currentTimeMillis());
        String[] data = {String.valueOf(timestamp), String.valueOf(w), String.valueOf(h)};
        if (isOmlRunning) {
            Log.d("[LogService]", "video_mp: " + data);
            measure_points.get(0).inject(data);
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("[LogService]", "Iniciando servicio");
        super.onStartCommand(intent, flags, startId);
        new OmlSetup().execute("exoplayer-experiment");
        logServiceIsRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (oml != null) oml.close();
        Log.d("[LogService]", "OML close");
    }

    public void logVideoFormatChange(String videoFormat) {
        timestamp = formatter.format(System.currentTimeMillis());
        video_format = videoFormat;
        String[] data = {timestamp, resolution, video_format};
        if (video_format != null) {
            video_mp.inject(data);
        }
    }

    public void logAudioFormatChange(String audioFormat) {
        timestamp = formatter.format(System.currentTimeMillis());
        audio_format = audioFormat;
        String[] data = {timestamp, audio_format};
        if (video_format != null) {
            audio_mp.inject(data);
        }
    }

    public class LocalBinder extends Binder {
        public LogService getServerInstance() {
            return LogService.this;
        }
    }

    // AsyncTask<Input, Progression, Result>
    private class OmlSetup extends AsyncTask<String, Void, ArrayList<OmlMP>> {

        public OmlMP getVideoMP() {
            return video_mp;
        }

        @Override
        protected void onPostExecute(ArrayList<OmlMP> result) {
            isOmlRunning = true;
            measure_points = result;
        }

        @Override
        protected ArrayList<OmlMP> doInBackground(String... experimentName) {
            // Returns a value, that is catched by the onPostExecute method.
            String experiment_name = "ex_" + formatter.format(System.currentTimeMillis());
            OMLBase oml = new OMLBase("ExoPlayer", experiment_name, "exoplayer", "tcp:94.177.232.57:3003");
            ArrayList<OmlMP> measurePoints = new ArrayList<>();
            ArrayList<OMLMPFieldDef> videoMp = new ArrayList<>();
            videoMp.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            videoMp.add(new OMLMPFieldDef("width", OMLTypes.OML_STRING_VALUE));
            videoMp.add(new OMLMPFieldDef("height", OMLTypes.OML_STRING_VALUE));

            ArrayList<OMLMPFieldDef> audioMp = new ArrayList<>();
            audioMp.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            audioMp.add(new OMLMPFieldDef("audio_format", OMLTypes.OML_STRING_VALUE));

            OmlMP video_mp = new OmlMP(videoMp);
            OmlMP audio_mp = new OmlMP(audioMp);

            // Add schema
            oml.addmp("video", video_mp);
            oml.addmp("audio", audio_mp);

            oml.start();

            Log.d("[LogService]", "OML Server started");

            measurePoints.add(video_mp); // measure_points[0]
            measurePoints.add(audio_mp); // measure_points[1]

            return measurePoints;
        }
    }
}
