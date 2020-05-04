package com.wtach.stationremind.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import com.wtach.stationremind.AlarmActivity;
import com.wtach.stationremind.R;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.object.NotificationObject;

import java.util.Map;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.baidu.baidulocationdemo";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public static void sendHint(Context context, boolean isArrive, String title, String content, String change) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arrive", isArrive);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("change", change);
        context.startActivity(intent);
    }

    public static NotificationObject createNotificationObject(Context context, StationInfo currentStation, StationInfo nextStation) {
        if (currentStation == null) {
            return null;
        }
        String linename = "";
        String currentStationName = "";
        String direction = "";
        String nextStationName = "";
        String time = "2分钟";
        if (currentStation != null) {
            linename = DataManager.getInstance(context).getLineInfoList().get(currentStation.lineid).linename;
            currentStationName = context.getResources().getString(R.string.current_station) + currentStation.getCname();
            direction =  context.getResources().getString(R.string.direction);
        }
        if (nextStation != null) {
            nextStationName = context.getResources().getString(R.string.next_station) + nextStation.getCname();
        }
        NotificationObject mNotificationObject = new NotificationObject(linename, currentStationName, direction, nextStationName, time);
        return mNotificationObject;
    }

    public void createChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            // create android channel
            NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(androidChannel);
        }

    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        return new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH);// 设置该通知优先级
    }
}