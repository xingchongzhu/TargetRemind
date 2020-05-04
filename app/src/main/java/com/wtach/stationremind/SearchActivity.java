package com.wtach.stationremind;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import com.heytap.wearable.support.recycler.widget.DividerItemDecoration;
import com.heytap.wearable.support.recycler.widget.LinearLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.heytap.wearable.support.widget.HeyBackTitleBar;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.search.CustomAdapter;
import com.wtach.stationremind.utils.CommonConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SearchActivity extends BaseActivity implements View.OnClickListener, OnRecyItemClickListener {
    private final String TAG = "SearchActivity";
    private HeyBackTitleBar back_titlebar;
    private RecyclerView mRecyclerView;
    private String resultKey;
    private CustomAdapter mCustomAdapter;
    private DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_layout);
        initView();
        initTitleBar();
        initData();
    }

    private void initView() {
        back_titlebar = findViewById(R.id.back_titlebar);
        back_titlebar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, this);
        mRecyclerView = findViewById(R.id.recyler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //添加自定义分割线
        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
        mRecyclerView.addItemDecoration(divider);
    }

    private void initTitleBar(){
        Intent intent = getIntent();
        int type = intent.getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY,CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        String title="";
        switch (type){
            case CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE:
                title = getString(R.string.seletc_target_station_title);
                break;
            case CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                title = getString(R.string.seletc_city_title);
                break;
        }
        getString(R.string.seletc_target_station_title);
        Log.d(TAG,"initTitleBar title ="+title);
        back_titlebar.setTitle(title);
    }

    private void initData(){
        Intent intent = getIntent();
        int type = intent.getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY,CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        List<Object> list = null;
        switch (type){
            case CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE:
                if(DataManager.getInstance(this).getCityInfoList() != null){
                    list = DataManager.getInstance(this).getCityInfoList().values().stream().collect(Collectors.toList());
                }
                break;
            case CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                if(DataManager.getInstance(this).getAllstations() != null){
                    list = DataManager.getInstance(this).getAllstations().values().stream().collect(Collectors.toList());
                }
                break;
        }
        initAdapter(list);
    }

    private void initAdapter(List<Object> list) {
        mCustomAdapter = new CustomAdapter(list);
        mCustomAdapter.setOnRecyItemClickListener(this);
        mRecyclerView.setAdapter(mCustomAdapter);
    }

    @Override
    public void finish() {
        Log.d(TAG,"finish resultKey = "+resultKey);
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_titlebar:
                    selectFinish();
                break;
        }
    }

    private void selectFinish() {
        int type = getIntent().getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY,CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        Intent intent = new Intent();
        intent.putExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY,resultKey);
        setResult(type,intent);
        finish();
    }


    @Override
    public void onItemClick(View view, int position) {
        Object object = mCustomAdapter.getList().get(position);
        if(object instanceof StationInfo){
            resultKey = ((StationInfo) object).cname;
        }else if(object instanceof CityInfo){
            resultKey = ((CityInfo) object).getCityName();
        }
        selectFinish();
        finish();
    }
}
