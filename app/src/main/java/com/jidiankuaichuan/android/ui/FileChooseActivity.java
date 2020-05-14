package com.jidiankuaichuan.android.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.callback.FileChooseCallback;
import com.jidiankuaichuan.android.data.AppInfo;
import com.jidiankuaichuan.android.data.DeviceInfo;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.data.FileBean;
import com.jidiankuaichuan.android.data.Music;
import com.jidiankuaichuan.android.data.Picture;
import com.jidiankuaichuan.android.data.Video;
import com.jidiankuaichuan.android.service.FileTransService;
import com.jidiankuaichuan.android.threads.controler.ChatController;
import com.jidiankuaichuan.android.ui.Adapter.DirAdapter;
import com.jidiankuaichuan.android.ui.dialog.MyDialog;
import com.jidiankuaichuan.android.ui.fragment.AppFragment;
import com.jidiankuaichuan.android.ui.fragment.DirFragment;
import com.jidiankuaichuan.android.ui.fragment.DocFragment;
import com.jidiankuaichuan.android.ui.fragment.MusicFragment;
import com.jidiankuaichuan.android.ui.fragment.PictureFragment;
import com.jidiankuaichuan.android.ui.fragment.VideoFragment;
import com.jidiankuaichuan.android.utils.BlueToothUtils;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import q.rorbin.badgeview.QBadgeView;

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

    private final int OPENBLUETOOTH = 0x99;

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
                            BlueToothUtils.getInstance().setCanBeDiscovered(FileChooseActivity.this);
                            //开启接收线程
                            fileTransBinder.startServerReceive(receiveHandler);
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

    private DirFragment dirFragment;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private FileFragmentPagerAdapter fileFragmentPagerAdapter;

    private LocalBroadcastManager localBroadcastManager;

    private LocalReceiver localReceiver;

    private int fileSelectedNumber = 0;

    private ProgressDialog progressDialog;

    private FileTransService.FileTransBinder fileTransBinder;

    private Boolean isStop = false;

    private int badgeCount = 0;

    private QBadgeView badgeView;

    private Boolean isConnected = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLog.e(TAG, "服务已开启");
            fileTransBinder = (FileTransService.FileTransBinder) service;
            progressDialog.dismiss();

            Intent intent = getIntent();
            String state = intent.getStringExtra("state");
            if (BlueToothUtils.getInstance().isBlueEnable()) {
                if ("send".equals(state)) {
                    //开启客户端的接收线程
                    fileTransBinder.startClientReceive(sendHandler);

                    //发送设备名和头像
                    ChatController.getInstance().sendDeviceInfo(Constant.deviceName, Constant.imageId);
                } else if ("recv".equals(state)) {
                    //设置设备可被发现
                    BlueToothUtils.getInstance().setCanBeDiscovered(FileChooseActivity.this);
                    //开启接收线程
                    fileTransBinder.startServerReceive(receiveHandler);
                    MyLog.e(TAG, "开启服务端1");
                }

                //set name
//                BlueToothUtils.getInstance().setName("传文件~" + Constant.deviceName);
                BlueToothUtils.getInstance().setName(Constant.deviceName);
            } else {
                BlueToothUtils.getInstance().openBlueSync(FileChooseActivity.this, OPENBLUETOOTH);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //服务端用的handler
    @SuppressLint("HandlerLeak")
    private Handler receiveHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_ERROR:
                    //服务端开启异常
                    MyLog.d(TAG, "服务端开启异常");
                    break;
                case Constant.MSG_DEVICEINFO:
                    isConnected = true;
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
                    if (ChatController.getInstance().isConnected()) {
                        ChatController.getInstance().sendDeviceInfo(Constant.deviceName, Constant.imageId);
                    }
                    break;
                case Constant.MSG_RECV_ERROR:
                    isConnected = false;
                    MyLog.d(TAG, "接收线程中断");
                    friendLayout.setVisibility(View.GONE);
                    waitText.setVisibility(View.VISIBLE);
                    fileTransBinder.restartAcceptReceive(receiveHandler);
                    break;
                case Constant.MSG_GOT_DATA:
                    MyLog.e(TAG, "got data");
                    if (!isStop) {
                        ++badgeCount;
                        badgeView.setBadgeNumber(badgeCount);
                    }
                    break;
                case Constant.MSG_FINISH_LISTENING:
                    MyLog.e(TAG, "stop listening");
                    break;
            }
        }
    };

    //客户端用的handler
    @SuppressLint("HandlerLeak")
    private Handler sendHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_DEVICEINFO:
                    isConnected = true;
                    MyLog.e(TAG, "收到服务端设备信息");
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
                case Constant.MSG_RECV_ERROR:
                    MyLog.d(TAG, "接收线程中断");
                    ChatController.getInstance().stopChat();
                    finish();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_choose);
        //初始化布局
        initView();
        //本地广播管理器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

//        Intent intent = getIntent();
//        String state = intent.getStringExtra("state");

        Intent serviceIntent = new Intent(this, FileTransService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("服务加载中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
        //蓝牙广播
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

        //注册选择的文件数的本地广播
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction("SELECTED_NUMBER_CHANGED");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, localFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dirFragment.setOnClickListener(new DirAdapter.OnClickListener() {
            @Override
            public void onClick(FileBase fileBase) {
                if (!isConnected) {
                    ToastUtil.s("尚未连接任何设备");
                    return;
                }
                List<FileBase> list = new ArrayList<>();
                list.add(fileBase);
                fileTransBinder.startSend(list);
                Intent intent = new Intent(FileChooseActivity.this, TransRecordActivity.class);
                intent.putExtra("action", "sendFile");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStop = true;
        unregisterReceiver(mBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    /**
     * 初始化布局
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
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
        mTitleList.add("存储");

        mFileFragmentList = new ArrayList<>();

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

        //加载碎片
        appFragment = new AppFragment();
        pictureFragment = new PictureFragment();
        musicFragment = new MusicFragment();
        videoFragment = new VideoFragment();
        docFragment = new DocFragment();
        dirFragment = new DirFragment();

        mFileFragmentList.add(appFragment);
        mFileFragmentList.add(pictureFragment);
        mFileFragmentList.add(musicFragment);
        mFileFragmentList.add(videoFragment);
        mFileFragmentList.add(docFragment);
        mFileFragmentList.add(dirFragment);

//        fileFragmentPagerAdapter.notifyDataSetChanged();
        fileFragmentPagerAdapter = new FileFragmentPagerAdapter(getSupportFragmentManager(), mFileFragmentList, mTitleList);
        viewPager.setAdapter(fileFragmentPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        numberSelected = (TextView) findViewById(R.id.selected_num);
        sendButton = (Button) findViewById(R.id.send_file);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    ToastUtil.s("尚未连接任何设备");
                    return;
                }
                //统计选择的文件
                List<FileBase> fileBaseList = new ArrayList<>();
                List<AppInfo> appCheckList = appFragment.getCheckList();
                if (appCheckList != null && appCheckList.size() > 0) {
                    for (AppInfo appInfo : appCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(appInfo.getApkName());
                        fileBase.setType("app");
                        fileBase.setSize(appInfo.getApkSize());
                        fileBase.setPath(appInfo.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<Picture> pictureCheckList = pictureFragment.getCheckList();
                if (pictureCheckList!= null && pictureCheckList.size() > 0) {
                    for (Picture picture : pictureCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(picture.getName());
                        fileBase.setType("image");
                        fileBase.setSize(picture.getSize());
                        fileBase.setPath(picture.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<Music> musicCheckList = musicFragment.getCheckList();
                if (musicCheckList != null && musicCheckList.size() > 0) {
                    for (Music music : musicCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(music.getName());
                        fileBase.setType("mp3");
                        fileBase.setSize(music.getSize());
                        fileBase.setPath(music.getPath());
                        fileBaseList.add(fileBase);
                    }
                }
                List<Video> videoCheckList = videoFragment.getCheckList();
                if (videoCheckList != null && videoCheckList.size() > 0) {
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
                if (docCheckList != null && docCheckList.size() > 0) {
                    for (FileBean fileBean : docCheckList) {
                        FileBase fileBase = new FileBase();
                        fileBase.setName(fileBean.name);
                        fileBase.setType("doc");
                        fileBase.setSize(fileBean.size);
                        fileBase.setPath(fileBean.path);
                        fileBaseList.add(fileBase);
                    }
                }
//                ChatControler.getInstance().sendFile(fileBaseList);
                fileTransBinder.startSend(fileBaseList);
                Intent intent = new Intent(FileChooseActivity.this, TransRecordActivity.class);
                intent.putExtra("action", "sendFile");
                startActivity(intent);
                appFragment.clearSelected();
                pictureFragment.clearSelected();
                musicFragment.clearSelected();
                videoFragment.clearSelected();
                docFragment.clearSelected();
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

        //badgeview
        badgeView = new QBadgeView(this);
        badgeView.bindTarget(myImage);
        badgeView.setBadgeNumber(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPENBLUETOOTH:
                if (!BlueToothUtils.getInstance().isBlueEnable()) {
                    finish();
                } else {
                    //set name
//                    BlueToothUtils.getInstance().setName("传文件~" + Constant.deviceName);
                    BlueToothUtils.getInstance().setName(Constant.deviceName);
                }
                break;
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_file_choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                boolean hasTaskRuning = ChatController.getInstance().hasFileTransporting();
                if (hasTaskRuning) {
                    MyDialog dialog = new MyDialog.Builder(FileChooseActivity.this).setTitle("文件传输中，请稍等")
                            .setNoTextGone().setYesText(null).create();
                    dialog.show();
                } else {
                    MyDialog dialog = new MyDialog.Builder(this).setTitle("确定断开连接？").setNoText()
                            .setYesText(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ChatController.getInstance().stopChat();
                                    finish();
                                }
                            }).create();
                    dialog.show();
                }
                break;
            case R.id.trans_record:
                //clear badgeview
                badgeCount = 0;
                badgeView.setBadgeNumber(0);
                Intent intent = new Intent(FileChooseActivity.this, TransRecordActivity.class);
                intent.putExtra("action", "null");
                startActivity(intent);
                break;
            case R.id.locate:
                BlueToothUtils.getInstance().setCanBeDiscovered(this);
                break;
            case R.id.my_address:
                // TODO show mac address
                MyDialog dialog = new MyDialog.Builder(this)
                        .setTitle(BlueToothUtils.getInstance().getAddress(this))
                        .setNoTextGone()
                        .setEmptyYesText()
                        .create();
                dialog.show();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BlueToothUtils.getInstance().isBlueEnable()) {
            MyLog.d(TAG, "关闭蓝牙");
            BlueToothUtils.getInstance().closeBlueAsyn();
        }
        unbindService(connection);
    }

    @Override
    public void onBackPressed() {
        boolean hasTaskRuning = ChatController.getInstance().hasFileTransporting();
        if (hasTaskRuning) {
            MyDialog dialog = new MyDialog.Builder(FileChooseActivity.this).setTitle("文件传输中，请稍等")
                    .setNoTextGone().setYesText(null).create();
            dialog.show();
        } else {
            MyDialog dialog = new MyDialog.Builder(this).setTitle("确定断开连接？").setNoText()
                    .setYesText(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChatController.getInstance().stopChat();
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

    private class FileFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragmentLists;

        private List<String> mTitleList;

        public FileFragmentPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments, List<String> titleList) {
            super(fragmentManager);
            mFragmentLists = fragments;
            mTitleList = titleList;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mFragmentLists == null ? null : this.mFragmentLists.get(position);
        }

        @Override
        public int getCount() {
            return this.mFragmentLists == null ? 0 : this.mFragmentLists.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //重载该方法，防止其它视图被销毁，防止加载视图卡顿
            //super.destroyItem(container, position, object);
        }
    }
}
