package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.threads.SendThread;
import com.jidiankuaichuan.android.threads.controler.ChatController;
import com.jidiankuaichuan.android.ui.dialog.MyDialog;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import org.litepal.LitePal;

import java.util.List;

public class SendRecordAdapter extends BaseAdapter {

    private static final String TAG = "SendRecordAdapter";

    private List<FileBase> fileBaseList;

    private LayoutInflater layoutInflater;

    private Context context;

    public SendRecordAdapter(Context context, List<FileBase> fileBaseList) {
        this.fileBaseList = fileBaseList;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return fileBaseList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileBaseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FileBase fileBase = fileBaseList.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.send_record_item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.file_send_name);
            viewHolder.percent = (TextView) view.findViewById(R.id.send_percent);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.send_progress_bar);
            viewHolder.cancelButton = (Button) view.findViewById(R.id.send_cancel);
            viewHolder.delete = (ImageView) view.findViewById(R.id.delete_send_record);
            viewHolder.sizeAndState = (TextView) view.findViewById(R.id.file_send_size);
            viewHolder.time = (TextView) view.findViewById(R.id.file_send_time);
            viewHolder.type = (TextView) view.findViewById(R.id.file_send_type);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String name = fileBase.getName();
        if (name.length() > 20) {
            name = name.substring(0, 20) + "..";
        }
        viewHolder.name.setText(name);
        double progress = (double) fileBase.getProgress();
        double fileSize = (double) fileBase.getSize();
        int percent = (int) ((progress / fileSize) * 100.000);
        viewHolder.percent.setText("" + percent + "%") ;
        viewHolder.progressBar.setProgress(percent);
        viewHolder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.d(TAG, "fileBaseList的长度:" + fileBaseList.size());
                int id = fileBase.getId();
                MyLog.d(TAG, "要取消的文件id:" + id);
                List<SendThread> sendThreads = ChatController.getInstance().getSendThreadList();
                SendThread canceledThread = null;
                for (SendThread s : sendThreads) {
                    MyLog.d(TAG, "线程里文件的id:" + s.getFileId());
                    if (s.getFileId() == id) {
                        s.stopSend();
                        canceledThread = s;
                    }
                }
                sendThreads.remove(canceledThread);
//                ChatControler.getInstance().getSendThreadList().get(position).stopSend();
//                ChatControler.getInstance().getSendThreadList().remove(position);
                fileBaseList.remove(position);
                notifyDataSetChanged();
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除记录
                MyDialog dialog = new MyDialog.Builder(context).setTitle("删除文件记录").setNoText().setYesText(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LitePal.deleteAll(FileBase.class, "time = ?", fileBase.getTime());
                        MyLog.d(TAG, "删除记录");
                        fileBaseList.remove(fileBase);
                        notifyDataSetChanged();
                    }
                }).create();
                dialog.show();
            }
        });
        if (percent > 0) {
            viewHolder.cancelButton.setVisibility(View.GONE);
        }
        if (fileBase.getResult() == 0) {
            //传输中
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.soft_black));
            viewHolder.sizeAndState.setText("传输中");
        } else if (fileBase.getResult() == 1) {
            //传输成功
//            MyLog.d(TAG, fileBase.getName() + "传输成功");
//            viewHolder.percent.setVisibility(View.INVISIBLE);
//            viewHolder.progressBar.setVisibility(View.INVISIBLE);
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.soft_black));
            viewHolder.sizeAndState.setText(TransUnitUtil.getPrintSize(fileBase.getSize()));
            viewHolder.delete.setVisibility(View.VISIBLE);

        } else if (fileBase.getResult() == 2) {
            //传输失败
//            viewHolder.percent.setVisibility(View.INVISIBLE);
//            viewHolder.progressBar.setVisibility(View.INVISIBLE);
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.colorAccent));
            viewHolder.sizeAndState.setText("发送失败");
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.cancelButton.setVisibility(View.GONE);
        }
        viewHolder.time.setText(fileBase.getTime());
        viewHolder.type.setText(fileBase.getType());
        return view;
    }

    class ViewHolder {
        TextView name;
        TextView percent;
        ProgressBar progressBar;
        Button cancelButton;
        ImageView delete;
        TextView sizeAndState;
        TextView time;
        TextView type;
    }
}
