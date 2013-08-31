package com.amplify.wifibouncer;

import android.net.wifi.ScanResult;
import com.google.common.base.Predicate;

import java.util.Set;

class ConfiguredNetworkFilter implements Predicate<ScanResult> {
    private final Set<String> networks;

    public ConfiguredNetworkFilter(Set<String> networks) {
        this.networks = networks;
    }

    @Override
    public boolean apply(ScanResult scanResult) {
        return networks.contains(scanResult.SSID);
    }

}
