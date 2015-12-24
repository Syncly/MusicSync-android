package ee.arti.musicsync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SyncService extends Service {

    private static final String TAG = "SyncService";

    public static final String NOTIFICATION = "ee.arti.musicsync.service.SyncService";

    public static final String ACTION_START = "service.start";
    public static final String ACTION_STOP = "service.stop";
    public static final String ACTION_UPDATE_SETTINGS = "service.update_settings";
    public static final String ACTION_GET_PLAYLISTS = "service.get_playlists";
    public static final String ACTION_GET_SONGS = "service.get_songs";

    private boolean isRunning  = false;
    private Thread tEvents;

    // Settings
    private SharedPreferences SP;
    private String server;


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

    static public void updateSettings(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_UPDATE_SETTINGS);
        context.startService(intent);
    }

    static public void getPlaylists(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_PLAYLISTS);
        context.startService(intent);
    }


    public class HttpGet implements Runnable {

        private Context context;
        private String action;

        public HttpGet(Context context, String action) {
            this.context = context;
            this.action = action;
        }

        public HttpGet(Context context) {
            this.context = context;
        }

        public void run() {
            String addr = "";
            try {
                if (ACTION_GET_PLAYLISTS.equals(action)) {
                    addr = server+"/playlists";
                }
                URL url = new URL(addr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "HTTP response: " + urlConnection.getResponseCode());
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                Log.d(TAG, "Reading stream");
                responseParser(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Error on url openConnection: "+e.getMessage());
                e.printStackTrace();
            }
            Log.d(TAG, "thread has ended its code");
        }

        public void responseParser(InputStream inputStream) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            sendResponse(sb.toString());
        }

        public void sendResponse(String resp) {
            if (ACTION_GET_PLAYLISTS.equals(action)) {
                Intent intent = new Intent();
                intent.setAction(NOTIFICATION);
                intent.putExtra("action", this.action);

                try {
                    JSONArray jspls = new JSONArray(resp);

                    ArrayList<HashMap> ba = new ArrayList<>();
                    for (int i = 0; i < jspls.length(); i++) {
                        JSONObject jspl = jspls.getJSONObject(i);
                        HashMap<String, String> p = new HashMap();
                        p.put(PlaylistsActivity.TAG_PLAYLIST_STATUS, jspl.optString("status", "OK"));
                        p.put(PlaylistsActivity.TAG_PLAYLIST_TITLE, jspl.getString("title"));
                        p.put(PlaylistsActivity.TAG_PLAYLIST_ID, jspl.getString("_id"));
                        ba.add(p);
                    }
                    intent.putExtra("data", ba);
                    context.sendBroadcast(intent);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_GET_SONGS.equals(action)) {
                Log.d(TAG, ACTION_GET_PLAYLISTS + " Not implemented");
            }
        }
    }

    public class Events implements Runnable {

        Context context;

        public Events(Context context) {
            this.context = context;
        }

        public void run() {
            Log.d(TAG+"Events", "Ran the thread, server: "+ server);
        }
    }

    private void getSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // get the settings
        SP = PreferenceManager.getDefaultSharedPreferences(this);
        // load settings into local variables with defaults
        server = SP.getString("server", getResources().getString(R.string.default_server));
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;
        getSettings();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");

        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_START.equals(action)) {
                Log.d(TAG, "ACTION_START");
                if (tEvents == null || !tEvents.isAlive()) {
                    tEvents = new Thread(new Events(SyncService.this));
                    Log.d(TAG, "Starting tEvents");
                    tEvents.start();
                } else {
                    Log.d(TAG, "tEvents already running not starting a new one");
                }
            } else if (ACTION_STOP.equals(action)) {
                stopSelf();
            } else if (ACTION_UPDATE_SETTINGS.equals(action)) {
                getSettings();
            } else if (ACTION_GET_PLAYLISTS.equals(action)) {
                new Thread(new HttpGet(SyncService.this, action)).start();
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