package com.amplify.wifibouncer;

import android.app.Notification;
import android.app.NotificationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.concurrent.locks.ReentrantLock;

import static com.amplify.wifibouncer.Globals.RECONNECT_NOTIFICATION_ID;
import static com.amplify.wifibouncer.Globals.TAG;

public class ConfirmReconnectActivity extends RoboActivity {

    @Inject
    private NotificationManager notificationManager;

    @Inject
    private WifiManager wifiManager;
    @Inject
    private ReentrantLock reconnectLock;
    @InjectView(R.id.time_to_respond)
    private TextView timeToRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (reconnectLock.isLocked()) {
            Log.i(TAG, "Reconnection is already in progress. No need to do this again right now.");
            finish();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFeatureDrawableResource(Window.FEATURE_NO_TITLE, android.R.drawable.ic_dialog_alert);
        setContentView(R.layout.confirm_reconnect_dialog);
        new CountDownTimer(20000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeToRespond.setText("You have " + millisUntilFinished/1000 + " seconds to respond.");
            }

            public void onFinish() {
                timeToRespond.setText("Closing");
                ConfirmReconnectActivity.this.finish();
            }
        }.start();

    }

    private void tryReconnect(WifiConfiguration config) {
        Log.i(TAG, "Trying reconnect to network with id: " + config.SSID + ": " + config.BSSID);
        final int networkId = wifiManager.updateNetwork(config);
        if (networkId == -1) {
            Log.e(TAG, "Failed to update network with id: " + config.SSID + ": " + config.BSSID);
            return;
        }
        if (!wifiManager.enableNetwork(networkId, true)) {
            Log.e(TAG, "Failed to enable network with id: " + config.SSID + ": " + config.BSSID);
            return;
        }
        if (!reconnectLock.tryLock()) {
            Log.e(TAG, "Failed to acquire reconnect lock. A reconnect may already be in progress.");
            return;
        }
        reconnectTo(config);
    }

    private void reconnectTo(WifiConfiguration config) {
        if (wifiManager.reconnect()) {
            final Notification notification = createNotification(config);
            notificationManager.notify(RECONNECT_NOTIFICATION_ID, notification);
        } else {
            Log.e(TAG, "Failed to initiate wifi reconnect.");
            reconnectLock.unlock();
        }
    }

    private Notification createNotification(WifiConfiguration config) {
        return new Notification.Builder(this)
                .setOngoing(true)
                .setContentTitle("Reconnecting to wifi: " + config.SSID)
                .setContentText("Access point " + config.BSSID)
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setTicker("Reconnecting to wifi: " + config.SSID)
                .setProgress(0, 0, true)
                .build();
    }

    public void onConfirmClick(@SuppressWarnings("UnusedParameters") View ignored) {
        final WifiConfiguration configuration = getIntent().getParcelableExtra(Globals.ACCESS_POINT_EXTRA);
        if (configuration != null) {
            tryReconnect(configuration);
        } else {
            Log.w(Globals.TAG, "Could not find wifi configuration to connect to.");
        }
        finish();
    }

    public void onCancelClick(@SuppressWarnings("UnusedParameters") View ignored) {
        finish();
    }
}
