package com.jidiankuaichuan.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;

public class InitFragment extends Fragment implements View.OnClickListener{

    private EditText editText;

    private ImageView myImage;

    private ImageView image1;

    private ImageView image2;

    private ImageView image3;

    private ImageView image4;

    private int imageSelected = 1;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sava_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                Constant.imageId = imageSelected;
                String deviceName = editText.getText().toString();
                if(deviceName.isEmpty() || deviceName.matches(" *")) {
                    Toast.makeText(getContext(), "设备名称不能为空", Toast.LENGTH_SHORT).show();
                } else if (deviceName.length() > 15) {
                    Toast.makeText(getContext(), "设备名称不能超过15个字符", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("device_data", Context.MODE_PRIVATE).edit();
                    editor.putString("device_name", deviceName);
                    editor.putInt("imageId", imageSelected);
                    editor.apply();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                break;
            default:
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        Toolbar initToobar = (Toolbar) view.findViewById(R.id.toobar_init);
        editText = (EditText) view.findViewById(R.id.set_device_name);
        myImage = (ImageView) view.findViewById(R.id.my_head_image);
        image1 = (ImageView) view.findViewById(R.id.head_image1);
        image2 = (ImageView) view.findViewById(R.id.head_image2);
        image3 = (ImageView) view.findViewById(R.id.head_image3);
        image4 = (ImageView) view.findViewById(R.id.head_image4);

        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        image4.setOnClickListener(this);

        //获取默认设备名称
        String deviceName = Constant.deviceName;
        //初始化
        ((AppCompatActivity) getActivity()).setSupportActionBar(initToobar);
        editText.setText(deviceName);
        editText.setSelection(deviceName.length());
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_image1:
                myImage.setImageResource(R.drawable.head_image1);
                imageSelected = 1;
                break;
            case R.id.head_image2:
                myImage.setImageResource(R.drawable.head_image2);
                imageSelected = 2;
                break;
            case R.id.head_image3:
                myImage.setImageResource(R.drawable.head_image3);
                imageSelected = 3;
                break;
            case R.id.head_image4:
                myImage.setImageResource(R.drawable.head_image4);
                imageSelected = 4;
                break;
            default:
        }
    }
}
