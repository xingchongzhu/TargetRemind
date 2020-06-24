package com.wtach.stationremind;

import androidx.annotation.Nullable;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.heytap.wearable.support.recycler.widget.GridLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.listener.LoadDataListener;
import com.wtach.stationremind.listener.LocationChangerListener;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.object.CollectInfo;
import com.wtach.stationremind.object.FavoriteInfo;
import com.wtach.stationremind.object.SelectResultInfo;
import com.wtach.stationremind.adapter.CustomAdapter;
import com.wtach.stationremind.service.LocationService;
import com.wtach.stationremind.service.RemonderLocationService;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.FavoriteManager;
import com.wtach.stationremind.utils.FileUtil;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.utils.NetWorkUtils;
import com.wtach.stationremind.utils.StartActivityUtils;
import com.wtach.stationremind.views.DancingView;

import java.util.ArrayList;
import java.util.List;

import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE;
import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE;

public class MainActivity extends BaseActivity implements View.OnClickListener, LoadDataListener,
        RemonderLocationService.ArrivedCallback, OnRecyItemClickListener,Runnable ,CustomAdapter.AdapterChangeListener {
    private final String TAG = "MainActivity";
    private View startRemindBtn;
    private TextView selectCity;
    private View collect;
    private TextView selectTargetHint;
    private View targetStationView;
    private TextView currentStationView;
    private RecyclerView mTargetRecyclerView;
    private RecyclerView mFavoriteRecyler;
    private DancingView dancingView;
    private View serachLayoutManagerRoot;

    private DataManager mDataManager;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public boolean hasLocation = false;
    private String currentCity = IDef.DEFAULTCITY;

    private CustomAdapter mCustomAdapter;
    private CustomAdapter mFavoriteCustomAdapter;
    private FavoriteInfo mFavoriteInfo;
    private Handler handler = new Handler();

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
        targetStationView = findViewById(R.id.add_target_btn);
        selectCity = findViewById(R.id.select_city);
        dancingView = findViewById(R.id.dancing_ball);
        serachLayoutManagerRoot = findViewById(R.id.serach_layout_manager_root);
        selectTargetHint = findViewById(R.id.select_target_hint);
        collect = findViewById(R.id.collect);

        startRemindBtn.setOnClickListener(this);
        targetStationView.setOnClickListener(this);
        selectCity.setOnClickListener(this);
        collect.setOnClickListener(this);
        findViewById(R.id.favrite_btn).setOnClickListener(this);

        initRecycle();
        startRemindBtn.setEnabled(false);
        startRemindBtn.setClickable(false);
    }

    private void initRecycle() {
        mTargetRecyclerView = findViewById(R.id.target_recyler);
        mFavoriteRecyler = findViewById(R.id.favorite_recyler);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        mTargetRecyclerView.setLayoutManager(layoutManager);

        GridLayoutManager favoritelayoutManager = new GridLayoutManager(this,1);
        mFavoriteRecyler.setLayoutManager(favoritelayoutManager);
        loadHistoryTarget();
        mCustomAdapter = initAdapter(null,mTargetRecyclerView);
        mFavoriteCustomAdapter = initAdapter(null,mFavoriteRecyler);
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
        getFavoriteList();
        final List list = mFavoriteInfo != null ? mFavoriteInfo.favoriteMap : null;//Utils.getHistoryTargets(this);
        handler.post(new Runnable() {
            @Override
            public void run() {
                mFavoriteCustomAdapter.setData(list);
                updateRecycleVisible(true);
            }
        });

    }

    public void getFavoriteList(){
        if(mFavoriteInfo != null){
            mFavoriteInfo.clear();
        }
        mFavoriteInfo = FavoriteManager.getSerializableList(this);
        if(mFavoriteInfo != null){
            Log.d(TAG,"getFavoriteList "+mFavoriteInfo.toString());
        }
    }

    private CustomAdapter initAdapter(List<Object> list,RecyclerView recyclerView) {
        CustomAdapter adapter = new CustomAdapter(list);
        adapter.setAdapterChangeListener(this);
        adapter.setOnRecyItemClickListener(this);
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_remind_btn:
                if (checkoutGpsAndNetWork() && getPersimmions()){
                    updateStartBtnState();
                }
                break;
            case R.id.add_target_btn:
                StartActivityUtils.startActivity(this,SearchActivity.class, REQUES_SEARCH_ACTIVITY_END_STATION_CODE,
                        CommonConst.ACTIVITY_SELECT_TYPE_KEY, REQUES_SEARCH_ACTIVITY_END_STATION_CODE);
                break;
            case R.id.collect:
                updateCollectClick();
                break;
            case R.id.favrite_btn:
                if(mFavoriteRecyler.getVisibility() == View.GONE) {
                    new Thread(this).start();
                }else{
                    updateRecycleVisible(false);
                }
                break;
        }
    }

    private void updateCollectClick() {
        if(mFavoriteInfo.getCurrentCollectInfo() == null){
            RecognizerObserver.getInstance(this).showRecognizeDialog(this,new NameCallBack() {
                @Override
                public void nameComplete(String name) {
                    updateCollectState(new CollectInfo(name,mCustomAdapter.getList()));
                }

                @Override
                public void startRecoginze() {

                }
            });
        }else{
            removeCollectInfo(mFavoriteInfo.getCurrentCollectInfo());
        }
    }

    public void removeCollectInfo(CollectInfo collectInfo){
        mFavoriteCustomAdapter.removeData(collectInfo);
        FavoriteManager.removeCollect(this,collectInfo.getName());
        mFavoriteInfo.removeFavorite(collectInfo);
        mFavoriteInfo.setCurrentCollectInfo(null);
    }

    private void updateStartBtnState(){
        if(mRemonderLocationService != null) {
            if (!mRemonderLocationService.isReminder() && mCustomAdapter.getItemCount() > 0) {
                mRemonderLocationService.setStartReminder();
                //startRemindBtn.setText(R.string.stop_remind);
                startRemindBtn.setBackgroundResource(R.drawable.ic_pause);
                dancingView.startAnimation();
                serachLayoutManagerRoot.setVisibility(View.GONE);
            } else {
                dancingView.stopAnimation();
                mRemonderLocationService.setCancleReminder();
                startRemindBtn.setBackgroundResource(R.drawable.ic_start);
                //startRemindBtn.setText(R.string.start_remind);
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
                            updateRecycleVisible(false);
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

    private void updateRecycleVisible(boolean favoriteVisiable){
        if(mFavoriteInfo != null) {
            mFavoriteInfo.setCurrentCollectInfo(null);
        }
        if(favoriteVisiable) {
            mFavoriteRecyler.setVisibility(View.VISIBLE);
            mTargetRecyclerView.setVisibility(View.GONE);
            if(mFavoriteCustomAdapter.getItemCount() > 0){
                selectTargetHint.setText(getString(R.string.select_target_list));
            }else{
                selectTargetHint.setText("");
            }
        }else{
            mFavoriteRecyler.setVisibility(View.GONE);
            mTargetRecyclerView.setVisibility(View.VISIBLE);
            updateTargetNumberHint(mCustomAdapter.getItemCount());
        }
        updateCollectBtnVisiable();
    }

    private void updateCollectBtnVisiable(){
        if(mTargetRecyclerView.getVisibility() == View.VISIBLE && mCustomAdapter.getItemCount() > 0){
            collect.setVisibility(View.VISIBLE);
        }else{
            collect.setVisibility(View.GONE);
        }
        updateCollectDrawable();
    }

    private void updateCollectDrawable(){
        if(mFavoriteInfo.getCurrentCollectInfo() != null) {
            collect.setBackgroundResource(R.drawable.ic_collected);
        }else{
            collect.setBackgroundResource(R.drawable.ic_collect);
        }
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
        RecognizerObserver.getInstance(null).release();
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
        if(mFavoriteRecyler.getVisibility() == View.VISIBLE){
            Object object = mFavoriteCustomAdapter.getDataIndex(position);
            if(object instanceof CollectInfo){
                CollectInfo collectInfo = (CollectInfo) object;
                List<Object> list = new ArrayList<>();
                for(SelectResultInfo selectResultInfo : collectInfo.getList()){
                    list.add(selectResultInfo);
                }
                mCustomAdapter.setData(list);
            }
            updateRecycleVisible(true);
        }
    }

    @Override
    public void onItemDelete(View view, int position) {
        if(mTargetRecyclerView.getVisibility() == View.VISIBLE) {
            Object object = mCustomAdapter.getList().get(position);
            if (object instanceof SelectResultInfo) {
                removeRemindList((SelectResultInfo) object);
                if (mRemonderLocationService != null) {
                    mRemonderLocationService.removetReminder((SelectResultInfo) object);
                }
            }
            if (mCustomAdapter.getItemCount() <= 0) {
                updateStartBtnState();
            }
        }else if(mFavoriteRecyler.getVisibility() == View.VISIBLE) {
            mFavoriteCustomAdapter.removeData(position);
            removeCollectInfo((CollectInfo) mFavoriteCustomAdapter.getDataIndex(position));
        }
    }

    private void removeRemindList(SelectResultInfo selectResultInfo){
        mCustomAdapter.removeData(selectResultInfo);
        if(mRemonderLocationService != null) {
            mRemonderLocationService.removetReminder(selectResultInfo);
        }
    }

    @Override
    public void notifyChange(int num) {
        updateTargetNumberHint(num);
    }

    private void updateTargetNumberHint(int number){
        if(number > 0) {
            selectTargetHint.setGravity(ViewGroup.TEXT_ALIGNMENT_TEXT_START);
            selectTargetHint.setText(String.format(getString(R.string.select_target_numbuer),number));
        }else{
            selectTargetHint.setText("");
            selectTargetHint.setGravity(ViewGroup.TEXT_ALIGNMENT_CENTER);
        }
    }

    private void updateCollectState(CollectInfo collectInfo){
        FavoriteManager.saveShareList(this,collectInfo);
        getFavoriteList();
        if(mFavoriteInfo != null) {
            mFavoriteInfo.setCurrentCollectInfo(collectInfo);
        }
        updateCollectDrawable();
    }

    @Override
    public void handleMsg(Message msg) {

    }
}
