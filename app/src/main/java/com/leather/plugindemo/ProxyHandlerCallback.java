package com.leather.plugindemo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.leather.plugindemo.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.List;

import static com.leather.plugindemo.utils.HookUtils.TARGET_INTENT;

/**
 * 我们自己的用于Hook的Handler.Callback
 */
public class ProxyHandlerCallback implements Handler.Callback {
    @Override
    public boolean handleMessage(Message msg) {
        try {
            LogUtil.i("msg.what : " + msg.what + ",msg.obj : " + msg.obj);
            Object transObj = msg.obj;
            switch (msg.what) {
                case 100://API 28之前启动Activity会走public static final int LAUNCH_ACTIVITY = 100;这个case
                    //反射获取
                    Field intentField = transObj.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    //这个intent就是我们前面hook替换的我们自己的代理intent
                    Intent proxyIntent = (Intent) intentField.get(transObj);
                    if (proxyIntent != null) {
                        LogUtil.i("hookHandler proxyIntent : " + proxyIntent.getComponent().getClassName());
                        Intent targetIntent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                        if (targetIntent != null && targetIntent.getComponent() != null) {
                            //判断下是否是我们之前hook的代理Activity
                            String className = targetIntent.getComponent().getClassName();
                            LogUtil.i("hookHandler targetIntent : " + className);
                            if (className.contains("com.leather.plugin.")) {
                                intentField.set(transObj, targetIntent);
                            }
                        }
                    }
                    break;
                case 159://API 28及以后采用了状态模式，Activity的启动统一用了EXECUTE_TRANSACTION=159这个值
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

                    LogUtil.i("launchAcItem : " + launchAcItem.toString());
                    Field mIntentField = launchAcItem.getClass().getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);
                    Intent mIntent = (Intent) mIntentField.get(launchAcItem);
                    if (mIntent != null) {
                        LogUtil.i("hookHandler proxyIntent : " + mIntent.getComponent().getClassName());
                        Intent targetIntent = mIntent.getParcelableExtra(TARGET_INTENT);
                        if (targetIntent != null && targetIntent.getComponent() != null) {
                            //判断下是否是我们之前hook的代理Activity
                            String className = targetIntent.getComponent().getClassName();
                            LogUtil.i("hookHandler targetIntent : " + className);
                            if (className.contains("com.leather.plugin.")) {
                                mIntentField.set(launchAcItem, targetIntent);
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("handleMessage : " + e.getMessage());
        }
        return false;
    }
}
