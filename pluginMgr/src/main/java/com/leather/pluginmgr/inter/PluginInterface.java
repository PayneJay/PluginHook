package com.leather.pluginmgr.inter;

import android.app.Activity;
import android.os.Bundle;

/**
 * 符合加载标准（Activity生命周期 + Context注入）
 */
public interface PluginInterface {
    void onCreate(Bundle saveInstance);

    void onStart();

    void onRestart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    /**
     * 注入context
     *
     * @param context 上下文
     */
    void attachContext(Activity context);
}
