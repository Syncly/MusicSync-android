package ee.arti.musicsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistsActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistsActivity";
    private static final String TAG_PLAYLIST_TITLE = "title";
    private static final String TAG_PLAYLIST_STATUS = "status";
    private static final String TAG_PLAYLIST_ID = "_id";

    // contains all the playlists
    ArrayList<HashMap<String, String>> playlists = new ArrayList<HashMap<String, String>>();;
    // converts array list to a suitable format for ListView
    SimpleAdapter adapter;
    // playlists list view
    private ListView playlistsListView;

    private SwipeRefreshLayout swipeContainer;

    // settings object
    private SharedPreferences SP;

    // internet suff
    String server;
    String api_key;
    // volley
    RequestQueue queue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        // get app settings
        getSettings();

         // Create the adapter that will show our playlists in a scrollable list view
        adapter=new SimpleAdapter(this, playlists, R.layout.playlist_element,
                new String[] {TAG_PLAYLIST_TITLE, TAG_PLAYLIST_STATUS},
                new int[] {R.id.name, R.id.status});

        // Setup playlist viewer list
        playlistsListView = (ListView) findViewById(R.id.playlistsList);
        playlistsListView.setAdapter(adapter);
        playlistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text or do whatever you need.
                HashMap<String, String> playlist = playlists.get(position);
                Log.d(TAG, "Showing songs in " + playlist.get(TAG_PLAYLIST_TITLE));
                Intent showPlaylist = new Intent(getApplicationContext(), PlaylistListActivity.class);
                showPlaylist.putExtra(TAG_PLAYLIST_TITLE, playlist.get(TAG_PLAYLIST_TITLE));
                showPlaylist.putExtra(TAG_PLAYLIST_ID, playlist.get(TAG_PLAYLIST_ID));
                startActivity(showPlaylist);
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // On swipedown redownload playlists
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refresh");
                getPLaylists();
            }
        });


        queue = Volley.newRequestQueue(this);

        getPLaylists();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        getSettings(); // settings may have changed meantime
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "Launching SettingsActivity from toolbar settings button");
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void getPLaylists() {
        showSpinner();
        JsonArrayRequest playlistsReq = new JsonArrayRequest(Request.Method.GET, server + "playlists", null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray resp) {
                        Log.d(TAG, "Got playlists");
                        playlists.clear();
                        for(int i = 0; i < resp.length(); i++) {
                            try {
                                JSONObject playlist = resp.getJSONObject(i);
                                HashMap<String, String> plitem = new HashMap<String, String>();
                                // add some dummy data
                                plitem.put(TAG_PLAYLIST_TITLE, playlist.getString(TAG_PLAYLIST_TITLE));
                                plitem.put(TAG_PLAYLIST_ID, playlist.getString(TAG_PLAYLIST_ID));
                                plitem.put(TAG_PLAYLIST_STATUS, "OK");
                                playlists.add(plitem);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        swipeContainer.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
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
