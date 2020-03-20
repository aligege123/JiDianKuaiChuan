package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.DeviceInfo;

import java.util.List;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

    private Context context;

    private List<DeviceInfo> mDeviceInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceImage;
        TextView deviceName;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            deviceImage = (ImageView) view.findViewById(R.id.device_image);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            checkBox = (CheckBox) view.findViewById(R.id.check_device);
        }
    }

    public DeviceInfoAdapter(List<DeviceInfo> deviceInfoList, Context context) {
        this.context = context;
        mDeviceInfoList = deviceInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DeviceInfo deviceInfo = mDeviceInfoList.get(position);
        holder.deviceImage.setImageResource(deviceInfo.getImageId());
        holder.deviceName.setText(deviceInfo.getDeviceName());
        holder.checkBox.setChecked(deviceInfo.isSelected());

        //为checkBox设置点击事件
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDeviceInfoList.get(position).setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceInfoList.size();
    }

}
