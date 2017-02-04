package com.google.android.exoplayer2.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.demo.SampleChooserActivity;
import com.google.android.exoplayer2.demo.PlayerActivity;


// Tenemos que llamar a esta funcion:
/*
private void onSampleSelected(Sample sample) {
    PlayerActivity.VIDEO_SOURCE = sample.uri;
    Intent mpdIntent = new Intent(this, PlayerActivity.class)
    .setData(Uri.parse(sample.uri))
    .putExtra(PlayerActivity.CONTENT_ID_EXTRA, sample.contentId)
    .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, sample.type)
    .putExtra(PlayerActivity.PROVIDER_EXTRA, sample.provider);
    startActivity(mpdIntent);
    }
*/
public class AutomationReceiver extends BroadcastReceiver {

    /*
    * Parametros del BroadcastReceiver
    *
    * - COMMAND_INTENT: esto creo que lo puedo quitar
    * - EXTRA_COMMAND: esto es la accion a realizar (reproducir, reproducir x2, parar, etc)
    * - VIDEO_SOURCE: la url donde esta alojado el video
    * - EXTRA_PARAMS: esto creo que lo puedo quitar
    * - DRM_SCHEME_UUID_EXTRA: parametros necesarios para videos con drm
    * - DRM_LICENSE_URL: parametros necesarios para videos con drm
    * - DRM_KEY_REQUEST_PROPERTIES: parametros necesarios para videos con drm
    * - PREFER_EXTENSION_DECODERS: parametros necesarios para videos con drm
    * - DBNAME: nombre de la base de datos que va a crear
    *
    * */
    public static final String COMMAND_INTENT = "COMMAND";
    public static final String DBNAME = "DBNAME";
    public static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    public static final String VIDEO_SOURCE = "VIDEO_SOURCE";
    public static final String EXTRA_PARAMS = "EXTRA_PARAMS";
    public static final String DRM_SCHEME_UUID_EXTRA = "DRM_SCHEME_UUID_EXTRA";
    public static final String DRM_LICENSE_URL = "DRM_LICENSE_URL";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "DRM_KEY_REQUEST_PROPERTIES";
    public static final String PREFER_EXTENSION_DECODERS = "PREFER_EXTENSION_DECODERS";
    public static final String EXTENSION_EXTRA = "EXTENSION_EXTRA";
    private static final String TAG = "AutomationReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        Log.d(TAG, "onReceive executed!");
        if (action.equals(COMMAND_INTENT)) {
            String command = intent.getStringExtra(EXTRA_COMMAND); // el comando ej: PLAY:VID:INI
            if (command != null) {
                Log.d(TAG, "Processing command...");
                // Procesamos los parametros opcionales
                String drm_scheme_uuid = null;
                String drm_license_url = null;
                String drm_key_request_properties = null;
                String prefer_extension_decoders = null;
                String dbname = null;

                if (intent.getStringExtra(DRM_SCHEME_UUID_EXTRA) != null) { drm_scheme_uuid = intent.getStringExtra(DRM_SCHEME_UUID_EXTRA); }
                if (intent.getStringExtra(DRM_LICENSE_URL) != null) { drm_license_url = intent.getStringExtra(DRM_LICENSE_URL); }
                if (intent.getStringExtra(DRM_KEY_REQUEST_PROPERTIES) != null) { drm_key_request_properties = intent.getStringExtra(DRM_KEY_REQUEST_PROPERTIES); }
                if (intent.getStringExtra(PREFER_EXTENSION_DECODERS) != null) { prefer_extension_decoders = intent.getStringExtra(PREFER_EXTENSION_DECODERS); }
                if (intent.getStringExtra(DBNAME) != null) { dbname = intent.getStringExtra(DBNAME); }
                String video_source = intent.getStringExtra(VIDEO_SOURCE);
                processCommand(command, video_source, dbname, drm_scheme_uuid, drm_license_url, drm_key_request_properties, prefer_extension_decoders);
            }
        }
    }

    // Estos son las funciones que se ejecutan con cada comando.
    // La sitaxis de los comandos es SCAPY.
    private void processCommand(String command,
                                String video_source,
                                String dbname,
                                String drm_sheme_uuid,
                                String drm_license_url,
                                String drm_key_request_properties,
                                String prefer_extension_decoders) {
        if (command.equalsIgnoreCase("PLAY:VID:INI")) {
            playVideo(video_source, dbname, drm_sheme_uuid, drm_license_url, drm_key_request_properties, prefer_extension_decoders);
            Log.d(TAG, "Command match: PLAY:VID:INI");
        } else if (command.equalsIgnoreCase("PLAY:VID:5:MIN")) {
            playVideoTo(video_source, 5);
            Log.d(TAG, "Command match: PLAY:VID:5:MIN");
        } else if (command.equalsIgnoreCase("STOP:VID")) {
            stopVideo();
            Log.d(TAG, "Command match: STOP:VID");
        }
    }

    // Estas son las funciones que se ejecutan cuando se recibe cierto comando
    public void playVideo(String video_source,
                          String dbname,
                          String drm_sheme_uuid,
                          String drm_license_url,
                          String drm_key_request_properties,
                          String prefer_extension_decoders) {
        Log.d(TAG, "Play video!");
        Intent mpdIntent = new Intent(mContext, PlayerActivity.class);
        mpdIntent.setData(Uri.parse(video_source));
        if (dbname != null) { mpdIntent.putExtra(PlayerActivity.DBNAME, dbname); }
        if (drm_sheme_uuid != null) { mpdIntent.putExtra(PlayerActivity.DRM_SCHEME_UUID_EXTRA, drm_sheme_uuid); }
        if (drm_license_url != null) { mpdIntent.putExtra(PlayerActivity.DRM_LICENSE_URL, drm_license_url); }
        if (drm_key_request_properties != null) { mpdIntent.putExtra(PlayerActivity.DRM_KEY_REQUEST_PROPERTIES, drm_key_request_properties); }
        if (prefer_extension_decoders != null) { mpdIntent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS, prefer_extension_decoders); }
        mpdIntent.setAction("com.google.android.exoplayer.demo.action.VIEW");
        mContext.startActivity(mpdIntent);
    }

    public void playVideoTo(String video_source, int time) {
        Log.d(TAG, "Play video for 5 minutes!");
        /*Intent mpdIntent = new Intent(mContext, PlayerActivity.class)
                .setData(Uri.parse(video_source))
                .putExtra(PlayerActivity.CONTENT_ID_EXTRA, "0894c7c8719b28a0")
                .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, Util.TYPE_DASH)
                .putExtra(PlayerActivity.PROVIDER_EXTRA, "widevine_test")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mpdIntent);*/
    }

    public void stopVideo() {
        Log.d(TAG, "Stop video!");
        Intent intent = new Intent(mContext, SampleChooserActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //llamamos a la actividad
        mContext.startActivity(intent);
    }

}