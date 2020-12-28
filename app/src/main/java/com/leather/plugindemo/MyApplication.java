package com.leather.plugindemo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;

import com.leather.plugindemo.utils.HookUtils;
import com.leather.plugindemo.utils.LoadUtils;

public class MyApplication extends Application {

    private String pluginPath;
    private Resources mResources;
    private PackageInfo packageInfo;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        pluginPath = Environment.getExternalStorageDirectory().getPath() + "/plugin-debug.apk";
        packageInfo = getPackageManager().getPackageArchiveInfo(pluginPath, PackageManager.GET_ACTIVITIES);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LoadUtils.loadClass(this, pluginPath);

        mResources = LoadUtils.loadResource(this, pluginPath);

        HookUtils.hookAMS();
        HookUtils.hookHandler();
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            return super.getResources();
        }
        return mResources;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
}
