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

import java.util.List;
import java.util.Set;

import static android.net.wifi.WifiManager.calculateSignalLevel;
import static com.amplify.wifibouncer.Globals.TAG;
import static com.amplify.wifibouncer.Globals.WIFI_LEVELS;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

public class WifiScanBroadcastReceiver extends RoboBroadcastReceiver {

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
    public static final int FAIR_SIGNAL = 1;

    @Inject
    private WifiManager wifiManager;

    @Override
    protected void handleReceive(Context context, Intent intent) {
        super.handleReceive(context, intent);

        Log.i(TAG, "Received intent: " + intent.getAction() + ": " + intent.getExtras());

        final WifiInfo current = wifiManager.getConnectionInfo();
        final int currentSignalStrength = current.getRssi();
        Log.i(TAG, "Connected to an access point with signal strength " + currentSignalStrength + " - level " + calculateSignalLevel(currentSignalStrength, WIFI_LEVELS));
        if (isGoodEnough(current)) {
            Log.i(TAG, "Connected to a good enough access point. No need to initiate wifi reconnect!");
            return;
        }
        Optional<WifiConfiguration> accessPoint = bestAccessPoint();

        if (accessPoint.isPresent()) {
            launchConfirmActivity(context, accessPoint.get());
        }
    }

    private boolean isGoodEnough(WifiInfo wifiInfo) {
        return calculateSignalLevel(wifiInfo.getRssi(), Globals.WIFI_LEVELS) > FAIR_SIGNAL;
    }

    private void launchConfirmActivity(Context context, WifiConfiguration accessPoint) {
        final Intent confirm = new Intent(context, ConfirmReconnectActivity.class);
        confirm.putExtra(Globals.ACCESS_POINT_EXTRA, accessPoint);
        confirm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(confirm);
    }

    private Optional<WifiConfiguration> bestAccessPoint() {
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        Optional<ScanResult> bestAccessPoint = bestAccessPoint(configuredNetworks);

        if (bestAccessPoint.isPresent()) {
            return Optional.of(adaptScanResultToWifiConfiguration(configuredNetworks, bestAccessPoint));
        }
        return Optional.absent();
    }

    private WifiConfiguration adaptScanResultToWifiConfiguration(List<WifiConfiguration> configuredNetworks, Optional<ScanResult> bestAccessPoint) {
        final ScanResult scanResult = bestAccessPoint.get();
        final WifiConfiguration config = FluentIterable.from(configuredNetworks)
                .firstMatch(new WifiConfigurationPredicate(scanResult)).get();
        config.BSSID = scanResult.BSSID;
        config.priority = 1;
        return config;
    }

    private Optional<ScanResult> bestAccessPoint(List<WifiConfiguration> configuredNetworks) {
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        final Set<String> configuredNetworkNames = newHashSet(transform(configuredNetworks, TO_NAME));
        Log.i(TAG, "The following networks are configured: " + configuredNetworkNames);
        final List<ScanResult> unfiltered = wifiManager.getScanResults();

        final ImmutableList<ScanResult> filtered = FluentIterable.from(unfiltered)
                .filter(new CurrentNetworkFilter(connectionInfo))
                .filter(new ConfiguredNetworkFilter(configuredNetworkNames))
                .filter(new GreaterSignalStrengthFilter(connectionInfo))
                .toSortedList(BY_SIGNAL_STRENGTH);
        Log.i(TAG, "Signal strength of current access point is " + connectionInfo.getSSID() + ": " + connectionInfo.getBSSID() + " is " + connectionInfo.getRssi() + ", level = " + calculateSignalLevel(connectionInfo.getRssi(), WIFI_LEVELS));
        Log.i(TAG, "Found " + filtered.size() + " better access point(s).");
        for (ScanResult scanResult : filtered) {
            Log.i(TAG, "Signal strength of " + scanResult.SSID + ": " + scanResult.BSSID + " is " + scanResult.level + ", level = " + calculateSignalLevel(scanResult.level, WIFI_LEVELS));
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
