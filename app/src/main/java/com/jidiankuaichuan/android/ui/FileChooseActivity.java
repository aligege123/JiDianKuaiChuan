package com.jidiankuaichuan.android.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.callback.FileChooseCallback;
import com.jidiankuaichuan.android.data.DeviceInfo;
import com.jidiankuaichuan.android.data.Music;
import com.jidiankuaichuan.android.threads.controler.ChatControler;
import com.jidiankuaichuan.android.ui.fragment.AppFragment;
import com.jidiankuaichuan.android.ui.fragment.DocFragment;
import com.jidiankuaichuan.android.ui.fragment.MusicFragment;
import com.jidiankuaichuan.android.ui.fragment.PictureFragment;
import com.jidiankuaichuan.android.ui.fragment.VideoFragment;
import com.jidiankuaichuan.android.utils.BlueToothUtil;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;
import com.jidiankuaichuan.android.utils.WifiUtil;

import java.util.ArrayList;
import java.util.List;

public class FileChooseActivity extends AppCompatActivity implements FileChooseCallback {

//    private WifiUtil mWifiUtil;

    private static final String TAG = "FileChooseActivity";

    private List<DeviceInfo> deviceInfoList = new ArrayList<>();

    private TextView waitText;

    private TextView numberSelected;

    private Button sendButton;

    private final int OPENBLUETOOTH = 0x1;

    private BlueToothUtil mBlueToothUtil = new BlueToothUtil();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            finish();
                            break;
                    }
                    break;
            }
        }
    };

    private List<String> mTitleList;

    private List<Fragment> mFileFragmentList;

    private AppFragment appFragment;

    private PictureFragment pictureFragment;

    private MusicFragment musicFragment;

    private VideoFragment videoFragment;

    private DocFragment docFragment;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private LocalBroadcastManager localBroadcastManager;

    private LocalReceiver localReceiver;

    private int fileSelectedNumber = 0;

    private Handler receiveHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_ERROR:
                    //服务端开启异常
                    break;
            }
        }
    };

    private Handler sendHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_choose);
        //初始化布局
        initView();
        //本地广播管理器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //打开蓝牙
        if (!mBlueToothUtil.isBlueEnable()) {
            mBlueToothUtil.openBlueSync(FileChooseActivity.this, OPENBLUETOOTH);
        }
        Intent intent = getIntent();
        String state = intent.getStringExtra("state");

        if (mBlueToothUtil.isBlueEnable() && "recv".equals(state)) {
            mBlueToothUtil.setCanBeDiscovered(FileChooseActivity.this);
            //开启接收线程
            ChatControler.getInstance().waitForClient(BluetoothAdapter.getDefaultAdapter(), receiveHandler);
            MyLog.d(TAG, "开启服务端");
//            ToastUtil.s("开启服务端");
        } else if ("send".equals(state)) {
            //开启连接监听线程,时刻监听连接的状态
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

        //注册选择的文件数的本地广播
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction("SELECTED_NUMBER_CHANGED");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, localFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.file_selected_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tabLayout = (TabLayout) findViewById(R.id.file_tab);
        viewPager = (ViewPager) findViewById(R.id.file_view_pager);

        mTitleList = new ArrayList<>();
        mTitleList.add("应用");
        mTitleList.add("图片");
        mTitleList.add("音乐");
        mTitleList.add("视频");
        mTitleList.add("文档");

        appFragment = new AppFragment();
        pictureFragment = new PictureFragment();
        musicFragment = new MusicFragment();
        videoFragment = new VideoFragment();
        //docFragment = new DocFragment();

        mFileFragmentList = new ArrayList<>();
        mFileFragmentList.add(appFragment);
        mFileFragmentList.add(pictureFragment);
        mFileFragmentList.add(musicFragment);
        mFileFragmentList.add(videoFragment);
        mFileFragmentList.add(new Fragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFileFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFileFragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitleList.get(position);
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        numberSelected = (TextView) findViewById(R.id.selected_num);
        sendButton = (Button) findViewById(R.id.send_file);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //统计选择的文件
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPENBLUETOOTH:
                if (!mBlueToothUtil.isBlueEnable()) {
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            mBlueToothUtil.closeBlueAsyn();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBlueToothUtil.isBlueEnable()) {
            mBlueToothUtil.closeBlueAsyn();
        }
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //统计所有选择的文件数
            fileSelectedNumber = appFragment.getSelectedCount()+ musicFragment.getSelectedCount() +
                    videoFragment.getSelectedCount() + pictureFragment.getSelectedCount();
            numberSelected.setText("" + fileSelectedNumber);
            if (fileSelectedNumber > 0) {
                sendButton.setEnabled(true);
            } else {
                sendButton.setEnabled(false);
            }
        }
    }

    @Override
    public void noitifyFileSeletedNumberChanged() {
        fileSelectedNumber = appFragment.getSelectedCount();
        numberSelected.setText("" + fileSelectedNumber);
    }
}
