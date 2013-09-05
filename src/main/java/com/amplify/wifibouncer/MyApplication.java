package com.amplify.wifibouncer;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import roboguice.RoboGuice;

import java.util.concurrent.locks.ReentrantLock;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.setBaseApplicationInjector(this, Stage.PRODUCTION,
                RoboGuice.newDefaultRoboModule(this),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ReentrantLock.class).in(Singleton.class);
                    }
                });
    }
}
