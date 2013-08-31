package com.amplify.wifibouncer;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import com.google.common.base.Predicate;

class CurrentNetworkFilter implements Predicate<ScanResult> {
    private final WifiInfo connectionInfo;

    public CurrentNetworkFilter(WifiInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public boolean apply(ScanResult result) {
        return connectionInfo == null || !result.BSSID.equals(connectionInfo.getBSSID());
    }
}
