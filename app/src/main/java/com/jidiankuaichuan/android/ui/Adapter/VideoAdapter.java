package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.jidiankuaichuan.android.data.Video;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.TransUnitUtil;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends BaseAdapter {

    private List<Video> videoList;

    private LayoutInflater layoutInflater;

    private Context mContext;

    List<Boolean> checkList = new ArrayList<>();

    List<Video> videoChecked = new ArrayList<>();

    private LocalBroadcastManager localBroadcastManager;

    public VideoAdapter(Context context, List<Video> videos) {
        videoList = videos;
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        for (int i = 0; i < videoList.size(); i++) {
            checkList.add(false);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Video video = videoList.get(position);
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.video_item, null);
            viewHolder = new ViewHolder();
            viewHolder.videoName = (TextView) view.findViewById(R.id.video_name);
            viewHolder.videoDuration = (TextView) view.findViewById(R.id.video_duration);
            viewHolder.videoImage = (ImageView) view.findViewById(R.id.video_image);
            viewHolder.videoCheck = (CheckBox) view.findViewById(R.id.video_check);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String name = video.getName();
        if (name.length() > 11) {
            name = name.substring(0, 11) + "..";
            viewHolder.videoName.setText(name);
        } else {
            viewHolder.videoName.setText(name);
        }
        Bitmap bitmap = FileManager.getInstance(mContext).getVideoThumbnail(video.getId());
//        Drawable drawable = new BitmapDrawable(bitmap);
//        viewHoder.videoImage.setImageDrawable(drawable);
        Bitmap bmp = TransUnitUtil.imageScale(bitmap, 270, 270);
        viewHolder.videoImage.setImageBitmap(bmp);
        viewHolder.videoDuration.setText(TransUnitUtil.getPrintDuration(video.getDuration()));
        viewHolder.videoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
                if (isChecked) {
                    viewHolder.videoCheck.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.videoCheck.setVisibility(View.INVISIBLE);
                }
                Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        viewHolder.videoCheck.setChecked(checkList.get(position));
        if (checkList.get(position) == true) {
            viewHolder.videoCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.videoCheck.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    class ViewHolder {

        TextView videoName;

        TextView videoDuration;

        ImageView videoImage;

        CheckBox videoCheck;
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
