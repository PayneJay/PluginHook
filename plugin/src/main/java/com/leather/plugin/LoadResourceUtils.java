package com.leather.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Method;

public class LoadResourceUtils {
    /**
     * 加载资源
     * 通过反射调用AssetManager的addAssetPath方法，去加载指定路径下的资源
     *
     * @param path 资源路径
     * @return 返回我们自己的Resources，用来加载插件中的资源
     */
    public static Resources loadResource(Context context, String path) {
        Resources skinResources = null;
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(assetManager, path);

            Resources appResources = context.getApplicationContext().getResources();
            //用来加载资源包的资源对象
            skinResources = new Resources(assetManager, appResources.getDisplayMetrics(), appResources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jack", "loadResource Exception : " + e.getMessage());
        }

        return skinResources;
    }
}
