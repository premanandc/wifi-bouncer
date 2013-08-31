package com.amplify.wifibouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    private static final IntentFilter WIFI_SCAN_FILTER = new IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION);
    private static final IntentFilter CONNECTIVITY_FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (connectivityManager.getActiveNetworkInfo() != null) {
                log("Network reconnected to " + connectivityManager.getActiveNetworkInfo());
            } else {
                log("Not connected right now.");
            }
        }
    };

    private final BroadcastReceiver wifiReceiver = new WifiScanBroadcastReceiver();

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
        Log.i(Globals.TAG, message.toString());
        logText.setText(logText.getText() + "\n" + message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

