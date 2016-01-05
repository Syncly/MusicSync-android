package ee.arti.musicsync.backend;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Events implements Runnable {

    private static final String TAG ="tEvents";

    private Context context;
    private URL server;

    public Events(Context context, String server) {
        this.context = context;
        try {
            this.server = new URL(server);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Log.d(TAG, "Ran the thread, server: " + server.toString());
    }
}