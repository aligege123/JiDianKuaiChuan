package com.jidiankuaichuan.android.chat.model;

import android.bluetooth.BluetoothDevice;

public class Friend {

    private BluetoothDevice device;

    private String state = "未连接";

    private String message = "";

    public Friend(BluetoothDevice device) {
        this.device = device;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
