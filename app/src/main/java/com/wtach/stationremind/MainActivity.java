package com.wtach.stationremind;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.heytap.wearable.support.widget.HeyShapeButton;
import com.heytap.wearable.support.widget.HeySingleDefaultItem;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.listener.LoadDataListener;
import com.wtach.stationremind.listener.LocationChangerListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.recognize.RecogizeManager;
import com.wtach.stationremind.service.LocationService;
import com.wtach.stationremind.service.RemonderLocationService;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.FileUtil;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.utils.NetWorkUtils;
import com.wtach.stationremind.utils.PathSerachUtil;
import com.wtach.stationremind.utils.StartActivityUtils;

import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE;
import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE;

public class MainActivity extends BaseActivity implements View.OnClickListener, LoadDataListener , RemonderLocationService.ArrivedCallback {
    private final String TAG = "MainActivity";
    private HeyShapeButton startRemindBtn;
    private HeySingleDefaultItem selectCity;
    private TextView targetStationView;
    private TextView currentStationView;

    private DataManager mDataManager;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public boolean hasLocation = false;
    private String currentCity = IDef.DEFAULTCITY;
    private SuggestionResult.SuggestionInfo mTargetStation;
    private boolean isStartRemind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        bindService(new Intent(this, RemonderLocationService.class), connection, BIND_AUTO_CREATE);
    }

    private void initData() {
        mDataManager = DataManager.getInstance(this);
        mDataManager.addLoadDataListener(this);
        mDataManager.loadData(this);
    }

    private void initView() {
        startRemindBtn = findViewById(R.id.start_remind_btn);
        currentStationView = findViewById(R.id.current_station);
        targetStationView = findViewById(R.id.target_station);
        selectCity = findViewById(R.id.select_city);

        startRemindBtn.setOnClickListener(this);
        targetStationView.setOnClickListener(this);
        selectCity.setOnClickListener(this);

        startRemindBtn.setEnabled(false);
        startRemindBtn.setClickable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_remind_btn:
                if (checkoutGpsAndNetWork() && getPersimmions()){
                    isStartRemind = !isStartRemind;
                    updateStartBtnState();
                }
                break;
            case R.id.target_station:
                StartActivityUtils.startActivity(this,SearchActivity.class, REQUES_SEARCH_ACTIVITY_END_STATION_CODE,
                        CommonConst.ACTIVITY_SELECT_TYPE_KEY, REQUES_SEARCH_ACTIVITY_END_STATION_CODE);
                break;
            case R.id.select_city:
               // StartActivityUtils.startActivity(this,SearchActivity.class, REQUES_SEARCH_ACTIVITY_CITY_CODE,
               //         CommonConst.ACTIVITY_SELECT_TYPE_KEY, REQUES_SEARCH_ACTIVITY_CITY_CODE);
                break;
        }
    }

    private void updateStartBtnState(){
        if(!isStartRemind){
            mRemonderLocationService.setCancleReminder();
            startRemindBtn.setText(R.string.start_remind);
        }else{
            mRemonderLocationService.setStartReminder(mTargetStation);
            startRemindBtn.setText(R.string.stop_remind);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = "";
        switch (resultCode) {
            case REQUES_SEARCH_ACTIVITY_CITY_CODE:
                if(data != null) {
                    result = data.getStringExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY);
                    if(hasLocation && currentCity.endsWith(result)){
                        Toast.makeText(this,getString(R.string.select_city_not_match_location_city),Toast.LENGTH_LONG).show();
                    }else{
                        selectCity.setTitle(result);
                        setNewCity(result);
                    }
                }
                break;
            case REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                if(data != null) {
                    mTargetStation= data.getParcelableExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY);
                    if(mTargetStation != null) {
                        targetStationView.setText(mTargetStation.key);
                        startRemindBtn.setEnabled(true);
                        startRemindBtn.setClickable(true);
                        isStartRemind = false;
                    }
                }
                break;
        }
        Log.d(TAG,"onActivityResult result = "+result);
    }

    @Override
    public void loadFinish() {
       if(mDataManager.getCurrentCityNo() != null){
           Log.d(TAG,"loadFinish city "+mDataManager.getCurrentCityNo().getCityName());
       }
        Log.d(TAG,"loadFinish allstation size = "+ mDataManager.getAllstations().size());
    }

    @Override
    public void updataFinish() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        mDataManager.releaseResource();
        LocationService locationService = ((LocationApplication) getApplication()).locationService;
        locationService.getLocationClient().disableLocInForeground(true);
    }

    private ServiceConnection connection = new ServiceConnection() {
        /**
         * 服务解除绑定时候调用
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        /**
         * 绑定服务的时候调用
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdateBinder = (RemonderLocationService.UpdateBinder) service;
            mRemonderLocationService = mUpdateBinder.getService();
            if (mRemonderLocationService != null) {
                mRemonderLocationService.setArrivedCallback(MainActivity.this);
                if (mRemonderLocationService != null) {
                    mRemonderLocationService.startLocationService();
                }
                mRemonderLocationService.setLocationChangerListener(new LocationChangerListener() {
                    @Override
                    public void loactionStation(BDLocation location) {

                        if (location.getCity() != null) {
                            String tempCity = location.getCity().substring(0, location.getCity().length() - 1);
                            CommonFuction.writeSharedPreferences(MainActivity.this, CityInfo.LOCATIONNAME, tempCity);
                            if (!hasLocation && FileUtil.dbIsExist(MainActivity.this, (CityInfo) mDataManager.getCityInfoList().get(tempCity)) &&
                                    mDataManager.getCityInfoList() != null && mDataManager.getCityInfoList().get(tempCity) != null) {
                                setNewCity(tempCity);
                            }
                        }

                        //StationInfo nerstStationInfo = PathSerachUtil.getNerastNextStation(location, mDataManager.getLineInfoList());
                        if(location.getAddress() != null){
                            currentStationView.setText(location.getAddress().address);
                            Log.d(TAG,"loactionStation location = "+location+" location.getAddress().address = "+location.getAddress().address);

                        }
                        //for (LocationChangerListener locationChangerListener : locationChangerListenerList) {
                         //   locationChangerListener.loactionStation(location);
                       // }
                    }

                    @Override
                    public void stopRemind() {
                        //for (LocationChangerListener locationChangerListener : locationChangerListenerList) {
                       //     locationChangerListener.stopRemind();
                      //  }
                    }
                });
            }
        }
    };

    public void setNewCity(String city) {
        Log.d(TAG, "setNewCity tempCity = " + city);
        selectCity.setTitle(city);
        currentCity = city;
        CommonFuction.writeSharedPreferences(MainActivity.this, CityInfo.CITYNAME, currentCity);
        mDataManager.loadData(MainActivity.this);
        hasLocation = true;
    }

    @Override
    public void arriaved(BDLocation mlocation, float distance) {
        isStartRemind = false;
        updateStartBtnState();
        finish();
       // Toast.makeText(this,"已经到达目的地附件 "+mlocation.getAddress(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void errorHint(String error) {
        Toast.makeText(this,error,Toast.LENGTH_LONG).show();
    }
}
