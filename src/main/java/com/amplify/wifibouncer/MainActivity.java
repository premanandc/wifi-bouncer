package com.amplify.wifibouncer;

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

import static com.amplify.wifibouncer.Globals.TAG;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    @Inject
    private ConnectivityManager connectivityManager;

    @Inject
    private WifiManager wifiManager;

    @InjectView(R.id.log_text)
    private TextView logText;

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
        } else if (menuItemPressed == R.id.clear_log) {
            logText.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

