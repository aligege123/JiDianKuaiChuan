package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.AppInfo;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends BaseAdapter {

    private List<AppInfo> appInfoList;

    private List<Boolean> checkList = new ArrayList<>();

    private Context context;

    private LayoutInflater layoutInflater;

    private LocalBroadcastManager localBroadcastManager;

    public AppAdapter(Context context, List<AppInfo> appInfos) {
        appInfoList = appInfos;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        for (int i = 0; i < appInfoList.size(); i++) {
            checkList.add(false);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return appInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AppInfo appInfo = appInfoList.get(position);
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.app_item, null);
            viewHolder = new ViewHolder();
            viewHolder.appImage = (ImageView) view.findViewById(R.id.app_image);
            viewHolder.appName = (TextView) view.findViewById(R.id.app_name);
            viewHolder.appSize = (TextView) view.findViewById(R.id.app_size);
            viewHolder.appCheck = (CheckBox) view.findViewById(R.id.app_check);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.appImage.setImageDrawable(appInfo.getIcon());
//        String name = appInfo.getApkName();
        viewHolder.appName.setText(appInfo.getApkName());
        viewHolder.appSize.setText(TransUnitUtil.getPrintSize(appInfo.getApkSize()));
        viewHolder.appCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
                if (isChecked) {
                    viewHolder.appCheck.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.appCheck.setVisibility(View.GONE);
                }
                Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        viewHolder.appCheck.setChecked(checkList.get(position));
        if (checkList.get(position) == true) {
            viewHolder.appCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.appCheck.setVisibility(View.GONE);
        }
        return view;
    }

    class ViewHolder {
        ImageView appImage;
        TextView appName;
        TextView appSize;
        CheckBox appCheck;
    }

    public void setChecked(int position) {
        if (checkList.get(position) == true) {
            checkList.set(position, false);
        } else {
            checkList.set(position, true);
        }
        notifyDataSetChanged();
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
}
