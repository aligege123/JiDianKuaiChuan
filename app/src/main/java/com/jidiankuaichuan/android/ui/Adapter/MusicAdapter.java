package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.Music;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends ArrayAdapter<Music> {

    private static final String TAG = "MusicAdapter";

    private int resourceId;

    private List<Music> mMusicList;

    private List<Boolean> checkList = new ArrayList<>();

    private List<Music> musicChecked = new ArrayList<>();

    private LocalBroadcastManager localBroadcastManager;

//    private List<ViewHolder> viewHolderList = new ArrayList<>();

    public MusicAdapter(Context context, int textViewResourceId, List<Music> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        mMusicList = objects;
        for (int i = 0; i < mMusicList.size(); i++) {
            checkList.add(false);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Music music = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
            viewHolder.duration = (TextView) view.findViewById(R.id.music_duration);
            viewHolder.size = (TextView) view.findViewById(R.id.music_size);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.music_check);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
//        if (viewHolderList.indexOf(viewHolder) == -1) {
//            viewHolderList.add(viewHolder);
//        }
        String name = music.getName();
        if (name.length() > 27) {
            name = name.substring(0, 27) + "..";
            viewHolder.musicName.setText(name);
        } else {
            viewHolder.musicName.setText(name);
        }
        viewHolder.duration.setText(TransUnitUtil.getPrintDuration(music.getDuration()));
        viewHolder.size.setText(TransUnitUtil.getPrintSize(music.getSize()));
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
                Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        viewHolder.checkBox.setChecked(checkList.get(position));
        return view;
    }

    class ViewHolder {

        TextView musicName;

        TextView duration;

        TextView size;

        CheckBox checkBox;
    }

    public List<Music> getMusicChecked() {
        List<Music> musicList = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i) == true) {
                musicList.add(mMusicList.get(i));
            }
        }
        return musicList;
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
