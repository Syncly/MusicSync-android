package ee.arti.musicsync.backend;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpGet {

    private static final String TAG = "tHttpGet";
    private static final String ACTION = "HttpGet";

    public static final String RESPONSE_ERROR = "ee.arti.musicsync.backend.SyncService.ERROR";
    public static final String RESPONSE_SUCCESS = "ee.arti.musicsync.backend.SyncService.SUCCESS";

    protected Context context;
    protected URL server;

    public HttpGet(Context context, String server) {
        this.context = context;
        try {
            this.server = new URL(server);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError("Invalid backend server URL "+server);
        }
    }

    public InputStream get(URL url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "HTTP response: " + urlConnection.getResponseCode());
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            return inputStream;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError("Bad backend server URL");
        } catch (IOException e) {
            Log.e(TAG, "Error on url openConnection: " + e.getMessage());
            e.printStackTrace();
            sendError("Server connection failed: "+e.getMessage());
        }
        return null;
    }

    public void sendError(String message) {
        Intent intent = new Intent();
        intent.setAction(RESPONSE_ERROR);
        intent.putExtra("action", ACTION);
        intent.putExtra("error", message);
        context.sendBroadcast(intent);
    }

    public void sendError() {
        this.sendError(null);
    }
}