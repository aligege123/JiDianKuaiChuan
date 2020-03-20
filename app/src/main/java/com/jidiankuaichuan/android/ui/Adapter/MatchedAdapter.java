package com.jidiankuaichuan.android.ui.Adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jidiankuaichuan.android.R;

import java.util.List;

public class MatchedAdapter extends ArrayAdapter<BluetoothDevice> {
    private int resourceId;

    public MatchedAdapter(Context context, int textViewResourceId, List<BluetoothDevice> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BluetoothDevice bluetoothDevice = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.bluetooth_name);
            viewHolder.adress = (TextView) view.findViewById(R.id.adress);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceName.setText(bluetoothDevice.getName());
        viewHolder.adress.setText(bluetoothDevice.getAddress());
        return view;

    }

    class ViewHolder {

        TextView deviceName;

        TextView adress;
    }
}
