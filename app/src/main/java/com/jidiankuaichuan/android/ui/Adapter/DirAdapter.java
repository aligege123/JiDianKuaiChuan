package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.TransUnitUtil;
import com.jidiankuaichuan.android.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DirAdapter extends ArrayAdapter<File> {
    private static final String TAG = "DirAdapter";

    private int resourceId;

    private List<File> dirList;

    private OnClickListener onClickListener;

    public DirAdapter(Context context, int textViewResourceId, List<File> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        dirList = objects;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final File file = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.dir_name);
            viewHolder.size = (TextView) view.findViewById(R.id.dir_size);
            viewHolder.send = (Button) view.findViewById(R.id.dir_send);
            viewHolder.image = (ImageView) view.findViewById(R.id.file_image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        assert file != null;
        String name = file.getName();
        if (name.length() > 23) {
            name = name.substring(0, 23) + "..";
        }
        viewHolder.name.setText(name);
        viewHolder.size.setText(TransUnitUtil.getPrintSize(file.length()));
        viewHolder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.e(TAG, file.getParent());
                File zipFile = null;
                try {
                    if (file.isDirectory()) {
                        ZipUtils.zip(file.getAbsolutePath(), file.getParent() + "/" + file.getName() + ".zip");
                        zipFile = new File(file.getAbsolutePath() + ".zip");
                    } else {
                        zipFile = file;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //TODO send dir
                if (onClickListener != null) {
                    if (zipFile != null) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(zipFile.getName());
                        fileBase.setPath(zipFile.getAbsolutePath());
                        fileBase.setType("other");
                        fileBase.setSize(zipFile.length());
                        onClickListener.onClick(fileBase);
                    }
                }
            }
        });
        if (file.isDirectory()) {
            viewHolder.image.setImageResource(R.drawable.dir);
        } else {
            viewHolder.image.setImageResource(R.drawable.file);
        }
        return view;
    }

    static class ViewHolder {
        TextView name;
        TextView size;
        Button send;
        ImageView image;
    }

    public interface OnClickListener {
        void onClick(FileBase fileBase);
    }
}
