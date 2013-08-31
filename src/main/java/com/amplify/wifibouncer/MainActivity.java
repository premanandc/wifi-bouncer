package com.amplify.wifibouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    private static final IntentFilter WIFI_SCAN_FILTER = new IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION);
    private static final IntentFilter CONNECTIVITY_FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    private static final String TAG = MainActivity.class.getSimpleName();
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

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (connectivityManager.getActiveNetworkInfo() != null) {
                log("Network reconnected!");
            } else {
                log("Not connected right now.");
            }
        }
    };

    private final BroadcastReceiver wifiReceiver = new WifiScanBroadcastReceiver();

    private Optional<WifiConfiguration> candidateConfiguration() {
        log("Wifi scan completed!!");
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        List<ScanResult> scanResults = betterAccessPoints(configuredNetworks);
        log("Found " + scanResults.size() + " candidate(s).\n" + toPrettyString(scanResults));

        if (scanResults.size() > 0) {
            final ScanResult scanResult = scanResults.get(0);
            final Optional<WifiConfiguration> optional = FluentIterable.from(configuredNetworks)
                    .firstMatch(new Predicate<WifiConfiguration>() {
                        @Override
                        public boolean apply(WifiConfiguration input) {
                            return input.SSID.replaceAll(DOUBLE_QUOTE, "").equals(scanResult.SSID.replaceAll(DOUBLE_QUOTE, ""));
                        }
                    });
            final WifiConfiguration configuration = optional.get();
            configuration.BSSID = scanResult.BSSID;
            configuration.priority = 1;
            return optional;
        }
        return Optional.absent();
    }

    private String toPrettyString(Iterable<ScanResult> scanResults) {
        StringBuilder out = new StringBuilder();
        for (ScanResult scanResult : scanResults) {
            out.append(toPrettyString(scanResult));
        }
        return out.toString();
    }

    private String toPrettyString(ScanResult scanResult) {
        StringBuilder out = new StringBuilder();
        out.append(scanResult.SSID).append(":")
                .append(scanResult.BSSID)
                .append(" ").append(scanResult.level).append("\n");
        return out.toString();
    }

    private List<ScanResult> betterAccessPoints(List<WifiConfiguration> configuredNetworks) {
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        log("Current connection: " + connectionInfo);
        final Set<String> configuredNetworkNames = newHashSet(transform(configuredNetworks, TO_NAME));
        log("The following networks are configured: " + configuredNetworkNames);
        final List<ScanResult> unfiltered = wifiManager.getScanResults();
        Collections.sort(unfiltered, BY_SIGNAL_STRENGTH);
        for (ScanResult scanResult : unfiltered) {
            log("Unfiltered: " + scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
        }

        final ImmutableList<ScanResult> filtered = FluentIterable.from(unfiltered)
                .filter(new CurrentNetworkFilter(connectionInfo))
                .filter(new ConfiguredNetworkFilter(configuredNetworkNames))
                .filter(new GreaterSignalStrengthFilter(connectionInfo))
                .toSortedList(BY_SIGNAL_STRENGTH);
        for (ScanResult scanResult : filtered) {
            log("Filtered: " + scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
        }
        return filtered;
    }

    @Inject
    private ConnectivityManager connectivityManager;

    @Inject
    private WifiManager wifiManager;

    @InjectView(R.id.log_text)
    private TextView logText;

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(wifiReceiver, WIFI_SCAN_FILTER);
        registerReceiver(networkReceiver, CONNECTIVITY_FILTER);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(networkReceiver);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.wifi_scan) {
            log("Wifi scan started!!");

            wifiManager.setWifiEnabled(true);
            return wifiManager.startScan();
        } else if (item.getItemId() == R.id.clear_log) {
            logText.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(Object message) {
        Log.i(TAG, message.toString());
        logText.setText(logText.getText() + "\n" + message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static class ConfiguredNetworkFilter implements Predicate<ScanResult> {
        private final Set<String> networks;

        public ConfiguredNetworkFilter(Set<String> networks) {
            this.networks = networks;
        }

        @Override
        public boolean apply(ScanResult scanResult) {
            return networks.contains(scanResult.SSID);
        }
    }

    private static class GreaterSignalStrengthFilter implements Predicate<ScanResult> {
        private final WifiInfo connectionInfo;

        public GreaterSignalStrengthFilter(WifiInfo connectionInfo) {
            this.connectionInfo = connectionInfo;
        }

        @Override
        public boolean apply(ScanResult scanResult) {
            return connectionInfo == null || scanResult.level > connectionInfo.getRssi() + 2;
        }
    }

    private static class CurrentNetworkFilter implements Predicate<ScanResult> {
        private final WifiInfo connectionInfo;

        public CurrentNetworkFilter(WifiInfo connectionInfo) {
            this.connectionInfo = connectionInfo;
        }

        @Override
        public boolean apply(ScanResult result) {
            return connectionInfo == null || !result.BSSID.equals(connectionInfo.getBSSID());
        }
    }

    private class WifiScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Optional<WifiConfiguration> configuration = candidateConfiguration();
            if (configuration.isPresent()) {
                final int networkId = wifiManager.updateNetwork(configuration.get());
                wifiManager.saveConfiguration();
                log("Updated network with id: " + networkId + " SSID: " + configuration.get().SSID + " BSSID: " + configuration.get().BSSID);
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();
                log("Enabled network with id: " + networkId);
            }
        }
    }
}

