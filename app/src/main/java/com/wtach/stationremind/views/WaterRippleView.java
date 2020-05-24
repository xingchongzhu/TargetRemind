package com.wtach.stationremind.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.wtach.stationremind.R;
import com.wtach.stationremind.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class WaterRippleView{

    private float mMaxWaveAreaRadius = 0f;
    private float mWaveIntervalSize = 0f;//波距 = 0f
    private float mStirStep = 0f ;// 波移动的步幅 = 0f
    private int mWidth = 0;
    private float mWaveStartWidth = 0f;// px = 0f
    private float mWaveEndWidth = 0f;// px 最大半径，超过波消失 = 0f
    private int mWaveColor = 0;
    private float mViewCenterX = 0f;
    private float mViewCenterY = 0f;
    private int rippleColor = Color.BLUE;
    //波动属性设置
    private Paint mWavePaint = new Paint();
    //中心点属性设置
    private Paint mWaveCenterShapePaint = new Paint();
    private boolean mFillAllView = false;
    private float mFillWaveSourceShapeRadius = 0f;

    private final float FPS = 1000 / 60;
    private Wave mLastRmoveWave = null;

    private List<Wave> mWaves= new ArrayList();

    public WaterRippleView(Context context){
        init(context);
    }

    public void init(Context context) {
        setWaveInfo(2f, 1f, 2f, 15f, rippleColor);
        mWaveIntervalSize = DisplayUtils.dip2px(context, 20f);
        mWidth = DisplayUtils.dip2px(context, 2f);
        //初始化波动最大半径
        mWaveEndWidth = DisplayUtils.dip2px(context, 100f);

        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);

        mWaveCenterShapePaint.setAntiAlias(true);
        mWaveCenterShapePaint.setStyle(Paint.Style.FILL);
        rippleColor = context.getColor(R.color.water_ripple_color);
    }

    /**
     * 如果true会选择view的最大的对角线作为活动半径
     *
     */
    public void  setFillAllView(boolean fillAllView) {
        mFillAllView = fillAllView;
        resetWave();
    }

    private void setWaveInfo(float intervalSize, float stireStep, float startWidth, float endWidth, int color) {
        mWaveIntervalSize = intervalSize;
        mStirStep = stireStep;
        mWaveStartWidth = startWidth;
        mWaveEndWidth = endWidth;
        setWaveColor(color);
        resetWave();
    }

    public void resetWave() {
        mWaves.clear();
    }

    private void setWaveColor(int color) {
        mWaveColor = color;
        mWaveCenterShapePaint.setColor(mWaveColor);
    }

    /**
     * 填充波形起源的中心点
     *
     * @param radius 半径大小
     */
    public void  setFillWaveSourceShapeRadius(float radius) {
        mFillWaveSourceShapeRadius = radius;
    }


    protected void onLayout(boolean changed, int left, int top, int right, int bottom, int width, int height) {

        mViewCenterX = width / 2;
        mViewCenterY = height / 2;

        mWaveEndWidth = Math.min(mViewCenterX,mViewCenterY);
        float waveAreaRadius = mMaxWaveAreaRadius;
         if (mFillAllView) {
             waveAreaRadius = (float) Math.sqrt((mViewCenterX * mViewCenterX + mViewCenterY * mViewCenterY));
        } else {
             waveAreaRadius = Math.min(mViewCenterX, mViewCenterY);
        }
        if (mMaxWaveAreaRadius != waveAreaRadius) {
            mMaxWaveAreaRadius = waveAreaRadius;
            resetWave();
        }
    }

    public void onDraw(Canvas canvas) {
        stir();
        for (Wave w : mWaves) {
            mWavePaint.setColor(w.color);
            mWavePaint.setStrokeWidth(mWidth);
            mWavePaint.setAlpha((int) w.alpha);
            //canvas.drawCircle(mViewCenterX, mViewCenterY, w.radius, mWavePaint);
            RectF rectF = new RectF(mViewCenterX-w.radius/2, mViewCenterY-w.radius/2,
                    mViewCenterX+w.radius/2, mViewCenterY+w.radius/2);
            canvas.drawOval(rectF,mWavePaint);
        }
    }

    /**
     * 触发涌动传播
     */
    private void stir() {
        Wave nearestWave = mWaves.isEmpty()? null : mWaves.get(0);
        if (nearestWave == null || nearestWave.radius >= mWaveIntervalSize) {
            Wave w= null;
            if (mLastRmoveWave != null) {
                w = mLastRmoveWave;
                mLastRmoveWave = null;
                w.reset();
            } else {
                w = new Wave();
            }
            mWaves.add(0, w);
        }
        float waveWidthIncrease = mWaveEndWidth - mWaveStartWidth;
        int size = mWaves.size();
        for (int i = 0 ;i <size;i++) {
            Wave w = mWaves.get(i);
            float rP = w.radius / mMaxWaveAreaRadius;
            if (rP > 1f) {
                rP = 1f;
            }
            w.width = mWaveStartWidth + rP * waveWidthIncrease;
            w.radius += mStirStep;
            w.color = rippleColor;
            w.alpha = 255- 255 * ( w.radius /mWaveEndWidth);
        }
        Wave farthestWave = mWaves.get(size - 1);
        if (farthestWave != null && farthestWave.radius > mWaveEndWidth) {
            mWaves.remove(size - 1);
        }
    }

    /**
     * 波
     *
     */
    private  class Wave {
        float radius = 0f;
        float width = 0f;
        int color = 0;
        float alpha = 0;

        public Wave(){
            reset();
        }
        public void reset() {
            radius = 0f;
            width = mWaveStartWidth;
            color = mWaveColor;
        }

        @Override
         public String toString() {
            return ("Wave [radius=" + radius + ", width=" + width + ", color="
                    + color + "]");
        }
    }

}
