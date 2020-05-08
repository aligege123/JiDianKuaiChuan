package com.jidiankuaichuan.android.chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.widget.EditText;

import com.jidiankuaichuan.android.Constant;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.chat.adapter.MsgAdapter;
import com.jidiankuaichuan.android.chat.model.Msg;
import com.jidiankuaichuan.android.utils.ToastUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private static final int MSG_LISTENER_ONRECEIVE = 0x24;

    private static final int MSG_LISTENER_ONFAILURE = 0x25;

    private List<Msg> msgList = new ArrayList<>();

    private RecyclerView msgRecyclerView;

    private MsgAdapter msgAdapter;

    private EditText inputText;

    private Toolbar toolbar;

    private String friendName;

    private LocalBroadcastManager localBroadcastManager;

    private LocalReceiver localReceiver;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_LISTENER_ONRECEIVE) {
                msgAdapter.notifyItemInserted(msgList.size() - 1);
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
            } else if (msg.what == MSG_LISTENER_ONFAILURE) {
                toolbar.setTitle(friendName + "(未连接)");
            }
        }
    };

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (friendName != null && friendName.equals(BlueToothChatControler.getInstance().getFriendName())) {
                toolbar.setTitle(friendName + "(已连接)");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        friendName = intent.getStringExtra("friendName");
        String address = intent.getStringExtra("friendAddress");
        List<Msg> msgs = LitePal.where("address = ?", address).find(Msg.class);
        if (msgs != null && msgs.size() > 0) {
            msgList = msgs;
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BlueToothChatControler.getInstance().setOnReceiveListener(new ChatThread.OnReceiveListener() {
            @Override
            public void onReceive(String data) {
                Msg msg = new Msg(data, Msg.TYPE_RECEIVED);
                msgList.add(msg);
                mHandler.sendEmptyMessage(MSG_LISTENER_ONRECEIVE);
            }

            @Override
            public void onFailure() {
                if (BlueToothChatControler.state == BlueToothChatControler.STATE_CONNECTED) {
                    BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTION_LOST;
                }
                mHandler.sendEmptyMessage(MSG_LISTENER_ONFAILURE);
            }
        });

        // local broadcast
        IntentFilter localFilter = new IntentFilter("FRIEND_CONNECTED");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, localFilter);
    }

    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(localReceiver);
        super.onStop();
    }

    public void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        //toolbar
        toolbar = (Toolbar) findViewById(R.id.chat_bar);
        if (BlueToothChatControler.state == BlueToothChatControler.STATE_CONNECTED) {
            toolbar.setTitle(friendName + "(已连接)");
        } else {
            toolbar.setTitle(friendName + "(未连接)");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //RecyclerView
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        msgAdapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(msgAdapter);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
        //inputText
        inputText = (EditText) findViewById(R.id.input_text);
        //Button
        Button send = (Button) findViewById(R.id.send_msg);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO send message
                if (BlueToothChatControler.state != BlueToothChatControler.STATE_CONNECTED) {
                    ToastUtil.s("暂不支持离线消息");
                    return;
                }
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    BlueToothChatControler.getInstance().sendMessage(content);
                    Msg msg = new Msg(content, Msg.TYPE_SEND);
                    msgList.add(msg);
                    msgAdapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    inputText.setText("");

                    //save message
                    msg.setAddress(BlueToothChatControler.getInstance().getFriendAddress());
                    msg.save();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
