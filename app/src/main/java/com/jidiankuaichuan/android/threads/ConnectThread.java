package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.utils.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectThread extends Thread{

    private static final String TAG = "ConnectThread";

    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);
    /** 客户端socket*/
    private BluetoothSocket mSocket;
    /** 要连接的设备*/
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    /** 主线程通信的Handler*/
    private Handler mHandler;
    /** 发送和接收数据的处理类*/
    private ReceiveThread mReceiveThread;

    private List<SendThread> sendThreadList = new ArrayList<>();

    public List<SendThread> getSendThreadList() {
        return sendThreadList;
    }

    //锁
    private Object lock = new Object();

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Handler mUIhandler) {
        BluetoothSocket tmp = null;
        try {
            // 创建客户端Socket
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDevice = device;
        mBluetoothAdapter = bluetoothAdapter;
        mHandler = mUIhandler;
        mSocket = tmp;
    }

    public void setHandler(Handler handler) {
        if (mReceiveThread != null) {
            mReceiveThread.setHandler(handler);
        }
    }

    public void setOnSendListener(SendThread.OnSendListener onSendListener) {
        if (sendThreadList.size() > 0) {
            for (SendThread sendThread : sendThreadList) {
                sendThread.setOnSendListener(onSendListener);
            }
        }
    }

    public void setOnReceiveListener(ReceiveThread.OnReceiveListener onReceiveListener) {
        if (mReceiveThread != null) {
            mReceiveThread.setOnReceiveListener(onReceiveListener);
        }
    }

    @Override
    public void run() {
        super.run();
        // 关闭正在发现设备.(如果此时又在查找设备，又在发送数据，会有冲突，影响传输效率)
        mBluetoothAdapter.cancelDiscovery();
        try {
            // 连接服务器
            mSocket.connect();
        } catch (IOException e) {
            // 连接异常就关闭
            e.printStackTrace();
            mHandler.sendEmptyMessage(Constant.MSG_CONNECT_FAIL);
            try {
                mSocket.close();
            } catch (IOException e1) {
            }
            return;
        }
        mHandler.sendEmptyMessage(Constant.MSG_CONNECT_SUCCESS);
    }

    public void createReceiveThread(Handler handler) {

        if (isConnected()) {
            // 新建一个线程进行通讯,不然会发现线程堵塞
            MyLog.d(TAG, "开启客户端接收线程");
            mReceiveThread = new ReceiveThread(mSocket, handler);
            mReceiveThread.start();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected() {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (mDevice == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(mDevice);
    }

    /**
     * 关闭当前客户端
     */
    public void cancel() {
        try {
//            mReceiveThread.close();
            if (mReceiveThread != null) {
                mReceiveThread = null;
            }
            if (mSocket.isConnected()) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDeviceInfo(String name, int imageId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceName", name);
            jsonObject.put("deviceImageId", imageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        synchronized (lock) {
            try {
                DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
                out.writeInt(Constant.FLAG_MSG);
                out.writeUTF(jsonObject.toString());
                out.flush();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送文件
     */
    public void sendFile(List<FileBase> fileBaseList) {
        for (final FileBase fileBase : fileBaseList) {
            SendThread sendThread = new SendThread(mSocket, fileBase, lock);
            sendThreadList.add(sendThread);
            sendThread.start();
        }
    }

    /**
     * 发送断开信号
     */
    public void sendCloseFlag() {
        if (mSocket != null && mSocket.isConnected()) {
            synchronized (lock) {
                try {
                    DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
                    out.writeInt(Constant.FLAG_CLOSE);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否有文件在传输
     */
    public boolean hasFileTransporting() {
        boolean flag = false;
        if (sendThreadList.size() > 0) {
            for (SendThread s : sendThreadList) {
                if (s.isRunning()) {
                    flag = true;
                    break;
                }
            }
        }
        if (mReceiveThread != null) {
            if (mReceiveThread.isRuning()) {
                flag = true;
            }
        }
        return flag;
    }

}
