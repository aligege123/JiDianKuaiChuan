package com.jidiankuaichuan.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.andserver.WebServerActivity;
import com.jidiankuaichuan.android.ui.PictureChooseTestActivity;

public class WebTransFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.web_trans_fragment, container, false);
        Button choose = (Button) view.findViewById(R.id.choose_picture);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PictureChooseTestActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
