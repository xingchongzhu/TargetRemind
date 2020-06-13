/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wtach.stationremind.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;


/**
 * Manages playing the timer ringtone and vibrating the device.
 */
public class TimerKlaxon {
    private static String TAG = "TimerKlaxon";

    private static final long[] VIBRATE_PATTERN = {500, 500};
    private AsyncRingtonePlayer sAsyncRingtonePlayer;
    private Vibrator mVibrator;
    //private static TimerKlaxon mTimerKlaxon;

    public TimerKlaxon(Context context) {
        mVibrator = ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE));
    }

    /*public static TimerKlaxon getInstance(Context context){
        if(mTimerKlaxon == null){
            synchronized (TimerKlaxon.class){
                mTimerKlaxon = new TimerKlaxon(context);
            }
        }
        return mTimerKlaxon;
    }*/

    public void stop(Context context) {
        Log.i(TAG, "TimerKlaxon.stop()");
        getAsyncRingtonePlayer(context).stop();
        mVibrator.cancel();
    }

    public void start(Context context, boolean arrive) {
        // Make sure we are stopped before starting
        stop(context);
        Log.i(TAG, "TimerKlaxon.start()");

        final Uri uri = arrive ? AsyncRingtonePlayer.getArriveRingtoneUri(context) : AsyncRingtonePlayer.getChangeRingtoneUri(context);
        final long crescendoDuration = 0;
        getAsyncRingtonePlayer(context).play(uri, crescendoDuration);
        vibrateLOrLater(mVibrator);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void vibrateLOrLater(Vibrator vibrator) {
        vibrator.vibrate(VIBRATE_PATTERN, 0, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
    }


    private synchronized AsyncRingtonePlayer getAsyncRingtonePlayer(Context context) {
        if (sAsyncRingtonePlayer == null) {
            sAsyncRingtonePlayer = new AsyncRingtonePlayer(context.getApplicationContext());
        }

        return sAsyncRingtonePlayer;
    }
}