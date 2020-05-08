package com.jidiankuaichuan.android.chat.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.utils.BlueToothUtils;

import java.util.List;

public class NearbyAdapter extends ArrayAdapter<BluetoothDevice> {

    private static final String TAG = "NearbyAdapter";

    private int resourceId;

    public NearbyAdapter(Context context, int textViewResourceId, List<BluetoothDevice> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    static class ViewHolder {
        TextView name;
        TextView address;
        Button add;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final BluetoothDevice device = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.nearby_name);
            viewHolder.address = (TextView) view.findViewById(R.id.nearby_address);
            viewHolder.add = (Button) view.findViewById(R.id.add_friend_button);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.name.setText(device.getName().substring(4));
        viewHolder.address.setText(device.getAddress());
        viewHolder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BlueToothUtils.getInstance().isDiscovering()) {
                    BlueToothUtils.getInstance().cancelDiscover();
                }
                //create bond
                try {
                    BlueToothUtils.getInstance().createBond(device.getClass(), device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
