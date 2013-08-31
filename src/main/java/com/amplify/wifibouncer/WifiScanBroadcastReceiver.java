package com.amplify.wifibouncer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import roboguice.receiver.RoboBroadcastReceiver;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

class WifiScanBroadcastReceiver extends RoboBroadcastReceiver {

    public static final Ordering<ScanResult> BY_SIGNAL_STRENGTH = new Ordering<ScanResult>() {
        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            return -Ints.compare(lhs.level, rhs.level);
        }
    };
    public static final String DOUBLE_QUOTE = "\"";
    public static final Function<WifiConfiguration, String> TO_NAME = new Function<WifiConfiguration, String>() {
        @Override
        public String apply(WifiConfiguration wifiConfiguration) {
            return wifiConfiguration.SSID.replaceAll(DOUBLE_QUOTE, "");
        }
    };

    @Inject
    private WifiManager wifiManager;

    @Override
    protected void handleReceive(Context context, Intent intent) {
        super.handleReceive(context, intent);
        Optional<WifiConfiguration> accessPoint = betterAccessPoint();
        if (accessPoint.isPresent()) {
            reconnectTo(accessPoint.get());
        }
    }

    private void reconnectTo(WifiConfiguration config) {
        final int networkId = wifiManager.updateNetwork(config);
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
        Log.i(Globals.TAG, "Reconnecting to network with id: " + config.SSID + ": " + config.BSSID);
    }

    private Optional<WifiConfiguration> betterAccessPoint() {
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        Optional<ScanResult> bestAccessPoint = bestAccessPoint(configuredNetworks);

        if (bestAccessPoint.isPresent()) {
            final ScanResult scanResult = bestAccessPoint.get();
            final WifiConfiguration config = FluentIterable.from(configuredNetworks)
                    .firstMatch(new WifiConfigurationPredicate(scanResult)).get();
            config.BSSID = scanResult.BSSID;
            config.priority = 1;
            return Optional.of(config);
        }
        return Optional.absent();
    }

    private Optional<ScanResult> bestAccessPoint(List<WifiConfiguration> configuredNetworks) {
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        Log.i(Globals.TAG, "Currently connected to: " + connectionInfo.getSSID() + ": " + connectionInfo.getBSSID());
        final Set<String> configuredNetworkNames = newHashSet(transform(configuredNetworks, TO_NAME));
        Log.i(Globals.TAG, "The following networks are configured: " + configuredNetworkNames);
        final List<ScanResult> unfiltered = wifiManager.getScanResults();
        Collections.sort(unfiltered, BY_SIGNAL_STRENGTH);
        for (ScanResult scanResult : unfiltered) {
            Log.i(Globals.TAG, "Unfiltered: " + scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
        }

        final ImmutableList<ScanResult> filtered = FluentIterable.from(unfiltered)
                .filter(new CurrentNetworkFilter(connectionInfo))
                .filter(new ConfiguredNetworkFilter(configuredNetworkNames))
                .filter(new GreaterSignalStrengthFilter(connectionInfo))
                .toSortedList(BY_SIGNAL_STRENGTH);
        for (ScanResult scanResult : filtered) {
            Log.i(Globals.TAG, "Filtered: " + scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
        }
        return filtered.isEmpty() ? Optional.<ScanResult>absent() : Optional.of(filtered.get(0));
    }

    private static class WifiConfigurationPredicate implements Predicate<WifiConfiguration> {
        private final ScanResult scanResult;

        public WifiConfigurationPredicate(ScanResult scanResult) {
            this.scanResult = scanResult;
        }

        @Override
        public boolean apply(WifiConfiguration input) {
            return input.SSID.replaceAll(DOUBLE_QUOTE, "").equals(scanResult.SSID.replaceAll(DOUBLE_QUOTE, ""));
        }
    }
}
