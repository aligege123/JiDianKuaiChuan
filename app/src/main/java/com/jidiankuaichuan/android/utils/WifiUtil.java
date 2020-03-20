package com.jidiankuaichuan.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.ResultReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
public class WifiUtil {

    private static final String TAG = "WifiUtil";

    private WifiManager mWifiManager;

    private WifiInfo mWifiInfo;

    private WifiManager.WifiLock mWifiLock;

    private ConnectivityManager mConnectivityManager;

    private Context mContext;

    public WifiUtil(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mContext = context;
    }

    /**
     * Android 8.0 前打开热点
     * @return
     */
    public boolean createHotSpot() {
        //关闭wifi
        closeWifi();

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "AndroidShare_1234";
        config.preSharedKey = "1234";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(mWifiManager, config, true);
            if (!enable) {
                //创建热点失败
                MyLog.d(TAG, "创建热点失败");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.d(TAG, "创建热点异常");
        }
        return false;
    }

    /**
     * Android 8.0 后打开热点
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean startTethering() {
        //关闭wifi
        closeWifi();

        if (mConnectivityManager != null) {
            try {
                //反射机制，获取ConnectivityManager类的领域,当需要获取非public方法时，就需要获得field
                Field internalConnectivityManagerField = ConnectivityManager.class.getDeclaredField("mService");
                //让private成员变量可以访问
                internalConnectivityManagerField.setAccessible(true);
                //配置wificonfig
                WifiConfiguration config = new WifiConfiguration();
                if (config != null) {
                    config.SSID = "AndroidShare_1234";
                    config.preSharedKey = "1234";
                    config.hiddenSSID = true;
                    config.allowedAuthAlgorithms
                            .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    config.allowedPairwiseCiphers
                            .set(WifiConfiguration.PairwiseCipher.TKIP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    config.allowedPairwiseCiphers
                            .set(WifiConfiguration.PairwiseCipher.CCMP);
                    config.status = WifiConfiguration.Status.ENABLED;
                }

                //sb用来保存异常的信息
                //StringBuffer sb = new StringBuffer();
                //获取ConnectivityManager的class文件
                Class internalConnectivityManagerClass = Class.forName("android.net.ConnectivityManager");

                //线程间通信机制
                ResultReceiver dummyResultReceiver = new ResultReceiver(null);
                try {
                    //反射获取setWifiApConfiguration的方法
                    Method mMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                    //调用setWifiApConfiguration方法
                    mMethod.invoke(mWifiManager, config);
                    //反射获取startTethering
                    Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                    startTetheringMethod.invoke(internalConnectivityManagerClass, 0, dummyResultReceiver, true);
                } catch (NoSuchMethodException e) {
                    //部分机型要多一个参数
                    try {
                        Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering", int.class, ResultReceiver.class, boolean.class, String.class);
                        startTetheringMethod.invoke(internalConnectivityManagerClass, 0, dummyResultReceiver, false, mContext.getPackageName());
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                        MyLog.e(TAG, "反射获取方法后调用invoke函数异常(2)");
                        return false;
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    MyLog.e(TAG, "反射获取方法后调用invoke函数异常(1)");
                    return false;
                }
            } catch (Exception e) {
                //
                e.printStackTrace();
                MyLog.e(TAG, "反射调用startTethering函数异常");
                return false;
            }
        }

        return true;
    }

    public String getMacAddress() {

        String macAddress = null;

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            mWifiManager.setWifiEnabled(false);
        }

        if (mWifiInfo != null) {
            macAddress = mWifiInfo.getMacAddress();
        }

        return macAddress;
    }

    /**
     * 打开wifi
     */
    public void openWifi() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled() == false) {
            //如果wifi处于打开状态，则关闭wifi,
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭wifi
     */
    public void closeWifi() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled() == true) {
            //如果wifi处于打开状态，则关闭wifi,
            mWifiManager.setWifiEnabled(false);
        }
    }

}
