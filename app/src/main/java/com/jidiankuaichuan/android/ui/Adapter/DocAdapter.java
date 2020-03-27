package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBean;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import java.util.ArrayList;
import java.util.List;

public class DocAdapter extends ArrayAdapter<FileBean> {

    private int resourceId;

    private List<FileBean> docList;

    private List<Boolean> checkList = new ArrayList<>();

    private LocalBroadcastManager localBroadcastManager;

    public DocAdapter(Context context, int textViewResourceId, List<FileBean> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        docList = objects;
        for (int i = 0; i < docList.size(); i++) {
            checkList.add(false);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FileBean fileBean = docList.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.docImage = (ImageView) view.findViewById(R.id.doc_image);
            viewHolder.docName = (TextView) view.findViewById(R.id.doc_name);
            viewHolder.docSize = (TextView) view.findViewById(R.id.doc_size);
            viewHolder.docCheck = (CheckBox) view.findViewById(R.id.doc_check);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.docImage.setImageResource(fileBean.iconId);
        String name = fileBean.name;
        if (name.length() > 27) {
            name = name.substring(0, 27) + "..";
            viewHolder.docName.setText(name);
        } else {
            viewHolder.docName.setText(name);
        }
        viewHolder.docSize.setText("" + TransUnitUtil.getPrintSize(fileBean.size));
        viewHolder.docCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
                Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        viewHolder.docCheck.setChecked(checkList.get(position));
        return view;
    }

    class ViewHolder {
        ImageView docImage;

        TextView docName;

        TextView docSize;

        CheckBox docCheck;
    }

    public void setChecked(int position) {
        if (checkList.get(position) == true) {
            checkList.set(position, false);
        } else {
            checkList.set(position, true);
        }
        notifyDataSetInvalidated();
        Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
        localBroadcastManager.sendBroadcast(intent);
    }

    public int getSelectedCount() {
        int count = 0;
        for (Boolean i : checkList) {
            if (i == true) {
                ++count;
            }
        }
        return count;
    }

    public List<FileBean> getCheckList() {
        List<FileBean> fileBeans = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            if(checkList.get(i)) {
                fileBeans.add(docList.get(i));
            }
        }
        return fileBeans;
    }

    public void selectAll() {
        for (int i = 0; i < checkList.size(); i++) {
            checkList.set(i, true);
        }
        notifyDataSetChanged();
    }

    public void unselectAll() {
        for (int i = 0; i < checkList.size(); i++) {
            checkList.set(i, false);
        }
        notifyDataSetChanged();
    }
}
