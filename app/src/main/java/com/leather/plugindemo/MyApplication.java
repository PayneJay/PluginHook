package com.leather.plugindemo;

import android.app.Application;
import android.os.Environment;

import com.leather.plugindemo.utils.HookUtils;
import com.leather.plugindemo.utils.LoadUtils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LoadUtils.loadClass(this,"/sdcard/plugin-debug.apk");

        HookUtils.hookAMS();
        HookUtils.hookHandler();
    }
}
