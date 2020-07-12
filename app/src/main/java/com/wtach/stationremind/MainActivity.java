package com.wtach.stationremind;

import androidx.annotation.Nullable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.heytap.wearable.support.recycler.widget.GridLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.heytap.wearable.support.widget.HeyBackTitleBar;
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
import com.wtach.stationremind.utils.AnimUtil;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.FavoriteManager;
import com.wtach.stationremind.utils.FileUtil;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.utils.NetWorkUtils;
import com.wtach.stationremind.utils.StartActivityUtils;
import com.wtach.stationremind.views.DancingView;
import com.wtach.stationremind.views.RecyclerDialog;

import java.util.ArrayList;
import java.util.List;

import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE;
import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE;

public class MainActivity extends BaseActivity implements View.OnClickListener, LoadDataListener,
        RemonderLocationService.ArrivedCallback, OnRecyItemClickListener,Runnable ,CustomAdapter.AdapterChangeListener{
    private final String TAG = "MainActivity";

    private View startRemindBtn;
    private HeyBackTitleBar selectCity;
    private View collect;
    private TextView selectTargetHint;
    private View targetStationView;
    //private TextView currentStationView;
    private RecyclerView mTargetRecyclerView;
    private RecyclerView mFavoriteRecyler;
    private DancingView dancingView;
    private View serachLayoutManagerRoot;

    private DataManager mDataManager;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public boolean hasLocation = false;
    private String currentCity = IDef.DEFAULTCITY;

    private RecyclerDialog mRecyclerDialog;
    private CustomAdapter mCustomAdapter;
    private CustomAdapter mFavoriteCustomAdapter;
    private FavoriteInfo mFavoriteInfo;
    private Handler handler = new Handler();
    private AnimUtil mAnimUtil = new AnimUtil();
    private View emptyAddBtn;
    private View bottomView;
    private View favriteListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        emptyAddBtn = findViewById(R.id.empty_add_btn);
        emptyAddBtn.setClickable(true);
        emptyAddBtn.setOnClickListener(this);

        ((ViewStub)findViewById(R.id.main_layout)).inflate();
        initView();


        mAnimUtil.setAnimation(findViewById(R.id.start_anim_view), new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onAnimationComplete();
            }
        });
        //initData();
    }

    public void onAnimationComplete() {
        getWindow().getDecorView().setBackground(null);
        findViewById(R.id.start_anim_view).setVisibility(View.GONE);
        bindRemindService();
        loadHistoryTarget();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        bottomView = findViewById(R.id.bottom_btn_layout);
        startRemindBtn = findViewById(R.id.start_remind_btn);
        //currentStationView = findViewById(R.id.current_station);
        targetStationView = findViewById(R.id.add_target_btn);
        selectCity = findViewById(R.id.select_city);
        dancingView = findViewById(R.id.dancing_ball);

        collect = findViewById(R.id.collect);

        startRemindBtn.setOnClickListener(this);
        targetStationView.setOnClickListener(this);
        selectCity.setOnClickListener(this);
        collect.setOnClickListener(this);
        favriteListBtn = findViewById(R.id.favrite_btn);
        favriteListBtn.setOnClickListener(this);

        startRemindBtn.setEnabled(false);
        startRemindBtn.setClickable(false);
        initStatusBar();
        dancingView.setOnCenterClickListener(this);
    }

    private void initStatusBar(){
        selectCity.c.setTextSize(getResources().getDimension(R.dimen.main_clock_title_size));
        selectCity.a.setImageDrawable(getDrawable(R.drawable.ic_location));
        selectCity.d.setTextColor(getColor(R.color.blue));

        selectCity.d.setMarqueeRepeatLimit(Integer.MAX_VALUE);
        selectCity.d.setFocusable(true);
        selectCity.d.setMaxWidth(200);
        selectCity.d.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        selectCity.d.setFocusableInTouchMode(true);
        selectCity.d.setHorizontallyScrolling(true);

        selectCity.d.setTextSize(getResources().getDimension(R.dimen.main_title_size));
    }

    private void initRecycle() {
        if(mTargetRecyclerView == null){
            serachLayoutManagerRoot = ((ViewStub)findViewById(R.id.view_stub)).inflate();
            //serachLayoutManagerRoot =  ((ViewStub)findViewById(R.id.serach_layout_manager_root)).inflate();
        }else{
            return;
        }
        selectTargetHint = serachLayoutManagerRoot.findViewById(R.id.select_target_hint);
        mTargetRecyclerView = serachLayoutManagerRoot.findViewById(R.id.target_recyler);
        mFavoriteRecyler = serachLayoutManagerRoot.findViewById(R.id.favorite_recyler);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        mTargetRecyclerView.setLayoutManager(layoutManager);

        GridLayoutManager favoritelayoutManager = new GridLayoutManager(this,1);
        mFavoriteRecyler.setLayoutManager(favoritelayoutManager);
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
                initRecycle();
                if(list == null || list.size() <= 0){
                    startRemindBtn.setEnabled(false);
                    startRemindBtn.setClickable(false);
                }else{
                    startRemindBtn.setEnabled(true);
                    startRemindBtn.setClickable(true);
                }
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
            case R.id.dancing_ball:
                if (checkoutGpsAndNetWork() && getPersimmions()){
                    updateStartBtnState();
                }
                break;
            case R.id.add_target_btn:
            case R.id.empty_add_btn:
                StartActivityUtils.startActivity(MainActivity.this,SearchActivity.class, REQUES_SEARCH_ACTIVITY_END_STATION_CODE,
                        CommonConst.ACTIVITY_SELECT_TYPE_KEY, REQUES_SEARCH_ACTIVITY_END_STATION_CODE);
                break;
            case R.id.collect:
                showNameDialog();
                break;
            case R.id.favrite_btn:
                if(mFavoriteRecyler.getVisibility() == View.GONE) {
                    updateRecycleVisible(true);
                }else{
                    updateRecycleVisible(false);
                }
                break;
        }
    }

    private void showNameDialog() {
        if(mFavoriteInfo.getCurrentCollectInfo() == null) {
            mRecyclerDialog = new RecyclerDialog(this);
            mRecyclerDialog.showRecognizeDialog(mRecyclerDialog,new NameCallBack() {
                @Override
                public void nameComplete(String name) {
                    CollectInfo collectInfo = new CollectInfo(name,mCustomAdapter.getList());
                    saveFavorite(collectInfo);
                    updateCollectState(collectInfo);
                }

                @Override
                public void startRecoginze() {

                }
            });
        }else{
            removeCollectInfo(mFavoriteInfo.getCurrentCollectInfo());
        }
    }

    private void saveFavorite(CollectInfo collectInfo){
        FavoriteManager.saveShareList(this,collectInfo);
        getFavoriteList();
        final List list = mFavoriteInfo != null ? mFavoriteInfo.favoriteMap : null;
        mFavoriteCustomAdapter.setData(list);
    }

    public void removeCollectInfo(CollectInfo collectInfo){
        FavoriteManager.removeCollect(this,collectInfo.getName());
        mFavoriteInfo.removeFavorite(collectInfo);
        if(mFavoriteInfo.getCurrentCollectInfo() != null && collectInfo.getName().equals(mFavoriteInfo.getCurrentCollectInfo().getName())) {
            mFavoriteInfo.setCurrentCollectInfo(null);
        }
        updateCollectDrawable();
    }

    private void updateStartBtnState(){
        if(mRemonderLocationService != null) {
            if (!mRemonderLocationService.isReminder() && mCustomAdapter.getItemCount() > 0) {
                mRemonderLocationService.setStartReminder();
                //startRemindBtn.setText(R.string.stop_remind);
                if (mRemonderLocationService != null) {
                    mRemonderLocationService.addReminderList(mCustomAdapter.getList());
                }
                startRemindBtn.setBackgroundResource(R.drawable.ic_pause);
                dancingView.startAnimation();
                serachLayoutManagerRoot.setVisibility(View.GONE);
                bottomView.setVisibility(View.GONE);
                dancingView.addReminderList(mCustomAdapter.getList());
                dancingView.setClickable(true);
            } else {
                dancingView.stopAnimation();
                mRemonderLocationService.setCancleReminder();
                startRemindBtn.setBackgroundResource(R.drawable.ic_start);
                //startRemindBtn.setText(R.string.start_remind);
                serachLayoutManagerRoot.setVisibility(View.VISIBLE);
                bottomView.setVisibility(View.VISIBLE);
                dancingView.setClickable(false);
            }
        }else{
            Log.e(TAG,"updateStartBtnState not bind mRemonderLocationService");
        }
        updateCollectBtnVisiable();
    }

    private boolean collectIsVisible(){
        if(mTargetRecyclerView != null && mTargetRecyclerView.getVisibility() == View.VISIBLE && mCustomAdapter.getItemCount() > 0 &&
                !mRemonderLocationService.isReminder()){
            return true;
        }
        return false;
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
                            updateRecycleVisible(false);

                            if(mFavoriteInfo.getCurrentCollectInfo() != null) {
                                CollectInfo collectInfo = new CollectInfo(mFavoriteInfo.getCurrentCollectInfo().getName(),mCustomAdapter.getList());
                                saveFavorite(collectInfo);
                            }
                        }
                    }
                }
                break;
        }
        Log.d(TAG,"onActivityResult result = "+result);
    }

    private void updateEmptyBtn(){
        if(mCustomAdapter.getItemCount() > 0 || mFavoriteCustomAdapter.getItemCount() > 0) {
            bottomView.setVisibility(View.VISIBLE);
            emptyAddBtn.setVisibility(View.GONE);
            selectTargetHint.setClickable(false);
        }else{
            bottomView.setVisibility(View.GONE);
            emptyAddBtn.setVisibility(View.VISIBLE);
            selectTargetHint.setText(getString(R.string.add_target_hint));
            selectTargetHint.setClickable(true);
            selectTargetHint.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    private void updateRecycleVisible(boolean favoriteVisiable){
        if(favoriteVisiable) {
            mFavoriteRecyler.setVisibility(View.VISIBLE);
            mTargetRecyclerView.setVisibility(View.GONE);
            if(mFavoriteCustomAdapter.getItemCount() > 0){
                selectTargetHint.setGravity(Gravity.LEFT);
                selectTargetHint.setText(getString(R.string.favorite_list_title));
            }else{
                selectTargetHint.setGravity(Gravity.CENTER_HORIZONTAL);
                selectTargetHint.setText("");
                selectTargetHint.setHint(R.string.favorite_list_empty);
                selectTargetHint.setClickable(true);
            }
        }else{
            mFavoriteRecyler.setVisibility(View.GONE);
            mTargetRecyclerView.setVisibility(View.VISIBLE);
            updateTargetNumberHint(mCustomAdapter.getItemCount());
            selectTargetHint.setClickable(false);
        }
        updateCollectBtnVisiable();
        updateEmptyBtn();
    }

    private void updateCollectBtnVisiable(){
        if(collectIsVisible()) {
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
       if(mDataManager != null && mDataManager.getCurrentCityNo() != null){
           Log.d(TAG,"loadFinish city "+mDataManager.getCurrentCityNo().getCityName());
       }
        Log.d(TAG,"loadFinish allstation size = "+ mDataManager.getAllstations().size());
    }

    @Override
    public void updataFinish() {

    }

    @Override
    protected void onDestroy() {
        Log.d("zxc","onDestroy");
        super.onDestroy();
        if(connection != null) {
            unbindService(connection);
        }
        if(mDataManager != null) {
            mDataManager.releaseResource();
        }
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
                            /*if (!hasLocation && FileUtil.dbIsExist(MainActivity.this, (CityInfo) mDataManager.getCityInfoList().get(tempCity)) &&
                                    mDataManager.getCityInfoList() != null && mDataManager.getCityInfoList().get(tempCity) != null) {
                                setNewCity(tempCity);
                            }*/
                            if(!hasLocation){
                                setNewCity(tempCity);
                            }
                        }
                        //StationInfo nerstStationInfo = PathSerachUtil.getNerastNextStation(location, mDataManager.getLineInfoList());
                        if(location.getPoiList() != null && location.getPoiList().size() > 0){
                            selectCity.setTitle(location.getAddress().district+" "+location.getStreet()+location.getStreetNumber());
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
        //mDataManager.loadData(MainActivity.this);
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
                updateCollectState(collectInfo);
            }
            updateRecycleVisible(false);
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
            if(mFavoriteInfo.getCurrentCollectInfo() != null) {
                CollectInfo collectInfo = new CollectInfo(mFavoriteInfo.getCurrentCollectInfo().getName(),mCustomAdapter.getList());
                saveFavorite(collectInfo);
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
        dancingView.addReminderList(mCustomAdapter.getList());
    }

    @Override
    public void notifyChange(int num) {
        updateTargetNumberHint(num);
    }

    private void updateTargetNumberHint(int number){
        if(number > 0) {
            selectTargetHint.setGravity(Gravity.LEFT);
            selectTargetHint.setText(String.format(getString(R.string.select_target_numbuer),number));
            selectTargetHint.setClickable(false);
        }else{
            selectTargetHint.setText("");
            selectTargetHint.setHint(R.string.add_target_hint);
            selectTargetHint.setGravity(Gravity.CENTER_HORIZONTAL);
            selectTargetHint.setClickable(true);
        }
    }

    private void updateCollectState(CollectInfo collectInfo){
        if(mFavoriteInfo != null) {
            mFavoriteInfo.setCurrentCollectInfo(collectInfo);
        }
        updateCollectDrawable();
    }

    @Override
    public void handleMsg(Message msg) {

    }
}
