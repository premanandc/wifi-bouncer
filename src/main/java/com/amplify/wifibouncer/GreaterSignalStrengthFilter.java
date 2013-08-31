package com.amplify.wifibouncer;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import com.google.common.base.Predicate;

class GreaterSignalStrengthFilter implements Predicate<ScanResult> {
    private final WifiInfo connectionInfo;

    public GreaterSignalStrengthFilter(WifiInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public boolean apply(ScanResult scanResult) {
        return connectionInfo == null || scanResult.level > connectionInfo.getRssi() + 2;
    }
}
