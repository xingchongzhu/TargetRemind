package com.wtach.stationremind.database;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;


import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wtach.stationremind.listener.ImportDatabaseListener;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.FileUtil;
import com.wtach.stationremind.utils.IDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.ExitInfo;
import com.wtach.stationremind.model.item.bean.LineInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.listener.LoadDataListener;

public class DataManager implements ImportDatabaseListener {

    private static final String TAG = "DataManager";

    private List<LoadDataListener> mLoadDataListener = new ArrayList<>();
    private DataHelper mDataHelper;//数据库
    private Map<Integer, LineInfo> mLineInfoList;//地图线路
    private CityInfo currentCityNo = null;
    private Object mLock = new Object();
    private Map<String, StationInfo> allstations = new HashMap<>();
    private Map<String, CityInfo> cityInfoList;//所有城市信息

    private boolean isImportDatabaseFinish = false;

    GeoCoder mSearch;
    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            } else {
                Log.d(TAG, "longitude  " + result.getLocation().longitude + "," + result.getLocation().latitude + " getAddress = " + result.getAddress());
            }
            //获取地理编码结果
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            } else {
                Log.d(TAG, "22222  " + result.toString());
            }
        }
    };

    private static DataManager mDataManager;


    public DataManager(Context context) {

    }

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                mDataManager = new DataManager(context);
            }
        }
        if (mDataManager.mDataHelper == null) {
            mDataManager.mDataHelper = new DataHelper(context, mDataManager);
        }
        return mDataManager;
    }

    public void loadData(Context context) {
        if(cityInfoList != null){
            cityInfoList.clear();
        }
        if (mLineInfoList != null) {
            mLineInfoList.clear();
        }
        if (mLineInfoList != null)
            mLineInfoList.clear();
        new MyAsyncTask().execute(context);
        if (mSearch == null) {
            //mSearch = GeoCoder.newInstance();
            //mSearch.setOnGetGeoCodeResultListener(com.wtach.stationremind.listener);
        }
    }

    public void releaseResource() {
        mDataHelper.Close();
        mDataHelper = null;
        if(cityInfoList != null)
            cityInfoList.clear();
        if (allstations != null)
            allstations.clear();
        if (mLineInfoList != null)
            mLineInfoList.clear();
        if (mLoadDataListener != null)
            mLoadDataListener.clear();
        mDataManager = null;
        if (mSearch != null) {
            mSearch.destroy();
        }
    }

    public Map<String, CityInfo> getCityInfoList(){
        return cityInfoList;
    }

    public CityInfo getCurrentCityNo() {
        return currentCityNo;
    }

    public List<CityInfo> queryByCityName(String name) {
        return mDataHelper.QueryCityByCityNo(name);
    }

    public void addLoadDataListener(LoadDataListener loadDataListener) {
        mLoadDataListener.add(loadDataListener);
    }

    public void removeLoadDataListener(LoadDataListener loadDataListener) {
        mLoadDataListener.remove(loadDataListener);
    }

    public void notificationUpdata() {
        Log.d(TAG, "notificationUpdata");
        for (LoadDataListener loadDataListener : mLoadDataListener) {
            loadDataListener.loadFinish();
        }
    }

    public Map<Integer, LineInfo> getLineInfoList() {
        return mLineInfoList;
    }

    public DataHelper getDataHelper() {
        return mDataHelper;
    }

    @Override
    public void startImport() {

    }

    @Override
    public void finishImport() {
        isImportDatabaseFinish = true;
    }

    class MyAsyncTask extends AsyncTask<Context, Void, Map<Integer, LineInfo>> {

        //onPreExecute用于异步处理前的操作
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //在doInBackground方法中进行异步任务的处理.
        @Override
        protected Map<Integer, LineInfo> doInBackground(Context... params) {
            if (mDataHelper == null) {
                Log.e(TAG, "MyAsyncTask load data mDataHelper is null");
                return null;
            }
            cityInfoList = mDataHelper.getAllCityInfo();

            while (!isImportDatabaseFinish) {
                try {
                    Log.w(TAG, "MyAsyncTask wait database load ");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String shpno = CommonFuction.getSharedPreferencesValue((Context) params[0], CityInfo.CITYNAME);
            if (TextUtils.isEmpty(shpno)) {
                shpno = IDef.DEFAULTCITY;
            }

            List<CityInfo> cityList = mDataHelper.QueryCityByCityNo(shpno);

            if (cityList != null && cityList.size() > 0) {
                currentCityNo = cityList.get(0);
            }

            if (currentCityNo == null || !FileUtil.dbIsExist((Context) params[0], currentCityNo)) {
                Log.e(TAG, "MyAsyncTask city = " + shpno + " can't load the city");
                return null;
            }

            mDataHelper.setCityHelper((Context) params[0], currentCityNo.getPingying());
            Map<Integer, LineInfo> list = mDataHelper.getLineList(LineInfo.LINEID, "ASC");
            if (list != null) {
                for (Map.Entry<Integer, LineInfo> entry : list.entrySet()) {
                    entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey()));
                }
                mLineInfoList = list;
                setAllstations(mLineInfoList);
            }

            //getEmityString();
            //getAddr((Context) params[0],shpno);
            return list;
        }

        @Override
        protected void onPostExecute(Map<Integer, LineInfo> list) {
            super.onPostExecute(list);
            notificationUpdata();
        }
    }

    public void getAddr(Context context, String city) {
        List<StationInfo> lists = mDataHelper.QueryAllByStationLineNo();
        for (StationInfo stationInfo : lists) {
            mSearch.geocode(new GeoCodeOption()
                    .city(city)
                    .address(city + stationInfo.getCname() + "地铁站"));
            //FileUtil.getGeoPointBystr(context,city+stationInfo.getCname()+"地铁站");
        }
    }

    public void getEmityString() {
        Map<String, String> stringBuffer = new HashMap<>();
        List<StationInfo> lists = mDataHelper.QueryAllByStationLineNo();
        for (StationInfo stationInfo : lists) {
            List<ExitInfo> existInfoList = mDataManager.getDataHelper().QueryByExitInfoCname(stationInfo.getCname());
            if (existInfoList.size() <= 0) {
                stringBuffer.put(stationInfo.getCname(), stationInfo.getCname() + "  " + stationInfo.getLineid());
            }
        }

        StringBuffer str = new StringBuffer();
        for (Map.Entry<String, String> entry : stringBuffer.entrySet()) {
            str.append(entry.getValue() + " -> ");
        }
        Log.d("zxc002", str.toString());
        str.delete(0, str.length());
        stringBuffer.clear();
        lists.clear();
    }

    public Map<String, StationInfo> getAllstations() {
        return allstations;
    }

    private void setAllstations(Map<Integer, LineInfo> allLines) {
        allstations.clear();
        if (allLines != null) {
            for (Map.Entry<Integer, LineInfo> entry : allLines.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    allstations.put(stationInfo.getCname(), stationInfo);
                }
            }
        }
    }

}
