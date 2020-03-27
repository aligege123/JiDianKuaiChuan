package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

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

public class AcceptThread extends Thread{

    private static final String TAG = "AcceptThread";

    /** 连接的名称*/
    private static final String NAME = "BluetoothClass";
    /** UUID*/
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);
    /** 服务端蓝牙Sokcet*/
    private BluetoothServerSocket mServerSocket;
    private BluetoothAdapter mBluetoothAdapter;
    /** 线程中通信的更新UI的Handler*/
    private Handler mHandler;
    /** 监听到有客户端连接，新建一个线程单独处理，不然在此线程中会堵塞*/
    private ReceiveThread mReceiveThread;

    //管理发送线程
    private List<SendThread> sendThreadList = new ArrayList<>();

    public List<SendThread> getSendThreadList() {
        return sendThreadList;
    }

    private BluetoothSocket mSocket;

    private boolean isListening = false;

    //锁
    private Object lock = new Object();

    public AcceptThread(BluetoothAdapter adapter, Handler handler) throws IOException {
        mBluetoothAdapter = adapter;
        this.mHandler = handler;

        // 获取服务端蓝牙socket
        mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
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
        // 连接的客户端socket
        BluetoothSocket socket = null;
//        isListening = true;
        // 服务端是不退出的,要一直监听连接进来的客户端，所以是死循环
        while (true){

            try {
                // 获取连接的客户端socket
                socket =  mServerSocket.accept();
            } catch (IOException e) {
                // 通知主线程更新UI, 获取异常
                mHandler.sendEmptyMessage(Constant.MSG_ERROR);
                e.printStackTrace();
                // 服务端退出一直监听线程
                break;
            }

            if(socket != null) {
                // 管理连接的客户端socket
                mSocket = socket;
                manageConnectSocket(socket);
            }
        }
    }

    /**
     * 管理连接的客户端socket
     * @param socket
     */
    private void manageConnectSocket(BluetoothSocket socket) {
        // 只支持同时处理一个连接
        // mConnectedThread不为空,踢掉之前的客户端
//        if(mReceiveThread != null) {
//            mReceiveThread.cancle();
//        }

        // 新建一个线程,处理客户端发来的数据
        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread(socket, mHandler);
            mReceiveThread.start();
        }
    }

    public void restartReceiveThread() {
        //关闭上一个thread和socket
        if (mReceiveThread != null) {
            mReceiveThread = null;
            mSocket = null;
        }
    }

    /**
     * 断开服务端，结束监听
     */
    public void cancel() {
        try {
            if (mReceiveThread != null) {
                mReceiveThread.close();
                mReceiveThread = null;
            }
            mServerSocket.close();
//            isListening = false;
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送设备名和头像
     */
    public void sendDeviceInfo(String name, int imageId) {
        MyLog.d(TAG, "服务端发送设备信息");
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
        for (FileBase fileBase : fileBaseList) {
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
