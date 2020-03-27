package com.jidiankuaichuan.android.ui.fragment;

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
import com.jidiankuaichuan.android.threads.ReceiveThread;
import com.jidiankuaichuan.android.threads.controler.ChatControler;
import com.jidiankuaichuan.android.ui.Adapter.ReceiveRecordAdapter;
import com.jidiankuaichuan.android.utils.MyLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReceiveRecordFragment extends Fragment {

    private static final String TAG = "ReceiveRecordFragment";

    private final int MSG_CALLBACK = 0x60;

    private List<FileBase> receiveFileList;

    private ReceiveRecordAdapter receiveRecordAdapter;

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_CALLBACK:
                    receiveRecordAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.recv_record_fragment, container, false);
        receiveFileList = ChatControler.getInstance().getFileReceiveList();
        receiveRecordAdapter = new ReceiveRecordAdapter(getContext(), receiveFileList);
        ListView listView = (ListView) view.findViewById(R.id.recv_record_list);
        listView.setAdapter(receiveRecordAdapter);

        ChatControler.getInstance().setOnReceiveListener(new ReceiveThread.OnReceiveListener() {
            @Override
            public void onStart(FileBase fileBase) {
                receiveFileList.add(fileBase);
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }

            @Override
            public void onProgress(FileBase fileBase, long progress) {
                int index = receiveFileList.indexOf(fileBase);
                receiveFileList.get(index).setProgress(progress);
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }

            @Override
            public void onSuccess(FileBase fileBase) {
                int index = receiveFileList.indexOf(fileBase);
                FileBase file = receiveFileList.get(index);
                file.setResult(1);
                file.setProgress(file.getSize());
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }

            @Override
            public void onFailure(FileBase fileBase) {
                int index = receiveFileList.indexOf(fileBase);
                receiveFileList.get(index).setResult(2);
                myHandler.sendEmptyMessage(MSG_CALLBACK);
            }
        });
        return view;
    }
}
