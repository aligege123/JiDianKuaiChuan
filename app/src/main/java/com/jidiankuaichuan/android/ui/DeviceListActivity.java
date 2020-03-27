package com.jidiankuaichuan.android.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.threads.controler.ChatControler;
import com.jidiankuaichuan.android.ui.Adapter.MatchedAdapter;
import com.jidiankuaichuan.android.ui.Adapter.NonMatchedAdapter;
import com.jidiankuaichuan.android.utils.BlueToothUtil;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";

    private final int OPENBLUETOOTH = 0x1;

    private int found = 0;

    private List<BluetoothDevice> matchedList = new ArrayList<>();

    private List<BluetoothDevice> nonMatchList = new ArrayList<>();

    private MatchedAdapter matchedAdapter;

    private NonMatchedAdapter nonMatchedAdapter;

    private BlueToothUtil mBlueToothUtil = new BlueToothUtil();

    private Button button;

    private Boolean isConnecting = false;

    private final BroadcastReceiver mFindBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    MyLog.d(TAG, "开始扫描");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    MyLog.d(TAG, "结束扫描");
                    ToastUtil.s("结束扫描");
                    button.setText("扫描");
                    if (found == 0) {
                        matchedList.clear();
                        matchedAdapter.notifyDataSetChanged();
                    }
                    found = 0;
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            finish();
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    MyLog.d(TAG, "发现设备");
                    //ToastUtil.s("发现设备");
                    found = 1;
                    //获取已配对蓝牙设备
                    Set<BluetoothDevice> devices = mBlueToothUtil.getBondedDevices();
                    matchedList.clear();
                    for (BluetoothDevice bonddevice : devices) {
                        matchedList.add(bonddevice);
                        matchedAdapter.notifyDataSetChanged();
                    }
                    //判断未配对的设备
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                        String name = device.getName();
                        if (name != null && ("".equals(name)) == false) {
                            nonMatchList.add(device);
                            nonMatchedAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    MyLog.d(TAG, "设备绑定状态改变...");
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            MyLog.d(TAG, "正在配对...");
                            ToastUtil.s("正在配对...");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            MyLog.d( TAG, "配对完成");
                            ToastUtil.s("配对完成");
                            if (nonMatchList.indexOf(device) != -1) {
                                nonMatchList.remove(device);
                                nonMatchedAdapter.notifyDataSetChanged();
                                matchedList.add(device);
                                matchedAdapter.notifyDataSetChanged();
                            }
                            break;
                        case BluetoothDevice.BOND_NONE:
                            MyLog.d(TAG, "取消配对");
                            ToastUtil.s("取消配对");
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_CONNECT_SUCCESS:
                    //进入文件选择活动
                    ToastUtil.s("连接成功");
                    isConnecting = false;
                    Intent intent = new Intent(DeviceListActivity.this, FileChooseActivity.class);
                    intent.putExtra("state", "send");
                    startActivity(intent);
                    finish();
                    break;
                case Constant.MSG_CONNECT_FAIL:
                    //连接失败
                    MyLog.d(TAG, "连接失败，对方可能未打开蓝牙");
                    ToastUtil.s("连接失败，对方可能未打开蓝牙");
                    isConnecting = false;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initView();
        //打开蓝牙
        if (!mBlueToothUtil.isBlueEnable()) {
            mBlueToothUtil.openBlueSync(DeviceListActivity.this, OPENBLUETOOTH);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //搜索开始的过滤器
        IntentFilter filter1 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //搜索结束的过滤器
        IntentFilter filter2 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //寻找到设备的过滤器
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //绑定状态改变
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //配对请求
        //IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        //蓝牙状态
        IntentFilter filter5 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mFindBlueToothReceiver ,filter1);
        registerReceiver(mFindBlueToothReceiver ,filter2);
        registerReceiver(mFindBlueToothReceiver ,filter3);
        registerReceiver(mFindBlueToothReceiver ,filter4);
        registerReceiver(mFindBlueToothReceiver, filter5);
        //registerReceiver(mFindBlueToothReceiver ,filter5);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mFindBlueToothReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    @Override
    public void onBackPressed() {
        if (mBlueToothUtil.isBlueEnable()) {
            mBlueToothUtil.closeBlueAsyn();
        }
        super.onBackPressed();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        matchedAdapter = new MatchedAdapter(DeviceListActivity.this, R.layout.non_bond_device_item, matchedList);
        ListView matchedListView = (ListView) findViewById(R.id.bonded_list);
        matchedListView.setAdapter(matchedAdapter);
        matchedListView.setDivider(null);

        nonMatchedAdapter = new NonMatchedAdapter(DeviceListActivity.this, R.layout.non_bond_device_item, nonMatchList);
        ListView nonMatchedListView = (ListView) findViewById(R.id.non_bonded_list);
        nonMatchedListView.setAdapter(nonMatchedAdapter);
        nonMatchedListView.setDivider(null);

        //扫描按钮
        button = (Button) findViewById(R.id.scan_cancel_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = button.getText().toString();
                if ("扫描".equals(text)) {
                    //开始搜索设备
                    mBlueToothUtil.discover();
                    button.setText("停止");
                } else {
                    //停止
                    mBlueToothUtil.cancelDiscover();
                    button.setText("扫描");
                }
            }
        });

        //设置点击事件
        nonMatchedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ToastUtil.s("配对中...");
                String adress = nonMatchList.get(position).getAddress();
                mBlueToothUtil.cancelDiscover();

                try {
                    BluetoothDevice remoteDevice = mBlueToothUtil.getRemoteDevice(adress);
                    if(mBlueToothUtil.createBond(remoteDevice.getClass(), remoteDevice)) {
                        MyLog.d(TAG,"可以配对");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MyLog.d(TAG, "配对异常");
                }
            }
        });

        matchedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = matchedList.get(position);
                if (!isConnecting) {
                    ChatControler.getInstance().startChatWith(device, BluetoothAdapter.getDefaultAdapter(), handler);
                    MyLog.d(TAG, "开启客户端");
                    isConnecting = true;
                } else {
                    ToastUtil.s("正在连接");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            mBlueToothUtil.closeBlueAsyn();
        }
        return super.onOptionsItemSelected(item);
    }
}
