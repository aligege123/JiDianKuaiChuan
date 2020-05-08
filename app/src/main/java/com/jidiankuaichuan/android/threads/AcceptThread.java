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

    private static final String NAME = "BluetoothClass";

    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);

    private BluetoothServerSocket mServerSocket;

    private Handler mHandler;

    private ReceiveThread mReceiveThread;

    private List<SendThread> sendThreadList = new ArrayList<>();

    public List<SendThread> getSendThreadList() {
        return sendThreadList;
    }

    private BluetoothSocket mSocket;

    //lock
    private Object lock = new Object();

    public AcceptThread(BluetoothAdapter adapter, Handler handler) throws IOException {
        this.mHandler = handler;
        // get server socket
        mServerSocket = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
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
        // client socket
        BluetoothSocket socket = null;
        // server don't have to quit
        while (true){

            try {
                // get client socket
                socket =  mServerSocket.accept();
            } catch (IOException e) {
                // notify activity
                mHandler.sendEmptyMessage(Constant.MSG_ERROR);
                e.printStackTrace();
                break;
            }

            if(socket != null) {
                close();
                // manage connection
                mSocket = socket;
                manageConnectSocket(socket);
                break;
            }
        }
    }

    /**
     * manageConnectSocket
     * @param socket
     */
    private void manageConnectSocket(BluetoothSocket socket) {
        // new a ReceiveThread
        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread(socket, mHandler);
            mReceiveThread.start();
        }
    }

    /**
     * cancel server listening
     */
    private void close() {
        try {
            mServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * break connection
     */
    public void cancel() {
        if (mReceiveThread != null) {
            mReceiveThread.close();
            mReceiveThread = null;
        }
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    /**
     * send device name and head image
     */
    public void sendDeviceInfo(String name, int imageId) {
        MyLog.e(TAG, "server send info");
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
        for (FileBase fileBase : fileBaseList) {
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
