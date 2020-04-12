package com.jidiankuaichuan.android;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.jidiankuaichuan.android.utils.ToastUtil;

import org.litepal.LitePal;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtil.init(this);
        Constant.deviceName = Build.MODEL;
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext() {
        return context;
    }
}
