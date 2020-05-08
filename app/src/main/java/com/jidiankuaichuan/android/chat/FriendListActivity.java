package com.jidiankuaichuan.android.chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.chat.adapter.FriendAdapter;
import com.jidiankuaichuan.android.chat.model.Friend;
import com.jidiankuaichuan.android.ui.DeviceListActivity;
import com.jidiankuaichuan.android.utils.BlueToothUtils;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FriendListActivity extends AppCompatActivity {

    private static final String TAG = "FriendListActivity";

    private final int OPENBLUETOOTH = 0x88;

    private FriendAdapter friendAdapter;

    private List<Friend> friendList = new ArrayList<>();

    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            assert device != null;
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //not used yet
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //not used yet
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        //if device not in the friend list
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            return;
                        }
                        boolean flag = false;
                        for (Friend friend : friendList) {
                            if (device.getAddress().equals(friend.getDevice().getAddress())) {
                                flag = true;
                                if (!device.getName().equals(friend.getDevice().getName())) {
                                    // friend name has been changed
                                    friend.setDevice(device);
                                    friendAdapter.notifyDataSetChanged();
                                }
                                break;
                            }
                        }
                        if (!flag) {
                            Friend friend = new Friend(device);
                            friendList.add(friend);
                        }

                        for (Friend friend : friendList) {
                            if (device.getAddress().equals(friend.getDevice().getAddress()) && !friend.getState().equals("已连接")) {
                                friend.setState("在线");
                                friendAdapter.notifyDataSetChanged();
                            } else {
                                friend.setState("未连接");
                                friendAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                        switch (state) {
                            case BluetoothAdapter.STATE_OFF:
                                //TODO clear resources
                                finish();
                                break;
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        MyLog.e(TAG, "bond state change");
                        switch (device.getBondState()) {
                            case BluetoothDevice.BOND_BONDING:
                                MyLog.e(TAG, "bonding");
                                break;
                            case BluetoothDevice.BOND_BONDED:
                                MyLog.e(TAG, "bonded");
                                ToastUtil.s("添加成功");
                                for (Friend friend : friendList) {
                                    if (friend.getDevice().getAddress().equals(device.getAddress())) {
                                        //this friend has been in the friend list
                                        return;
                                    }
                                }
                                Friend friend = new Friend(device);
                                friendList.add(friend);
                                if (BlueToothChatControler.state == BlueToothChatControler.STATE_CONNECTED
                                        && device.getAddress().equals(BlueToothChatControler.getInstance().getFriendAddress())) {
                                    friend.setState("已连接");
                                }
                                friendAdapter.notifyDataSetInvalidated();
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MSG_ERROR:
                    MyLog.e(TAG, "服务端监听断开");
                    break;
                case Constant.MSG_CONNECT_FAIL:
                    MyLog.e(TAG, "connection failed");
                    ToastUtil.s("对方可能未上线或未打开小互传");
                    //restart server
                    BlueToothChatControler.getInstance().restart(mHandler);
                    BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
                    break;
                case Constant.MSG_CONNECT_SUCCESS:
                    MyLog.e(TAG, "connection succeed");
                    BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTED;
                    //get device info, save info
                    Bundle bundle = msg.getData();
                    String name = bundle.getString("name");
                    String address = bundle.getString("address");
                    BlueToothChatControler.getInstance().setFriendName(name);
                    BlueToothChatControler.getInstance().setFriendAddress(address);
                    ToastUtil.s("已连接" + name);

                    for (Friend friend : friendList) {
                        if (friend.getDevice().getAddress().equals(address)) {
                            friend.setState("已连接");
                            break;
                        }
                    }
                    friendAdapter.notifyDataSetChanged();

                    //send local broadcast
                    Intent intent = new Intent("FRIEND_CONNECTED");
                    localBroadcastManager.sendBroadcast(intent);
                    break;
                case Constant.MSG_GOT_DATA:
                    String data = (String) msg.obj;
                    for (Friend friend : friendList) {
                        if (friend.getDevice().getAddress().equals(BlueToothChatControler.getInstance().getFriendAddress())) {
                            friend.setMessage(data);
                            friendAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                case Constant.MSG_RECV_ERROR:
                    MyLog.e(TAG, "connection lost");
                    ToastUtil.s("连接断开");
                    //reset info
                    for (Friend friend : friendList) {
                        if (friend.getDevice().getAddress().equals(BlueToothChatControler.getInstance().getFriendAddress())) {
                            friend.setState("未连接");
                            break;
                        }
                    }
                    friendAdapter.notifyDataSetChanged();
                    BlueToothChatControler.getInstance().setFriendName("");
                    BlueToothChatControler.getInstance().setFriendAddress("");
                    //restart AcceptThread
                    if (BlueToothChatControler.state != BlueToothChatControler.STATE_NONE) {
                        BlueToothChatControler.getInstance().restart(mHandler);
                        BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
                        MyLog.e(TAG, "handler: service restart");
                    }
                    break;
                default:
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        //open BT
        if (!BlueToothUtils.getInstance().isBlueEnable()) {
            BlueToothUtils.getInstance().openBlueSync(FriendListActivity.this, OPENBLUETOOTH);
        } else {
            Set<BluetoothDevice> devices = BlueToothUtils.getInstance().getBondedDevices();
            if (devices.size() > 0) {
                for (BluetoothDevice device : devices) {
//                    if (device.getName().substring(0, 4).equals("小互传~")) {
//                        Friend friend = new Friend(device);
//                        friendList.add(friend);
//                    }
                    Friend friend = new Friend(device);
                    friendList.add(friend);
                }
            }

            //set name
//            BlueToothUtils.getInstance().setName("小互传~" + Constant.deviceName);
            BlueToothUtils.getInstance().setName(Constant.deviceName);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //initview
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //register broadcast
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter filter3 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter4 = new IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myBroadcastReceiver, filter1);
        registerReceiver(myBroadcastReceiver, filter2);
        registerReceiver(myBroadcastReceiver, filter3);
        registerReceiver(myBroadcastReceiver, filter4);
        registerReceiver(myBroadcastReceiver, filter5);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //start ChatAcceptThread
        if (BlueToothUtils.getInstance().isBlueEnable()) {
            if (BlueToothChatControler.state == BlueToothChatControler.STATE_NONE) {
                MyLog.e(TAG, "onResume(): service start");
                BlueToothChatControler.getInstance().start(mHandler);
                BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
            }
            if (BlueToothChatControler.state == BlueToothChatControler.STATE_CONNECTION_LOST) {
                MyLog.e(TAG, "onResume(): service restart");
                //reset
                for (Friend friend : friendList) {
                    if (friend.getDevice().getAddress().equals(BlueToothChatControler.getInstance().getFriendAddress())) {
                        friend.setState("未连接");
                        break;
                    }
                }
                friendAdapter.notifyDataSetChanged();
                BlueToothChatControler.getInstance().setFriendName("");
                BlueToothChatControler.getInstance().setFriendAddress("");
                //restart
                BlueToothChatControler.getInstance().restart(mHandler);
                BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
            }
            if (BlueToothChatControler.state == BlueToothChatControler.STATE_ADDING_FRIEND) {
                MyLog.e(TAG, "onResume(): service restart");
                //restart
                BlueToothChatControler.getInstance().restart(mHandler);
                BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister broadcast
        unregisterReceiver(myBroadcastReceiver);
        //close BT
        if (BlueToothUtils.getInstance().isBlueEnable()) {
            BlueToothUtils.getInstance().closeBlueAsyn();
        }
    }

    @Override
    public void onBackPressed() {
        mHandler = null;
        BlueToothChatControler.getInstance().stop();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_friend);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //listview
        friendAdapter = new FriendAdapter(this, R.layout.friend_item, friendList);
        ListView listView = (ListView) findViewById(R.id.friend_list);
        listView.setAdapter(friendAdapter);
        friendAdapter.setHandler(mHandler);
        friendAdapter.setListener(new FriendAdapter.Listener() {
            @Override
            public void onRemove(Friend friend) {
                friendList.remove(friend);
                friendAdapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = friendList.get(position);
                Intent intent = new Intent(FriendListActivity.this, ChatActivity.class);
                intent.putExtra("friendName", friend.getDevice().getName());
                intent.putExtra("friendAddress", friend.getDevice().getAddress());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mHandler = null;
                BlueToothChatControler.getInstance().stop();
                finish();
                break;
            case R.id.add_friend:
                //stop connection
                BlueToothChatControler.getInstance().stopConnect();
                BlueToothChatControler.state = BlueToothChatControler.STATE_ADDING_FRIEND;
                //to add friend activity
                Intent intent = new Intent(FriendListActivity.this, AddFriendActivity.class);
                startActivity(intent);
                break;
            case R.id.find_me:
                BlueToothUtils.getInstance().setCanBeDiscovered(this);
                break;
            case R.id.refresh:
                // refresh friend list
                if (!BlueToothUtils.getInstance().isDiscovering()) {
                    BlueToothUtils.getInstance().discover();
                } else {
                    ToastUtil.s("正在刷新");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPENBLUETOOTH:
                if (!BlueToothUtils.getInstance().isBlueEnable()) {
                    finish();
                } else {
                    //set can be discovered
                    MyLog.d(TAG, "bluetooth opened");
                    BlueToothUtils.getInstance().setCanBeDiscovered(this);

                    //load friend list
                    Set<BluetoothDevice> devices = BlueToothUtils.getInstance().getBondedDevices();
                    if (devices.size() > 0) {
                        for (BluetoothDevice device : devices) {
//                            if (device.getName().substring(0, 4).equals("小互传~")) {
//                                Friend friend = new Friend(device);
//                                friendList.add(friend);
//                            }
                            Friend friend = new Friend(device);
                            friendList.add(friend);
                        }
                    }
                    friendAdapter.notifyDataSetChanged();

                    //set name
//                    BlueToothUtils.getInstance().setName("小互传~" + Constant.deviceName);
                    BlueToothUtils.getInstance().setName(Constant.deviceName);

                    //setup service
                    BlueToothChatControler.getInstance().start(mHandler);
                    BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
                }
                break;
            default:
        }
    }
}
