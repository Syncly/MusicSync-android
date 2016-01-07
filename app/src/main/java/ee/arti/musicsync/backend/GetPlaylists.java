package ee.arti.musicsync.backend;

import android.content.ContentValues;
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
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
            ArrayList<ContentValues> playlists = parse(get(url));
            if (playlists != null) {
                sendResponse(playlists);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError("Creating url for playlists endpoint failed.");
        }
    }

    private void sendResponse(ArrayList<ContentValues> playlists) {
        Intent intent = new Intent();
        intent.setAction(RESPONSE_SUCCESS);
        intent.putExtra("action", ACTION);
        intent.putExtra("data", playlists);
        context.sendBroadcast(intent);
    }

    private ArrayList<ContentValues> parse(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            JSONArray jspls = new JSONArray(sb.toString());
            ArrayList<ContentValues> playlists = new ArrayList<>();
            ArrayList<Playlist> currentPlaylists = db.getAllPlaylists();
            HashSet<String> newPlaylists = new HashSet<>();

            for (int i = 0; i < jspls.length(); i++) {
                JSONObject jspl = jspls.getJSONObject(i);
                Playlist playlist = db.getPlaylist(jspl.getString("_id"));

                if (playlist == null) {
                    playlist = new Playlist();
                    playlist.setId(jspl.getString("_id"));
                    playlist.setName(jspl.getString("title"));
                    playlist.setType(jspl.getString("type"));
                    playlist.setStatusType("Unknown");
                    playlist.setStatusProgress("Unknown");
                    db.addPlaylist(playlist);
                }
                playlist = db.updatePlaylistStatusProgress(playlist);
                playlists.add(playlist.getValues());
                newPlaylists.add(playlist.getId());
            }

            for (Playlist playlist: currentPlaylists) {
                if (!newPlaylists.contains(playlist.getId())) {
                    db.deletePlaylist(playlist);
                }
            }
            return playlists;

        } catch (IOException | NullPointerException | JSONException e) {
            e.printStackTrace();
            sendError("Response reading failed " + e.getMessage());
        }
        return null;
    }

}