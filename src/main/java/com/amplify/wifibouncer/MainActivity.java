package com.amplify.wifibouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.widget.Toast.LENGTH_SHORT;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    private static final IntentFilter WIFI_STATE_CHANGED = new IntentFilter(NETWORK_STATE_CHANGED_ACTION);

    private final BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                log("Network connected to " + networkInfo);
                Toast.makeText(context, getString(R.string.wifi_connected_message,
                        networkInfo.getExtraInfo()),
                        LENGTH_SHORT).show();
            } else {
                log("Not connected to any network interface right now.");
            }
        }
    };

    @Inject
    private ConnectivityManager connectivityManager;

    @Inject
    private WifiManager wifiManager;

    @InjectView(R.id.log_text)
    private TextView logText;

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(wifiStateReceiver, WIFI_STATE_CHANGED);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(wifiStateReceiver);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int menuItemPressed = item.getItemId();
        if (menuItemPressed == R.id.wifi_scan) {
            if (!wifiManager.isWifiEnabled()) {
                log("Wifi is currently turned off. Enabling!");
                wifiManager.setWifiEnabled(true);
            }
            log("Initiating manual wifi scan!!");
            return wifiManager.startScan();
        } else if (menuItemPressed == R.id.clear_log) {
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

