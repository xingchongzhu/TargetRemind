package com.wtach.stationremind.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.wtach.stationremind.SearchActivity;

import static com.wtach.stationremind.utils.CommonConst.REQUES_SEARCH_ACTIVITY_CITY_CODE;

public class StartActivityUtils {

    public static void startActivity(Activity context, Class targetClass, int requestCode, String extraKey, int extraValue) {
        Intent intent = new Intent(context, targetClass);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(extraKey, extraValue);
        context.startActivityForResult(intent, requestCode);
    }
}
