package com.google.android.exoplayer2.demo;

import java.sql.Array;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.lang.Thread;

/* OML libraries */
import omlBasePackage.OMLBase;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import omlBasePackage.OmlMP;

/* PostgreSQL libraries */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    //
    public static void logVideoSizeChange(int w, int h) {
        Log.d("[LogService]", "logVideoSizeChange");
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), String.valueOf(w), String.valueOf(h)};
        if (isOmlRunning) {
            // hay que inyectar los datos en un nuevo thread, sino
            // se bloquea por un error NetworkOnMainThreadException
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(0).inject(data);
                    Log.d("[LogService]", "video_mp: " + data[0] + ", " + data[1] + ", " + data[2]);
                }
            });
            t.start();
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
            // String experiment_name = "ex_" + formatter.format(System.currentTimeMillis());
            // PostgreSQL connection to get db name
//            try {
//                Class.forName("org.postgresql.Driver");
//                // "jdbc:postgresql://IP:PUERTO/DB", "USER", "PASSWORD")
//                Log.d("[dbService]", "Database connection...");
//                String url = "jdbc:postgresql://94.177.232.57:5432/experiments_registry";
//                Connection conn = DriverManager.getConnection(url, "oml", "tester");
//                //En el stsql se puede agregar cualquier consulta SQL deseada.
//                String stsql = "Select version()";
//                Log.d("[dbService]", "Select version()");
//                Statement st = conn.createStatement();
//                ResultSet rs = st.executeQuery(stsql);
//                rs.next();
//                DatabaseMetaData meta = conn.getMetaData();
//                ResultSet res = meta.getCatalogs();
//                while (rs.next()) {
//                    System.out.println("TABLE_CAT = " + rs.getString("TABLE_CAT") );
//                    Log.d("[dbService]", "TABLE_CAT = " + rs.getString("TABLE_CAT"));
//                }
//                res.close();
//                conn.close();
//            } catch (SQLException se) {
//                System.out.println("oops! No se puede conectar. Error: " + se.toString());
//                Log.d("[dbService]", "oops! No se puede conectar. Error: " + se.toString());
//                se.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                System.out.println("oops! No se encuentra la clase. Error: " + e.getMessage());
//                Log.d("[dbService]", "oops! No se encuentra la clase. Error: " + e.getMessage());
//                e.printStackTrace();
//            }
            // otra forma
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://94.177.232.57:5432/experiments_registry?user=oml&password=tester";
            Connection conn;
            String exp_name = null;
            try {
                DriverManager.setLoginTimeout(5);
                Log.d("[dbService]", "Connecting to database...");
                conn = DriverManager.getConnection(url);
                Log.d("[dbService]", "Connected! :-)");
                Statement st = conn.createStatement();
                String query = "SELECT * FROM experiments ORDER BY id DESC LIMIT 1";
                ResultSet rs = st.executeQuery(query);
                while (rs.next()) {
                    exp_name = rs.getString("experiment_name");
                    Log.d("[dbService]", "Experiment name = " + exp_name);
                }
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String experiment_name = exp_name;
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
            oml.addmp("ExoPlayer_video", video_mp);
            oml.addmp("ExoPlayer_audio", audio_mp);

            oml.start();

            Log.d("[LogService]", "OML Server started");

            measurePoints.add(video_mp); // measure_points[0]
            measurePoints.add(audio_mp); // measure_points[1]

            return measurePoints;
        }

    }
}
