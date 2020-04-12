package com.jidiankuaichuan.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.ui.InfoSettingActivity;

public class InfoFragment extends Fragment {

    private ImageView headImageSetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.info_fragment, container, false);
        headImageSetting = (ImageView) view.findViewById(R.id.nav_head_image);
        switch (Constant.imageId) {
            case 1:
                headImageSetting.setImageResource(R.drawable.head_image1);
                break;
            case 2:
                headImageSetting.setImageResource(R.drawable.head_image2);
                break;
            case 3:
                headImageSetting.setImageResource(R.drawable.head_image3);
                break;
            case 4:
                headImageSetting.setImageResource(R.drawable.head_image4);
                break;
        }
        headImageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoSettingActivity.class);
                startActivity(intent);
            }
        });

        TextView help = (TextView) view.findViewById(R.id.help_and_feedback);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        TextView aboutUs = (TextView) view.findViewById(R.id.about_us);
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    @Override
    public void onStart() {
        switch (Constant.imageId) {
            case 1:
                headImageSetting.setImageResource(R.drawable.head_image1);
                break;
            case 2:
                headImageSetting.setImageResource(R.drawable.head_image2);
                break;
            case 3:
                headImageSetting.setImageResource(R.drawable.head_image3);
                break;
            case 4:
                headImageSetting.setImageResource(R.drawable.head_image4);
                break;
        }
        super.onStart();
    }
}
