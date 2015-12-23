package ee.arti.musicsync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 */
public class SyncEventsService extends IntentService {
    // logger tag
    private static final String TAG = "SyncEventsService";
    private static final String TAG_EVENTS = TAG+"Events";

    // settings object
    private SharedPreferences SP;

    // internet suff
    String server;

    public SyncEventsService() {
        super("SyncEventsService");
    }

    public static void startActionGetEvents(Context context) {
        Intent intent = new Intent(context, SyncEventsService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        if (intent != null) {
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);
            // get the settings
            SP = PreferenceManager.getDefaultSharedPreferences(this);
            // load settings into local variables with defaults
            server = SP.getString("server", getResources().getString(R.string.default_server));
            Log.d(TAG, "Our server is "+server);
            handleActionGetEvents(server);
        }
    }

    private void handleActionGetEvents(String server) {
        Log.d(TAG_EVENTS, "Start");
        while (true) {
            try {
                URL url = new URL(server+"sub/event");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG_EVENTS, "HTTP response: " + urlConnection.getResponseCode());
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                Log.d(TAG_EVENTS, "Reading stream");
                eventsParser(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG_EVENTS, "Error on url openConnection: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void eventsParser(InputStream inputStream) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while((line = reader.readLine()) != null){
                Log.d(TAG_EVENTS, "SSE event: "+line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
