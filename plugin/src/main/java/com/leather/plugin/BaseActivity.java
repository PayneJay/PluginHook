package com.leather.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        Resources resources = LoadResourceUtils.loadResource(getApplicationContext(),
                Environment.getExternalStorageDirectory().getPath() + "/plugin-debug.apk");

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
}
