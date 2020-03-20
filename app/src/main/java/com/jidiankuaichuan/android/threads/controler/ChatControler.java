package com.jidiankuaichuan.android.threads.controler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.AcceptThread;
import com.jidiankuaichuan.android.threads.ConnectThread;

import java.io.IOException;
import java.util.List;


public class ChatControler {

    private AcceptThread mAcceptThread;

    private ConnectThread mConnectThread;

    private List<FileBase> fileBaseList;

    private static ChatControler instance;

    private ChatControler() {}

    public static synchronized ChatControler getInstance() {
        if (instance == null) {
            instance = new ChatControler();
        }
        return instance;
    }


    //与服务器进行聊天
    public void startChatWith(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        mConnectThread = new ConnectThread(device, adapter, handler);
        mConnectThread.start();
    }

    //等待客户端连接
    public void waitForClient(BluetoothAdapter adapter, Handler handler) {
        try {
            mAcceptThread = new AcceptThread(adapter, handler);
            mAcceptThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //关闭线程
    public void stopChat() {

    }

    //发送设备名和头像
    public void sendDeviceInfo(String name, int headImageId) {

    }

    //发送文件
    public void sendFile() {

    }

    //设置发送监听
    public void setOnSendLisnter() {

    }

    //设置接收监听
    public void setOnReceiveListener() {

    }
}
