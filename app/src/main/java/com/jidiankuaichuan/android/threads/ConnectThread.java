package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread{
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);
    /** 客户端socket*/
    private final BluetoothSocket mSoket;
    /** 要连接的设备*/
    private final BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    /** 主线程通信的Handler*/
    private final Handler mHandler;
    /** 发送和接收数据的处理类*/
    private ReceiveThread mReceiveThread;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Handler mUIhandler) {
        mDevice = device;
        mBluetoothAdapter = bluetoothAdapter;
        mHandler = mUIhandler;

        BluetoothSocket tmp = null;
        try {
            // 创建客户端Socket
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSoket = tmp;
    }

    @Override
    public void run() {
        super.run();
        // 关闭正在发现设备.(如果此时又在查找设备，又在发送数据，会有冲突，影响传输效率)
        mBluetoothAdapter.cancelDiscovery();
        try {
            // 连接服务器
            mSoket.connect();
        } catch (IOException e) {
            // 连接异常就关闭
            mHandler.sendEmptyMessage(Constant.MSG_CONNECT_FAIL);
            try {
                mSoket.close();
            } catch (IOException e1) {
            }
            return;
        }
        mHandler.sendEmptyMessage(Constant.MSG_CONNECT_SUCCESS);
        manageConnectedSocket(mSoket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSoket) {

        // 新建一个线程进行通讯,不然会发现线程堵塞
        mReceiveThread = new ReceiveThread(mmSoket,mHandler);
        mReceiveThread.start();
    }

    /**
     * 关闭当前客户端
     */
    public void cancle() {
        try {
            mSoket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
