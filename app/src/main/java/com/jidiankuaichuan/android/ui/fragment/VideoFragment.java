package com.jidiankuaichuan.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.Video;
import com.jidiankuaichuan.android.ui.Adapter.VideoAdapter;
import com.jidiankuaichuan.android.utils.FileManager;

import java.util.List;

public class VideoFragment extends Fragment {

    private List<Video> mVideoList;

    private VideoAdapter videoAdapter;

    private TextView selectText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.video_fragment, container, false);
        mVideoList = FileManager.getInstance(getContext()).getVideos();
        final VideoAdapter adapter = new VideoAdapter(getContext(), mVideoList);
        videoAdapter = adapter;
        GridView gridView = (GridView) view.findViewById(R.id.video_grid_view);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setChecked(position);
            }
        });

        selectText = (TextView) view.findViewById(R.id.video_select_all_none);
        selectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = selectText.getText().toString();
                switch (text) {
                    case "全选":
                        videoAdapter.selectAll();
                        selectText.setText("全不选");
                        break;
                    case "全不选":
                        videoAdapter.unselectAll();
                        selectText.setText("全选");
                        break;
                    default:
                }
            }
        });
        return view;
    }

    public int getSelectedCount() {
        return videoAdapter == null ? 0 : videoAdapter.getSelectedCount();
    }

    public List<Video> getCheckList() {
        if (videoAdapter != null) {
            return videoAdapter.getCheckList();
        }
        return null;
    }

    public void clearSelected() {
        if (videoAdapter != null) {
            videoAdapter.unselectAll();
        }
    }
}
