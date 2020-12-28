package com.leather.plugin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class PluginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);

        TextView tvContent = findViewById(R.id.tv_content);
        String string = mContext.getString(R.string.app_name);
        tvContent.setText(string);

        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }
}
