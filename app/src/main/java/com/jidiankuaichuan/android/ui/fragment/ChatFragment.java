package com.jidiankuaichuan.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.chat.FriendListActivity;

public class ChatFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, container, false);
        Button button = (Button) view.findViewById(R.id.enter_chat);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enter FriendListActivity
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
