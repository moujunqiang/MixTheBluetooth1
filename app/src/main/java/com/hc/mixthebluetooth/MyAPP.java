package com.hc.mixthebluetooth;

import android.app.Application;

/**
 * @Version 1.0
 */
public class MyAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 本地异常捕捉
      //  CrashHandler.register(this);
    }
}
