package ee.arti.musicsync;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PlaylistsActivity extends AppCompatActivity {

    private static final String TAG = "PlaylistsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playilist_list);
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
