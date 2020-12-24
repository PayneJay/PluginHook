package com.leather.plugindemo.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadUtils {

    /******************************************************************************************************
     * 这个方法的目的是将插件中的dexElements[]提取出来，和宿主的dexElements[]进行合并，然后将合并后的新数组重新赋值给
     * 宿主的dexElements[]，这样我们在宿主中就能加载到插件中的类了
     ***********************************************************************************************
     * 无论是PathClassLoader还是DexClassLoader，它们都是继承自BaseDexClassLoader，而本身却没有实现findClass()
     * 方法，因此通过它们加载类的时候实际上都是调用的BaseDexClassLoader中的findClass()方法
     ***********************************************************************************************
     * 加载插件类需要先拿到类对应的dex文件，而dex文件又需要拿到dexElements[]数组，dexElements又是DexPathList这个
     * 类中的一个成员变量，pathList（DexPathList的实例对象）又是BaseDexClassLoader中的一个成员变量，BaseDexClassLoader
     * 的findClass()方法实际上是调用了pathList的findClass()方法，pathList的findClass()方法中通过遍历dexElements
     * 进而调用Element的findClass()方法，才最终拿到类的Class
     *******************************************************************************************************
     * @param context 上下文
     */
    public static void loadClass(Context context, String path) {
        Log.d("jack", "context : " + context.toString() + ", path : " + path);
        try {
            //准备工作
            //首先拿到BaseDexClassLoader
            Class<?> baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            //通过反射获取成员变量pathList,并设置
            Field pathList = baseDexClassLoader.getDeclaredField("pathList");
            if (!pathList.isAccessible()) {
                //如果是private的私有属性的话，必须加这一句，否则无法反射
                pathList.setAccessible(true);
            }
            //获取DexPathList
            Class<?> dexPathList = Class.forName("dalvik.system.DexPathList");
            //通过反射获取DexPathList中的成员变量dexElements[]
            Field dexElements = dexPathList.getDeclaredField("dexElements");
            if (!dexElements.isAccessible()) {
                dexElements.setAccessible(true);
            }

            //获取宿主ClassLoader
            PathClassLoader hostClassLoader = (PathClassLoader) context.getClassLoader();
            /*
             * 创建加载插件的类加载器，获取其中的dexElements[]数组
             */
            //这里的context.getClassLoader()获取到的实际上就是宿主的类加载器，因为是通过宿主的上下文获取的
            DexClassLoader dexClassLoader = new DexClassLoader(path,
                    context.getCacheDir().getAbsolutePath(), null,
                    hostClassLoader);
            //获取插件ClassLoader中的pathList的值
            Object pluginPathList = pathList.get(dexClassLoader);
            //获取插件中的dexElements数组的值，这里我们直接强转为数组类型
            Object[] pluginDexElements = (Object[]) dexElements.get(pluginPathList);

            /*
             * 获取宿主中的dexElements[]数组
             */
            //获取宿主ClassLoader中的pathList的值
            Object hostPathList = pathList.get(hostClassLoader);
            //获取宿主中的dexElements数组的值
            Object[] hostDexElements = (Object[]) dexElements.get(hostPathList);

            if (hostDexElements != null && pluginDexElements != null) {
                //创建新的dexElements数组，长度为宿主+插件的dexElements和
                Object[] newDexElements = (Object[]) Array.newInstance(pluginDexElements.getClass().getComponentType(),
                        hostDexElements.length + pluginDexElements.length);
                //将宿主和插件的dexElements拷贝到新的数组中
                //从hostDexElements的第0个位置往newDexElements的第0个位置开始拷贝，长度为hostDexElements的长度
                System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexElements.length);
                System.arraycopy(pluginDexElements, 0, newDexElements, hostDexElements.length, pluginDexElements.length);

                //将新数组的值赋给宿主的dexElements
                dexElements.set(hostPathList, newDexElements);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("jack", "loadClass Exception : " + e.getMessage());
        }
    }
}
