package com.jidiankuaichuan.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.callback.FileChooseCallback;
import com.jidiankuaichuan.android.data.AppInfo;
import com.jidiankuaichuan.android.ui.Adapter.AppAdapter;
import com.jidiankuaichuan.android.utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment {

    private List<AppInfo> appInfoList = new ArrayList<>();

    private AppAdapter appAdapter;

//    private FileChooseCallback fileChooseCallback;
//
//    public AppFragment(FileChooseCallback fileChooseCallback) {
//        this.fileChooseCallback = fileChooseCallback;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment, container, false);
        List<AppInfo> appInfos = FileManager.getInstance(getContext()).getAppInfos();
        for (AppInfo appInfo : appInfos) {
            if (appInfo.isUserApp()) {
                appInfoList.add(appInfo);
            }
        }
        GridView gridView = (GridView) view.findViewById(R.id.app_grid_view);
        final AppAdapter adapter = new AppAdapter(getContext(), appInfoList);
        appAdapter = adapter;
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setChecked(position);
//                fileChooseCallback.noitifyFileSeletedNumberChanged();
            }
        });
        return view;
    }

    public int getSelectedCount() {
        return appAdapter == null ? 0 : appAdapter.getSelectedCount();
    }
}
