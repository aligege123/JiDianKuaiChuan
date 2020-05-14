package com.jidiankuaichuan.android.chat.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.chat.BlueToothChatControler;
import com.jidiankuaichuan.android.chat.FriendListActivity;
import com.jidiankuaichuan.android.chat.model.Friend;
import com.jidiankuaichuan.android.chat.model.Msg;
import com.jidiankuaichuan.android.ui.Adapter.MusicAdapter;
import com.jidiankuaichuan.android.ui.dialog.MyDialog;
import com.jidiankuaichuan.android.utils.BlueToothUtils;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import org.litepal.LitePal;
import org.w3c.dom.Text;

import java.util.List;

public class FriendAdapter extends ArrayAdapter<Friend> {
    private static final String TAG = "friendAdapter";

    private int resourceId;

    private List<Friend> friendList;

    private Handler handler;

    private Listener listener;

    public FriendAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        friendList = objects;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Friend friend = getItem(position);
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.friendName = (TextView) view.findViewById(R.id.chat_friend_name);
            viewHolder.friendAddress = (TextView) view.findViewById(R.id.chat_friend_address);
            viewHolder.state = (TextView) view.findViewById(R.id.friend_state);
            viewHolder.message = (TextView) view.findViewById(R.id.message);
            viewHolder.connect = (Button) view.findViewById(R.id.connect);
            viewHolder.popUp = (Button) view.findViewById(R.id.popup);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String name = friend.getDevice().getName();
        if (name.length() > 8) {
            name = name.substring(0, 8) + "..";
        }
        viewHolder.friendName.setText(name);
        viewHolder.friendAddress.setText(friend.getDevice().getAddress());
        viewHolder.state.setText(friend.getState());
        //handle message
        String message = friend.getMessage();
        if (message == null || "".equals(message)) {
            Msg msg = LitePal.where("address = ?", friend.getDevice().getAddress()).findLast(Msg.class);
            if (msg == null) {
                message = "";
            } else {
                message = msg.getContent();
            }
        }
        if (message.length() > 12) {
            message = message.substring(0, 12) + "..";
        }
        viewHolder.message.setText(message);

        //handle button
        if (friend.getState().equals("未连接") || friend.getState().equals("在线")) {
            viewHolder.connect.setText("发起聊天");
        } else {
            viewHolder.connect.setText("断开连接");
        }
        viewHolder.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.connect.getText().equals("发起聊天")) {
                    BluetoothDevice device = friend.getDevice();
                    //启动connectThread
                    BlueToothChatControler.getInstance().connect(friend.getDevice(), handler);
                    BlueToothChatControler.state = BlueToothChatControler.STATE_CONNECTING;
                } else if (viewHolder.connect.getText().equals("断开连接")) {
                    BlueToothChatControler.getInstance().stopConnect();
                }
            }
        });
        //popUpMenu
        final PopupMenu popupMenu = new PopupMenu(getContext(), viewHolder.popUp);
        popupMenu.getMenuInflater().inflate(R.menu.menu_delete_friend, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                MyDialog dialog = new MyDialog.Builder(getContext())
                        .setTitle("是否删除该好友")
                        .setNoText()
                        .setYesText(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (friend.getState().equals("已连接")) {
                                        ToastUtil.s("请先断开连接");
                                        return;
                                    }
                                    BlueToothUtils.getInstance().removeBond(friend.getDevice().getClass(), friend.getDevice());
                                    if (friend.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
                                        MyLog.e(TAG, "delete friend");
                                        //remove bond success
                                        if (listener != null) {
                                            listener.onRemove(friend);
                                        }
                                        // delete msg record
                                        LitePal.deleteAll(Msg.class, "address = ?", friend.getDevice().getAddress());
                                    } else {
                                        //remove bond failed
                                        getContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).create();
                dialog.show();
                return true;
            }
        });
        viewHolder.popUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
        return view;
    }

    static class ViewHolder {
        TextView friendName;
        TextView friendAddress;
        TextView state;
        TextView message;
        Button connect;
        Button popUp;
    }

    public interface Listener {
        void onRemove(Friend friend);
    }
}
