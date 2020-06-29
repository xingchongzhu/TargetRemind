package com.wtach.stationremind.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.wtach.stationremind.R;

public class AnimUtil {
    private final static int DURATION = 1500;
    private AnimatorSet animatorSet = new AnimatorSet();
    /**
     * 设置旋转的动画
     */
    public void setAnimation(final View view, AnimatorListenerAdapter animatorListenerAdapter) {
        ObjectAnimator  mObjectAnimator = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        // 用 AnimatorSet 的方法来让三个动画协作执行
        animatorSet.playTogether(mObjectAnimator);
        animatorSet.setDuration(DURATION);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.addListener(animatorListenerAdapter);
        startAnimation();
    }

    /**
     * 暂停旋转
     */
    private void stopAnimation() {
        animatorSet.cancel();
    }

    /**
     * 开始旋转
     */
    private void startAnimation() {
        animatorSet.start();
    }

}
