package com.jidiankuaichuan.android.chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.chat.adapter.NearbyAdapter;
import com.jidiankuaichuan.android.utils.BlueToothUtils;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AddFriendActivity extends AppCompatActivity {

    private static final String TAG = "AddFriendActivity";
    
    private List<BluetoothDevice> deviceList = new ArrayList<>();

    private NearbyAdapter nearbyAdapter;
    
    private Button scan;
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action != null) {
                switch (action){
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        MyLog.d(TAG, "start scanning");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        MyLog.d(TAG, "scanning over");
//                        ToastUtil.s("结束扫描");
                        scan.setText("扫描");
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            finish();
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        MyLog.d(TAG, "found devices");
                        //判断未配对的设备
                        if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                            String name = device.getName();
                            if (name != null && !("".equals(name)) && !name.matches(" *")) {
                                deviceList.add(device);
                                nearbyAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        MyLog.e(TAG, "bond state change");
                        switch (device.getBondState()) {
                            case BluetoothDevice.BOND_BONDING:
                                MyLog.d(TAG, "bonding");
                                break;
                            case BluetoothDevice.BOND_BONDED:
                                MyLog.e( TAG, "bonded");
                                ToastUtil.s("添加成功");
                                if (deviceList.indexOf(device) != -1) {
                                    deviceList.remove(device);
                                    nearbyAdapter.notifyDataSetChanged();
                                }
                                break;
                            case BluetoothDevice.BOND_NONE:
                                MyLog.e(TAG, "cancel bonding");
                            default:
                                break;
                        }
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //broadcast
        IntentFilter filter1 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter2 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        IntentFilter filter5 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mBroadcastReceiver ,filter1);
        registerReceiver(mBroadcastReceiver ,filter2);
        registerReceiver(mBroadcastReceiver ,filter3);
        registerReceiver(mBroadcastReceiver ,filter4);
        registerReceiver(mBroadcastReceiver, filter5);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void initView() {
        //toolbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.nearby_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //listview
        ListView listView = (ListView) findViewById(R.id.nearby_list);
        nearbyAdapter = new NearbyAdapter(this, R.layout.nearby_item, deviceList);
        listView.setAdapter(nearbyAdapter);
        //button
        scan = (Button) findViewById(R.id.scan_nearby);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = scan.getText().toString();
                if ("扫描".equals(text)) {
                    //scan
                    BlueToothUtils.getInstance().discover();
                    scan.setText("停止");
                } else {
                    //stop
                    BlueToothUtils.getInstance().cancelDiscover();
                    scan.setText("扫描");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
