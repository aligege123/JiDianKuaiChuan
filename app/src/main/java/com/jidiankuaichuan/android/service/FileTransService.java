package com.jidiankuaichuan.android.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


import androidx.annotation.NonNull;

import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.controler.ChatController;

import java.util.List;

public class FileTransService extends Service {
    public FileTransService() {
    }

    private FileTransBinder mBinder = new FileTransBinder();

    public static class FileTransBinder extends Binder {

        public void startClientReceive(Handler handler) {
            ChatController.getInstance().startClientReceive(handler);
        }

        public void startServerReceive(Handler handler) {
            ChatController.getInstance().waitForClient(BluetoothAdapter.getDefaultAdapter(), handler);
        }

        public void startSend(List<FileBase> fileBaseList) {
            ChatController.getInstance().sendFile(fileBaseList);
        }

        public void restartAcceptReceive(Handler handler) {
            ChatController.getInstance().restartAcceptReceive(BluetoothAdapter.getDefaultAdapter(), handler);
        }
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
