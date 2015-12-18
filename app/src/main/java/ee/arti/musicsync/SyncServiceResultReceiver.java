package ee.arti.musicsync;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Used to pass data back from SyncService to the caller
 */
public class SyncServiceResultReceiver extends ResultReceiver {
    private Receiver receiver;

    public SyncServiceResultReceiver (Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int status, Bundle result);
    }

    protected void onReceiveResult(int status, Bundle result) {
        if (receiver != null) {
            receiver.onReceiveResult(status, result);
        }
    }

}
