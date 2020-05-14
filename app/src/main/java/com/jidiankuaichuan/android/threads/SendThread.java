package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.ui.fragment.SendRecordFragment;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private boolean mIsPaused = false;

    /**
     * 判断此线程是否完毕
     */
    private boolean mIsSending = false;

    /**
     * 设置未执行的线程不执行的标识
     */
    private boolean mIsStop = false;

    private OnSendListener onSendListener;

    public SendThread(BluetoothSocket socket, FileBase fileBase, Object lock) {
        mSocket = socket;
        mFileBase = fileBase;
        this.lock = lock;
    }

    //监听器
    public void setOnSendListener(OnSendListener onSendListener) {
        this.onSendListener = onSendListener;
    }

    @Override
    public void run() {
        synchronized (lock) {
            //设置当前任务不执行
            if (mIsStop) {
                MyLog.e(TAG, mFileBase.getName() + "取消发送");
                return;
            }
            mIsSending = true;
            try {
                //时间
                mFileBase.setAction("SEND");

                FileInputStream in = new FileInputStream(new File(mFileBase.getPath()));
                DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
                out.writeInt(Constant.FLAG_FILE);
                byte[] headBytes = FileBase.toJsonStr(mFileBase).getBytes("UTF-8");
                out.writeInt(headBytes.length);
                out.write(headBytes);

                int id = mFileBase.getId();

                byte[] bytes = new byte[4 * 1024];
                long total = 0;
                int len = 0;

                long sTime = System.currentTimeMillis();
                long eTime = 0;

                while ((len = in.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                    total = total + len;
                    mFileBase.setProgress(total);
                    eTime = System.currentTimeMillis();
                    if (eTime - sTime > 200) { //大于500ms 才进行一次监听
                        sTime = eTime;
                        if (onSendListener != null) {
                            MyLog.d(TAG, "send thread onProgress() " + TransUnitUtil.getPrintSize(total));
                            onSendListener.onProgress(id, total);
                        }
                    }
                }
                if (total == mFileBase.getSize()) {
                    MyLog.d(TAG, "send thread onSuccess()");
                    mFileBase.setResult(1);
                    if (onSendListener != null) {
                        onSendListener.onSuccess(id);
                    }
                } else if (total < mFileBase.getSize() || total > mFileBase.getSize()){
                    MyLog.e(TAG, "send thread onFailure()");
                    mFileBase.setResult(2);
                    if (onSendListener != null) {
                        onSendListener.onFailure(id);
                    }
                }
                out.flush();
                mIsSending = false;
            } catch (IOException e) {
                if (onSendListener != null) {
                    MyLog.e(TAG, "send thread onFailure() exception");
                    mFileBase.setResult(2);
                    onSendListener.onFailure(mFileBase.getId());
                }
            }
        }
        if (mFileBase.getProgress() > 0) {
            //save record only when progress > 0
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mFileBase.setTime(df.format(new Date()));
            mFileBase.save();
        }
        //delete zip file
        if (mFileBase.getName().endsWith(".zip") && mFileBase.getType().equals("dir")) {
            File file = new File(mFileBase.getPath());
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
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
     * 获取filebase的id
     */
    public int getFileId() {
        return mFileBase.getId();
    }

    /**
     * 文件是否在传送中？
     * @return
     */
    public boolean isRunning(){
        return mIsSending && !mIsStop;
    }

    /**
     * 文件传送的监听
     */
    public interface OnSendListener{
        void onProgress(int id, long progress);
        void onSuccess(int id);
        void onFailure(int id);
    }
}
