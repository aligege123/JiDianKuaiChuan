package com.jidiankuaichuan.android.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.ui.fragment.InitFragment;
import com.jidiankuaichuan.android.utils.FileUtils;
import com.jidiankuaichuan.android.utils.SDCardHelper;

import org.litepal.LitePal;

public class InitActivity extends AppCompatActivity {

    private static final String TAG = "InitActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        SharedPreferences pref = getSharedPreferences("device_data", MODE_PRIVATE);
        String deviceName = pref.getString("device_name", "");
        if ("".equals(deviceName)) {
            //创建文件存放路径
            if (SDCardHelper.isSDCardMounted()) {
                FileUtils.mkdirs(Constant.APK_PATH);
                FileUtils.mkdirs(Constant.MUSIC_PATH);
                FileUtils.mkdirs(Constant.PICTURE_PATH);
                FileUtils.mkdirs(Constant.VIDEO_PATH);
                FileUtils.mkdirs(Constant.OTHER_PATH);
            }
            //初始化数据库
            LitePal.getDatabase();
            //加载初始化碎片
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.init_layout, new InitFragment());
            transaction.commit();
        } else {
            //已经初始化过，进入主界面
            int imageId = pref.getInt("imageId", 0);
            Constant.deviceName = deviceName;
            Constant.imageId = imageId;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
