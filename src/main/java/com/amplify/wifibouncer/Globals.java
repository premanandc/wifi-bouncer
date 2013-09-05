package com.amplify.wifibouncer;

import java.util.concurrent.locks.ReentrantLock;

public class Globals {
    public static final int WIFI_LEVELS = 4;
    public static final String ACCESS_POINT_EXTRA = "WifiBouncer.AccessPoint";
    public static final int RECONNECT_NOTIFICATION_ID = 1;
    public static final String TAG = "WifiBouncer";
    public static final ReentrantLock LOCK = new ReentrantLock();
}
