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
import com.jidiankuaichuan.android.ui.dialog.MyDialog;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import org.litepal.LitePal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReceiveRecordAdapter extends BaseAdapter {

    private static final String TAG = "ReceiveRecordAdapter";

    private List<FileBase> fileBaseList;

    private LayoutInflater layoutInflater;

    private Context context;

    public ReceiveRecordAdapter(Context context, List<FileBase> fileBaseList) {
        this.context = context;
        this.fileBaseList = fileBaseList;
        layoutInflater = LayoutInflater.from(context);
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
            view = layoutInflater.inflate(R.layout.recv_record_item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.file_recv_name);
            viewHolder.percent = (TextView) view.findViewById(R.id.recv_percent);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.recv_progress_bar);
            viewHolder.delete = (ImageView) view.findViewById(R.id.delete_recv_record);
            viewHolder.sizeAndState = (TextView) view.findViewById(R.id.file_recv_size);
            viewHolder.time = (TextView) view.findViewById(R.id.file_recv_time);
            viewHolder.type = (TextView) view.findViewById(R.id.file_recv_type);
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

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog dialog = new MyDialog.Builder(context).setTitle("删除文件记录").setCheckLayout()
                        .setCheckCallBack(new MyDialog.Builder.CheckCallBack() {
                    @Override
                    public void onCheck(boolean isChecked) {
                        if (isChecked) {
                            if (fileBase.getPath() != null) {
                                File file = new File(fileBase.getPath());
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            //删除记录
                            LitePal.deleteAll(FileBase.class, "time = ?", fileBase.getTime());
                            MyLog.d(TAG, "删除记录");
                            fileBaseList.remove(fileBase);
                            notifyDataSetChanged();
                        } else {
                            //删除记录
                            LitePal.deleteAll(FileBase.class, "time = ?", fileBase.getTime());
                            MyLog.d(TAG, "删除记录");
                            fileBaseList.remove(fileBase);
                            notifyDataSetChanged();
                        }
                    }
                }).setNoText().setEmptyYesText().create();
                dialog.show();
            }
        });

        if (fileBase.getResult() == 0) {
            //传输中
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.soft_black));
            viewHolder.sizeAndState.setText("传输中");
        }
        if (fileBase.getResult() == 1) {
            //传输成功
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.soft_black));
            viewHolder.sizeAndState.setText(TransUnitUtil.getPrintSize((long) fileSize));
            viewHolder.delete.setVisibility(View.VISIBLE);

        } else if (fileBase.getResult() == 2) {
            //传输失败
            viewHolder.sizeAndState.setTextColor(context.getResources().getColor(R.color.colorAccent));
            viewHolder.sizeAndState.setText("接收失败");
            viewHolder.delete.setVisibility(View.VISIBLE);
        }
        viewHolder.time.setText(fileBase.getTime());
        viewHolder.type.setText(fileBase.getType());
        return view;
    }

    class ViewHolder {
        TextView name;
        TextView percent;
        ProgressBar progressBar;
        ImageView delete;
        TextView sizeAndState;
        TextView time;
        TextView type;
    }
}
