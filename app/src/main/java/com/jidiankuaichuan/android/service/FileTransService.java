package com.jidiankuaichuan.android.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


import androidx.annotation.NonNull;

import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.ReceiveThread;
import com.jidiankuaichuan.android.threads.SendThread;
import com.jidiankuaichuan.android.threads.controler.ChatControler;

import java.util.List;

public class FileTransService extends Service {
    public FileTransService() {
    }

    private FileTransBinder mBinder = new FileTransBinder();

    public static class FileTransBinder extends Binder {

        public void startClientReceive(Handler handler) {
            ChatControler.getInstance().startClientReceive(handler);
        }

        public void startServerReceive(Handler handler) {
            ChatControler.getInstance().waitForClient(BluetoothAdapter.getDefaultAdapter(), handler);
        }

        public void startSend(List<FileBase> fileBaseList) {
            ChatControler.getInstance().sendFile(fileBaseList);
        }
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
