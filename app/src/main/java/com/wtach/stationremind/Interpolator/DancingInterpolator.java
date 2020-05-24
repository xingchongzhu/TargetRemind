package com.wtach.stationremind.Interpolator;

import android.view.animation.Interpolator;

public class DancingInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return (float) (1 - Math.exp(-3 * input) * Math.cos(10 * input));
    }
}