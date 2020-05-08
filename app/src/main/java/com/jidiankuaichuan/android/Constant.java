package com.jidiankuaichuan.android;

import android.os.Environment;

import com.jidiankuaichuan.android.ui.fragment.ReceiveRecordFragment;
import com.jidiankuaichuan.android.ui.fragment.SendRecordFragment;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class Constant {

    public static String deviceName;

    public static int imageId = 1;

    public static String CONNECTION_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static final int MSG_ERROR = -1;

    public static final int MSG_FINISH_LISTENING = 0;

    public static final int MSG_GOT_DATA = 1;

    public static final int MSG_CONNECT_SUCCESS = 2;

    public static final int MSG_CONNECT_FAIL = 3;

    public static final int FLAG_MSG = 4;

    public static final int FLAG_FILE = 5;

    public static final int MSG_DEVICEINFO = 6;

    public static final int FLAG_CLOSE = 7;

    public static final int MSG_RECV_ERROR = 8;

    public static final String APK_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/app/";

    public static final String PICTURE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/image/";

    public static final String MUSIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/mp3/";

    public static final String VIDEO_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/mp4/";

    public static final String DOC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/doc/";

    public static final String OTHER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/小互传/files/";

    public static SendRecordFragment sendRecordFragment = new SendRecordFragment();

    public static ReceiveRecordFragment receiveRecordFragment = new ReceiveRecordFragment();
}
