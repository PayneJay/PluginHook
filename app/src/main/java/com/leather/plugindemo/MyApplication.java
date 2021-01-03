package com.leather.plugindemo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.leather.plugindemo.utils.HookUtils;
import com.leather.plugindemo.utils.LoadUtils;

public class MyApplication extends Application {

    private String pluginPath;
    private Resources mResources;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        pluginPath = Environment.getExternalStorageDirectory().getPath() + "/plugin-debug.apk";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LoadUtils.loadClass(this, pluginPath);

        mResources = LoadUtils.loadResource(this, pluginPath);

        //方案1
        HookUtils.hookAMS();
        HookUtils.hookHandler();


        /*
        方案2

        必须在Activity初始化之前用InstrumentationProxy替换Instrumentation，
        这样所有的Activity和ActivityThread中的mInstrumentation这个成员变量
        才会都是InstrumentationProxy这个对象
         */
//        HookUtils.hookInstrumentation();
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            return super.getResources();
        }
        return mResources;
    }

}
