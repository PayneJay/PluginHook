package com.leather.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.leather.pluginmgr.inter.PluginInterface;

public class BasicActivity extends AppCompatActivity implements PluginInterface {
    public static final String TAG = "Jack BasicActivity";
    protected Activity thatActivity;

    /**
     * 注入自己的上下文
     * 如果为空 使用父类
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(int layoutResID) {
        if (thatActivity == null) {
            super.setContentView(layoutResID);
        } else {
            thatActivity.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if (thatActivity == null) {
            super.setContentView(view);
        } else {
            thatActivity.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (thatActivity == null) {
            super.setContentView(view, params);
        } else {
            thatActivity.setContentView(view, params);
        }
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        if (thatActivity == null) {
            return super.getLayoutInflater();
        } else {
            return thatActivity.getLayoutInflater();
        }
    }

    @Override
    public Window getWindow() {
        if (thatActivity == null) {
            return super.getWindow();
        } else {
            return thatActivity.getWindow();
        }
    }

    @Override
    public View findViewById(int id) {
        if (thatActivity == null) {
            return super.findViewById(id);
        } else {
            return findViewById(id);
        }
    }


    @Override
    public ClassLoader getClassLoader() {
        if (thatActivity == null) {
            return super.getClassLoader();
        } else {
            return getClassLoader();
        }

    }

    @Override
    public WindowManager getWindowManager() {
        if (thatActivity == null) {
            return super.getWindowManager();
        } else {
            return thatActivity.getWindowManager();
        }
    }


    @Override
    public ApplicationInfo getApplicationInfo() {
        if (thatActivity == null) {
            return super.getApplicationInfo();
        } else {
            return thatActivity.getApplicationInfo();
        }
    }

    @Override
    public void attachContext(Activity context) {
        thatActivity = context;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void onBackPressed() {
        if (thatActivity == null) {
            super.onBackPressed();
        } else {
            thatActivity.onBackPressed();
        }
    }

    @Override
    public void finish() {
        if (thatActivity == null) {
            super.finish();
        } else {
            thatActivity.finish();
        }
    }

    /**
     * 注意上下文对象 thatActivity
     *
     * @param intent
     */
    @Override
    public void startActivity(Intent intent) {
        if (thatActivity == null) {
            super.startActivity(intent);
        } else {
            intent.putExtra("className", intent.getComponent().getClassName());
            thatActivity.startActivity(intent);
        }
    }
}
