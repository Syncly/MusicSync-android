package ee.arti.musicsync.backend;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import ee.arti.musicsync.PlaylistsActivity;


public class GetPlaylists extends HttpGet implements Runnable {

    private static final String TAG = "tGetPlaylists";
    public static final String ACTION = "GetPlaylists";

    public GetPlaylists(Context context, String server) {
        super(context, server);
    }

    public void run() {
        try {
            if (server == null) {
                Log.d(TAG, "Server is null, can't proceed");
                sendError();
                return;
            }

            URL url = new URL(server, "playlists");
            parse(get(url));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError("Creating url for playlists endpoint failed.");
        }
    }

    private void parse(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            Intent intent = new Intent();
            intent.setAction(RESPONSE_SUCCESS);
            intent.putExtra("action", ACTION);

            JSONArray jspls = new JSONArray(sb.toString());

            ArrayList<HashMap> ba = new ArrayList<>();
            for (int i = 0; i < jspls.length(); i++) {
                JSONObject jspl = jspls.getJSONObject(i);
                HashMap<String, String> p = new HashMap();
                p.put(PlaylistsActivity.TAG_PLAYLIST_STATUS, jspl.optString("status", "OK"));
                p.put(PlaylistsActivity.TAG_PLAYLIST_TITLE, jspl.getString("title"));
                p.put(PlaylistsActivity.TAG_PLAYLIST_ID, jspl.getString("_id"));
                ba.add(p);
            }
            intent.putExtra("data", ba);
            context.sendBroadcast(intent);

        } catch (IOException | NullPointerException | JSONException e) {
            e.printStackTrace();
            sendError("Response reading failed " + e.getMessage());
        }
    }

}