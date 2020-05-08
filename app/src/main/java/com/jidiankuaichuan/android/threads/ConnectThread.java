package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.MyApplication;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.service.FileTransService;
import com.jidiankuaichuan.android.utils.BlueToothUtils;
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

    private BluetoothSocket mSocket;

    private BluetoothDevice mDevice;

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;

    private ReceiveThread mReceiveThread;

    private List<SendThread> sendThreadList = new ArrayList<>();

    public List<SendThread> getSendThreadList() {
        return sendThreadList;
    }

    private final Object lock = new Object();

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Handler mUIhandler) {
        BluetoothSocket tmp = null;
        try {
            // create client socket
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
        // cancel discovering
        if (BlueToothUtils.getInstance().isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        try {
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(Constant.MSG_CONNECT_FAIL);
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        mHandler.sendEmptyMessage(Constant.MSG_CONNECT_SUCCESS);
    }

    public void createReceiveThread(Handler handler) {
        if (isConnected()) {
            // new a ReceiveThread
            MyLog.d(TAG, "开启客户端接收线程");
            mReceiveThread = new ReceiveThread(mSocket, handler);
            mReceiveThread.start();
        }
    }

    /**
     * if is connected
     */
    public boolean isConnected() {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (mDevice == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(mDevice);
    }

    /**
     * quit
     */
    public void cancel() {
        try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * send files
     */
    public void sendFile(List<FileBase> fileBaseList) {
        for (final FileBase fileBase : fileBaseList) {
            SendThread sendThread = new SendThread(mSocket, fileBase, lock);
            sendThreadList.add(sendThread);
            sendThread.start();
        }
    }

    /**
     * if has file transporting
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
