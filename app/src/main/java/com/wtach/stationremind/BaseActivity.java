package com.wtach.stationremind;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.heytap.wearable.support.widget.HeyDialog;
import com.wtach.stationremind.recognize.RecognizerImp;
import com.wtach.stationremind.utils.AppSharePreferenceMgr;
import com.wtach.stationremind.utils.CommonConst;
import com.wtach.stationremind.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends Activity implements RecognizerImp.HandleResultCallback{

    private final String TAG = "BaseActivity";
    private final int SDK_PERMISSION_REQUEST = 127;
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();

    private HeyDialog netWorkDialog;
    private HeyDialog favoriteDialog;
    protected RecognizerImp mRecognizerImp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(23)
    public boolean getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionList.clear();//清空没有通过的权限

            //逐个判断你要的权限是否已经通过
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);//添加还未授予的权限
                }
            }

            //申请权限
            if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, permissions, SDK_PERMISSION_REQUEST);
                return false;
            } else {
                return true;
            }

        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasDeclaredSecretPermission()) {
            secretPermissionDeclareDialog();
        }else {
        }
        if(getPersimmions()){
            checkoutGpsAndNetWork();
        }
    }

    protected boolean checkoutGpsAndNetWork(){
        if (!NetWorkUtils.isGPSEnabled(this) || (!NetWorkUtils.isMobileConnected(this) && !NetWorkUtils.isNetworkConnected(this))) {
            showGpsDialog(this);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SDKInitializer.initialize(getApplicationContext());
        }
        checkoutGpsAndNetWork();
    }

    protected boolean hasDeclaredSecretPermission() {
        boolean value = (Boolean) AppSharePreferenceMgr.get(this, CommonConst.SECRET_PERMISSION_DECLARE_KEY, false);
        Log.d(TAG, "hasDeclaredSecretPermission = " + value);
        return value;
    }

    private void networkDeclare() {
        HeyDialog.HeyBuilder builder = new HeyDialog.HeyBuilder(this);
        builder.setContentView(R.layout.custom_layout).setNegativeButton(getResources().getString(R.string.cancel), null).
                setPositiveButton(getResources().getString(R.string.enture), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppSharePreferenceMgr.put(BaseActivity.this, CommonConst.SECRET_PERMISSION_DECLARE_KEY, true);
                        Toast.makeText(BaseActivity.this, "隐私申明",
                                Toast.LENGTH_LONG).show();
                    }
                });
        HeyDialog dialog = builder.create();
        dialog.show();
    }

    protected void secretPermissionDeclareDialog() {
        HeyDialog.HeyBuilder builder = new HeyDialog.HeyBuilder(getBaseContext());
        builder.setContentViewStyle(HeyDialog.STYLE_PROTOCOL)
                .setTitle(getResources().getString(R.string.secret_declare_title)).
                setMessage(getResources().getString(R.string.secret_declare_content))
                .setButtonOrientation(LinearLayout.HORIZONTAL).
                setSummary(getResources().getString(R.string.secret_declare_enture_hint)).
                setNegativeButton(getResources().getString(R.string.enture), null).
                setPositiveButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppSharePreferenceMgr.put(BaseActivity.this, CommonConst.SECRET_PERMISSION_DECLARE_KEY, true);
                        Toast.makeText(BaseActivity.this, "隐私申明",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
        HeyDialog dialog = builder.create();
    }

    @Override
    protected void onDestroy() {
        dismissNetWorkDialog();
        release();
        super.onDestroy();
    }

    private void dismissNetWorkDialog(){
        if(netWorkDialog != null && netWorkDialog.isShowing()){
            netWorkDialog.dismiss();
        }
    }

    private void showGpsDialog(final Context context){
        if(netWorkDialog != null && netWorkDialog.isShowing()){
            return;
        }
        HeyDialog.HeyBuilder builder = new HeyDialog.HeyBuilder(context);
        builder.setContentViewStyle(HeyDialog.STYLE_TITLE_CONTENT).setTitle(context.getString(R.string.open_gps_titls))
                .setMessage(context.getString(R.string.open_gps_content))
                .setPositiveButton(context.getString(R.string.enture), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetWorkUtils.openGPS(context);
                    }
                });
        netWorkDialog = builder.create();
        netWorkDialog.show();
    }

    protected void addFavorite(final NameCallBack nameCallBack){
        if(favoriteDialog != null && favoriteDialog.isShowing()){
            return;
        }
        final StringBuffer name = new StringBuffer();
        HeyDialog.HeyBuilder builder = new HeyDialog.HeyBuilder(this);
        builder.setContentViewStyle(HeyDialog.STYLE_CONTENT).setTitle(this.getString(R.string.name_favorite_title))
                .setMessage(getString(R.string.default_name))
                .setNegativeButton(getResources().getString(R.string.cancle), null)
                .setPositiveButton(getString(R.string.enture), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nameCallBack != null){
                            nameCallBack.nameComplete(name.toString());
                        }
                    }
                });
        favoriteDialog = builder.create();
        favoriteDialog.getMessage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.append("测试");
                favoriteDialog.getMessage().setText(name.toString());
                Toast.makeText(BaseActivity.this,name.toString(),Toast.LENGTH_LONG).show();
            }
        });
        favoriteDialog.show();
    }

    private void showNetWorkDialog(String title,String context){
        if(netWorkDialog != null){
            netWorkDialog.dismiss();
        }
        HeyDialog.HeyBuilder builder = new HeyDialog.HeyBuilder(this);
        builder.setContentViewStyle(HeyDialog.STYLE_TITLE_CONTENT).setTitle(title)
                .setMessage(context)
                .setPositiveButton("确定", null);
        netWorkDialog = builder.create();
        netWorkDialog.show();
    }

    public interface NameCallBack{
        void nameComplete(String name);
    }

    protected void release(){
    }
}
