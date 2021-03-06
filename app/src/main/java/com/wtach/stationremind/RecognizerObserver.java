package com.wtach.stationremind;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.heytap.wearable.support.widget.HeyDialog;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.recognize.RecognizerImp;
import com.wtach.stationremind.utils.CommonFuction;
import com.wtach.stationremind.utils.IDef;
import com.wtach.stationremind.views.AudioWaveView;
import com.wtach.stationremind.views.CommonDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_FINISHED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_LONG_SPEECH_FINISHED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_NONE;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_READY;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_RECOGNITION;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_SPEAKING;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_STOPPED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_WAITING_READY;

public class RecognizerObserver implements RecognizerImp.HandleResultCallback {

    private final static String TAG = "RecognizerObserver";
    private final static String NO_SPEECH = "VAD detect no speech";
    private static RecognizerObserver mRecognizerObserver;
    protected RecognizerImp mRecognizerImp;
    private List<RecognizerImp.HandleResultCallback> handleResultCallbackList = new ArrayList<>();

    final StringBuffer text = new StringBuffer();
    private CommonDialog dialog;
    private Handler handler = new Handler();
    private String recoginzeing = "正在识别";
    private RecognizerObserver(Context context){
        if(context != null) {
            recoginzeing = context.getString(R.string.stop_recognizeing);
            //mRecognizerImp = new RecognizerImp(context, this);
        }
    }

    public static RecognizerObserver getInstance(Context context){
        if(mRecognizerObserver == null){
            synchronized (RecognizerObserver.class){
                mRecognizerObserver = new RecognizerObserver(context);
            }
        }
        return mRecognizerObserver;
    }

    public void showRecognizeDialog(final Context context, final BaseActivity.NameCallBack nameCallBack) {
        if(dialog != null){
            dialog.dismiss();
        }
        Log.d(TAG,"showRecognizeDialog");
        dialog = new CommonDialog(context);

        //dialog.setMessage(context.getString(R.string.default_name))
               // .setImageResId(R.mipmap.ic_launcher)
        dialog.setTitle(context.getString(R.string.start_record_recognize))
                .setSingle(false).setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
                String string = dialog.messageTv.getText().toString().replace("。","");
                if(nameCallBack != null && checkRecognizeContentValid(context,string)){
                    nameCallBack.nameComplete(string);
                    dialog.dismiss();
                }
            }

            @Override
            public void onNegtiveClick() {
                dialog.dismiss();
            }
        }).show();
        /*startRecognizer();
        dialog.messageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.messageTv.setVisibility(View.GONE);
                startRecognizer();
            }
        });*/
    }

    private boolean checkRecognizeContentValid(Context context, String string){
        if (NO_SPEECH.equals(string)) {
            Toast.makeText(context,context.getString(R.string.no_recognize_speech),Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(string)) {
            Toast.makeText(context,context.getString(R.string.name_same_empty_hint),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void addHandleResultCallback(RecognizerImp.HandleResultCallback callback) {
        if(!handleResultCallbackList.contains(callback)){
            handleResultCallbackList.add(callback);
        }
    }

    public void removeHandleResultCallback(RecognizerImp.HandleResultCallback callback) {
        handleResultCallbackList.remove(callback);
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    String result = msg.obj.toString().trim().replace("，", "");
                    if(dialog != null ){
                        dialog.messageTv.setVisibility(View.VISIBLE);
                        if(NO_SPEECH.equals(result)){
                            //startRecognizer();
                            dialog.messageTv.setHint("点击语音识别");
                        }else {
                            dialog.messageTv.setText(result);
                        }
                    }
                    text.append(result);
                }
                if(dialog != null) {
                    dialog.stopAnimation();
                }
                break;
            case STATUS_NONE:
            case STATUS_LONG_SPEECH_FINISHED:
            case STATUS_STOPPED:
                if(dialog != null) {
                    dialog.stopAnimation();
                }
                break;
            default:
                break;

        }
        notifyObserver(msg);
    }

    private void notifyObserver(Message msg){
        Iterator< RecognizerImp.HandleResultCallback> iterator = handleResultCallbackList.iterator();
        while (iterator.hasNext()){
            RecognizerImp.HandleResultCallback handleResultCallback = iterator.next();
            handleResultCallback.handleMsg(msg);
        }
    }

    public void startRecognizer(){
        if(text.length() > 0) {
            text.delete(0, text.length());
        }
        if(mRecognizerImp != null) {
            mRecognizerImp.start();
        }
        if(dialog != null){
            //dialog.startAnimation();
        }
    }

    public void stopRecognizer(){
        if(mRecognizerImp != null) {
            mRecognizerImp.stop();
        }
    }

    public void cancelRecognizer(){
        if(mRecognizerImp != null) {
            mRecognizerImp.cancel();
        }
    }

    public void release(){
        if(mRecognizerImp != null) {
            mRecognizerImp.release();
        }
    }
}
