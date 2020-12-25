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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                //Field IActivityManagerSingleton
                enterField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            } else {
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
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable {
                            Log.e(TAG, "invoke:   ~~~~~~ " + method.getName());

                            if ("startActivity".equals(method.getName())) {
                                Log.i(TAG, "准备启动activity");
                                Intent rawIntent = null;
                                int index = 0;
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        rawIntent = (Intent) args[i];
                                        index = i;
                                        break;
                                    }
                                }


                                // 将需要被启动的Activity替换成StubActivity
                                Intent newIntent = new Intent();
                                String stubPackage = "com.leather.plugindemo";
                                newIntent.setComponent(new ComponentName(stubPackage, ProxyActivity.class.getName()));
//                              newIntent.setClassName(rawIntent.getComponent().getPackageName(), StubActivity.class.getName());
                                //把这个newIntent放回到args,达到了一个欺骗AMS的目的
                                newIntent.putExtra(TARGET_INTENT, rawIntent);
                                args[index] = newIntent;

                            }

                            return method.invoke(mIActivityManagerObject, args);
                        }
                    });

            //把我们的代理对象融入到framework
            //IActivityManager 在源码中是AIDL
            mInstanceField.set(singletonObject, mIActivityManagerProxy);

        } catch (Exception e) {
            Log.e(TAG, "hookIActivityManager: " + e.getMessage());
            e.printStackTrace();

        }

    }

    public static void hookAMS1() {
        try {
            Class<?> iActivityManagerCls = Class.forName("android.app.IActivityTaskManager");
            Class<?> actManagerCls = Class.forName("android.app.ActivityTaskManager");
            //通过反射获取ActivityTaskManager中对成员变量IActivityTaskManagerSingleton
            Field singletonField = actManagerCls.getDeclaredField("IActivityTaskManagerSingleton");
            if (!singletonField.isAccessible()) {
                singletonField.setAccessible(true);
            }
            //因为IActivityTaskManagerSingleton是个静态变量，所以可以直接反射get得到
            Object singleObj = singletonField.get(null);

            Class<?> singletonCls = Class.forName("android.util.Singleton");
            Field instanceField = singletonCls.getDeclaredField("mInstance");
            if (!instanceField.isAccessible()) {
                instanceField.setAccessible(true);
            }
            final Object instanceObj = instanceField.get(singleObj);

            //动态代理IActivityTaskManager类，hook IActivityTaskManager中的startActivity方法
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{iActivityManagerCls},
                    new InvocationHandler() {
                        Intent targetIntent;

                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            //这个invoke方法会多次回调，因为IActivityTaskManager接口中不止有一个方法
                            //我们需要判断找到我们要hook的方法做处理，其他方法照常走以前的逻辑
                            if (TextUtils.equals(method.getName(), "startActivity")) {
                                int index = 0;
                                //遍历参数列表，找到我们需要替换的参数，这里也就是intent
                                for (int i = 0; i < objects.length; i++) {
                                    if (objects[i] instanceof Intent) {
                                        index = i;
                                        targetIntent = (Intent) objects[i];
                                        break;
                                    }
                                }

                                //创建我们自己的代理intent，然后把我们的代理Activity设置给这个intent
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.leather.plugindemo",
                                        "com.leather.plugindemo.ProxyActivity");
                                //这里将targetIntent作为参数携带在proxyIntent上，AMS校验完后我们需要将代理Activity
                                // 替换为我们的目标Activity，才能正常启动目标Activity
                                proxyIntent.putExtra(TARGET_INTENT, targetIntent);

                                ComponentName component = targetIntent.getComponent();
                                if (component != null) {
                                    String packageName = component.getPackageName();
                                    //因为启动的Activity有可能是宿主中的，这种情况我们是不需要做替换的
                                    // 所以这里我们做个判断，如果不是宿主中的，即只有是插件中的才做替换
                                    if (TextUtils.equals(packageName, "com.leather.plugin")) {
                                        //替换目标参数
                                        objects[index] = proxyIntent;
                                    }
                                }

                            }
                            return method.invoke(instanceObj, objects);
                        }
                    });
            //利用反射替换掉IActivityTaskManager对象为我们做过改动的对象
            instanceField.set(instanceObj, proxy);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jack", "hookAMS : " + e.getMessage());
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
                        Log.d("jack", "handleMessage : " + e.getMessage());
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jack", "hookHandler : " + e.getMessage());
        }
    }
}
