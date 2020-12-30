package com.leather.plugindemo.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.leather.plugindemo.ProxyActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class HookUtils {
    public static final String TARGET_INTENT = "target_intent";
    public static final String TAG = "Jack";

    public static void hookAMS() {
        try {
            Field enterField;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {//API 26以后走这个方法
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                //Field IActivityManagerSingleton
                enterField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            } else {//以前老版本走这个方法
                Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
                enterField = activityManagerNative.getDeclaredField("gDefault");
            }

            enterField.setAccessible(true);
            //Singleton<IActivityManager>的实例，因为IActivityManagerSingleton是静态的
            Object singletonObject = enterField.get(null);

            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            // 获取singletonObject中变量mInstance的值即IActivityManager类型的实例
            final Object mIActivityManagerObject = mInstanceField.get(singletonObject);

            //IActivityManager 是接口，通过动态代理来处理
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");

            //生产IActivityManager的代理对象
            Object mIActivityManagerProxy = Proxy.newProxyInstance(
                    classLoader, new Class[]{iActivityManagerInterface}, new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
                            Log.e(TAG, "invoke:   ~~~~~~ " + method.getName());

                            //这个invoke方法会多次回调，因为IActivityTaskManager接口中不止有一个方法
                            //我们需要判断找到我们要hook的方法做处理，其他方法照常走以前的逻辑
                            if (TextUtils.equals("startActivity", method.getName())) {
                                Intent targetIntent = null;
                                int index = 0;
                                //遍历参数列表，找到我们需要替换的参数，这里也就是intent
                                for (int i = 0; i < objects.length; i++) {
                                    if (objects[i] instanceof Intent) {
                                        index = i;
                                        targetIntent = (Intent) objects[i];
                                        break;
                                    }
                                }

                                ComponentName component = targetIntent.getComponent();
                                //因为启动的Activity有可能是宿主中的，这种情况我们是不需要做替换的
                                // 所以这里我们做个判断，如果不是宿主中的，即只有是插件中的才做替换
                                if (component != null && component.getClassName().contains("com.leather.plugin")) {
                                    //创建我们自己的代理intent，然后把我们的代理Activity设置给这个intent
                                    Intent proxyIntent = new Intent();
                                    String packageName = component.getPackageName();
                                    proxyIntent.setComponent(new ComponentName(packageName,
                                            ProxyActivity.class.getName()));
                                    //这里将targetIntent作为参数携带在proxyIntent上，AMS校验完后我们需要将代理Activity
                                    // 替换为我们的目标Activity，才能正常启动目标Activity
                                    proxyIntent.putExtra(TARGET_INTENT, targetIntent);
                                    //替换目标参数
                                    objects[index] = proxyIntent;
                                }

                            }

                            return method.invoke(mIActivityManagerObject, objects);
                        }
                    });

            //把我们的代理对象融入到framework
            //IActivityManager 在源码中是AIDL
            mInstanceField.set(singletonObject, mIActivityManagerProxy);

        } catch (Exception e) {
            Log.e(TAG, "hookIActivityManager: " + e.getMessage());
            e.printStackTrace();
            Log.d(TAG, "hookAMS : " + e.getMessage());
        }

    }

    public static void hookHandler() {
        try {
            Class<?> actThreadCls = Class.forName("android.app.ActivityThread");
            //反射获取ActivityThread的成员变量sCurrentActivityThread
            Field actThreadClsField = actThreadCls.getDeclaredField("sCurrentActivityThread");
            if (!actThreadClsField.isAccessible()) {
                actThreadClsField.setAccessible(true);
            }
            //获取ActivityThread的实例对象,因为是静态的所以get传null可以
            Object actThreadObj = actThreadClsField.get(null);
            //反射获取H类，Handler
            Field mHField = actThreadCls.getDeclaredField("mH");
            if (!mHField.isAccessible()) {
                mHField.setAccessible(true);
            }
            Object mHObj = mHField.get(actThreadObj);

            Class<?> handlerCls = Class.forName("android.os.Handler");
            Field mCallbackField = handlerCls.getDeclaredField("mCallback");
            if (!mCallbackField.isAccessible()) {
                mCallbackField.setAccessible(true);
            }
            //hook替换系统的Handler.Callback为我们自定义的Handler.Callback
            mCallbackField.set(mHObj, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    try {
                        if (msg.what == 159) {
                            Object transObj = msg.obj;
                            Field actCallbacksField = transObj.getClass().getDeclaredField("mActivityCallbacks");
                            if (!actCallbacksField.isAccessible()) {
                                actCallbacksField.setAccessible(true);
                            }

                            List<Object> clientTransItems = (List<Object>) actCallbacksField.get(transObj);
                            Object launchAcItem = null;
                            if (null == clientTransItems) {
                                return false;
                            }

                            //遍历事务集合，找到启动Activity的事务对象
                            for (Object item : clientTransItems) {
                                if (item.getClass().getName().contains("android.app.servertransaction.LaunchActivityItem")) {
                                    launchAcItem = item;
                                    break;
                                }
                            }

                            if (null == launchAcItem) {
                                return false;
                            }

                            //反射获取
                            Field intentField = launchAcItem.getClass().getDeclaredField("mIntent");
                            intentField.setAccessible(true);
                            //这个intent就是我们前面hook替换的我们自己的代理intent
                            Intent proxyIntent = (Intent) intentField.get(launchAcItem);
                            if (proxyIntent != null) {
                                Intent targetIntent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                if (targetIntent != null && targetIntent.getComponent() != null) {
                                    //判断下是否是我们之前hook的代理Activity
                                    String className = targetIntent.getComponent().getClassName();
                                    if (TextUtils.equals(className, "com.leather.plugindemo.ProxyActivity")) {
                                        intentField.set(launchAcItem, targetIntent);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "handleMessage : " + e.getMessage());
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hookHandler : " + e.getMessage());
        }
    }
}
