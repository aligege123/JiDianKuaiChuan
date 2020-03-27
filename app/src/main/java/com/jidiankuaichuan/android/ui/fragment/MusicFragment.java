package com.jidiankuaichuan.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.Music;
import com.jidiankuaichuan.android.ui.Adapter.MusicAdapter;
import com.jidiankuaichuan.android.utils.FileManager;

import java.util.List;

public class MusicFragment extends Fragment {

    private List<Music> musicList;

    private MusicAdapter musicAdapter;

    private TextView selectText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.music_fragment, container, false);
        musicList = FileManager.getInstance(getContext()).getMusics();
        final MusicAdapter adapter = new MusicAdapter(getActivity(), R.layout.music_list_item, musicList);
        musicAdapter = adapter;
        ListView musicListView = (ListView) view.findViewById(R.id.music_list);
        musicListView.setAdapter(adapter);

        //设置点击事件
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setChecked(position);
            }
        });

        selectText = (TextView) view.findViewById(R.id.music_select_all_none);
        selectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = selectText.getText().toString();
                switch (text) {
                    case "全选":
                        musicAdapter.selectAll();
                        selectText.setText("全不选");
                        break;
                    case "全不选":
                        musicAdapter.unselectAll();
                        selectText.setText("全选");
                        break;
                    default:
                }
            }
        });
        return view;
    }

    public int getSelectedCount() {
        return musicAdapter == null ? 0 : musicAdapter.getSelectedCount();
    }

    public List<Music> getCheckList() {
        return musicAdapter.getMusicChecked();
    }
}
