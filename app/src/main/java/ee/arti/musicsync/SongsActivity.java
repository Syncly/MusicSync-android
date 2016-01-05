package ee.arti.musicsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ee.arti.musicsync.backend.SyncService;
import ee.arti.musicsync.backend.HttpGet;

public class SongsActivity extends AppCompatActivity {

    private static final String TAG = "SongsActivity";
    public static final String TAG_SONG_TITLE = "title";
    public static final String TAG_SONG_STATUS = "status";
    public static final String TAG_SONG_ID = "_id";
    public static final String TAG_PLAYLIST_ID = "playlist";

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

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString("message");
                //Toast.makeText(PlaylistsActivity.this, string, Toast.LENGTH_LONG).show();
                if (bundle.containsKey("action") && !bundle.containsKey("error")) {
                    Log.d(TAG, "action " + bundle.get("action"));
                    ArrayList<HashMap> pl = (ArrayList) bundle.getSerializable("data");
                    songs.clear();
                    for (HashMap<String, String> el : pl) {
                        songs.add(el);
                    }
                    adapter.notifyDataSetChanged();
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
            if (bundle != null) {
                if (bundle.containsKey("error")) {
                    setContentView(R.layout.acivity_error);
                    TextView terr = (TextView)findViewById(R.id.errorMessage);
                    terr.setText(bundle.getString("error"));
                    swipeContainer.setRefreshing(false);
                }
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(receiver, new IntentFilter(HttpGet.RESPONSE_SUCCESS));
        registerReceiver(responseError, new IntentFilter(HttpGet.RESPONSE_ERROR));
        SyncService.startService(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(receiver);
        unregisterReceiver(responseError);
    }

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

        // adapter for showing the list of songs
        adapter=new SimpleAdapter(this, songs, R.layout.playlist_element,
                new String[] {TAG_SONG_TITLE, TAG_SONG_STATUS},
                new int[] {R.id.name, R.id.status});

        // setup song view list
        songsListView = (ListView) findViewById(R.id.playlistsList);
        songsListView.setAdapter(adapter);
        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text or do whatever you need.
                HashMap<String, String> song = songs.get(position);
                Log.d(TAG, song.get(TAG_SONG_TITLE));
                Toast.makeText(getApplicationContext(), song.get(TAG_SONG_TITLE), Toast.LENGTH_SHORT).show();
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // On swipedown redownload the song list
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refresh");
                SyncService.getSongs(getApplicationContext(), playlist_id);
            }
        });

        SyncService.getSongs(this, playlist_id);
        showSpinner();
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
