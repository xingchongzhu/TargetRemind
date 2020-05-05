package com.wtach.stationremind;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OfflineRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OnlineRecogParams;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.heytap.wearable.support.recycler.widget.DividerItemDecoration;
import com.heytap.wearable.support.recycler.widget.LinearLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.heytap.wearable.support.widget.HeyBackTitleBar;
import com.heytap.wearable.support.widget.HeyShapeButton;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.recognize.RecogizeManager;
import com.wtach.stationremind.recognize.RecognizerImp;
import com.wtach.stationremind.search.CustomAdapter;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.IDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SearchActivity extends BaseActivity implements View.OnClickListener, OnRecyItemClickListener, IStatus, RecognizerImp.HandleResultCallback {
    private final String TAG = "SearchActivity";
    private HeyBackTitleBar back_titlebar;
    private RecyclerView mRecyclerView;
    private HeyShapeButton recordBtn;
    private TextView txtResult;
    private TextView sugResult;

    private String resultKey;
    private CustomAdapter mCustomAdapter;
    private DataManager mDataManager;
    private RecognizerImp mRecognizerImp;
    /**
     * 控制UI按钮的状态
     */
    protected int status;

    //Sug检索
    private SuggestionSearch mSuggestionSearch;

    private SuggestionResult.SuggestionInfo mSuggestionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_layout);
        init();
    }

    private void init() {
        status = STATUS_NONE;
        mRecognizerImp = new RecognizerImp(this, this);
        SDKInitializer.initialize(getApplicationContext());
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);

        initView();
        initTitleBar();
        initData();
    }

    private void initView() {
        back_titlebar = findViewById(R.id.back_titlebar);
        sugResult = findViewById(R.id.sug_result);
        txtResult = findViewById(R.id.text_result);
        recordBtn = findViewById(R.id.start_record);
        back_titlebar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, this);
        mRecyclerView = findViewById(R.id.recyler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //添加自定义分割线
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.custom_divider));
        mRecyclerView.addItemDecoration(divider);

        txtResult.setOnClickListener(this);
        recordBtn.setOnClickListener(this);

        setVoiceIconDrawable(R.drawable.ic_voice_icon);
    }

    private void initTitleBar() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY, CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        String title = "";
        switch (type) {
            case CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                title = getString(R.string.seletc_target_station_title);
                //recordBtn.setVisibility(View.VISIBLE);
                txtResult.setVisibility(View.VISIBLE);
                break;
            case CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE:
                title = getString(R.string.seletc_city_title);
                sugResult.setText(R.string.city_list);
                //recordBtn.setVisibility(View.GONE);
                txtResult.setVisibility(View.GONE);
                break;
        }
        Log.d(TAG, "initTitleBar title =" + title);
        back_titlebar.setTitle(title);
    }

    private void initData() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY, CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        List<Object> list = null;
        switch (type) {
            case CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE:
                if (DataManager.getInstance(this).getCityInfoList() != null) {
                    list = DataManager.getInstance(this).getCityInfoList().values().stream().collect(Collectors.toList());
                }
                break;
            case CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                if (DataManager.getInstance(this).getAllstations() != null) {
                    //list = DataManager.getInstance(this).getAllstations().values().stream().collect(Collectors.toList());
                }
                break;
        }
        initAdapter(list);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecognizerImp.cancel();
    }

    private void initAdapter(List<Object> list) {
        mCustomAdapter = new CustomAdapter(list);
        mCustomAdapter.setOnRecyItemClickListener(this);
        mRecyclerView.setAdapter(mCustomAdapter);
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish resultKey = " + resultKey);
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_titlebar:
                selectFinish();
                break;
            case R.id.start_record:
            case R.id.text_result:
                if (checkoutGpsAndNetWork() && getPersimmions()) {
                    switch (status) {
                        case STATUS_NONE: // 初始状态
                            mRecognizerImp.start();
                            status = STATUS_WAITING_READY;
                            updateBtnTextByStatus();
                            //txtResult.setText("");
                            break;
                        case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                        case STATUS_READY: // 引擎准备完毕。
                        case STATUS_SPEAKING: // 用户开始讲话
                        case STATUS_FINISHED: // 一句话识别语音结束
                        case STATUS_RECOGNITION: // 识别中
                            mRecognizerImp.stop();
                            status = STATUS_STOPPED; // 引擎识别中
                            updateBtnTextByStatus();
                            break;
                        case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                        case STATUS_STOPPED: // 引擎识别中
                            mRecognizerImp.cancel();
                            status = STATUS_NONE; // 识别结束，回到初始状态
                            updateBtnTextByStatus();
                            break;
                        default:
                            break;
                    }
                }
        }
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    String result = msg.obj.toString();
                    /**
                     * 在您的项目中，keyword为随您的输入变化的值
                     */
                    String shpno = CommonFuction.getSharedPreferencesValue(this, CityInfo.CITYNAME);
                    if (TextUtils.isEmpty(shpno)) {
                        shpno = IDef.DEFAULTCITY;
                    }
                    mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(shpno).keyword(result));
                    txtResult.setText(shpno + " " + result.trim().replace("，", ""));
                }
                status = msg.what;
                updateBtnTextByStatus();
                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                updateBtnTextByStatus();
                break;
            default:
                break;

        }
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                //recordBtn.setText(getString(R.string.start_recognizeing));
                //recordBtn.setEnabled(true);
                txtResult.setClickable(true);
                setVoiceIconDrawable(R.drawable.ic_voice_icon);
                break;
            case STATUS_READY:
            case STATUS_WAITING_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                //recordBtn.setText(getString(R.string.stop_recognizeing));
                //recordBtn.setEnabled(false);
                txtResult.setText(getString(R.string.stop_recognizeing));
                setVoiceIconDrawable(R.drawable.ic_voice_listener_icon);
                txtResult.setClickable(false);
                break;
            case STATUS_LONG_SPEECH_FINISHED:
            case STATUS_STOPPED:
                //recordBtn.setText(getString(R.string.calcel_recognizeing));
                //recordBtn.setEnabled(true);
                txtResult.setClickable(true);
                setVoiceIconDrawable(R.drawable.ic_voice_icon);
                break;
            default:
                break;
        }
    }

    private void setVoiceIconDrawable(int resId) {
        Drawable drawable = getDrawable(resId);
        drawable.setBounds(0, 0, (int) getResources().getDimension(R.dimen.voice_icon_size),
                (int) getResources().getDimension(R.dimen.voice_icon_size));
        txtResult.setCompoundDrawables(null, null, drawable, null);
    }

    private void selectFinish() {
        int type = getIntent().getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY, CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        Intent intent = new Intent();
        intent.putExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY, mSuggestionInfo);
        setResult(type, intent);
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        Object object = mCustomAdapter.getList().get(position);
        if (object instanceof StationInfo) {
            resultKey = ((StationInfo) object).cname;
        } else if (object instanceof CityInfo) {
            resultKey = ((CityInfo) object).getCityName();
        } else if (object instanceof SuggestionResult.SuggestionInfo) {
            mSuggestionInfo = ((SuggestionResult.SuggestionInfo) object);
        }
        selectFinish();
        finish();
    }

    @Override
    protected void onDestroy() {
        // 基于DEMO的5.2 退出事件管理器
        mRecognizerImp.release();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            List list = suggestionResult.getAllSuggestions();
            mCustomAdapter.setData(list);
            //处理sug检索结果
            if (list == null && list.size() <= 0) {
                sugResult.setText(getString(R.string.no_sug_result));
            } else {
                sugResult.setText(getString(R.string.sug_result));
            }
            //Log.d(TAG,"onGetSuggestionResult suggestionResult = "+suggestionResult.getAllSuggestions()+" error = "+suggestionResult.error);
        }
    };

}
