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

/* Librerias auxiliares */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*
    * Funciones de log:
    * =================
    * - logAudioEnabled ------------------- [audio_events]
    * - logAudioSessionId ----------------- [audio_events]
    * - logAudioDecoderInitialized -------- [audio_events]
    * - logAudioDisabled ------------------ [audio_events]
    * - logAudioTrackUnderrun ------------- [audio_trackunderrun]
    * - logAudioInputFormatChanged -------- [audio_format]
    * - logVideoEnabled ------------------- [video_events]
    * - logVideoDecoderInitialized -------- [video_events]
    * - logVideoDisabled ------------------ [video_events]
    * - logRenderedFirstFrame ------------- [video_events][disabled]
    * - logDrmSessionManagerError --------- [video_events]
    * - logDrmKeysLoaded ------------------ [video_events]
    * - logVideoInputFormatChanged -------- [video_format]
    * - logDroppedFrames ------------------ [video_dropped]
    *
    * */

    /*
    * AUDIO
    *
    * */

    // Tabla audio_events
    // Idea: eventos con parametros en json anidado
    public static void logAudioEnabled() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "audioEnabled"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(5).inject(data); // audio_events field
                    Log.d("[LogService]", "audioEnabled at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logAudioSessionId(int audioSessionId) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "AudioSessionId " + String.valueOf(audioSessionId)};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(5).inject(data); // audio_events field
                    Log.d("[LogService]", "audioSessionId at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logAudioDecoderInitialized(String decoderName) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "AudioDecoderInitialized " + decoderName};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(5).inject(data); // audio_events field
                    Log.d("[LogService]", "AudioDecoderInitialized at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logAudioDisabled() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "AudioDisabled"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(5).inject(data); // audio_events field
                    Log.d("[LogService]", "AudioDisabled at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

    // Tabla audio_trackunderrun
    public static void logAudioTrackUnderrun(int bufferSize, long bufferSizeMs) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), String.valueOf(bufferSize), String.valueOf(bufferSizeMs)};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(4).inject(data); // audio_track_underrun field
                    Log.d("[LogService]", "AudioTrackUnderrun at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

    // Tabla audio_format
    public static void logAudioInputFormatChanged(String mimetype, String bitrate, String channels, String sample_rate, String language) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), mimetype, bitrate, channels, sample_rate, language};
        Log.d("[LogService]", "AudioInputFormatChanged at " + data[0] + "; Format: " + data[1]);
    }

    /*
    * VIDEO
    *
    * */

    // Tabla video_events
    public static void logVideoEnabled() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "videoEnabled"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(2).inject(data); // video_events field
                    Log.d("[LogService]", "videoEnabled at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logVideoDecoderInitialized(String decoder) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "VideoDecoderInitialized: " + decoder};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(2).inject(data); // video_events field
                    Log.d("[LogService]", "VideoDecoderInitialized at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logVideoDisabled() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "videoDisabled"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(2).inject(data); // video_events field
                    Log.d("[LogService]", "videoDisabled at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logRenderedFirstFrame() {
        // Do nothing.
    }
    public static void logDrmSessionManagerError() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "DrmSessionManagerError"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(2).inject(data); // video_events field
                    Log.d("[LogService]", "DrmSessionManagerError at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }
    public static void logDrmKeysLoaded() {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), "DrmKeysLoaded"};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(2).inject(data); // video_events field
                    Log.d("[LogService]", "DrmKeysLoaded at " + data[0]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

    // Tabla video_format
    public static void logVideoInputFormatChanged(String mimetype, String bitrate, String resolution, String fps) {
        timestamp = formatter.format(System.currentTimeMillis());
        final String[] data = {String.valueOf(timestamp), mimetype, bitrate, resolution, fps};
        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(0).inject(data); // video_format field
                    Log.d("[LogService]", "VideoInputFormatChanged at " + data[0] + "; mimetype: " + data[1] + "; bitrate: " + data[2] + "; resolution: " + data[3] + "; fps: " + data[4]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

    // Tabla video_dropped_frames
    public static void logDroppedFrames(String count) {
        timestamp = formatter.format(System.currentTimeMillis());

        // data = {timestamp, count}
        final String[] data = {String.valueOf(timestamp), count};

        if (isOmlRunning) {
            // hay que injectar en un thread para no bloquear
            Thread t = new Thread(new Runnable() {
                public void run() {
                    measure_points.get(1).inject(data); // video_dropped field
                    Log.d("[LogService]", "DroppedFrames at " + data[0] + "; count: " + data[1]);
                }
            });
            t.start();
        } else {
            Log.d("[LogService]", "OML Server not running yet.");
        }
    }

//    public static void logVideoSizeChanged() {
//        // TODO 15/01/17
//    }
//
//    public static void logVideoSizeChange(int w, int h) {
//        Log.d("[LogService]", "logVideoSizeChange");
//        timestamp = formatter.format(System.currentTimeMillis());
//        final String[] data = {String.valueOf(timestamp), String.valueOf(w), String.valueOf(h)};
//        if (isOmlRunning) {
//            // hay que inyectar los datos en un nuevo thread, sino
//            // se bloquea por un error NetworkOnMainThreadException
//            Thread t = new Thread(new Runnable() {
//                public void run() {
//                    measure_points.get(0).inject(data);
//                    Log.d("[LogService]", "video_mp: " + data[0] + ", " + data[1] + ", " + data[2]);
//                }
//            });
//            t.start();
//        } else {
//            Log.d("[LogService]", "OML Server not running yet.");
//        }
//    }
//
//    public static void logVideoFormatChange(String videoFormat) {
//        timestamp = formatter.format(System.currentTimeMillis());
//        video_format = videoFormat;
//        String[] data = {timestamp, resolution, video_format};
//        if (video_format != null) {
//            video_mp.inject(data);
//        }
//    }

// ---------- esta la puedo quitar
//    public void logAudioFormatChange(String audioFormat) {
//        timestamp = formatter.format(System.currentTimeMillis());
//        audio_format = audioFormat;
//        String[] data = {timestamp, audio_format};
//        if (video_format != null) {
//            audio_mp.inject(data);
//        }
//    }

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
        // Hemos metido en el Intent parametros adicionales, los sacamos con esto
        Bundle extras = intent.getExtras();
        String dbname = "exoplayer_" + formatter.format(System.currentTimeMillis()); // default
        if(extras.get("DBNAME") == null) {
            Log.d("[LogService]", "null"); // no hay extras, nombre por defecto para la base de datos
        }
        else
        {
            Log.d("[LogService]","not null");
            dbname = (String) extras.get("DBNAME");
        }
        Log.d("[LogService]", "Iniciando servicio");
        Log.d("[LogService]", "Escribiendo en " + dbname);
        super.onStartCommand(intent, flags, startId);
        new OmlSetup().execute(dbname);
        logServiceIsRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (oml != null) oml.close();
        Log.d("[LogService]", "OML close");
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
            // El metodo doInBackground devuelve un 'result' que es capturado por el
            // metodo 'onPostExecute'. Lo usamos para pasar los measure_points.
            // Le pasamos la String dbname en la llamada: new OmlSetup().execute(dbname);
            // Podiamos haber pasado un array de Strings, por eso el [0].
            String experiment_name = experimentName[0];
            Log.d("[LogService]", "!!! experiment_name="+experiment_name);

            /*
            * Esto creo que es una prueba. Luego lo quito y lo compruebo.
            *
            * */
            try {
                Class.forName("org.postgresql.Driver");
                // "jdbc:postgresql://IP:PUERTO/DB", "USER", "PASSWORD")
                Log.d("[dbService]", "Database connection...");
                String url = "jdbc:postgresql://94.177.232.57:5432/experiments_registry";
                Connection conn = DriverManager.getConnection(url, "oml", "tester");
                //En el stsql se puede agregar cualquier consulta SQL deseada.
                String stsql = "Select version()";
                Log.d("[dbService]", "Select version()");
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(stsql);
                rs.next();
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet res = meta.getCatalogs();
                while (rs.next()) {
                    System.out.println("TABLE_CAT = " + rs.getString("TABLE_CAT") );
                    Log.d("[dbService]", "TABLE_CAT = " + rs.getString("TABLE_CAT"));
                }
                res.close();
                conn.close();
            } catch (SQLException se) {
                System.out.println("oops! No se puede conectar. Error: " + se.toString());
                Log.d("[dbService]", "oops! No se puede conectar. Error: " + se.toString());
                se.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("oops! No se encuentra la clase. Error: " + e.getMessage());
                Log.d("[dbService]", "oops! No se encuentra la clase. Error: " + e.getMessage());
                e.printStackTrace();
            }

// Lo haciamos asi para capturar el nombre de la base de datos directamente del servidor OML
// Ahora le pasamos el nombre de la DB como parametro o le ponemos uno por defecto.
//
//            try {
//                Class.forName("org.postgresql.Driver");
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            String url = "jdbc:postgresql://94.177.232.57:5432/experiments_registry?user=oml&password=tester";
//            Connection conn;
//            String exp_name = null;
//            try {
//                DriverManager.setLoginTimeout(5);
//                Log.d("[dbService]", "Connecting to database...");
//                conn = DriverManager.getConnection(url);
//                Log.d("[dbService]", "Connected! :-)");
//                Statement st = conn.createStatement();
//                String query = "SELECT * FROM experiments ORDER BY id DESC LIMIT 1";
//                ResultSet rs = st.executeQuery(query);
//                while (rs.next()) {
//                    exp_name = rs.getString("experiment_name");
//                    Log.d("[dbService]", "Experiment name = " + exp_name);
//                }
//                rs.close();
//                st.close();
//                conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            /* Configuramos el servidor OML:
            * =============================
            * - Establecemos la uri del servidor, la base de datos y alguna informacion mas
            * - Establecemos los puntos de medida: que queremos medir (TODO)
            * - Iniciamos el servidor oml
            *
            *
            * Definición de los Measure Points:
            * ================================
            *
            * video_format = {timestamp, mimetype, bitrate, resolution, fps}
            * video_dropped_frames = {timestamp, count}
            * video_events = {timestamp, event (enabled/disabled/loadDRMkeys...)}
            *
            * audio_events = {timestamp, event (enabled/disabled/loadDRMkeys...)}
            * audio_format = {timestamp, mimetype, bitrate, channels, sample_rate, language}
            *
            * */
            OMLBase oml = new OMLBase("ExoPlayer", experiment_name, "exoplayer", "tcp:94.177.232.57:3003");
            ArrayList<OmlMP> measurePoints = new ArrayList<>();

            // video_format
            ArrayList<OMLMPFieldDef> videoFormat = new ArrayList<>();
            videoFormat.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            videoFormat.add(new OMLMPFieldDef("mimetype", OMLTypes.OML_STRING_VALUE));
            videoFormat.add(new OMLMPFieldDef("bitrate", OMLTypes.OML_STRING_VALUE));
            videoFormat.add(new OMLMPFieldDef("resolution", OMLTypes.OML_STRING_VALUE));
            videoFormat.add(new OMLMPFieldDef("fps", OMLTypes.OML_STRING_VALUE));
            OmlMP video_format = new OmlMP(videoFormat);

            // video_dropped
            ArrayList<OMLMPFieldDef> videoDropped = new ArrayList<>();
            videoDropped.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            videoDropped.add(new OMLMPFieldDef("count", OMLTypes.OML_STRING_VALUE));
            OmlMP video_dropped = new OmlMP(videoDropped);

            // video_events
            ArrayList<OMLMPFieldDef> videoEvents = new ArrayList<>();
            videoEvents.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            videoEvents.add(new OMLMPFieldDef("event", OMLTypes.OML_STRING_VALUE));
            OmlMP video_events = new OmlMP(videoEvents);

            // audio_format
            ArrayList<OMLMPFieldDef> audioFormat = new ArrayList<>();
            audioFormat.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            audioFormat.add(new OMLMPFieldDef("mimetype", OMLTypes.OML_STRING_VALUE));
            audioFormat.add(new OMLMPFieldDef("bitrate", OMLTypes.OML_STRING_VALUE));
            audioFormat.add(new OMLMPFieldDef("channels", OMLTypes.OML_STRING_VALUE));
            audioFormat.add(new OMLMPFieldDef("sample_rate", OMLTypes.OML_STRING_VALUE));
            audioFormat.add(new OMLMPFieldDef("language", OMLTypes.OML_STRING_VALUE));
            OmlMP audio_format = new OmlMP(audioFormat);

            // audio_track_underrun
            // bufferSize - The size of the track's buffer, in bytes.
            // bufferSizeMs - The size of the track's buffer, in milliseconds,
            //    if it is configured for PCM output. C.TIME_UNSET if it is configured for passthrough
            //    output, as the buffered media can have a variable bitrate so the duration may be
            //    unknown.
            // elapsedSinceLastFeedMs - The time since the track was last fed data, in milliseconds.
            ArrayList<OMLMPFieldDef> audioTrackUnderrun = new ArrayList<>();
            audioTrackUnderrun.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            audioTrackUnderrun.add(new OMLMPFieldDef("buffer_size", OMLTypes.OML_STRING_VALUE));
            audioTrackUnderrun.add(new OMLMPFieldDef("buffer_size_ms", OMLTypes.OML_STRING_VALUE));
            OmlMP audio_track_underrun = new OmlMP(audioTrackUnderrun);

            // audio_events
            ArrayList<OMLMPFieldDef> audioEvents = new ArrayList<>();
            audioEvents.add(new OMLMPFieldDef("timestamp", OMLTypes.OML_STRING_VALUE));
            audioEvents.add(new OMLMPFieldDef("event", OMLTypes.OML_STRING_VALUE));
            OmlMP audio_events = new OmlMP(audioEvents);

            // Add schema
            oml.addmp("video_format", video_format);
            oml.addmp("video_dropped", video_dropped);
            oml.addmp("video_events", video_events);
            oml.addmp("audio_format", audio_format);
            oml.addmp("audio_track_underrun", audio_track_underrun);
            oml.addmp("audio_events", audio_events);

            oml.start();

            Log.d("[LogService]", "OML Server started");

            measurePoints.add(video_format); // measure_points[0] measure_points.get(0)
            measurePoints.add(video_dropped); // measure_points[1] measure_points.get(1)
            measurePoints.add(video_events); // measure_points[2] measure_points.get(2)
            measurePoints.add(audio_format); // measure_points[3] measure_points.get(3)
            measurePoints.add(audio_track_underrun); // measure_points[4] measure_points.get(4)
            measurePoints.add(audio_events); // measure_points[4] measure_points.get(5)

            return measurePoints;
        }

    }
}
