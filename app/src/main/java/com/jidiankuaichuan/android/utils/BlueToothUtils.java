package com.jidiankuaichuan.android.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Method;
import java.util.Set;

public class BlueToothUtils {

    private static final String TAG = "BlueToothUtil";

    private static BlueToothUtils instance;

    private BluetoothAdapter mBluetoothAdapter;

    private BlueToothUtils() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BlueToothUtils getInstance() {
        if (instance == null) {
            instance = new BlueToothUtils();
        }
        return instance;
    }

    /**
     * 设备是否支持蓝牙  true为支持
     * @return
     */
    public boolean isSupportBlue(){
        return mBluetoothAdapter != null;
    }

    /**
     * 蓝牙是否打开   true为打开
     * @return
     */
    public boolean isBlueEnable(){
        return isSupportBlue() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 自动打开蓝牙（异步：蓝牙不会立刻就处于开启状态）
     * 这个方法打开蓝牙不会弹出提示
     */
    public void openBlueAsyn(){
        if (isSupportBlue()) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * 自动打开蓝牙（同步）
     * 这个方法打开蓝牙会弹出提示
     * 需要在onActivityResult 方法中判断resultCode == RESULT_OK  true为成功
     */
    public void openBlueSync(Activity activity, int requestCode){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 关闭蓝牙
     */
    public void closeBlueAsyn() {
        mBluetoothAdapter.disable();
    }

    /**
     * 设置设备可以被搜索
     */
    public void setCanBeDiscovered(Context context) {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 设置被发现时间，最大值是3600秒,0表示设备总是可以被发现的(小于0或者大于3600则会被自动设置为120秒)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }


    /**
     * 搜索设备
     */
    public void discover() {
        if (!mBluetoothAdapter.isDiscovering()) {//判断是否正在搜索
            mBluetoothAdapter.startDiscovery();//开始搜索附近设备
        } else {
            mBluetoothAdapter.cancelDiscovery();//停止搜索
        }
    }

    /**
     * 停止搜索
     */
    public void cancelDiscover() {
        mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * 判断是否正在搜索
     */
    public Boolean isDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    /**
     * 获取已配对的蓝牙设备
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    /**
     * 获取对方的设备
     */
    public BluetoothDevice getRemoteDevice(String adress) {
        return mBluetoothAdapter.getRemoteDevice(adress);
    }

    /**
     * 获取蓝牙状态
     */
    public int getState() {
        return mBluetoothAdapter.getState();
    }

    /**
     * set BT device name
     */
    public void setName(String name) {
        mBluetoothAdapter.setName(name);
    }

    /**
     * 与设备配对
     */
    public boolean createBond(Class btClass,BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对
     */
    public boolean removeBond(Class btClass,BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
}
