package com.jidiankuaichuan.android.chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.jidiankuaichuan.android.chat.model.Friend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BlueToothChatControler {
    private static final String TAG = "BlueToothChatControler";

    private ChatAcceptThread mChatAcceptThread;

    private ChatConnectThread mChatConnectThread;

    private static BlueToothChatControler instance;

    public static final int STATE_NONE = 0;

    public static final int STATE_CONNECTED = 1;

    public static final int STATE_CONNECTION_LOST = 2;

    public static final int STATE_CONNECTING = 3;

    public static final int STATE_ADDING_FRIEND = 4;

    public static final int STATE_DELETING_FRIEND = 5;

    public static int state = STATE_NONE;

    private String friendName;

    private String friendAddress;

    private BlueToothChatControler() {}

    //single instance mode
    public static synchronized BlueToothChatControler getInstance() {
        if (instance == null) {
            instance = new BlueToothChatControler();
        }
        return instance;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendAddress() {
        return friendAddress;
    }

    public void setFriendAddress(String friendAddress) {
        this.friendAddress = friendAddress;
    }

    /**
     * create AcceptThread
     */
    public void start(Handler handler) {
        try {
            if (mChatAcceptThread == null) {
                mChatAcceptThread = new ChatAcceptThread(BluetoothAdapter.getDefaultAdapter(), handler);
                mChatAcceptThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create ConnectThread
     */
    public void connect(BluetoothDevice device, Handler handler) {
        if (mChatAcceptThread != null) {
            mChatAcceptThread.cancel();
            mChatAcceptThread = null;
        }
        if (mChatConnectThread != null) {
            mChatConnectThread.cancel();
            mChatConnectThread = null;
        }
        mChatConnectThread = new ChatConnectThread(device, handler);
        mChatConnectThread.start();
    }

    /**
     * stop chat
     */
    public void stop() {
        if (mChatAcceptThread != null) {
            mChatAcceptThread.cancel();
            mChatAcceptThread = null;
        }
        if (mChatConnectThread != null) {
            mChatConnectThread.cancel();
            mChatConnectThread = null;
        }

        state = STATE_NONE;
    }

    /**
     * break connection by clicking
     */
    public void stopConnect() {
        if (mChatConnectThread != null) {
            mChatConnectThread.cancel();
            mChatConnectThread = null;
        }
        if (mChatAcceptThread != null) {
            mChatAcceptThread.cancel();
            mChatAcceptThread = null;
        }
    }

    /**
     * restart AcceptThread
     */
    public void restart(Handler handler) {
        if (mChatConnectThread != null) {
            mChatConnectThread.cancel();
            mChatConnectThread = null;
        }

        if (mChatAcceptThread != null) {
            mChatAcceptThread.cancel();
            mChatAcceptThread = null;
        }

        try {
            mChatAcceptThread = new ChatAcceptThread(BluetoothAdapter.getDefaultAdapter(), handler);
            mChatAcceptThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send message
     */
    public void sendMessage(String msg) {
        byte[] data = encodeMsg(msg);
        if (mChatConnectThread != null) {
            mChatConnectThread.sendData(data);
        } else if (mChatAcceptThread != null) {
            mChatAcceptThread.sendData(data);
        }
    }

    /**
     * encode message
     */
    private byte[] encodeMsg(String data) {
        if (data == null) {
            return new byte[0];
        } else {
            try {
                return data.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    public void setOnReceiveListener(ChatThread.OnReceiveListener onReceiveListener) {
        if (mChatAcceptThread != null) {
            mChatAcceptThread.setOnReceiveListener(onReceiveListener);
        }
        if (mChatConnectThread != null) {
            mChatConnectThread.setOnReceiveListener(onReceiveListener);
        }
    }
}
