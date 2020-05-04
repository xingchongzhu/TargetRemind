package com.wtach.stationremind;

import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;


/**
 * Base activity class that changes the app window's color based on the current hour.
 */
public abstract class AlarmBaseActivity extends Activity {
    public static final TypeEvaluator<Integer> ARGB_EVALUATOR = new ArgbEvaluator();
    /** Sets the app window color on each frame of the {@link #mAppColorAnimator}. */
    private final AppColorAnimationListener mAppColorAnimationListener
            = new AppColorAnimationListener();

    /** The current animator that is changing the app window color or {@code null}. */
    private ValueAnimator mAppColorAnimator;

    /** Draws the app window's color. */
    private ColorDrawable mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final  int color = getResources().getColor(R.color.clock_gray);//ThemeUtils.resolveColor(this, android.R.attr.windowBackground);
        adjustAppColor(color, false /* animate */);
    }

    /**
     * 判断颜色是不是亮色
     *
     * @param color
     * @return
     * @from https://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
     */
    private boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ensure the app window color is up-to-date.
        final  int color = getResources().getColor(R.color.clock_gray);//ThemeUtils.resolveColor(this, android.R.attr.windowBackground);
        adjustAppColor(color, false /* animate */);
    }

    /**
     * Adjusts the current app window color of this activity; animates the change if desired.
     *
     * @param color   the ARGB value to set as the current app window color
     * @param animate {@code true} if the change should be animated
     */
    protected void adjustAppColor( int color, boolean animate) {
        // Create and install the drawable that defines the window color.
        if (mBackground == null) {
            mBackground = new ColorDrawable(color);
            getWindow().setBackgroundDrawable(mBackground);
        }

        // Cancel the current window color animation if one exists.
        if (mAppColorAnimator != null) {
            mAppColorAnimator.cancel();
        }

        final int currentColor = mBackground.getColor();
        if (currentColor != color) {
            if (animate) {
                mAppColorAnimator = ValueAnimator.ofObject(ARGB_EVALUATOR, currentColor, color)
                        .setDuration(3000L);
                mAppColorAnimator.addUpdateListener(mAppColorAnimationListener);
                mAppColorAnimator.addListener(mAppColorAnimationListener);
                mAppColorAnimator.start();
            } else {
                setAppColor(color);
            }
        }
    }

    private void setAppColor( int color) {
        mBackground.setColor(color);
    }

    /**
     * Sets the app window color to the current color produced by the animator.
     */
    private final class AppColorAnimationListener extends AnimatorListenerAdapter
            implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            final int color = (int) valueAnimator.getAnimatedValue();
            setAppColor(color);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mAppColorAnimator == animation) {
                mAppColorAnimator = null;
            }
        }
    }
}
