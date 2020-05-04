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
import com.wtach.stationremind.LocationApplication;
import com.wtach.stationremind.listener.LocationChangerListener;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.object.NotificationObject;
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
    private Callback callback;
    private LocationChangerListener mLocationChangerListener;
    public static boolean state = false;

    BDLocation currentLocation;

    private NotificationUtil mNotificationUtil;
    private boolean isReminder = false;
    private boolean locationServiceHasStart = false;
    StationInfo nextStation, currentStation,preStation;
    private List<StationInfo> needChangeStationList;


    public IBinder onBind(Intent intent) {
        return updateBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.callback = null;
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
        mNotificationUtil = new NotificationUtil(this);
        //startForeground(NotificationUtil.arriveNotificationId,mNotificationUtil.arrivedNotification(getApplicationContext(),NotificationUtil.arriveNotificationId));
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
    int n = 0;
    int number = 0;
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

    private boolean isBacrground = false;

    public boolean moveTaskToBack() {
        isBacrground = true;
        Log.d(TAG, "moveTaskToBack isRemind = " + isReminder);
        if (isReminder) {
            NotificationObject mNotificationObject = NotificationUtils.createNotificationObject(getApplicationContext(), currentStation, nextStation);
            setNotification(mNotificationObject);
        }
        return true;
    }

    public void moveInForeground(){
        isBacrground = false;
    }

    public void setNotification(NotificationObject mNotificationObject) {
        //保持cpu一直运行，不管屏幕是否黑屏
        Notification mNotification = mNotificationUtil.showNotification(getApplicationContext(),
                NotificationUtil.notificationId, mNotificationObject);
        //startForeground(NotificationUtil.notificationId,mNotification);
        locationService.getLocationClient().enableLocInForeground(NotificationUtil.notificationId,mNotification);
    }

    public void updataNotification(NotificationObject mNotificationObject) {
        locationService.getLocationClient().enableLocInForeground(NotificationUtil.notificationId,
               mNotificationUtil.updateProgress(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject));
        //mNotificationUtil.updateProgress(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject);
    }

    public void setCancleReminder() {
        isReminder = false;
        stopForeground(true);
        if (mNotificationUtil != null)
            mNotificationUtil.cancel(NotificationUtil.notificationId);
        locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }

    public void cancleNotification() {
        stopForeground(true);
        if (mNotificationUtil != null) {
            mNotificationUtil.cancel(NotificationUtil.notificationId);
        }
        locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }

    /**
     * 回调接口
     *
     * @author lenovo
     */
    public static interface Callback {
        void setCurrentStation(String startCname, String endName, String current);

        void arriaved(boolean state);

        void loactionStation(BDLocation location);

        void errorHint(String error);
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

}