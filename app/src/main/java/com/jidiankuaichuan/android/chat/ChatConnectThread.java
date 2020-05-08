package com.jidiankuaichuan.android.chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.utils.BlueToothUtils;

import java.io.IOException;
import java.util.UUID;

public class ChatConnectThread extends Thread{

    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);

    //client socket
    private final BluetoothSocket mmSoket;

    private final Handler mHandler;

    private ChatThread mChatThread;

    private BluetoothDevice mDevice;

    public ChatConnectThread(BluetoothDevice device, Handler mUIhandler) {
        mHandler = mUIhandler;
        mDevice = device;
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSoket = tmp;
    }

    @Override
    public void run() {
        super.run();
        //shutdown discover
        if (BlueToothUtils.getInstance().isDiscovering()) {
            BlueToothUtils.getInstance().cancelDiscover();
        }
        try {
            //connect server
            mmSoket.connect();
        } catch (IOException e) {
            //can't connet to server
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Constant.MSG_CONNECT_FAIL);
            }
            try {
                mmSoket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        //success
        if (mHandler != null) {
            Bundle bundle = new Bundle();
            bundle.putString("name", mDevice.getName());
            bundle.putString("address", mDevice.getAddress());
            Message message = new Message();
            message.setData(bundle);
            message.what = Constant.MSG_CONNECT_SUCCESS;
            mHandler.sendMessage(message);
        }
        manageConnectedSocket();
    }

    private void manageConnectedSocket() {
        mChatThread = new ChatThread(mmSoket,mHandler);
        mChatThread.start();
    }

    /**
     * client exit
     */
    public void cancel() {
        try {
            if (mChatThread != null) {
                mChatThread.cancel();
                mChatThread = null;
            }
            if (mmSoket.isConnected()) {
                mmSoket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send data
     * @param data
     */
    public void sendData(byte[] data) {
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
