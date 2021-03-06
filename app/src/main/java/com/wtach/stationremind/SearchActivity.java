package com.wtach.stationremind;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.mapapi.SDKInitializer;
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
import com.wtach.stationremind.object.SelectResultInfo;
import com.wtach.stationremind.adapter.CustomAdapter;
import com.wtach.stationremind.utils.AppSharePreferenceMgr;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.utils.Utils;
import com.wtach.stationremind.views.AudioWaveView;

import java.util.List;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;

public class SearchActivity extends BaseActivity implements View.OnClickListener, OnRecyItemClickListener,
        IStatus, TextWatcher {
    private final String TAG = "SearchActivity";
    private HeyBackTitleBar back_titlebar;
    private RecyclerView mRecyclerView;
    private HeyShapeButton recordBtn;
    private TextView txtResult;
    private TextView sugResult;
    private View resultLinear;

    private String resultKey;
    private CustomAdapter mCustomAdapter = new CustomAdapter(null);
    private AudioWaveView audioview;
    /**
     * 控制UI按钮的状态
     */
    protected int  status = STATUS_NONE;

    //Sug检索
    private SuggestionSearch mSuggestionSearch;

    private Object mSuggestionInfo;

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().requestFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_layout);
        RecognizerObserver.getInstance(this).addHandleResultCallback(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               initView();
            }
        },300);

        mHandler.postDelayed(initRunnable,800);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();
        //mHandler.postDelayed(initRunnable,1000);
    }

    Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            init();
        }
    };

    private void init() {
        SDKInitializer.initialize(getApplicationContext());
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
    }

    private void initView() {
        back_titlebar = findViewById(R.id.back_titlebar);
        sugResult = findViewById(R.id.sug_result_title);
        txtResult = findViewById(R.id.text_result);
        recordBtn = findViewById(R.id.start_record);
        audioview = findViewById(R.id.audioview);
        resultLinear = findViewById(R.id.result_linear);

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
        //mRecyclerView.addItemDecoration(divider);

        //txtResult.setOnClickListener(this);
        //recordBtn.setOnClickListener(this);

        //audioview.setVisibility(View.GONE);
        setVoiceIconDrawable(R.drawable.ic_voice_icon);
        //audioview.startAnimation();
        initTitleBar();
        initAdapter(null);
        txtResult.addTextChangedListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private void initTitleBar() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(CommonConst.ACTIVITY_SELECT_TYPE_KEY, CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE);
        String title = "";
        switch (type) {
            case CommonConst.REQUES_SEARCH_ACTIVITY_END_STATION_CODE:
                title = getString(R.string.seletc_target_station_title);
                //recordBtn.setVisibility(View.VISIBLE);
                resultLinear.setVisibility(View.VISIBLE);
                break;
            case CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE:
                title = getString(R.string.seletc_city_title);
                sugResult.setText(R.string.city_list);
                //recordBtn.setVisibility(View.GONE);
                resultLinear.setVisibility(View.GONE);
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
                list = Utils.getHistoryTargets(this);
                break;
        }
        final List templist = list;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCustomAdapter.setData(templist);
            }
        });

    }

    private void initAdapter(List<Object> list) {
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
                            RecognizerObserver.getInstance(this).startRecognizer();
                            status = STATUS_WAITING_READY;
                            updateBtnTextByStatus();
                            //txtResult.setText("");
                            break;
                        case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                        case STATUS_READY: // 引擎准备完毕。
                        case STATUS_SPEAKING: // 用户开始讲话
                        case STATUS_FINISHED: // 一句话识别语音结束
                        case STATUS_RECOGNITION: // 识别中
                            RecognizerObserver.getInstance(this).stopRecognizer();
                            status = STATUS_STOPPED; // 引擎识别中
                            updateBtnTextByStatus();
                            break;
                        case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                        case STATUS_STOPPED: // 引擎识别中
                            RecognizerObserver.getInstance(this).cancelRecognizer();
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
        //Log.d(TAG,"handleMsg msg = "+msg);
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    String result = msg.obj.toString();
                    handleResultMessage(result);
                    audioview.setVisibility(View.GONE);
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

    private void handleResultMessage(String result){
        /**
         * 在您的项目中，keyword为随您的输入变化的值
         */
        String shpno = CommonFuction.getSharedPreferencesValue(this, CityInfo.CITYNAME);
        if (TextUtils.isEmpty(shpno)) {
            shpno = IDef.DEFAULTCITY;
        }
        if(result.length() > 0) {
            mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(shpno).keyword(result));
            //txtResult.setText(shpno + " " + result.trim().replace("，", ""));
        }
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                //recordBtn.setText(getString(R.string.start_recognizeing));
                //recordBtn.setEnabled(true);
                txtResult.setClickable(true);
                audioview.setVisibility(View.GONE);
                setVoiceIconDrawable(R.drawable.ic_voice_icon);
                break;
            case STATUS_READY:
            case STATUS_WAITING_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                //recordBtn.setText(getString(R.string.stop_recognizeing));
                //recordBtn.setEnabled(false);
                audioview.setVisibility(View.VISIBLE);
                txtResult.setText(getString(R.string.stop_recognizeing));
                setVoiceIconDrawable(R.drawable.ic_voice_listener_icon);
                txtResult.setClickable(false);
                break;
            case STATUS_LONG_SPEECH_FINISHED:
            case STATUS_STOPPED:
                //recordBtn.setText(getString(R.string.calcel_recognizeing));
                //recordBtn.setEnabled(true);
                txtResult.setClickable(true);
                audioview.setVisibility(View.GONE);
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
        SelectResultInfo selectResultInfo = null;
        if(mSuggestionInfo instanceof SuggestionResult.SuggestionInfo){
            try {
                SuggestionResult.SuggestionInfo tempSuggestionInfo = (SuggestionResult.SuggestionInfo) mSuggestionInfo;
                selectResultInfo = new SelectResultInfo(tempSuggestionInfo.key, tempSuggestionInfo.city, tempSuggestionInfo.district,
                        tempSuggestionInfo.pt.latitude, tempSuggestionInfo.pt.longitude, tempSuggestionInfo.uid, tempSuggestionInfo.address);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(mSuggestionInfo instanceof SelectResultInfo){
            selectResultInfo = (SelectResultInfo) mSuggestionInfo;
        }

        saveShareList(selectResultInfo);
        intent.putExtra(CommonConst.ACTIVITY_RESULT_SELECT_KEY, selectResultInfo);
        setResult(type, intent);
        finish();
    }

    private void saveShareList(SelectResultInfo selectResultInfo){
        AppSharePreferenceMgr.putSerializableEntity(this,selectResultInfo.getKey(),selectResultInfo);

        String string = (String) AppSharePreferenceMgr.get(this,IDef.RECENT_TARGET_SELECT_LIST_KEY,"");
        String[] splits = null;
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(string)) {
            if(string.contains(selectResultInfo.getKey())){
                return;
            }
            splits = string.split(IDef.TARGET_LIST_SPLIT);
            if(splits.length < IDef.MAX_HISTORY_SIZE){
                stringBuilder.append(selectResultInfo.getKey());
                stringBuilder.append(IDef.TARGET_LIST_SPLIT);
                stringBuilder.append(string);
            }else{
                stringBuilder.append(selectResultInfo.getKey());
                stringBuilder.append(IDef.TARGET_LIST_SPLIT);
                for(int i = 0;i < IDef.MAX_HISTORY_SIZE -1;i ++){
                    stringBuilder.append(splits[i]);
                    stringBuilder.append(IDef.TARGET_LIST_SPLIT);
                }
                AppSharePreferenceMgr.remove(this,splits[IDef.MAX_HISTORY_SIZE -1]);
            }
        }else{
            stringBuilder.append(selectResultInfo.getKey());
            stringBuilder.append(IDef.TARGET_LIST_SPLIT);
        }

        AppSharePreferenceMgr.put(this,IDef.RECENT_TARGET_SELECT_LIST_KEY,stringBuilder.toString());
        AppSharePreferenceMgr.putSerializableEntity(this,selectResultInfo.getKey(),selectResultInfo);
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
        }else if(object instanceof SelectResultInfo){
            mSuggestionInfo = ((SelectResultInfo) object);
        }
        selectFinish();
        finish();
    }

    @Override
    public void onItemDelete(View view, int position) {
        Object object = mCustomAdapter.getList().get(position);
        if(object instanceof SelectResultInfo){
            AppSharePreferenceMgr.remove(this,((SelectResultInfo) object).getKey());
            mCustomAdapter.removeData(object);
        }
    }

    @Override
    protected void release(){
        mHandler.removeCallbacks(initRunnable);
        if(mSuggestionSearch != null) {
            mSuggestionSearch.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        RecognizerObserver.getInstance(this).removeHandleResultCallback(this);
        release();
        super.onDestroy();
    }

    OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            List list = suggestionResult.getAllSuggestions();
            mCustomAdapter.setData(list);
            //处理sug检索结果
            if (list == null || list.size() <= 0) {
                sugResult.setText(getString(R.string.no_sug_result));
            } else {
                sugResult.setText(getString(R.string.sug_result));
            }
            //Log.d(TAG,"onGetSuggestionResult suggestionResult = "+suggestionResult.getAllSuggestions()+" error = "+suggestionResult.error);
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String originText = s.toString();
        Log.d("zxc","afterTextChanged "+originText);
        handleResultMessage(originText.replace("。",""));
    }
}
