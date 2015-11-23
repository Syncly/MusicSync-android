package ee.arti.musicsync;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistsActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistsActivity";
    private static final String TAG_PLAYLIST_NAME = "name";
    private static final String TAG_PLAYLIST_STATUS = "status";
    private static final String TAG_PLAYLIST_DEBUG = "debug_data";

    // contains all the playlists
    ArrayList<HashMap<String, String>> playlists = new ArrayList<HashMap<String, String>>();;
    // converts array list to a suitable format for ListView
    SimpleAdapter adapter;
    // playlists list view
    private ListView playlistsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        HashMap<String, String> plitem = new HashMap<String, String>();
        // add some dummy data
        plitem.put(TAG_PLAYLIST_NAME, "Music I like");
        plitem.put(TAG_PLAYLIST_DEBUG, "youtube:PLB5VrND_o3PgZNzNdohFDWE5BTFIPDImQ");
        plitem.put(TAG_PLAYLIST_STATUS, "Syncing 0/136");
        playlists.add(plitem);

        // Create the adapter that will show our playlists in a scrollable list view
        adapter=new SimpleAdapter(this, playlists, R.layout.playlist_element,
                new String[] {TAG_PLAYLIST_NAME, TAG_PLAYLIST_DEBUG, TAG_PLAYLIST_STATUS},
                new int[] {R.id.name, R.id.debug_data, R.id.status});

        playlistsListView = (ListView) findViewById(R.id.playlistsList);
        playlistsListView.setAdapter(adapter);

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
}
