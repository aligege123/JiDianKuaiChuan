package com.jidiankuaichuan.android.chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.utils.MyLog;

import java.io.IOException;
import java.util.UUID;

public class ChatAcceptThread extends Thread{

    private static final String TAG = "ChatAcceptThread";

    //connection name
    private static final String NAME = "BluetoothClass";

    //UUID
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);

    //server socket
    private final BluetoothServerSocket mmServerSocket;

    //handler
    private final Handler mHandler;

    private ChatThread mChatThread;

    private Boolean isClosed = false;

    public ChatAcceptThread(BluetoothAdapter adapter, Handler handler) throws IOException {
        this.mHandler = handler;
        BluetoothServerSocket tmp = null;
        // get server socket
        try {
            tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            MyLog.e(TAG, "server listen failed");
        }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        super.run();
        // client socket
        BluetoothSocket socket = null;
        MyLog.e(TAG, "start listening");
        while (true){
            try {
                socket =  mmServerSocket.accept();
            } catch (IOException e) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(Constant.MSG_ERROR);
                }
                e.printStackTrace();
                break;
            }

            if(socket != null) {
                //send message
                if (mHandler != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", socket.getRemoteDevice().getName());
                    bundle.putString("address", socket.getRemoteDevice().getAddress());
                    Message message = new Message();
                    message.setData(bundle);
                    message.what = Constant.MSG_CONNECT_SUCCESS;
                    mHandler.sendMessage(message);
                }
                close();
                manageConnectSocket(socket);
                break;
            }
        }
    }

    /**
     * @param socket
     */
    private void manageConnectSocket(BluetoothSocket socket) {
        if (mChatThread == null) {
            mChatThread = new ChatThread(socket, mHandler);
            mChatThread.start();
        }
    }

    /**
     * cancel server listening
     */
    private void close() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
            isClosed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * close conneted socket
     */
    public void cancel() {
        if (!isClosed) {
            close();
        }
        if (mChatThread != null) {
            mChatThread.cancel();
            mChatThread = null;
        }
    }

    /**
     * send data
     * @param data
     */
    public void sendData(byte[] data){
        if(mChatThread != null) {
            mChatThread.write(data);
        }
    }

    public void setOnReceiveListener(ChatThread.OnReceiveListener onReceiveListener) {
        if (mChatThread != null) {
            mChatThread.setListener(onReceiveListener);
        }
    }

}
