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

import ee.arti.musicsync.SongsActivity;

public class GetSongs extends HttpGet implements Runnable {

    private static final String TAG = "tGetSongs";
    private static final String ACTION = "GetSongs";

    private String playlist_id;

    public GetSongs(Context context, String server, String playlist_id) {
        super(context, server);
        this.playlist_id = playlist_id;
    }

    public void run() {
        try {
            if (server == null) {
                Log.d(TAG, "Server is null, can't proceed");
                sendError();
                return;
            } else if (this.playlist_id == null) {
                Log.d(TAG, "Playlist id can't be null");
                sendError("Playlist id was null, can't get songs");
                return;
            }

            URL url = new URL(server, "playlists/"+playlist_id+"/songs");
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

            JSONArray jssongs = new JSONArray(sb.toString());

            ArrayList<HashMap> ba = new ArrayList<>();
            for (int i = 0; i < jssongs.length(); i++) {
                JSONObject jssong = jssongs.getJSONObject(i);
                HashMap<String, String> p = new HashMap();
                p.put(SongsActivity.TAG_SONG_STATUS, jssong.optString("status", "OK"));
                p.put(SongsActivity.TAG_SONG_TITLE, jssong.getString("title"));
                p.put(SongsActivity.TAG_SONG_ID, jssong.getString("_id"));
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