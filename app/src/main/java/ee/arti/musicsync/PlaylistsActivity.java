package ee.arti.musicsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.ArrayList;
import java.util.HashMap;

import ee.arti.musicsync.backend.Events;
import ee.arti.musicsync.backend.HttpGet;
import ee.arti.musicsync.backend.SyncService;

public class PlaylistsActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistsActivity";
    public static final String TAG_PLAYLIST_TITLE = "title";
    public static final String TAG_PLAYLIST_STATUS = "status";
    public static final String TAG_PLAYLIST_ID = "_id";

    // contains all the playlists
    ArrayList<HashMap<String, String>> playlists = new ArrayList<HashMap<String, String>>();;
    // converts array list to a suitable format for ListView
    SimpleAdapter adapter;

    private SwipeRefreshLayout swipeContainer;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString("message");
                //Toast.makeText(PlaylistsActivity.this, string, Toast.LENGTH_LONG).show();
                if (bundle.containsKey("action") && !bundle.containsKey("error")){
                    Log.d(TAG, "action " + bundle.get("action"));
                    ArrayList<HashMap> pl = (ArrayList)bundle.getSerializable("data");
                    playlists.clear();
                    for (HashMap<String, String> el: pl) {
                        playlists.add(el);
                    }
                    adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                } else if (bundle.containsKey("error")) {
                    setContentView(R.layout.acivity_error);
                    TextView terr = (TextView)findViewById(R.id.errorMessage);
                    terr.setText(bundle.getString("error"));
                    swipeContainer.setRefreshing(false);
                }
            }
        }
    };

    private BroadcastReceiver responseError = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "responseError");
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("error") && bundle.containsKey("action")) {
                if (bundle.getString("action").equals(Events.ACTION)) {
                    Toast.makeText(PlaylistsActivity.this, "Error: "+bundle.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    setContentView(R.layout.acivity_error);
                    TextView terr = (TextView) findViewById(R.id.errorMessage);
                    terr.setText(bundle.getString("error"));
                    swipeContainer.setRefreshing(false);
                }
                Log.d(TAG, "responseError, action: "+bundle.getString("action") + " error: " + bundle.getString("error"));
            }
        }
    };

    private BroadcastReceiver responseEvent= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d(TAG, "responseEvent, make toast, event: "+bundle.getString("event"));
                Toast.makeText(PlaylistsActivity.this, "Event: "+bundle.getString("event"), Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(receiver, new IntentFilter(HttpGet.RESPONSE_SUCCESS));
        registerReceiver(responseError, new IntentFilter(HttpGet.RESPONSE_ERROR));
        registerReceiver(responseEvent, new IntentFilter(Events.RESPONSE_EVENT));
        SyncService.startService(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(receiver);
        unregisterReceiver(responseError);
        unregisterReceiver(responseEvent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_playlists);

         // Create the adapter that will show our playlists in a scrollable list view
        adapter=new SimpleAdapter(this, playlists, R.layout.playlist_element,
                new String[] {TAG_PLAYLIST_TITLE, TAG_PLAYLIST_STATUS},
                new int[] {R.id.name, R.id.status});

        // Setup playlist viewer list
        ListView playlistsListView = (ListView) findViewById(R.id.playlistsList);
        playlistsListView.setAdapter(adapter);
        playlistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text or do whatever you need.
                HashMap<String, String> playlist = playlists.get(position);
                Log.d(TAG, "Showing songs in " + playlist.get(TAG_PLAYLIST_TITLE));
                Intent showPlaylist = new Intent(getApplicationContext(), SongsActivity.class);
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
                SyncService.getPlaylists(getApplicationContext());
            }
        });

        SyncService.getPlaylists(this);
        showSpinner();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        SyncService.startService(this);
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

    private void showSpinner() {
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
            }
        });
    }
}
