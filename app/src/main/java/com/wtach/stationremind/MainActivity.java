package com.wtach.stationremind;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.heytap.wearable.support.recycler.widget.DividerItemDecoration;
import com.heytap.wearable.support.recycler.widget.GridLayoutManager;
import com.heytap.wearable.support.recycler.widget.LinearLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.heytap.wearable.support.widget.HeyShapeButton;
import com.heytap.wearable.support.widget.HeySingleDefaultItem;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.listener.LoadDataListener;
import com.wtach.stationremind.listener.LocationChangerListener;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.object.SelectResultInfo;
import com.wtach.stationremind.recognize.RecogizeManager;
import com.wtach.stationremind.search.CustomAdapter;
import com.wtach.stationremind.service.LocationService;
import com.wtach.stationremind.service.RemonderLocationService;
import com.wtach.stationremind.utils.AppSharePreferenceMgr;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.DisplayUtils;
import com.wtach.stationremind.utils.FileUtil;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.utils.NetWorkUtils;
import com.wtach.stationremind.utils.PathSerachUtil;
import com.wtach.stationremind.utils.StartActivityUtils;
import com.wtach.stationremind.utils.Utils;
import com.wtach.stationremind.views.DancingView;

import java.util.List;

import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE;
import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE;

public class MainActivity extends BaseActivity implements View.OnClickListener, LoadDataListener,
        RemonderLocationService.ArrivedCallback, OnRecyItemClickListener,Runnable {
    private final String TAG = "MainActivity";
    private Button startRemindBtn;
    private TextView selectCity;
    private TextView targetStationView;
    private TextView currentStationView;
    private RecyclerView mRecyclerView;
    private DancingView dancingView;
    private View serachLayoutManagerRoot;

    private DataManager mDataManager;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public boolean hasLocation = false;
    private String currentCity = IDef.DEFAULTCITY;

    private CustomAdapter mCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        bindRemindService();
    }

    private void bindRemindService() {
        Intent intent = new Intent(this, RemonderLocationService.class);
        intent.setAction("com.android.remind.location.Service");
        bindService(intent, connection, BIND_AUTO_CREATE);
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
        dancingView = findViewById(R.id.dancing_ball);
        serachLayoutManagerRoot = findViewById(R.id.serach_layout_manager_root);

        startRemindBtn.setOnClickListener(this);
        targetStationView.setOnClickListener(this);
        selectCity.setOnClickListener(this);

        initRecycle();
        startRemindBtn.setEnabled(false);
        startRemindBtn.setClickable(false);
    }

    private void initRecycle() {
        mRecyclerView = findViewById(R.id.recyler);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(layoutManager);
        loadHistoryTarget();
    }

    private void loadHistoryTarget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getMainExecutor().execute(this);
        }else{
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        List list = null;//Utils.getHistoryTargets(this);
        initAdapter(list);
    }

    private void initAdapter(List<Object> list) {
        mCustomAdapter = new CustomAdapter(list);
        mCustomAdapter.setOnRecyItemClickListener(this);
        mRecyclerView.setAdapter(mCustomAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_remind_btn:
                if (checkoutGpsAndNetWork() && getPersimmions()){
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
        if(mRemonderLocationService != null) {
            if (!mRemonderLocationService.isReminder() && mCustomAdapter.getItemCount() > 0) {
                mRemonderLocationService.setStartReminder();
                startRemindBtn.setText(R.string.stop_remind);
                startRemindBtn.setBackgroundResource(R.drawable.ic_stop_bg);
                dancingView.startAnimation();
                serachLayoutManagerRoot.setVisibility(View.GONE);
            } else {
                dancingView.stopAnimation();
                mRemonderLocationService.setCancleReminder();
                startRemindBtn.setBackgroundResource(R.drawable.ic_start_bg);
                startRemindBtn.setText(R.string.start_remind);
                serachLayoutManagerRoot.setVisibility(View.VISIBLE);
            }
        }else{
            Log.e(TAG,"updateStartBtnState not bind mRemonderLocationService");
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
                        selectCity.setText(result);
                        setNewCity(result);
                    }
                }
                break;
            case REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                if(data != null) {
                    SelectResultInfo mTargetStation= (SelectResultInfo) data.getSerializableExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY);
                    if(mCustomAdapter.getItemCount() >= CommonConst.REMIND_MAX_COUNT){
                        Toast.makeText(this,getString(R.string.max_target_count_hint),Toast.LENGTH_LONG).show();
                    }else {
                        if (mTargetStation != null) {
                            //targetStationView.setText(mTargetStation.getKey());
                            int count = mCustomAdapter.getItemCount();
                            for(int i = 0;i < count; i++){
                                SelectResultInfo selectResultInfo = (SelectResultInfo) mCustomAdapter.getList().get(i);
                                if(selectResultInfo.getKey().equals(mTargetStation.getKey())){
                                    return;
                                }
                            }
                            mCustomAdapter.addData(mTargetStation);
                            startRemindBtn.setEnabled(true);
                            startRemindBtn.setClickable(true);
                            if (mRemonderLocationService != null) {
                                mRemonderLocationService.addReminder((SelectResultInfo) mTargetStation);
                            }
                        }
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
                        if(location.getPoiList() != null && location.getPoiList().size() > 0){
                            currentStationView.setText(location.getAddress().district+" "+location.getStreet()+location.getStreetNumber());
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
        selectCity.setText(city);
        currentCity = city;
        CommonFuction.writeSharedPreferences(MainActivity.this, CityInfo.CITYNAME, currentCity);
        mDataManager.loadData(MainActivity.this);
        hasLocation = true;
    }

    @Override
    public void arriaved(BDLocation mlocation, SelectResultInfo station) {
        removeRemindList(station);
        if(mCustomAdapter.getItemCount() <= 0) {
            updateStartBtnState();
            dancingView.stopAnimation();
            finish();
        }
       // Toast.makeText(this,"已经到达目的地附件 "+mlocation.getAddress(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void errorHint(String error) {
        //Toast.makeText(this,error,Toast.LENGTH_LONG).show();
        if (!NetWorkUtils.isGPSEnabled(getApplication()) || (!NetWorkUtils.isMobileConnected(getApplication()) && !NetWorkUtils.isNetworkConnected(getApplication()))) {
            if(mRemonderLocationService != null) {
                if (mRemonderLocationService.isReminder()) {
                    updateStartBtnState();
                }
            }
            checkoutGpsAndNetWork();
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onItemDelete(View view, int position) {
        Object object = mCustomAdapter.getList().get(position);
        if(object instanceof SelectResultInfo){
            removeRemindList((SelectResultInfo) object);
            if(mRemonderLocationService != null) {
                mRemonderLocationService.removetReminder((SelectResultInfo) object);
            }
        }
        if(mCustomAdapter.getItemCount() <= 0){
            updateStartBtnState();
        }
    }

    private void removeRemindList(SelectResultInfo selectResultInfo){
        mCustomAdapter.removeData(selectResultInfo);
        if(mRemonderLocationService != null) {
            mRemonderLocationService.removetReminder(selectResultInfo);
        }
    }
}
