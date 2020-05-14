package com.jidiankuaichuan.android.threads.controler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.AcceptThread;
import com.jidiankuaichuan.android.threads.ConnectThread;
import com.jidiankuaichuan.android.threads.ReceiveThread;
import com.jidiankuaichuan.android.threads.SendThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChatController {

    private static final String TAG = "ChatControler";

    private AcceptThread mAcceptThread;

    private ConnectThread mConnectThread;

    private List<FileBase> fileSendList = new ArrayList<>();

//    private List<FileBase> filesToBeSendList = new ArrayList<>();

    private int fileSendCount = 0;

    private List<FileBase> fileReceiveList = new ArrayList<>();

    private static ChatController instance;

    private ChatController() {}

    public static synchronized ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
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

    // connect server
    public void startChatWith(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        mConnectThread = new ConnectThread(device, adapter, handler);
        mConnectThread.start();
    }

    // client start receiving
    public void startClientReceive(Handler handler) {
        if (mConnectThread != null) {
            mConnectThread.createReceiveThread(handler);
        }
    }

    // server wait for client
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

    // break connection
    public void stopChat() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        } else if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    // restart server
    public void restartAcceptReceive(BluetoothAdapter adapter, Handler handler) {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        try {
            mAcceptThread = new AcceptThread(adapter, handler);
            mAcceptThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // if thread is alive
    public boolean isConnected() {
        if (mConnectThread != null) {
            return mConnectThread.isConnected();
        } else if (mAcceptThread != null) {
            return mAcceptThread.isConnected();
        }
        return false;
    }

    // send device name and head image
    public void sendDeviceInfo(String name, int headImageId) {
        if (mConnectThread != null) {
            mConnectThread.sendDeviceInfo(name, headImageId);
        } else if (mAcceptThread != null) {
            mAcceptThread.sendDeviceInfo(name, headImageId);
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
        } else if (mConnectThread != null) {
            mConnectThread.sendFile(fileBaseList);
        }
    }

    //保存要发送的文件
//    public void saveFilesToBeSend(List<FileBase> fileBaseList) {
//        for (FileBase f : fileBaseList) {
//            f.setId(fileSendCount);
//            ++fileSendCount;
//        }
//        filesToBeSendList.addAll(fileBaseList);
//        fileSendList.addAll(fileBaseList);
//    }


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
