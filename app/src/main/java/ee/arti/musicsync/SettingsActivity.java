package ee.arti.musicsync;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private static final int ACTION_SELECT_STORAGE_PATH = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inserts the default android settings fragments into the settings layout
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference prefS) {
            if (prefS == (Preference)prefScreen.findPreference("storage_path")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, ACTION_SELECT_STORAGE_PATH);
            }
            return false;
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData){
            Log.d(TAG, "ActivityResult");
            if (requestCode == ACTION_SELECT_STORAGE_PATH){
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, resultData.getData().toString());
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d(TAG, "Result canceled");
                }
            }
        }
    }
}
