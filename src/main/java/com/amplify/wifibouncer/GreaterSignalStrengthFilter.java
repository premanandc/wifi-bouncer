package com.amplify.wifibouncer;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import com.google.common.base.Predicate;

import static android.net.wifi.WifiManager.calculateSignalLevel;
import static com.amplify.wifibouncer.Globals.WIFI_LEVELS;

class GreaterSignalStrengthFilter implements Predicate<ScanResult> {
    private final WifiInfo currentConnection;

    public GreaterSignalStrengthFilter(WifiInfo currentConnection) {
        this.currentConnection = currentConnection;
    }

    @Override
    public boolean apply(ScanResult scanResult) {
        return notConnected() || signalLevel(scanResult.level) > signalLevel(currentConnection.getRssi());
    }

    private int signalLevel(int signalStrength) {
        return calculateSignalLevel(signalStrength, WIFI_LEVELS);
    }

    private boolean notConnected() {
        return currentConnection == null;
    }
}
