package com.leather.plugindemo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 代理Activity，用来在目标Activity启动的时候代替目标Activity进行AMS的校验
 * 1、在进入AMS之前，找HOOK点，将目标Activity（插件中的Activity）替换为ProxyActivity；
 * 2、在从AMS出来之后，找HOOK点，将ProxyActivity替换为目标Activity；
 */
public class ProxyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
    }
}
