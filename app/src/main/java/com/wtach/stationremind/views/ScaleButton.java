package com.wtach.stationremind.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.heytap.wearable.support.widget.HeyBackTitleBar;

import androidx.annotation.Nullable;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_UP;

public class ScaleButton extends View {
    public ScaleButton(Context context) {
        super(context);
    }

    public ScaleButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScaleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean onTouchEvent(MotionEvent var1) {
        if (var1.getAction() == MotionEvent.ACTION_DOWN) {
            touchDown(this);
        } else if (2 == var1.getAction()) {

        } else if (ACTION_UP == var1.getAction() || ACTION_CANCEL == var1.getAction()) {
            touchUp(this);
        }

        return super.onTouchEvent(var1);
    }

    public void touchDown(View view) {
        if (this.isClickable()) {
            AnimatorSet var10000 = new AnimatorSet();
            var10000.setDuration(66L);
            float[] var1;
            float[] var10003 = var1 = new float[2];
            var10003[0] = 1.0F;
            var10003[1] = 0.8F;
            ObjectAnimator var10002 = ObjectAnimator.ofFloat(view, "scaleX", var1);
            float[] var2;
            float[] var10005 = var2 = new float[2];
            var10005[0] = 1.0F;
            var10005[1] = 0.8F;
            ObjectAnimator var3 = ObjectAnimator.ofFloat(view, "scaleY", var2);
            int[] var4;
            int[] var8 = var4 = new int[2];
            var8[0] = 50;
            var8[1] = 250;
            ObjectAnimator var5 = ObjectAnimator.ofInt(view, "alpha", var4);
            var10000.play(var10002).with(var3).with(var5);
            var10000.start();
        }
    }

    public void touchUp(View view) {
        if (this.isClickable()) {
            AnimatorSet var10000 = new AnimatorSet();
            var10000.setDuration(333L);
            float[] var1;
            float[] var10003 = var1 = new float[2];
            var10003[0] = 0.8F;
            var10003[1] = 1.0F;
            ObjectAnimator var10002 = ObjectAnimator.ofFloat(view, "scaleX", var1);
            float[] var2;
            float[] var10005 = var2 = new float[2];
            var10005[0] = 0.8F;
            var10005[1] = 1.0F;
            ObjectAnimator var3 = ObjectAnimator.ofFloat(view, "scaleY", var2);
            int[] var4;
            int[] var8 = var4 = new int[2];
            var8[0] = 250;
            var8[1] = 50;
            ObjectAnimator var5 = ObjectAnimator.ofInt(view, "alpha", var4);
            var10000.play(var10002).with(var3).with(var5);
            var10000.start();
        }
    }


}
