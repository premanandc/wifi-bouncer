package com.amplify.wifibouncer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.inject.Inject;
import roboguice.receiver.RoboBroadcastReceiver;

import static android.net.wifi.WifiManager.EXTRA_NEW_RSSI;
import static android.net.wifi.WifiManager.calculateSignalLevel;
import static com.amplify.wifibouncer.Globals.TAG;
import static com.amplify.wifibouncer.Globals.WIFI_LEVELS;

public class SignalStrengthReceiver extends RoboBroadcastReceiver {

    @Inject
    private WifiManager wifiManager;

    public static final int DEFAULT_SIGNAL_STRENGTH = -200;

    @Override
    protected void handleReceive(Context context, Intent intent) {
        final int signalStrength = intent.getIntExtra(EXTRA_NEW_RSSI, DEFAULT_SIGNAL_STRENGTH);
        Log.i(TAG, "Connected to wifi access point with signal strength: " + signalStrength);
        final int signalLevel = calculateSignalLevel(signalStrength, WIFI_LEVELS);
        if (isPoorConnection(signalLevel)) {
            Log.i(TAG, "The current signal level is " + signalLevel);
            wifiManager.startScan();
        }
    }

    private boolean isPoorConnection(int signalLevel) {
        return signalLevel <= 0;
    }
}
