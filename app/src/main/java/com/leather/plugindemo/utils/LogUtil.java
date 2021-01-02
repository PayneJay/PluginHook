package com.leather.plugindemo.utils;

import android.util.Log;

public class LogUtil {
    private static final String TAG = "Jack";

    public static void i(String msg) {
        Log.i(TAG, "info****************" + msg + "***************");
    }

    public static void e(String msg) {
        Log.e(TAG, "error****************" + msg + "***************");
    }
}
