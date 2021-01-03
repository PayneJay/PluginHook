package com.leather.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceSystemResources();
    }

    /**
     * 替换系统的mResources
     */
    private void replaceSystemResources() {
        Resources resources = LoadResourceUtils.getPluginResources(this,
                Environment.getExternalStorageDirectory().getPath() + "/plugin-debug.apk");

        Log.e("Jack", getClass().getName() + " onCreate");
        mContext = new ContextThemeWrapper(getBaseContext(), 0);
        Class<? extends Context> aClass = mContext.getClass();
        try {
            Field declaredField = aClass.getDeclaredField("mResources");
            declaredField.setAccessible(true);
            declaredField.set(mContext, resources);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Jack", "error : " + e.getMessage());
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mContext != null) {
            View view = LayoutInflater.from(mContext).inflate(layoutResID, null);
            setContentView(view);
            return;
        }
        super.setContentView(layoutResID);
    }


//    @Override
//    public Resources getResources() {
//        /**
//         * 这个方案在继承自Activity的时候测试可行，但是如果继承AppCompatActivity的话会抛异常崩溃
//         * 也就是资源冲突了,解决方案有两种：
//         * 1、通过aapt修改资源的开头标识7f（通常资源的开头是7f）
//         * 2、通过反射替换系统的Context和mResources{@link BaseActivity#replaceSystemResources},然后重写{@link BaseActivity#setContentView}
//         *
//         * Caused by: java.lang.NullPointerException: Attempt to invoke interface method 'void androidx.appcompat.widget.DecorContentParent.setWindowCallback(android.view.Window$Callback)' on a null object reference
//         *         at androidx.appcompat.app.AppCompatDelegateImpl.createSubDecor(AppCompatDelegateImpl.java:900)
//         *         at androidx.appcompat.app.AppCompatDelegateImpl.ensureSubDecor(AppCompatDelegateImpl.java:806)
//         *         at androidx.appcompat.app.AppCompatDelegateImpl.setContentView(AppCompatDelegateImpl.java:693)
//         *         at androidx.appcompat.app.AppCompatActivity.setContentView(AppCompatActivity.java:170)
//         *         at com.leather.plugin.PluginActivity.onCreate(PluginActivity.java:11)
//         */
//        if (getApplication() != null && getApplication().getResources() != null) {
//            return getApplication().getResources();
//        }
//        return super.getResources();
//    }
}
