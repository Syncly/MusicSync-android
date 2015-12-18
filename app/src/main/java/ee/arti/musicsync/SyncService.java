package ee.arti.musicsync;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import ee.arti.musicsync.SyncServiceResultReceiver.Receiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncService extends IntentService {

    // logger tag
    private static final String TAG = "SyncService";
    private static final String TAG_EVENTS = TAG+"Events";

    // Actions provided by this service
    private static final String ACTION_GET_PLAYLISTS = "ee.arti.musicsync.action.GET_PLAYLISTS";
    private static final String ACTION_GET_SONGS = "ee.arti.musicsync.action.GET_SONGS";
    private static final String ACTION_GET_EVENTS = "ee.arti.musicsync.action.GET_EVENTS";

    // Params
    // Callback
    private static final String EXTRA_RESULT = "ee.arti.musicsync.extra.RESULT";
    // Api server to talk to
    private static final String EXTRA_SERVER = "ee.arti.musicsync.extra.SERVER";
    // Playlist id to use
    private static final String EXTRA_PLAYLIST = "ee.arti.musicsync.extra.PLAYLIST";

    private static final int STATUS_OK = 0;
    private static final int STATUS_ERROR = 1;

    public static SyncServiceResultReceiver receiver;

    private static RequestQueue requestQueue;

    public SyncService() {
        super("SyncService");
        requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetPlaylists(Context context, String server) {
        Intent intent = new Intent(context, SyncService.class);
        receiver = new SyncServiceResultReceiver(new Handler());
        receiver.setReceiver((Receiver) context);
        intent.putExtra(EXTRA_RESULT, receiver);
        intent.setAction(ACTION_GET_PLAYLISTS);
        intent.putExtra(EXTRA_SERVER, server);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetSongs(Context context, String server, String playlist) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_SONGS);
        receiver = new SyncServiceResultReceiver(new Handler());
        receiver.setReceiver((Receiver) context);
        intent.putExtra(EXTRA_RESULT, receiver);
        intent.putExtra(EXTRA_SERVER, server);
        intent.putExtra(EXTRA_PLAYLIST, playlist);
        context.startService(intent);
    }

    public static void startActionGetEvents(Context context, String server) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_GET_EVENTS);
        receiver = new SyncServiceResultReceiver(new Handler());
        receiver.setReceiver((Receiver) context);
        intent.putExtra(EXTRA_RESULT, receiver);
        intent.putExtra(EXTRA_SERVER, server);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RESULT);
            final String server = intent.getStringExtra(EXTRA_SERVER);
            final String action = intent.getAction();
            if (ACTION_GET_PLAYLISTS.equals(action)) {
                handleActionGetPlaylists(receiver, server);
            } else if (ACTION_GET_SONGS.equals(action)) {
                final String playlist = intent.getStringExtra(EXTRA_SERVER);
                handleActionGetSongs(receiver, server, playlist);
            } else if (ACTION_GET_EVENTS.equals(action)) {
                try {
                    handleActionGetEvents(receiver, server);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get playlists in a background thread
     */
    private void handleActionGetPlaylists(final ResultReceiver receiver, String server) {
        Log.d(TAG, "GetPlaylists " + server);

        JsonArrayRequest playlistsReq = new JsonArrayRequest(Request.Method.GET, server + "playlists", null,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray resp) {
                    Log.d(TAG, "Got playlists in sync service");
                    Bundle respBundle = new Bundle();
                    respBundle.putString("playlists", resp.toString());
                    receiver.send(STATUS_OK, respBundle);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

            }
        });
        requestQueue.add(playlistsReq);
    }

    /**
     * Get songs in a playlist
     */
    private void handleActionGetSongs(ResultReceiver receiver, String server, String playlist) {
        Log.d(TAG, "GetSongs " + server + " " + playlist);
    }

    private void handleActionGetEvents(ResultReceiver receiver, String server) throws IOException {
        Log.d(TAG_EVENTS, "Start");
        while (true) {
            URL url = new URL(server+"sub/event");
            Log.d(TAG_EVENTS, "Connect");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Log.d(TAG_EVENTS, "buffer reader");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            Log.d(TAG_EVENTS, "read lines");
            while ((line = rd.readLine()) != null) {
                Log.d(TAG_EVENTS, line);
            }
        }
    }
}
