package com.wtach.stationremind.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.wtach.stationremind.R;
import com.wtach.stationremind.object.SelectResultInfo;

import java.util.List;

public class FavoriteManager {

    public static boolean saveShareList(Context context, String collectName, List<Object> collectList){
        if (TextUtils.isEmpty(collectName)) {
            Toast.makeText(context,context.getString(R.string.name_same_empty_hint),Toast.LENGTH_LONG).show();
            return false;
        }
        String string = (String) AppSharePreferenceMgr.get(context,IDef.FAVORITE_LIST_KEY,"");
        String[] splits = null;
        StringBuilder stringBuilder = new StringBuilder(string);
        if (!TextUtils.isEmpty(string)) {
            splits = string.split(IDef.TARGET_LIST_SPLIT);
           for(String str : splits){
               if(str.equals(collectName)){
                   Toast.makeText(context,context.getString(R.string.name_same_hnit),Toast.LENGTH_LONG).show();
                   return false;
               }
           }
        }
        stringBuilder.append(collectName);
        stringBuilder.append(IDef.TARGET_LIST_SPLIT);
        AppSharePreferenceMgr.put(context,IDef.FAVORITE_LIST_KEY,stringBuilder.toString());

        AppSharePreferenceMgr.putSerializableList(context,collectName,collectList);
        return true;
    }

    public static  List<SelectResultInfo> getSerializableList(Context context, String key){
        return AppSharePreferenceMgr.getSerializableList(context,key);
    }
}
