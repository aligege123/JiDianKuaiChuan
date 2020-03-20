package com.jidiankuaichuan.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jidiankuaichuan.android.ui.fragment.FileTransFragment;
import com.jidiankuaichuan.android.ui.fragment.InfoFragment;
import com.jidiankuaichuan.android.ui.fragment.WebTransFragment;
import com.jidiankuaichuan.android.utils.MyLog;
import com.jidiankuaichuan.android.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final int REQUEST_CODE_PERMISSION_LOCATION = 0x2;

    private final int REQUEST_CODE_OPEN_GPS = 0x3;

    private ViewPager viewPager;

    private BottomNavigationView bottomNavigationView;

    private List<Fragment> mFragments;

    private HomeFragmentPagerAdapter homeFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermissions();
    }

    private void initView() {

        //find View
        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar_main);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav_view);

        //初始化toolbar
        toolbar.setTitle("传送文件");
        setSupportActionBar(toolbar);

        //init fragments
        List<Fragment> homeFragmentList = new ArrayList<>();
        homeFragmentList.add(new FileTransFragment());
        homeFragmentList.add(new WebTransFragment());
        homeFragmentList.add(new InfoFragment());

        //init viewPager
        HomeFragmentPagerAdapter adapter = new HomeFragmentPagerAdapter(getSupportFragmentManager(), homeFragmentList);
        viewPager.setAdapter(adapter);

        //register viewPager listener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //register BottomNavigationView listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                // 跳转指定页面：Fragment
                switch (menuId) {
                    case R.id.tab_one:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.tab_two:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_three:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 检查权限
     */
    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission);
            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }


    /**
     * 开启GPS
     * @param permission
     */
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能。")
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                } else {
                    //GPS已经开启了
                }
                break;
        }
    }

    /**
     * 检查GPS是否打开
     * @return
     */
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }


    /**
     * 权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = true;
        //MyLog.d(TAG, "ffffffffff");
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        } else if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //ToastUtil.s("应用未授权，无法正常使用");
                            flag = false;
                            break;
                        } else if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //ToastUtil.s("应用未授权，无法正常使用");
                            flag = false;
                            break;
                        }
                    }
                }
                break;
        }
        if (!flag) {
            ToastUtil.l("应用未授权，无法正常使用");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GPS:
                if (!checkGPSIsOpen()) {
                    finish();
                }
                break;
            default:
        }
    }

    private class HomeFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragmentLists;

        public HomeFragmentPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragmentLists = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mFragmentLists == null ? null : this.mFragmentLists.get(position);
        }

        @Override
        public int getCount() {
            return this.mFragmentLists == null ? 0 : this.mFragmentLists.size();
        }
    }
}
