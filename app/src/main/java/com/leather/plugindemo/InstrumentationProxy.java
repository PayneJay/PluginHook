package com.leather.plugindemo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.leather.plugindemo.utils.LogUtil;

import java.lang.reflect.Method;

public class InstrumentationProxy extends Instrumentation {
    public static final String TARGET_INTENT = "target_intent";
    private final Instrumentation mInstrumentation;

    public InstrumentationProxy(Instrumentation instrumentation) {
        this.mInstrumentation = instrumentation;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        LogUtil.i("调用了代理类的execStartActivity");

        Intent proxyIntent;
        String className = intent.getComponent().getClassName();
        if (!TextUtils.isEmpty(className) && className.contains("com.leather.plugin")) {
            //如果这个intent挂的Activity是我们插件中的Activity的话，那我们就将它替换为我们的代理Activity
            proxyIntent = new Intent();
            String pkgName = ProxyActivity.class.getPackage().getName();
            String clsName = ProxyActivity.class.getName();
            LogUtil.i("pkgName  : " + pkgName + ", clsName : " + clsName);
            proxyIntent.setComponent(new ComponentName(pkgName, clsName));
            proxyIntent.putExtra(TARGET_INTENT, intent);
        } else {
            //如果是宿主中的Activity则用原本的intent
            proxyIntent = intent;
        }

        //反射调用execStartActivity
        try {
            Method declaredMethod = mInstrumentation.getClass().getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class,
                    int.class, Bundle.class);
            declaredMethod.setAccessible(true);

            return (ActivityResult) declaredMethod.invoke(mInstrumentation, who, contextThread, token, target,
                    proxyIntent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("hook execStartActivity " + e.getMessage());
        }
        return null;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) {
        Intent parcelableExtra = intent.getParcelableExtra(TARGET_INTENT);
        LogUtil.i("newActivity enter : " + className);
        try {
            if (null != parcelableExtra && null != parcelableExtra.getComponent()) {
                String intentName = parcelableExtra.getComponent().getClassName();
                if (!TextUtils.isEmpty(intentName)) {
                    //还原，将占坑activity还原成插件activity,这种方式还原是配合hook instrumentation方案
                    return super.newActivity(cl, intentName, intent);
                }
                LogUtil.i(parcelableExtra.getComponent() + "");
            }
            return super.newActivity(cl, className, intent);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("newActivity : " + e.getMessage());
        }

        return null;
    }
}
