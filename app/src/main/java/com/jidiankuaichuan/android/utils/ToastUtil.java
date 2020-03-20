package com.jidiankuaichuan.android.utils;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Application app;

    private ToastUtil() {
    }

    public static void init(Application app) {
        ToastUtil.app = app;
    }

    public static void s(String msg) {
        if (app == null) return;
        s(app, msg);
    }

    public static void l(String msg) {
        if (app == null) return;
        l(app, msg);
    }

    public static void s(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static void l(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
