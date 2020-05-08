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
import com.jidiankuaichuan.android.ui.Adapter.DirAdapter;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.SDCardHelper;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DirFragment extends Fragment {
    private static final String TAG = "DirFragment";

    private List<File> fileList = new ArrayList<>();

    private DirAdapter dirAdapter;

    private TextView back;

    private String publicPath;

    private String currentPath = "";

    private DirAdapter.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.dir_fragment, container, false);
        ListView listView = (ListView) view.findViewById(R.id.dir_list);
        //get public dir
        publicPath = SDCardHelper.getSDCardBaseDir();
        if (publicPath == null) {
            return null;
        }
        currentPath = publicPath;
        File file = new File(publicPath);
        File[] files = file.listFiles();
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
            // sort by name
            sortFileByName();
        }
        dirAdapter = new DirAdapter(getContext(), R.layout.dir_item, fileList);
        listView.setAdapter(dirAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file_clicked = fileList.get(position);
                if (file_clicked.isDirectory()) {
                    File[] childFiles = file_clicked.listFiles();
                    if (childFiles != null && childFiles.length > 0) {
                        fileList.clear();
                        fileList.addAll(Arrays.asList(childFiles));
                        //sort by name
                        sortFileByName();
                        dirAdapter.notifyDataSetChanged();
                        if (back.getVisibility() == View.INVISIBLE) {
                            back.setVisibility(View.VISIBLE);
                        }
                        currentPath = file_clicked.getAbsolutePath();
                    } else {
                        ToastUtil.s("文件夹为空");
                    }
                }
            }
        });

        back = (TextView) view.findViewById(R.id.go_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentFile = new File(currentPath);
                File parentFile = currentFile.getParentFile();
                if (parentFile != null) {
                    File[] childFiles = parentFile.listFiles();
                    if (childFiles != null && childFiles.length > 0) {
                        fileList.clear();
                        fileList.addAll(Arrays.asList(childFiles));
                        dirAdapter.notifyDataSetChanged();
                    }
                    currentPath = parentFile.getAbsolutePath();
                }
                if (currentPath.equals(publicPath)) {
                    back.setVisibility(View.INVISIBLE);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        dirAdapter.setOnClickListener(onClickListener);
    }

    public void setOnClickListener(DirAdapter.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void sortFileByName() {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

}
