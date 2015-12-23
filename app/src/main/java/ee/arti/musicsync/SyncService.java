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

import ee.arti.musicsync.SyncServiceResultReceiver.Receiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SyncService extends IntentService {

    // logger tag
    private static final String TAG = "SyncService";

    // Actions provided by this service
    private static final String ACTION_GET_PLAYLISTS = "ee.arti.musicsync.action.GET_PLAYLISTS";
    private static final String ACTION_GET_SONGS = "ee.arti.musicsync.action.GET_SONGS";

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
}
