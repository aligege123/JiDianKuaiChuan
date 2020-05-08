package com.jidiankuaichuan.android.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.utils.ToastUtil;

public class InfoSettingActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView myImage;

    private int imageSelected;

    private EditText editText;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_setting);

        imageSelected = Constant.imageId;
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //imageview
        myImage = (ImageView) findViewById(R.id.my_head_image);
        switch (Constant.imageId) {
            case 1:
                myImage.setImageResource(R.drawable.head_image1);
                break;
            case 2:
                myImage.setImageResource(R.drawable.head_image2);
                break;
            case 3:
                myImage.setImageResource(R.drawable.head_image3);
                break;
            case 4:
                myImage.setImageResource(R.drawable.head_image4);
                break;
        }

        ImageView image1 = (ImageView) findViewById(R.id.head_image1);
        ImageView image2 = (ImageView) findViewById(R.id.head_image2);
        ImageView image3 = (ImageView) findViewById(R.id.head_image3);
        ImageView image4 = (ImageView) findViewById(R.id.head_image4);

        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        image4.setOnClickListener(this);

        //edittext
        editText = (EditText) findViewById(R.id.set_device_name);
        editText.setText(Constant.deviceName);
        editText.setSelection(Constant.deviceName.length());
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sava_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                String deviceName = editText.getText().toString();
                if(deviceName.isEmpty() || deviceName.matches(" *")) {
                    Toast.makeText(this, "设备名称不能为空", Toast.LENGTH_SHORT).show();
                } else if (deviceName.length() > 15) {
                    Toast.makeText(this, "设备名称不能超过15个字符", Toast.LENGTH_SHORT).show();
                } else {
                    Constant.imageId = imageSelected;
                    Constant.deviceName = deviceName;
                    SharedPreferences.Editor editor = getSharedPreferences("device_data", Context.MODE_PRIVATE).edit();
                    editor.putString("device_name", deviceName);
                    editor.putInt("image_id", imageSelected);
                    editor.apply();
                    ToastUtil.s("修改成功");
                }
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
