package com.wtach.stationremind.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.wtach.stationremind.R;
import com.wtach.stationremind.object.CollectInfo;
import com.wtach.stationremind.object.FavoriteInfo;
import com.wtach.stationremind.object.SelectResultInfo;

import java.util.List;

public class FavoriteManager {

    public static boolean saveShareList(Context context, CollectInfo collectInfo){
        if (TextUtils.isEmpty(collectInfo.getName()) && collectInfo.getList().size() > 0) {
            Toast.makeText(context,context.getString(R.string.name_same_empty_hint),Toast.LENGTH_LONG).show();
            return false;
        }
        String string = (String) AppSharePreferenceMgr.get(context,IDef.FAVORITE_LIST_NAME_KEY,"");
        String[] splits = null;
        StringBuilder stringBuilder = new StringBuilder(string);
        if (!TextUtils.isEmpty(string)) {
            splits = string.split(IDef.TARGET_LIST_SPLIT);
            for(String str : splits){
                if(str.equals(collectInfo.getName())){
                    removeCollect(context,collectInfo.getName());
                    //Toast.makeText(context,context.getString(R.string.name_same_hnit),Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        stringBuilder.append(collectInfo.getName());
        stringBuilder.append(IDef.TARGET_LIST_SPLIT);
        AppSharePreferenceMgr.put(context,IDef.FAVORITE_LIST_NAME_KEY,stringBuilder.toString());

        AppSharePreferenceMgr.putSerializableList(context,collectInfo);
        return true;
    }

    public static void removeCollect(Context context,String collectName){
        String string = (String) AppSharePreferenceMgr.get(context,IDef.FAVORITE_LIST_NAME_KEY,"");
        String[] splits = null;
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(string)) {
            splits = string.split(IDef.TARGET_LIST_SPLIT);
            for(String str : splits){
                if(!str.equals(collectName)){
                    stringBuilder.append(str);
                    stringBuilder.append(IDef.TARGET_LIST_SPLIT);
                }
            }
            AppSharePreferenceMgr.put(context,IDef.FAVORITE_LIST_NAME_KEY,stringBuilder.toString());
            AppSharePreferenceMgr.remove(context,collectName);
        }
    }

    public static FavoriteInfo getSerializableList(Context context){
        String string = (String) AppSharePreferenceMgr.get(context,IDef.FAVORITE_LIST_NAME_KEY,"");
        String[] splits = null;
        FavoriteInfo mFavoriteInfo = new FavoriteInfo();
        if (!TextUtils.isEmpty(string)) {
            splits = string.split(IDef.TARGET_LIST_SPLIT);
            for(String key : splits){
                List<SelectResultInfo> list = getSerializable(context,key);
                CollectInfo collectInfo = new CollectInfo(key,list);
                mFavoriteInfo.addFavorite(collectInfo);
            }
        }
        return mFavoriteInfo;
    }

    public static  List<SelectResultInfo> getSerializable(Context context, String key){
        return AppSharePreferenceMgr.getSerializableList(context,key);
    }
}
