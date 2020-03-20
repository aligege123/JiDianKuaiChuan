package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothSocket;

import com.jidiankuaichuan.android.data.FileBase;

public class SendThread extends Thread{

    private static final String TAG = "SendThread";

    //传送的文件
    private FileBase mFileBase;

    //使用的socket
    private BluetoothSocket mSocket;

    private Object lock;

    /**
     * 判断线程是否暂停
     */
    boolean mIsPaused = false;

    /**
     * 判断此线程是否完毕
     */
    boolean mIsFinished = false;

    /**
     * 设置未执行的线程不执行的标识
     */
    boolean mIsStop = false;

    public SendThread(BluetoothSocket socket, FileBase fileBase) {
        mSocket = socket;
        mFileBase = fileBase;
    }

    //监听器
    public void setOnSendListener() {

    }

    @Override
    public void run() {
        super.run();
    }


    /**
     * 停止线程下载
     */
    public void pause() {
        synchronized(lock) {
            mIsPaused = true;
            lock.notifyAll();
        }
    }

    /**
     * 设置当前的发送任务不执行
     */
    public void stopSend(){
        mIsStop = true;
    }

    /**
     * 文件是否在传送中？
     * @return
     */
    public boolean isRunning(){
        return !mIsFinished;
    }

    /**
     * 文件传送的监听
     */
    public interface OnSendListener{
        void onStart();
        void onProgress(long progress, long total);
        void onSuccess();
        void onFailure();
    }
}
