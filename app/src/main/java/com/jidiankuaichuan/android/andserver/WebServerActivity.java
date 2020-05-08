/*
 * Copyright © 2016 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jidiankuaichuan.android.andserver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.andserver.controller.DownLoadController;
import com.yanzhenjie.loading.dialog.LoadingDialog;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Zhenjie Yan on 2018/6/9.
 */
public class WebServerActivity extends AppCompatActivity implements View.OnClickListener {

    private ServerManager mServerManager;

    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnBrowser;
    private TextView mTvMessage;

    private LoadingDialog mDialog;
    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_server);
        Toolbar toolbar = findViewById(R.id.toolbar_web_server);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnBrowser = findViewById(R.id.btn_browse);
        mTvMessage = findViewById(R.id.tv_message);

        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnBrowser.setOnClickListener(this);

        // AndServer run in the service.
        mServerManager = new ServerManager(this);
        mServerManager.register();

        // startServer;
        mBtnStart.performClick();

        Intent intent = getIntent();
        DownLoadController.filePath = intent.getStringExtra("file_path");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServerManager.unRegister();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_start: {
                showDialog();
                mServerManager.startServer();
                break;
            }
            case R.id.btn_stop: {
                showDialog();
                mServerManager.stopServer();
                break;
            }
            case R.id.btn_browse: {
                if (!TextUtils.isEmpty(mRootUrl)) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(mRootUrl));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    /**
     * Start notify.
     */
    public void onServerStart(String ip) {
        closeDialog();
        mBtnStart.setVisibility(View.GONE);
        mBtnStop.setVisibility(View.VISIBLE);
        mBtnBrowser.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(ip)) {
            //ip不为空
            List<String> addressList = new LinkedList<>();
            mRootUrl = "http://" + ip + ":5060/";
            addressList.add(mRootUrl);
            addressList.add("点击下载链接即可下载");
            mTvMessage.setText(TextUtils.join("\n", addressList));
        } else {
            mRootUrl = null;
            mTvMessage.setText("Did not get the server IP address.");
        }
    }

    /**
     * Error notify.
     */
    public void onServerError(String message) {
        closeDialog();
        mRootUrl = null;
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnStop.setVisibility(View.GONE);
        mBtnBrowser.setVisibility(View.GONE);
        mTvMessage.setText(message);
    }

    /**
     * Stop notify.
     */
    public void onServerStop() {
        closeDialog();
        mRootUrl = null;
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnStop.setVisibility(View.GONE);
        mBtnBrowser.setVisibility(View.GONE);
        mTvMessage.setText("The server has stopped.");
    }

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new LoadingDialog(this);
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void closeDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}