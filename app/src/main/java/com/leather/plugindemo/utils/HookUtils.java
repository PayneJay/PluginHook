package com.leather.plugindemo.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtils {
    public static final String TARGET_INTENT = "target_intent";

    public static void hookAMS() {
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
    }
}
