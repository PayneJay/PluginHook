package com.leather.plugindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private MyApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (MyApplication) getApplication();
//        int identifier = application.getResources().getIdentifier("activity_plugin", "layout", application.getPackageInfo().packageName);

        setContentView(R.layout.activity_main);
    }

    public void callPluginMethod(View view) {
        ClassLoader classLoader = getClassLoader();
        try {
            //加载插件中的类
            Class<?> pluginClass = classLoader.loadClass("com.leather.plugin.PluginTest");
            Method showToast = pluginClass.getDeclaredMethod("showToast", Context.class, String.class);
            if (!showToast.isAccessible()) {
                showToast.setAccessible(true);
            }
            //拿到插件中类的实例对象
            Object instance = pluginClass.newInstance();
            //反射调用插件中类的方法
            showToast.invoke(instance, this, "调用插件中类的方法成功！");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jack", "Exception : " + e.getMessage());
        }
    }

    public void startPluginActivity(View view) {
        ClassLoader classLoader = getClassLoader();
        try {
            Class<?> pluginClass = classLoader.loadClass("com.leather.plugin.PluginActivity");
            Intent intent = new Intent(this, pluginClass);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}