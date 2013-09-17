package com.amplify.wifibouncer;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.inject.Inject;
import roboguice.receiver.RoboBroadcastReceiver;

import java.util.concurrent.locks.ReentrantLock;

import static com.amplify.wifibouncer.Globals.RECONNECT_NOTIFICATION_ID;
import static com.amplify.wifibouncer.Globals.TAG;

public class WifiStateChangeReceiver extends RoboBroadcastReceiver {

    @Inject
    private NotificationManager notificationManager;
    @Inject
    private ReentrantLock reconnectLock;

    @Override
    protected void handleReceive(Context context, Intent intent) {
        final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        Log.i(TAG, "Network status changed to: " + networkInfo.getDetailedState());
        if (networkInfo.isConnected()) {
            notificationManager.cancelAll();
            if (reconnectLock.isLocked()) {
                Log.i(TAG, "Unlocking reconnect lock!");
                reconnectLock.unlock();
                final Notification notification = new Notification.Builder(context)
                        .setProgress(0, 0, false)
                        .setContentTitle("Success!")
                        .setContentText("Reconnection successful!")
                        .setSmallIcon(R.drawable.wifi_scan)
                        .build();
                notificationManager.notify(RECONNECT_NOTIFICATION_ID, notification);
            }
        }
    }
}
