package com.jidiankuaichuan.android.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.controler.ChatController;
import com.jidiankuaichuan.android.utils.FileUtils;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiveThread extends Thread{

    /** 当前连接的客户端BluetoothSocket*/
    private final BluetoothSocket mSokcet;
    /** 读取数据流*/
    private final InputStream mInputStream;

    private FileBase mFileBase;

    /** 与主线程通信Handler*/
    private Handler mHandler;
    private String TAG = "ReceiveThread";

    private Boolean isRead = false;

    private boolean isReceiving = false;

    private OnReceiveListener mOnReceiveListener;

    private static final int BUFFER_SIZE = 4 * 1024;

    private Object lock = new Object();

    public ReceiveThread(BluetoothSocket socket, Handler handler) {
        mSokcet = socket;
        mHandler = handler;

        InputStream tmpIn = null;
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInputStream = tmpIn;

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setOnReceiveListener(OnReceiveListener mOnReceiveListener) {
        this.mOnReceiveListener = mOnReceiveListener;
    }

    @Override
    public void run() {
        super.run();
        try {
            isRead = true;
            DataInputStream in = new DataInputStream(mInputStream);
            while (isRead) {
                switch (in.readInt()) {
                    case Constant.FLAG_MSG:
                        MyLog.d(TAG, "收到设备信息");
                        String jsonStr = in.readUTF();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String friendName = (String) jsonObject.get("deviceName");
                            int friendImageId = jsonObject.getInt("deviceImageId");
                            //回调到活动
                            Bundle bundle = new Bundle();
                            bundle.putString("name", friendName);
                            bundle.putInt("imageId", friendImageId);
                            Message message = new Message();
                            message.setData(bundle);
                            message.what = Constant.MSG_DEVICEINFO;
                            if (mHandler != null) {
                                mHandler.sendMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constant.FLAG_FILE:
                        isReceiving = true;
                        try {
                            int headLen = in.readInt();
                            byte[] headByte = new byte[headLen];
                            byte readByte = -1;
                            int headTotal = 0;
                            while ((readByte = in.readByte()) != -1) {
                                headByte[headTotal] = readByte;
                                headTotal++;
                                if (headTotal == headLen) {
                                    break;
                                }
                            }
                            String str = new String(headByte, "UTF-8");
                            MyLog.d(TAG, str);
//                            String str = in.readUTF();
                            FileBase fileBase = FileBase.toFileBase(str);
                            mFileBase = fileBase;
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            fileBase.setTime(df.format(new Date()));
                            fileBase.setAction("RECV");
                            MyLog.d(TAG, "" + TransUnitUtil.getPrintSize(fileBase.getSize()));
                            if (mOnReceiveListener != null) {
                                mOnReceiveListener.onStart(fileBase);
                            } else {
                                List<FileBase> fileBases = new ArrayList<>();
                                fileBases.add(fileBase);
                                ChatController.getInstance().AddFileReceiveList(fileBases);
                            }
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(Constant.MSG_GOT_DATA);
                            }
                            long fileSize = fileBase.getSize();
                            String filePath = Constant.OTHER_PATH;
                            switch (fileBase.getType()) {
                                case "image":
                                    filePath = Constant.PICTURE_PATH;
                                    break;
                                case "app":
                                    filePath = Constant.APK_PATH;
                                    break;
                                case "mp4":
                                    filePath = Constant.VIDEO_PATH;
                                    break;
                                case "mp3":
                                    filePath = Constant.MUSIC_PATH;
                                    break;
                                case "doc":
                                    filePath = Constant.DOC_PATH;
                                    break;
                                case "dir":
                                    filePath = Constant.DIR_PATH;
                                    break;
                                case "other":
                                    filePath = Constant.OTHER_PATH;
                                    break;
                                default:
                            }
                            FileUtils.mkdirs(filePath);
                            mFileBase.setPath(filePath + fileBase.getName());
                            FileOutputStream out = new FileOutputStream(filePath + fileBase.getName());
                            byte[] bytes = new byte[BUFFER_SIZE];
                            long total = 0;
                            int len = 0;

                            long sTime = System.currentTimeMillis();
                            long eTime = 0;
                            while (true) {
                                if (total == fileSize) {
                                    break;
                                }
                                Long remain = fileSize - total;
                                int r = remain.intValue();
                                if (r < BUFFER_SIZE) {
                                    len = in.read(bytes, 0, r);
                                } else {
                                    len = in.read(bytes, 0, BUFFER_SIZE);
                                }
                                out.write(bytes, 0, len);
                                total = total + len;
                                eTime = System.currentTimeMillis();
                                if(eTime - sTime > 200) { //大于500ms 才进行一次监听
                                    sTime = eTime;
//                                    MyLog.d(TAG, TransUnitUtil.getPrintSize(total));
                                    if(mOnReceiveListener != null) {
                                        mOnReceiveListener.onProgress(fileBase, total);
                                        MyLog.d(TAG, "receive thread onProgress()" + TransUnitUtil.getPrintSize(total));
                                    }
                                }
                            }
                            if (total < fileSize || total > fileSize) {
                                if (mOnReceiveListener != null) {
                                    mOnReceiveListener.onFailure(fileBase);
                                    MyLog.d(TAG, "recv thread onFailure() 1");
                                } else {
                                    fileBase.setResult(2);
                                }
                            } else {
                                if (mOnReceiveListener != null) {
                                    mOnReceiveListener.onSuccess(fileBase);
                                    MyLog.d(TAG, "recv thread onSuccess()");
                                } else {
                                    MyLog.d(TAG, "recv thread success");
                                    fileBase.setResult(1);
                                    fileBase.setProgress(fileSize);
                                }
                            }
                            isReceiving = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                            isReceiving = false;
                            if (mOnReceiveListener != null) {
                                mOnReceiveListener.onFailure(mFileBase);
                                MyLog.d(TAG, "recv thread onFailure() 2");
                            } else {
                                mFileBase.setResult(2);
                            }
                        }
                        mFileBase.save();
                        break;
                }
            }
        } catch (Exception e) {
            MyLog.d(TAG, "接收信息异常");
            mHandler.sendEmptyMessage(Constant.MSG_RECV_ERROR);
            e.printStackTrace();
        }
    }

    /**
     * 判断是否正在接收文件
     */
    public boolean isRuning() {
        return isReceiving;
    }

    public void close() {
        try {
            mSokcet.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnReceiveListener {
        void onStart(FileBase fileBase);
        void onProgress(FileBase fileBase, long progress);
        void onSuccess(FileBase fileBase);
        void onFailure(FileBase fileBase);
    }
}
