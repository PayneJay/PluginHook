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
}
