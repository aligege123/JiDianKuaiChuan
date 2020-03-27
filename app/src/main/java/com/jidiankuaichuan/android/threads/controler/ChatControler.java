package com.jidiankuaichuan.android.threads.controler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.AcceptThread;
import com.jidiankuaichuan.android.threads.ConnectThread;
import com.jidiankuaichuan.android.threads.ReceiveThread;
import com.jidiankuaichuan.android.threads.SendThread;
import com.jidiankuaichuan.android.utils.MyLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChatControler {

    private static final String TAG = "ChatControler";

    private AcceptThread mAcceptThread;

    private ConnectThread mConnectThread;

    private List<FileBase> fileSendList = new ArrayList<>();

    private List<FileBase> filesToBeSendList = new ArrayList<>();

    private int fileSendCount = 0;

    private List<FileBase> fileReceiveList = new ArrayList<>();

    private static ChatControler instance;

    private ChatControler() {}

    public static synchronized ChatControler getInstance() {
        if (instance == null) {
            instance = new ChatControler();
        }
        return instance;
    }

    public void AddFileSendList(List<FileBase> fileSendList) {
        this.fileSendList.addAll(fileSendList);
        fileSendCount += fileSendList.size();
    }

    public void AddFileReceiveList(List<FileBase> fileReceiveList) {
        this.fileReceiveList.addAll(fileReceiveList);
    }

    //与服务器进行聊天
    public void startChatWith(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        mConnectThread = new ConnectThread(device, adapter, handler);
        mConnectThread.start();
    }

    //开启客户端接收线程
    public void startClientReceive(Handler handler) {
        if (mConnectThread != null) {
            mConnectThread.createReceiveThread(handler);
        }
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

    public void setHandler(Handler handler) {
        if (mConnectThread != null) {
            mConnectThread.setHandler(handler);
        } else if(mAcceptThread != null){
            mAcceptThread.setHandler(handler);
        }
    }

    //关闭线程
    public void stopChat() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        } else if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    //关闭服务端的监听
    public void restartAcceptReceive() {
        if (mAcceptThread != null) {
            mAcceptThread.restartReceiveThread();
        }
    }

    //判断线程是否在运行
    public Boolean isAlive() {
        if (mConnectThread != null) {
            return mConnectThread.isAlive();
        } else if (mAcceptThread != null) {
            return mAcceptThread.isAlive();
        }
        return false;
    }

    //发送设备名和头像
    public void sendDeviceInfo(String name, int headImageId) {
        if (mConnectThread != null) {
            mConnectThread.sendDeviceInfo(name, headImageId);
        } else if (mAcceptThread != null) {
            mAcceptThread.sendDeviceInfo(name, headImageId);
        }
    }

    //发送断开连接信号
    public void sendCloseFlag() {
        if (mConnectThread != null) {
            mConnectThread.sendCloseFlag();
        } else if (mAcceptThread != null) {
            mAcceptThread.sendCloseFlag();
        }
    }

    //发送文件
    public void sendFile(List<FileBase> fileBaseList) {
        for (FileBase f : fileBaseList) {
            f.setId(fileSendCount);
            ++fileSendCount;
        }
        fileSendList.addAll(fileBaseList);
        if (mAcceptThread != null) {
            mAcceptThread.sendFile(fileBaseList);
//            filesToBeSendList.clear();
        } else if (mConnectThread != null) {
            mConnectThread.sendFile(fileBaseList);
//            filesToBeSendList.clear();
        }
    }

    //保存要发送的文件
    public void saveFilesToBeSend(List<FileBase> fileBaseList) {
        for (FileBase f : fileBaseList) {
            f.setId(fileSendCount);
            ++fileSendCount;
        }
//        filesToBeSendList = new ArrayList<>();
        filesToBeSendList.addAll(fileBaseList);
        fileSendList.addAll(fileBaseList);
    }


    public List<FileBase> getFileSendList() {
        return fileSendList;
    }

    public List<FileBase> getFileReceiveList() {
        return fileReceiveList;
    }

    public List<SendThread> getSendThreadList() {
        if (mAcceptThread != null) {
            return mAcceptThread.getSendThreadList();
        } else if (mConnectThread != null) {
            return mConnectThread.getSendThreadList();
        }
        return null;
    }

    public void setOnSendListener(SendThread.OnSendListener onSendListener) {
        if (mAcceptThread != null) {
            mAcceptThread.setOnSendListener(onSendListener);
        } else if (mConnectThread != null){
            mConnectThread.setOnSendListener(onSendListener);
        }
    }

    public void setOnReceiveListener(ReceiveThread.OnReceiveListener onReceiveListener) {
        if (mAcceptThread != null) {
            mAcceptThread.setOnReceiveListener(onReceiveListener);
        } else if (mConnectThread != null){
            mConnectThread.setOnReceiveListener(onReceiveListener);
        }
    }

    /**
     * 判断是否有文件在传输
     */
    public boolean hasFileTransporting() {
        if (mAcceptThread != null) {
            return mAcceptThread.hasFileTransporting();
        } else if (mConnectThread != null) {
            return mConnectThread.hasFileTransporting();
        }
        return false;
    }



}
