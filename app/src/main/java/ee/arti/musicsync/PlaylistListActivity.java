package ee.arti.musicsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistListActivity extends AppCompatActivity {

    private static final String TAG = "SongsActivity";
    private static final String TAG_SONG_TITLE = "title";
    private static final String TAG_SONG_STATUS = "status";
    private static final String TAG_SONG_ID = "_id";
    private static final String TAG_PLAYLIST_ID = "playlist";

    private String title; // Playlist name shown as this activitys title
    private String playlist_id; //

    // contains all the playlists
    private ArrayList<HashMap<String, String>> songs = new ArrayList<>();;
    // converts array list to a suitable format for ListView
    private SimpleAdapter adapter;
    // playlists list view
    private ListView songsListView;

    // updates spinner
    private SwipeRefreshLayout swipeContainer;

    // settings object
    private SharedPreferences SP;

    // internet suff
    private String server;
    private String api_key;

    // volley
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);


        Intent intent = getIntent();

        // Set a title for this activity
        String title;
        if ((title = intent.getStringExtra("title")) == null) {
            title = "";
        }
        getSupportActionBar().setTitle(title);

        playlist_id = intent.getStringExtra("_id");

        getSettings();

        // adapter for showing the list of songs
        adapter=new SimpleAdapter(this, songs, R.layout.playlist_element,
                new String[] {TAG_SONG_TITLE, TAG_SONG_STATUS},
                new int[] {R.id.name, R.id.status});

        // setup song view list
        songsListView = (ListView) findViewById(R.id.playlistsList);
        songsListView.setAdapter(adapter);
        songsListView.setClickable(true);
        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text or do whatever you need.
                HashMap<String, String> song = songs.get(position);
                Log.d(TAG, song.get(TAG_SONG_TITLE));
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // On swipedown redownload the song list
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refresh");
                getSongs();
            }
        });

        queue = Volley.newRequestQueue(this);

        getSongs();
    }

    private void getSongs() {
        showSpinner();
        JsonArrayRequest playlistsReq = new JsonArrayRequest(
                Request.Method.GET, server + "playlists/"+playlist_id+"/songs",
                null, // params to send to the server
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray resp) {
                        //Log.d(TAG, resp.toString());
                        for(int i = 0; i < resp.length(); i++) {
                            try {
                                JSONObject song = resp.getJSONObject(i);
                                HashMap<String, String> plitem = new HashMap<String, String>();
                                //
                                plitem.put(TAG_SONG_TITLE, song.getString("title"));
                                if (song.has(TAG_SONG_STATUS)) {
                                    plitem.put(TAG_SONG_STATUS, song.getString("status"));
                                } else {
                                    plitem.put(TAG_SONG_STATUS, "Not synced");
                                }
                                plitem.put(TAG_SONG_ID, song.getString("_id"));
                                songs.add(plitem);
                                adapter.notifyDataSetChanged();
                                swipeContainer.setRefreshing(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                swipeContainer.setRefreshing(false);
                            }

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                        swipeContainer.setRefreshing(false);
                    }
                });
        queue.add(playlistsReq);
    }

    private void showSpinner() {
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
            }
        });
    }

    private void getSettings() {
        // Loads defaults from the settings ui file
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // get the settings
        SP = PreferenceManager.getDefaultSharedPreferences(this);
        // load settings into local variables with defaults
        server = SP.getString("server", getResources().getString(R.string.default_server));
        api_key = SP.getString("api_key", "");
    }
}
