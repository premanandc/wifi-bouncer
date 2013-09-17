package com.amplify.wifibouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.amplify.wifibouncer.charts.SignalStrengthBarChart;
import com.amplify.wifibouncer.charts.SignalStrengthDialChart;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

import static com.amplify.wifibouncer.Globals.TAG;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {
    private static final IntentFilter SCAN_FILTER = new IntentFilter(
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    @InjectView(R.id.dial_chart)
    private FrameLayout dialChartLayout;
    @InjectView(R.id.bar_chart)
    private FrameLayout barChartLayout;
    @Inject
    private ConnectivityManager connectivityManager;
    @Inject
    private SignalStrengthDialChart dialChart;
    @Inject
    private SignalStrengthBarChart barChart;

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final List<ScanResult> scanResults = wifiManager.getScanResults();
            barChart.update(scanResults);
            dialChart.update(wifiManager.getConnectionInfo().getRssi());
            wifiManager.startScan();
        }
    };
    @Inject
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barChartLayout.addView(barChart.view());
        dialChartLayout.addView(dialChart.view());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(scanReceiver, SCAN_FILTER);
        wifiManager.startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(scanReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int menuItemPressed = item.getItemId();
        if (menuItemPressed == R.id.wifi_scan) {
            if (!wifiManager.isWifiEnabled()) {
                Log.i(TAG, "Wifi is currently turned off. Enabling!");
                wifiManager.setWifiEnabled(true);
            }
            Log.i(TAG, "Initiating manual wifi scan!!");
            return wifiManager.startScan();
        } else if (menuItemPressed == R.id.preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

