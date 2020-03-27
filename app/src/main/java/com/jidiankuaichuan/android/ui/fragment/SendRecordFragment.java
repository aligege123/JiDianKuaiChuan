package com.jidiankuaichuan.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.SendThread;
import com.jidiankuaichuan.android.threads.controler.ChatControler;
import com.jidiankuaichuan.android.ui.Adapter.SendRecordAdapter;
import com.jidiankuaichuan.android.utils.MyLog;

import java.util.List;

public class SendRecordFragment extends Fragment {
    private static final String TAG = "SendRecordFragment";

    public final static int MSG_CALLBACK = 0x60;

    private List<FileBase> sendFileList;

    private SendRecordAdapter sendRecordAdapter;

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_CALLBACK:
                    sendRecordAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.send_record_fragment, container, false);
        sendFileList = ChatControler.getInstance().getFileSendList();
        sendRecordAdapter = new SendRecordAdapter(getContext(), sendFileList);
        ListView listView = (ListView) view.findViewById(R.id.send_record_list);
        listView.setAdapter(sendRecordAdapter);
//        ChatControler.getInstance().setActivitySendCallback(new ChatControler.ActivitySendCallback() {
//            @Override
//            public void onProgress(int id, long progress) {
//                MyLog.d(TAG, "send fragment onProgress()");
//                myHandler.sendEmptyMessage(MSG_CALLBACK);
//            }
//
//            @Override
//            public void onSuccess(int id) {
//                MyLog.d(TAG, "send fragment onSuccess()");
//                myHandler.sendEmptyMessage(MSG_CALLBACK);
//            }
//
//            @Override
//            public void onFailure(int id) {
//                MyLog.d(TAG, "send fragment onFailure()");
//                myHandler.sendEmptyMessage(MSG_CALLBACK);
//            }
//        });
        ChatControler.getInstance().setOnSendListener(new SendThread.OnSendListener() {
            @Override
            public void onProgress(int id, long progress) {
                MyLog.d(TAG, "send fragment onProgress()");
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }

            @Override
            public void onSuccess(int id) {
                MyLog.d(TAG, "send fragment onSuccess()");
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }

            @Override
            public void onFailure(int id) {
                MyLog.d(TAG, "send fragment onFailure()");
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }
        });
        return view;
    }

}
