package com.jidiankuaichuan.android.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.MyApplication;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.callback.FileChooseCallback;
import com.jidiankuaichuan.android.data.AppInfo;
import com.jidiankuaichuan.android.data.FileBase;
import com.jidiankuaichuan.android.ui.Adapter.AppAdapter;
import com.jidiankuaichuan.android.utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment {

    private List<AppInfo> appInfoList = new ArrayList<>();

    private AppAdapter appAdapter;

    private TextView selectText;

    private ProgressDialog progressDialog;

    private GridView gridView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment, container, false);
        gridView = (GridView) view.findViewById(R.id.app_grid_view);
        List<AppInfo> appInfos = FileManager.getInstance(getContext()).getAppInfos();
        for (AppInfo appInfo : appInfos) {
            if (appInfo.isUserApp()) {
                appInfoList.add(appInfo);
            }
        }
        appAdapter = new AppAdapter(getContext(), appInfoList);
        gridView.setAdapter(appAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                appAdapter.setChecked(position);
            }
        });

        selectText = (TextView) view.findViewById(R.id.app_select_all_none);
        selectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = selectText.getText().toString();
                switch (text) {
                    case "全选":
                        appAdapter.selectAll();
                        selectText.setText("全不选");
                        break;
                    case "全不选":
                        appAdapter.unselectAll();
                        selectText.setText("全选");
                        break;
                    default:
                }
            }
        });
        return view;
    }

    public int getSelectedCount() {
        return appAdapter == null ? 0 : appAdapter.getSelectedCount();
    }

    public List<AppInfo> getCheckList() {
        return appAdapter == null ? null : appAdapter.getCheckList();
    }

    public void clearSelected() {
        if (appAdapter != null) {
            appAdapter.unselectAll();
        }
    }

    private void showDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("界面加载中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void closeDialog() {
        progressDialog.dismiss();
    }
}
