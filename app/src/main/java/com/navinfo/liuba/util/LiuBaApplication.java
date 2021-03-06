package com.navinfo.liuba.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.litesuits.android.log.Log;
import com.navinfo.liuba.BuildConfig;
import com.navinfo.liuba.entity.RegisterUser;
import com.navinfo.liuba.location.GeoPoint;
import com.navinfo.liuba.location.GeometryTools;
import com.navinfo.liuba.location.MyLocationListener;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import org.xutils.x;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by zhangdezhi1702 on 2017/9/16.
 */

public class LiuBaApplication extends Application {
    //百度定位的核心类
    public LocationClient mLocationClient = null;
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口暂时同步保留
    public MyLocationListener myListener = new MyLocationListener();

    public static String rootPath = null;

    private SharedPreferences spf_config;//程序设置选项
    private RegisterUser currentUser;//当前正在登录的用户信息

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化百度地图组件
        SDKInitializer.initialize(getApplicationContext());
        //极光IM的初始化服务
        JMessageClient.setDebugMode(true);
        JMessageClient.init(this);
        //初始化xUtils
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.

        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        mLocationClient.start();

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //检查运行时权限
        withPermissionAll();

        spf_config = getSharedPreferences(SystemConstant.CONFIG_SPF, Context.MODE_PRIVATE);
        Log.setTag("LiuBa");
    }

    //初始化百度定位的参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span = 1000;
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(span);

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

//        option.setOpenGps(true);
//        //可选，默认false,设置是否使用gps

//        option.setLocationNotify(true);
//        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);

//        option.setIgnoreKillProcess(true);
//        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

//        option.setIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

//        option.setEnableSimulateGps(false);
//        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

//        option.setWifiValidTime(5*60*1000);
        //可选，7.2版本新增能力，如果您设置了这个接口，首次启动定位时，会先判断当前WiFi是否超出有效期，超出有效期的话，会先重新扫描WiFi，然后再定位

        mLocationClient.setLocOption(option);
    }

    //获取当前的位置
    public BDLocation getCurrentLocation() {
        if (myListener != null) {
            return myListener.getCurrentLocation();
        }
        return null;
    }

    //申请所需的权限
    private void withPermissionAll() {
        AndPermission.with(LiuBaApplication.this)
                .requestCode(100)
                .permission(Permission.CAMERA, Permission.LOCATION, Permission.STORAGE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        Toast.makeText(LiuBaApplication.this, "无法获取权限，部分功能可能不可用", Toast.LENGTH_SHORT).show();
                    }
                }).rationale(new RationaleListener() {
            @Override
            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                AndPermission.rationaleDialog(LiuBaApplication.this, rationale).show();
            }
        }).start();
    }

    public SharedPreferences getSpf_config() {
        return spf_config;
    }

    public RegisterUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(RegisterUser currentUser) {
        this.currentUser = currentUser;
    }

    public GeoPoint getCurrentGeoPoint() {
        BDLocation bdLocation = getCurrentLocation();
        if (bdLocation != null) {
            return new GeoPoint(bdLocation.getLongitude(), bdLocation.getLatitude());
        }
        return null;
    }

    public String getCurrentGeometry(){
        GeoPoint geoPoint=getCurrentGeoPoint();
        if (geoPoint!=null){
            return GeometryTools.createGeometry(geoPoint).toString();
        }
        return null;
    }
}
