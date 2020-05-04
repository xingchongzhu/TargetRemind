package com.wtach.stationremind.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.wtach.stationremind.AlarmActivity;
import com.wtach.stationremind.LocationApplication;
import com.wtach.stationremind.R;
import com.wtach.stationremind.listener.LocationChangerListener;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.object.NotificationObject;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.NotificationUtil;
import com.wtach.stationremind.utils.NotificationUtils;
import com.wtach.stationremind.utils.PathSerachUtil;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author baidu
 */
public class RemonderLocationService extends Service {

    private final static String TAG = "RemonderLocationService";
    public final static String CLOSE_REMINDER_SERVICE = "close.reminder.service";
    private UpdateBinder updateBinder= new UpdateBinder();
    private LocationService locationService;
    /**
     * 回调
     */
    private ArrivedCallback mArrivedCallback;
    private LocationChangerListener mLocationChangerListener;
    public static boolean state = false;

    BDLocation currentLocation;

    private boolean isReminder = false;
    private boolean locationServiceHasStart = false;

    private SuggestionResult.SuggestionInfo mTargetStation;
    //位置提醒
    public LocationClient mLocationClient = null;
    public BDNotifyListener myListener = new MyNotifyListener();

    public IBinder onBind(Intent intent) {
        return updateBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.mArrivedCallback = null;
        return super.onUnbind(intent);
    }

    public class UpdateBinder extends Binder {
        public RemonderLocationService getService() {
            return RemonderLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // -----------location config ------------
        locationService = ((LocationApplication) getApplication()).locationService;
        // 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //startForeground(NotificationUtil.arriveNotificationId,mNotificationUtil.arrivedNotification(getApplicationContext(),NotificationUtil.arriveNotificationId));
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());

    }

    public void setLocationChangerListener(LocationChangerListener locationChangerListener) {
        this.mLocationChangerListener = locationChangerListener;
    }

    public void startLocationService() {
        if (locationService != null && !locationServiceHasStart) {
            locationService.start();
            locationServiceHasStart = true;
        }
    }

    public BDLocation getCurrentLocation() {
        return currentLocation;
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            Log.d(TAG, "BDAbstractLocationListener location = "+location);
            if (null != location
                    && location.getLocType() != BDLocation.TypeServerError) {
                if (!CommonFuction.isvalidLocation(location)) {
                    Log.d(TAG, "BDAbstractLocationListener location invale ");
                    currentLocation = null;
                    return;
                }
                currentLocation = location;
                if (mLocationChangerListener != null) {
                    mLocationChangerListener.loactionStation(location);
                }
            }
        }
    };

    public void setStartReminder(SuggestionResult.SuggestionInfo targetStation){
        //注册监听函数
        mLocationClient.registerNotify(myListener);
        this.mTargetStation = targetStation;
        //设置位置提醒，四个参数分别是：纬度、经度、半径、坐标类型
        LatLng latLng = mTargetStation.getPt();
        myListener.SetNotifyLocation(latLng.latitude, latLng.longitude, CommonConst.TARGETRANGE, mLocationClient.getLocOption().getCoorType());
        //mLocationClient为第二步初始化过的LocationClient对象
        //调用LocationClient的start()方法，开启定位
        mLocationClient.start();

    }

    public void setCancleReminder() {
        isReminder = false;
        stopForeground(true);
        //myListener为第二步中定义过的BDNotifyListener对象
        //调用执行removeNotifyEvent方法，即可实现取消监听
        mLocationClient.removeNotifyEvent(myListener);

        locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }


    /**
     * 回调接口
     *
     * @author lenovo
     */
    public static interface ArrivedCallback {

        void arriaved(BDLocation mlocation, float distance);

        void errorHint(String error);
    }

    public void setArrivedCallback(ArrivedCallback mArrivedCallback) {
        this.mArrivedCallback = mArrivedCallback;
    }

    /**
     * 服务销毁的时候调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationService != null) {
            //locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
            locationService.unregisterListener(mListener); // 注销掉监听
            locationService.stop(); // 停止定位服务
        }
        setCancleReminder();
        Log.d(TAG, "onDestroy ");
    }

    public class MyNotifyListener extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance){
            if(mArrivedCallback != null){
                mArrivedCallback.arriaved(mlocation,distance);
            }
            sendHint(RemonderLocationService.this,true,getString(R.string.arrived_reminder),"","");
            setCancleReminder();
            //已到达设置监听位置附近
            Log.d(TAG,"onNotify 已到达设置监听位置附近 distance = "+distance+" "+mlocation.getAddress());
        }
    }

    public static void sendHint(Context context, boolean isArrive, String title, String content, String change) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arrive", isArrive);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("change", change);
        context.startActivity(intent);
    }

}