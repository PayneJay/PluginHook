package com.leather.plugin;

import android.os.Bundle;
import android.util.Log;

public class PluginActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);

        Log.e("Jack", getClass().getName() + " onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("Jack", getClass().getName() + " onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Jack", getClass().getName() + " onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Jack", getClass().getName() + " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Jack", getClass().getName() + " onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Jack", getClass().getName() + " onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Jack", getClass().getName() + " onDestroy");
    }
}
