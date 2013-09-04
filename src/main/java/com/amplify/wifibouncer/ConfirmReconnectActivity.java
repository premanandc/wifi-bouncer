package com.amplify.wifibouncer;

import android.app.Notification;
import android.app.NotificationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;

import static com.amplify.wifibouncer.Globals.RECONNECT_NOTIFICATION_ID;
import static com.amplify.wifibouncer.Globals.TAG;

public class ConfirmReconnectActivity extends RoboActivity {

    @Inject
    private NotificationManager notificationManager;

    @Inject
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFeatureDrawableResource(Window.FEATURE_NO_TITLE, android.R.drawable.ic_dialog_alert);
        setContentView(R.layout.confirm_reconnect_dialog);
    }

    private void reconnectTo(WifiConfiguration config) {
        Log.i(TAG, "Reconnecting to network with id: " + config.SSID + ": " + config.BSSID);
        final int networkId = wifiManager.updateNetwork(config);
        if (networkId == -1) {
            Log.e(TAG, "Failed to update network with id: " + config.SSID + ": " + config.BSSID);
        }
        if (!wifiManager.enableNetwork(networkId, true)) {
            Log.e(TAG, "Failed to enable network with id: " + config.SSID + ": " + config.BSSID);
        }
        if (wifiManager.reconnect()) {
            final Notification notification = new Notification.Builder(this)
                    .setOngoing(true)
                    .setContentTitle("Reconnecting to wifi: " + config.SSID)
                    .setSmallIcon(android.R.drawable.ic_notification_overlay).build();
            notificationManager.notify(RECONNECT_NOTIFICATION_ID, notification);
        } else {
            Log.i(TAG, "Failed to initiate wifi reconnect");
        }
    }

    public void onConfirmClick(@SuppressWarnings("UnusedParameters") View ignored) {
        final WifiConfiguration configuration = getIntent().getParcelableExtra(Globals.ACCESS_POINT_EXTRA);
        if (configuration != null) {
            reconnectTo(configuration);
        } else {
            Log.w(Globals.TAG, "Could not find wifi configuration to connect to.");
        }
        finish();
    }

    public void onCancelClick(@SuppressWarnings("UnusedParameters") View ignored) {
        finish();
    }
}
