package com.amplify.wifibouncer;

import android.app.Application;
import com.google.inject.Stage;
import roboguice.RoboGuice;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.setBaseApplicationInjector(this, Stage.PRODUCTION);
    }
}
