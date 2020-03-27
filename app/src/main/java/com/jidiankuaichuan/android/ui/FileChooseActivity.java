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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.jidiankuaichuan.android.data.AppInfo;
import com.jidiankuaichuan.android.data.DeviceInfo;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.data.FileBean;
import com.jidiankuaichuan.android.data.Music;
import com.jidiankuaichuan.android.data.Picture;
import com.jidiankuaichuan.android.data.Video;
import com.jidiankuaichuan.android.threads.controler.ChatControler;
import com.jidiankuaichuan.android.ui.dialog.MyDialog;
import com.jidiankuaichuan.android.ui.fragment.AppFragment;
import com.jidiankuaichuan.android.ui.fragment.DocFragment;
import com.jidiankuaichuan.android.ui.fragment.MusicFragment;
import com.jidiankuaichuan.android.ui.fragment.PictureFragment;
import com.jidiankuaichuan.android.ui.fragment.VideoFragment;
import com.jidiankuaichuan.android.utils.BlueToothUtil;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;
import com.jidiankuaichuan.android.utils.TransUnitUtil;
import com.jidiankuaichuan.android.utils.WifiUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileChooseActivity extends AppCompatActivity implements FileChooseCallback {

//    private WifiUtil mWifiUtil;

    private static final String TAG = "FileChooseActivity";

    private List<DeviceInfo> deviceInfoList = new ArrayList<>();

    private TextView waitText;

    private TextView numberSelected;

    private Button sendButton;

    private LinearLayout friendLayout;

    private ImageView friendImage;

    private TextView friendName;

    private final int OPENBLUETOOTH = 0x1;

    private BlueToothUtil mBlueToothUtil = new BlueToothUtil();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            finish();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            //说明用户点击的是接收按钮
                            //设置设备可被发现
                            mBlueToothUtil.setCanBeDiscovered(FileChooseActivity.this);
                            //开启接收线程
                            ChatControler.getInstance().waitForClient(BluetoothAdapter.getDefaultAdapter(), receiveHandler);
                            MyLog.d(TAG, "开启服务端2");
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

    //服务端用的handler
    private Handler receiveHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_ERROR:
                    //服务端开启异常
                    MyLog.d(TAG, "服务端开启异常");
                    break;
                case Constant.MSG_DEVICEINFO:
                    Bundle bundle = msg.getData();
                    String name = bundle.getString("name");
                    int imageId = bundle.getInt("imageId");
                    waitText.setVisibility(View.GONE);
                    friendLayout.setVisibility(View.VISIBLE);
                    switch (imageId) {
                        case 1:
                            friendImage.setImageResource(R.drawable.head_image1);
                            break;
                        case 2:
                            friendImage.setImageResource(R.drawable.head_image2);
                            break;
                        case 3:
                            friendImage.setImageResource(R.drawable.head_image3);
                            break;
                        case 4:
                            friendImage.setImageResource(R.drawable.head_image4);
                            break;
                    }
                    friendName.setText(name);

                    //发送服务端的设备名和头像
                    if (ChatControler.getInstance().isAlive()) {
                        ChatControler.getInstance().sendDeviceInfo(Constant.deviceName, Constant.imageId);
                    }
                    break;
                case Constant.FLAG_CLOSE:
                    MyLog.d(TAG, "客户端断开连接");
                    friendLayout.setVisibility(View.GONE);
                    waitText.setVisibility(View.VISIBLE);
                    ChatControler.getInstance().restartAcceptReceive();
                    break;
            }
        }
    };

    //客户端用的handler
    private Handler sendHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_DEVICEINFO:
                    MyLog.d(TAG, "收到服务端设备信息");
                    Bundle bundle = msg.getData();
                    String name = bundle.getString("name");
                    int imageId = bundle.getInt("imageId");
                    waitText.setVisibility(View.GONE);
                    friendLayout.setVisibility(View.VISIBLE);
                    switch (imageId) {
                        case 1:
                            friendImage.setImageResource(R.drawable.head_image1);
                            break;
                        case 2:
                            friendImage.setImageResource(R.drawable.head_image2);
                            break;
                        case 3:
                            friendImage.setImageResource(R.drawable.head_image3);
                            break;
                        case 4:
                            friendImage.setImageResource(R.drawable.head_image4);
                            break;
                    }
                    friendName.setText(name);
                    break;
                case Constant.FLAG_CLOSE:
                    MyLog.d(TAG, "收到服务端设备断开连接信号");
                    ChatControler.getInstance().stopChat();
                    finish();
                    break;
            }
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

        Intent intent = getIntent();
        String state = intent.getStringExtra("state");

        if (mBlueToothUtil.isBlueEnable()) {
            if ("send".equals(state)) {
                //开启客户端的接收线程
                ChatControler.getInstance().startClientReceive(sendHandler);
                //开启连接监听线程,时刻监听连接的状态

                //发送设备名和头像
                ChatControler.getInstance().sendDeviceInfo(Constant.deviceName, Constant.imageId);
            } else if ("recv".equals(state)) {
                //设置设备可被发现
                mBlueToothUtil.setCanBeDiscovered(FileChooseActivity.this);
                //开启接收线程
                ChatControler.getInstance().waitForClient(BluetoothAdapter.getDefaultAdapter(), receiveHandler);
                MyLog.d(TAG, "开启服务端1");
            }
        } else {
            mBlueToothUtil.openBlueSync(FileChooseActivity.this, OPENBLUETOOTH);
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
        docFragment = new DocFragment();

        mFileFragmentList = new ArrayList<>();
        mFileFragmentList.add(appFragment);
        mFileFragmentList.add(pictureFragment);
        mFileFragmentList.add(musicFragment);
        mFileFragmentList.add(videoFragment);
        mFileFragmentList.add(docFragment);

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
                List<FileBase> fileBaseList = new ArrayList<>();
                List<Music> musicCheckList = musicFragment.getCheckList();
                if (musicCheckList.size() > 0) {
                    for (Music music : musicCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(music.getName());
                        fileBase.setType("mp3");
                        fileBase.setSize(music.getSize());
                        fileBase.setPath(music.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<AppInfo> appCheckList = appFragment.getCheckList();
                if (appCheckList.size() > 0) {
                    for (AppInfo appInfo : appCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(appInfo.getApkName());
                        fileBase.setType("apk");
                        fileBase.setSize(appInfo.getApkSize());
                        fileBase.setPath(appInfo.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<Picture> pictureCheckList = pictureFragment.getCheckList();
                if (pictureCheckList.size() > 0) {
                    for (Picture picture : pictureCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(picture.getName());
                        fileBase.setType("image");
                        fileBase.setSize(picture.getSize());
                        fileBase.setPath(picture.getPath());
                        MyLog.d(TAG, picture.getPath() + "   " + fileBase.getSize());
                        fileBaseList.add(fileBase);
                    }
                }
                List<Video> videoCheckList = videoFragment.getCheckList();
                if (videoCheckList.size() > 0) {
                    for (Video video : videoCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(video.getName());
                        fileBase.setType("mp4");
                        fileBase.setSize(video.getSize());
                        fileBase.setPath(video.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<FileBean> docCheckList = docFragment.getCheckList();
                if (docCheckList.size() > 0) {
                    for (FileBean fileBean : docCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(fileBean.name);
                        fileBase.setType("other");
                        fileBase.setSize(fileBean.size);
                        fileBase.setPath(fileBean.path);
                        fileBaseList.add(fileBase);
                    }
                }
                ChatControler.getInstance().sendFile(fileBaseList);
                Intent intent = new Intent(FileChooseActivity.this, TransRecordActivity.class);
                intent.putExtra("action", "sendFile");
                startActivity(intent);
            }
        });

        waitText = (TextView) findViewById(R.id.wait_text);
        friendLayout = (LinearLayout) findViewById(R.id.friend_layout);
        friendImage = (ImageView) findViewById(R.id.friend_image);
        friendName = (TextView) findViewById(R.id.friend_name);

        ImageView myImage = (ImageView) findViewById(R.id.my_image);
        TextView myName = (TextView) findViewById(R.id.my_name);
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
        myName.setText(Constant.deviceName);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                boolean hasTaskRuning = ChatControler.getInstance().hasFileTransporting();
                if (hasTaskRuning) {
                    MyDialog dialog = new MyDialog.Builder(FileChooseActivity.this).setTitle("文件传输中，请稍等")
                            .setNoTextGone().setYesText(null).create();
                    dialog.show();
                } else {
                    MyDialog dialog = new MyDialog.Builder(this).setTitle("确定断开连接？").setNoText()
                            .setYesText(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ChatControler.getInstance().sendCloseFlag();
                                    ChatControler.getInstance().stopChat();
                                    finish();
                                }
                            }).create();
                    dialog.show();
                }
                break;
            case R.id.record:
                Intent intent = new Intent(FileChooseActivity.this, TransRecordActivity.class);
                intent.putExtra("action", "null");
                startActivity(intent);
                break;
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

    @Override
    public void onBackPressed() {
        boolean hasTaskRuning = ChatControler.getInstance().hasFileTransporting();
        if (hasTaskRuning) {
            MyDialog dialog = new MyDialog.Builder(FileChooseActivity.this).setTitle("文件传输中，请稍等")
                    .setNoTextGone().setYesText(null).create();
            dialog.show();
        } else {
            MyDialog dialog = new MyDialog.Builder(this).setTitle("确定断开连接？").setNoText()
                    .setYesText(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChatControler.getInstance().sendCloseFlag();
                            ChatControler.getInstance().stopChat();
                            finish();
                        }
                    }).create();
            dialog.show();
        }
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //统计所有选择的文件数
            fileSelectedNumber = appFragment.getSelectedCount() + musicFragment.getSelectedCount() +
                    videoFragment.getSelectedCount() + pictureFragment.getSelectedCount() + docFragment.getSelectedCount();
            numberSelected.setText("" + fileSelectedNumber);
            if (fileSelectedNumber > 0) {
                sendButton.setEnabled(true);
                sendButton.setBackgroundColor(getResources().getColor(R.color.soft_black));
            } else {
                sendButton.setEnabled(false);
                sendButton.setBackgroundColor(getResources().getColor(R.color.soft_gray));
            }
        }
    }

    @Override
    public void noitifyFileSeletedNumberChanged() {
        fileSelectedNumber = appFragment.getSelectedCount();
        numberSelected.setText("" + fileSelectedNumber);
    }
}
