package com.amplify;

import android.app.Application;
import com.google.inject.Stage;
import roboguice.RoboGuice;

public class Main extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.setBaseApplicationInjector(this, Stage.PRODUCTION);

    }
}
