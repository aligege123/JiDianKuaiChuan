package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jidiankuaichuan.android.R;
import com.bumptech.glide.Glide;
import com.jidiankuaichuan.android.data.Picture;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.kuaichuan_utils.OtherFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends BaseAdapter {

    private static final String TAG = "PictureAdapter";

    private List<String> pictureList;

    private List<Boolean> checkList = new ArrayList<>();

    private Context mContext;

    private LayoutInflater layoutInflater;

    private LocalBroadcastManager localBroadcastManager;

    public PictureAdapter(Context context, List<String> pictureList) {
        this.pictureList = pictureList;
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        for (int i = 0; i < pictureList.size(); i++) {
            checkList.add(false);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return pictureList.size();
    }

    @Override
    public Object getItem(int position) {
        return pictureList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String picturePath = pictureList.get(position);
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.picture_item, null);
            viewHolder = new ViewHolder();
            viewHolder.pictureImage = (ImageView) view.findViewById(R.id.picture_image);
            viewHolder.pictureCheck = (CheckBox) view.findViewById(R.id.picture_check);
            viewHolder.pictureSelectedLayout = (FrameLayout) view.findViewById(R.id.picture_framelayout);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        RequestOptions options = new RequestOptions().placeholder(R.drawable.place_image);
        Glide.with(mContext).load(picturePath).apply(options).into(viewHolder.pictureImage);
        viewHolder.pictureCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
                if (isChecked) {
                    viewHolder.pictureCheck.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.pictureCheck.setVisibility(View.INVISIBLE);
                }
                Intent intent = new Intent("SELECTED_NUMBER_CHANGED");
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        viewHolder.pictureCheck.setChecked(checkList.get(position));
        if (checkList.get(position) == true) {
            viewHolder.pictureCheck.setVisibility(View.VISIBLE);
            viewHolder.pictureSelectedLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.pictureCheck.setVisibility(View.INVISIBLE);
            viewHolder.pictureSelectedLayout.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    class ViewHolder {
        ImageView pictureImage;
        CheckBox pictureCheck;
        FrameLayout pictureSelectedLayout;
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

    public List<Picture> getCheckList() {
        List<String> paths = new ArrayList<>();
        List<Picture> pictures = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i)) {
                paths.add(pictureList.get(i));
            }
        }
        if (paths.size() > 0) {
            for (String path : paths) {
                File file = new File(path);
                String name = OtherFileUtils.getFileName(path);
                long size = file.length();
                Picture picture = new Picture(name, path, size);
                pictures.add(picture);
            }
        }
        return pictures;
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
