package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.utils.MyLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread{

    /** 当前连接的客户端BluetoothSocket*/
    private final BluetoothSocket mSokcet;
    /** 读取数据流*/
    private final InputStream mInputStream;
    /** 发送数据流*/
    private final OutputStream mOutputStream;
    /** 与主线程通信Handler*/
    private Handler mHandler;
    private String TAG = "ReceiveThread";

    public ReceiveThread(BluetoothSocket socket, Handler handler) {
        mSokcet = socket;
        mHandler = handler;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInputStream = tmpIn;
        mOutputStream = tmpOut;
    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                // 读取数据
                int bytes = mInputStream.read(buffer);

                if(bytes > 0) {
                    String data = new String(buffer,0,bytes,"utf-8");
                    // 把数据发送到主线程, 此处还可以用广播
                    Message message = mHandler.obtainMessage(Constant.MSG_GOT_DATA,data);
                    mHandler.sendMessage(message);
                }
                MyLog.d(TAG, "messge size :" + bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 踢掉当前客户端
    public void cancle() {
        try {
            mSokcet.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
