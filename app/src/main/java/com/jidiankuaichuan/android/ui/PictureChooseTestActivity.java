package com.jidiankuaichuan.android.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.andserver.WebServerActivity;
import com.jidiankuaichuan.android.data.ImgFolderBean;
import com.jidiankuaichuan.android.data.Picture;
import com.jidiankuaichuan.android.ui.Adapter.PictureAdapter;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class PictureChooseTestActivity extends AppCompatActivity {

    private List<String> imagePathList = new ArrayList<>();

    private PictureAdapter pictureAdapter;

    private LocalBroadcastManager localBroadcastManager;

    private LocalReceiver localReceiver;

    private Button sureButton;

    private TextView selected_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_choose_test);

        initView();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //register local broadcast
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction("SELECTED_NUMBER_CHANGED");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, localFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    private void initView() {

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_select_picture);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //girdview
        List<ImgFolderBean> imgFolderBeanList = FileManager.getInstance(this).getImageFolders();
        for (ImgFolderBean i : imgFolderBeanList) {
            List<String> paths = FileManager.getInstance(this).getImgListByDir(i.getDir());
            imagePathList.addAll(paths);
        }
        imagePathList = FileManager.getInstance(this).getImagePathList();
        GridView pictureGridView = (GridView) findViewById(R.id.picture_grid_view_web);
        pictureAdapter = new PictureAdapter(this, imagePathList);
        pictureGridView.setAdapter(pictureAdapter);
        pictureGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!pictureAdapter.getCheck(position) && lock) {
                    ToastUtil.s("暂时只支持选择一张图片");
                    return;
                }
                pictureAdapter.setChecked(position);
            }
        });

        //button
        sureButton = (Button) findViewById(R.id.confirm_picture_selected);
        sureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picture picture = pictureAdapter.getCheckList().get(0);
                Intent intent = new Intent(PictureChooseTestActivity.this, WebServerActivity.class);
                intent.putExtra("file_path", picture.getPath());
                startActivity(intent);
                pictureAdapter.unselectAll();
            }
        });

        //textview
        selected_num = (TextView) findViewById(R.id.picture_selected_num);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private Boolean lock = false;

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
            //count the number
            int fileSelectedNumber = pictureAdapter.getSelectedCount();
            selected_num.setText("" + fileSelectedNumber);
            if (fileSelectedNumber == 1) {
                lock = true;
            } else if (fileSelectedNumber == 0) {
                lock = false;
            }
            if (fileSelectedNumber > 0) {
                sureButton.setEnabled(true);
                sureButton.setBackgroundColor(getResources().getColor(R.color.soft_black));
            } else {
                sureButton.setEnabled(false);
                sureButton.setBackgroundColor(getResources().getColor(R.color.soft_gray));
            }
        }
    }
}
