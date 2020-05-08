package com.jidiankuaichuan.android.chat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.chat.model.Msg;
import com.jidiankuaichuan.android.utils.MyLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChatThread extends Thread {

    private static final String TAG = "ChatThread";

    private final BluetoothSocket mmSokcet;

    private final InputStream mmInputStream;

    private final OutputStream mmOutputStream;

    private Handler mHandler;

    private BluetoothDevice mDevice;

    private OnReceiveListener listener;

    public ChatThread(BluetoothSocket socket,Handler handler) {
        mmSokcet = socket;
        mHandler = handler;
        if (socket != null) {
            mDevice = socket.getRemoteDevice();
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInputStream = tmpIn;
        mmOutputStream = tmpOut;
    }

    public void setListener(OnReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                //read data
                int bytes = mmInputStream.read(buffer);

                if(bytes > 0) {
                    String data = new String(buffer,0,bytes,"utf-8");
                    MyLog.e(TAG, data);
                    //send to activity
                    if (mHandler != null) {
                        Message message = mHandler.obtainMessage(Constant.MSG_GOT_DATA, data);
                        mHandler.sendMessage(message);
                    }
                    //callback to ChatActivity, onReceive()
                    if (listener != null) {
                        listener.onReceive(data);
                    }
                    //save message
                    Msg msg = new Msg(data, Msg.TYPE_RECEIVED);
                    msg.setAddress(BlueToothChatControler.getInstance().getFriendAddress());
                    msg.save();
                }
            } catch (IOException e) {
                //connection break, send error message
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(Constant.MSG_RECV_ERROR);
                }
                //onFailure()
                if (listener != null) {
                    listener.onFailure();
                }
                e.printStackTrace();
                break;
            }
        }
    }


    public void cancel() {
        try {
            mmSokcet.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send data
     * @param data
     */
    public void write(byte[] data) {
        try {
            mmOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnReceiveListener {
        void onReceive(String data);
        void onFailure();
    }
}
