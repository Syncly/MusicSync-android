package ee.arti.musicsync;

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

    public static final String EXTRA_PLAYLIST_ID = "service.playlist_id";

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

    static public void getSongs(Context context, String playlist_id) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_SONGS);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlist_id);
        context.startService(intent);
    }


    public class HttpGet implements Runnable {

        private Context context;
        private String action;
        private String playlist_id;

        public HttpGet(Context context, String action, String playlist_id) {
            this.context = context;
            this.action = action;
            this.playlist_id = playlist_id;
        }

        public HttpGet(Context context, String action) {
            this.context = context;
            this.action = action;
        }

        public HttpGet(Context context) {
            this.context = context;
        }

        public void run() {
            String addr = "";
            if (ACTION_GET_PLAYLISTS.equals(action)) {
                addr = server+"/playlists";
            } else if (ACTION_GET_SONGS.equals(action)) {
                addr = server+"/playlists/"+playlist_id+"/songs";
            }
            Log.d(TAG, "Reading stream");
            responseParser(get(addr));
            Log.d(TAG, "thread has ended its code");
        }

        public InputStream get(String addr) {
            try {
                URL url = new URL(addr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "HTTP response: " + urlConnection.getResponseCode());
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                return inputStream;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                sendError("Bad backend server URL");
            } catch (IOException e) {
                Log.e(TAG, "Error on url openConnection: " + e.getMessage());
                e.printStackTrace();
                sendError("Server connection failed: "+e.getMessage());
            }
            return null;
        }

        public void sendError(String message) {
            Intent intent = new Intent();
            intent.setAction(NOTIFICATION);
            intent.putExtra("action", this.action);
            intent.putExtra("error", message);
            context.sendBroadcast(intent);
        }

        public void responseParser(InputStream inputStream) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                sendResponse(sb.toString());
            } catch (IOException|NullPointerException e) {
                e.printStackTrace();
                sendError("Response reading failed "+ e.getMessage());
            }
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
                Intent intent = new Intent();
                intent.setAction(NOTIFICATION);
                intent.putExtra("action", this.action);

                try {
                    JSONArray jssongs = new JSONArray(resp);

                    ArrayList<HashMap> ba = new ArrayList<>();
                    for (int i = 0; i < jssongs.length(); i++) {
                        JSONObject jssong = jssongs.getJSONObject(i);
                        HashMap<String, String> p = new HashMap();
                        p.put(PlaylistListActivity.TAG_SONG_STATUS, jssong.optString("status", "OK"));
                        p.put(PlaylistListActivity.TAG_SONG_TITLE, jssong.getString("title"));
                        p.put(PlaylistListActivity.TAG_SONG_ID, jssong.getString("_id"));
                        ba.add(p);
                    }
                    intent.putExtra("data", ba);
                    context.sendBroadcast(intent);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
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
            Log.d(TAG, "Start Action: "+ action);
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
                Log.d(TAG, "updateSettings, server: "+ server);
                getSettings();
                Log.d(TAG, "updateSettings after, server: " + server);
            } else if (ACTION_GET_PLAYLISTS.equals(action)) {
                new Thread(new HttpGet(SyncService.this, action)).start();
            } else if (ACTION_GET_SONGS.equals(action)) {
                new Thread(new HttpGet(SyncService.this, action, intent.getStringExtra(EXTRA_PLAYLIST_ID))).start();
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