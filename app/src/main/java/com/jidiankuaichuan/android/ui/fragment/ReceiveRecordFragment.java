package com.jidiankuaichuan.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.ReceiveThread;
import com.jidiankuaichuan.android.threads.controler.ChatController;
import com.jidiankuaichuan.android.ui.Adapter.ReceiveRecordAdapter;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.OpenFileUtil;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.io.File;
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
        receiveFileList = ChatController.getInstance().getFileReceiveList();
        receiveRecordAdapter = new ReceiveRecordAdapter(getContext(), receiveFileList);
        ListView listView = (ListView) view.findViewById(R.id.recv_record_list);
        listView.setAdapter(receiveRecordAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileBase fileBase = receiveFileList.get(position);
                MyLog.d(TAG, "打开文件  " + fileBase.getPath());
                File file = new File(fileBase.getPath());

                if (file.exists()) {
                    if (fileBase.getType().equals("app") || fileBase.getType().equals("other")) {
                        ToastUtil.s("暂不支持打开该类型的文件");
                    } else {
                        Intent intent = OpenFileUtil.openFile(fileBase.getPath(), getContext());
                        startActivity(intent);
                    }
                } else {
                    ToastUtil.s("文件不存在或已被删除");
                }
            }
        });

        ChatController.getInstance().setOnReceiveListener(new ReceiveThread.OnReceiveListener() {
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
