package com.wtach.stationremind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.wtach.stationremind.utils.NetWorkUtils;

public class NetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            boolean isConnected = NetWorkUtils.isNetworkConnected(context);
            System.out.println("网络状态：" + isConnected);
            if (isConnected) {
                if (onNetConnect != null) {
                    onNetConnect.onNetConnect();
                }
            } else {
                if (onNetConnect != null) {
                    onNetConnect.onNetDisConnect();
                }
            }
        }
    }


    public interface OnNetConnect {
        void onNetConnect();

        void onNetDisConnect();
    }

    private OnNetConnect onNetConnect;

    public void setOnNetConnect(OnNetConnect onNetConnect) {
        this.onNetConnect = onNetConnect;
    }

}