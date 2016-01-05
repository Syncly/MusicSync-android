package ee.arti.musicsync.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import ee.arti.musicsync.PlaylistsActivity;
import ee.arti.musicsync.R;

public class SyncService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SyncService";

    public static final String ACTION_START = "service.start";
    public static final String ACTION_STOP = "service.stop";
    public static final String ACTION_GET_PLAYLISTS = "service.get_playlists";
    public static final String ACTION_GET_SONGS = "service.get_songs";

    public static final String EXTRA_PLAYLIST_ID = "service.playlist_id";

    public boolean isRunning  = false;
    private Thread tEvents;

    // Settings
    private SharedPreferences sharedPreferences;
    private String server;

    public static final String KEY_SETTINGS_SERVER = "server";


    static public void startService(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    static public void stopService(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    static public void getPlaylists(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_PLAYLISTS);
        context.startService(intent);
    }

    static public void getSongs(Context context, String playlist_id) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_SONGS);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlist_id);
        context.startService(intent);
    }



    private void getSettings() {
        onSharedPreferenceChanged(sharedPreferences, KEY_SETTINGS_SERVER);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG+"SettngsChnge", key);
        if (key.equals(KEY_SETTINGS_SERVER)) {
            server = sharedPreferences.getString(KEY_SETTINGS_SERVER, getResources().getString(R.string.default_server));
            Log.d(TAG+"SettngsChnge", server);
        }
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getSettings();  // load the values from sharedPreferences
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "Start Action: "+ action);
            if (ACTION_START.equals(action)) {
                Log.d(TAG, "ACTION_START");
                if (tEvents == null || !tEvents.isAlive()) {
                    tEvents = new Thread(new Events(SyncService.this, server));
                    Log.d(TAG, "Starting tEvents");
                    tEvents.start();
                } else {
                    Log.d(TAG, "tEvents already running not starting a new one");
                }
            } else if (ACTION_STOP.equals(action)) {
                stopSelf();
            } else if (ACTION_GET_PLAYLISTS.equals(action)) {
                new Thread(new GetPlaylists(SyncService.this, server)).start();
            } else if (ACTION_GET_SONGS.equals(action)) {
                new Thread(new GetSongs(SyncService.this, server, intent.getStringExtra(EXTRA_PLAYLIST_ID))).start();
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
        isRunning = false;
        try {
            tEvents.interrupt();
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
