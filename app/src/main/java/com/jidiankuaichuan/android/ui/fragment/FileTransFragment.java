package com.jidiankuaichuan.android.ui.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.MainActivity;
import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.ui.DeviceListActivity;
import com.jidiankuaichuan.android.ui.FileChooseActivity;
import com.jidiankuaichuan.android.utils.BlueToothUtil;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class FileTransFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "FileTransFragment";

    private ImageButton sendButton;

    private ImageButton recvButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.file_trans_fragment, container, false);
//        MainActivity.toolbar.setTitle("传送文件");
        sendButton = (ImageButton) view.findViewById(R.id.button_send);
        recvButton = (ImageButton) view.findViewById(R.id.button_recv);
        sendButton.setOnClickListener(this);
        recvButton.setOnClickListener(this);
        //requestWifiPermissions();
        //checkPermissions();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_send:
                //请求权限，开启蓝牙
                BlueToothUtil blueToothUtil = new BlueToothUtil();
                if (blueToothUtil.isSupportBlue()) {
                    Intent intent = new Intent(getActivity(), DeviceListActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.l("您的设备不支持蓝牙功能");
                }
                break;
            case R.id.button_recv:
                //进入扫描界面
                starNextActivity("recv");
                break;
            default:
                break;
        }
    }

    private void starNextActivity(String state) {
        Intent intent = new Intent(getActivity(), FileChooseActivity.class);
        intent.putExtra("state", state);
        startActivity(intent);
    }



//    private void requestWriteSettings() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //大于等于23 请求权限
//            if ( !Settings.System.canWrite(getActivity())) {
//                showAlertDialog();
//            } else {
////                MyLog.d(TAG, "ddddddddd");
//                starNextActivity();
//            }
//        }else{
//            //小于23直接设置
////            MyLog.d(TAG, "fffffffff");
//            starNextActivity();
//        }
//    }

//    private void showAlertDialog() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//        dialog.setTitle("权限设置");
//        dialog.setMessage("Anroid 6.0以上的系统需要设置允许修改系统配置的权限.");
//        dialog.setCancelable(false);
//        dialog.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                intent.setData(Uri.parse("package:" + getActivity().getPackageName() ));
//                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
//            }
//        });
//        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //
//            }
//        });
//        dialog.show();
//    }



//    private void requestWifiPermissions() {
//
//        String[] permissions = new String[]{};
//
//        List<String> mPermissionList = new ArrayList<>();
//        for (int i = 0; i < permissions.length; i++) {
//            if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) != PackageManager.PERMISSION_GRANTED) {
//                mPermissionList.add(permissions[i]);
//            }
//        }
//
//        if (mPermissionList.isEmpty()) {// 全部允许
//            MyLog.d(TAG, "权限已经允许");
//            requestWriteSettings();
//        } else {//存在未允许的权限
//            MyLog.d(TAG, "ffffffff");
//            String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
//            ActivityCompat.requestPermissions(getActivity(), permissionsArr, 101);
//        }
//    }
//
//    @NonNull
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 101:
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        //判断是否勾选禁止后不再询问
//                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i]);
//                        if (showRequestPermission) {
//                            requestWifiPermissions();
//                            return;
//                        } else { // false 被禁止了，不在访问
//                            ToastUtil.s("您拒绝了该权限，不再访问");
//                        }
//                    }
//                }
//                break;
//        }
//    }

    /*private  boolean mShowRequestPermission = true;

    private boolean isWifiPermissionsAllAllowed = true;

    private void requestWifiPermissions() {

        String[] permissions = new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.INTERNET};

        List<String> mPermissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }

        if (mPermissionList.isEmpty()) {// 全部允许
            mShowRequestPermission = true;
        } else {//存在未允许的权限
            String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissionsArr, 101);
        }
    }

    @NonNull
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isWifiPermissionsAllAllowed = false;
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i]);
                        if (showRequestPermission) {
                            requestWifiPermissions();
                            return;
                        } else { // false 被禁止了，不在访问
                            mShowRequestPermission = false;//已经禁止了
                        }
                    }
                }
                break;
        }
    }*/

}
