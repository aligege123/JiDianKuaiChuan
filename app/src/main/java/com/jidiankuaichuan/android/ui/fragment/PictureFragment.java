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
import com.jidiankuaichuan.android.data.ImgFolderBean;
import com.jidiankuaichuan.android.data.Picture;
import com.jidiankuaichuan.android.ui.Adapter.PictureAdapter;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.kuaichuan_utils.OtherFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PictureFragment extends Fragment {

    private static final String TAG = "PictureFragment";

    private List<ImgFolderBean> imgFolderBeanList;

    private List<String> imagePathList = new ArrayList<>();

    private PictureAdapter pictureAdapter;

    private TextView selectText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.picture_fragment, container, false);
        imgFolderBeanList = FileManager.getInstance(getContext()).getImageFolders();
        for (ImgFolderBean i : imgFolderBeanList) {
            List<String> paths = FileManager.getInstance(getContext()).getImgListByDir(i.getDir());
            imagePathList.addAll(paths);
        }
//        imagePathList = FileManager.getInstance(getContext()).getImagePathList();
        GridView pictureGridView = (GridView) view.findViewById(R.id.picture_grid_view);
        final PictureAdapter adapter = new PictureAdapter(getContext(), imagePathList);
        pictureAdapter = adapter;
        pictureGridView.setAdapter(adapter);
        pictureGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setChecked(position);
            }
        });

        selectText = (TextView) view.findViewById(R.id.picture_select_all_none);
        selectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = selectText.getText().toString();
                switch (text) {
                    case "全选":
                        pictureAdapter.selectAll();
                        selectText.setText("全不选");
                        break;
                    case "全不选":
                        pictureAdapter.unselectAll();
                        selectText.setText("全选");
                        break;
                    default:
                }
            }
        });
        return view;
    }

    public List<Picture> getPictureList() {

        List<Picture> pictureList = new ArrayList<>();

        if (imagePathList.size() > 0) {
            for (String str : imagePathList) {
                File file = new File(str);
                Picture picture = new Picture(OtherFileUtils.getFileName(str), str, file.length());
                pictureList.add(picture);
            }
        } else {
            return null;
        }

        return pictureList;
    }

    public int getSelectedCount() {
        return pictureAdapter == null ? 0 : pictureAdapter.getSelectedCount();
    }

    public List<Picture> getCheckList() {
        return pictureAdapter.getCheckList();
    }
}
