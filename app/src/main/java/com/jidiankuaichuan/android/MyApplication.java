package com.jidiankuaichuan.android;

import android.app.Application;
import android.os.Build;

import com.jidiankuaichuan.android.utils.ToastUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtil.init(this);
        Constant.deviceName = Build.MODEL;
    }
}
