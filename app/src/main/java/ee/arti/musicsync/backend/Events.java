package ee.arti.musicsync.backend;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Events extends HttpGet implements Runnable {

    private static final String TAG ="tEvents";

    public static final String RESPONSE_EVENT = "ee.arti.musicsync.backend.SyncService.EVENT";

    private String lastEventID = null;
    private int retry = 3000;  // retry connecting to the server after about 3000 ms

    public Events(Context context, String server) {
        super(context, server);
    }

    public void run() {
        while (((SyncService)context).isRunning) {
            try {
                if (server == null) {
                    Log.d(TAG, "Server is null, can't proceed");
                    sendError();
                    return;
                }
                URL url = new URL(server, "events");
                parse(get(url));

                Thread.sleep(retry);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                sendError("Creating url for events endpoint failed.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        Log.d(TAG, "Has ended, server was: " + server.toString());
    }

    public void parse(InputStream inputStream) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            Intent intent = null;
            // based on http://www.w3.org/TR/eventsource/#event-stream-interpretation
            while((line = reader.readLine()) != null) {
                Log.d(TAG, "SSE event line: "+line);
                if (line.isEmpty() && intent != null) {
                    context.sendBroadcast(intent);
                    Log.d(TAG, "intent - event:"+intent.getStringExtra("event")+" data:"+intent.getStringExtra("data"));
                    intent = null;
                }
                else if (line.startsWith(":")) {
                    // ignore
                }
                else if (line.contains(":")) {
                    if (intent == null) {
                        intent = new Intent();
                        intent.setAction(RESPONSE_EVENT);
                    }

                    String[] values = line.split(":", 2);
                    String field = values[0];
                    String value = values[1];

                    // remove a whitespace forom start of the value if there is any
                    if (value.startsWith(" ")) {
                        value = value.replaceFirst(" ", "");
                    }

                    if (field.equals("event")) {
                        intent.putExtra("event", value);
                    }
                    else if (field.equals("data")) {
                        String prev_data = intent.getStringExtra("data");
                        StringBuilder data;
                        if (prev_data == null) {
                            data = new StringBuilder();
                        } else {
                            data = new StringBuilder(prev_data);
                        }
                        data.append(value);
                        data.append("\n");
                        intent.putExtra("data", data.toString());
                    }
                    else if (field.equals("id")) {
                        this.lastEventID = value;
                        intent.putExtra("id", value);
                    }
                    else if (field.equals("retry")) {
                        try {
                            this.retry = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // server doesn't rtfm but lets not panic much about it
                            e.printStackTrace();
                        }
                    }
                    else {
                        // unknown field, ignoring
                    }
                }
                else {
                    // unknown field, ignoring
                }
            }
        }catch (IOException|NullPointerException e){
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