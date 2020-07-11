package com.wtach.stationremind.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.wtach.stationremind.R;
import com.wtach.stationremind.utils.DisplayUtils;
import com.wtach.stationremind.utils.ImageUtils;

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
    private final int START_WIDTH = 10;
    private final int END_WIDTH = 40;
    private int mWaveColor = 0;
    private float mViewCenterX = 0f;
    private float mViewCenterY = 0f;
    private int rippleColor = Color.BLUE;
    private int lineColor = Color.WHITE;
    private int buttonColor = Color.WHITE;
    //波动属性设置
    private Paint mWavePaint = new Paint();
    private Paint paint = new Paint();
    //中心点属性设置
    private Paint mWaveCenterShapePaint = new Paint();
    private boolean mFillAllView = false;
    private float mFillWaveSourceShapeRadius = 0f;

    private final float FPS = 1000 / 60;
    private Wave mLastRmoveWave = null;
   // private Bitmap pausIcon;
    private float buttonSize = 0;
    public RectF radiusRect = new RectF();
    public RectF line1 = new RectF();
    public RectF line2 = new RectF();
    private List<Wave> mWaves= new ArrayList();
    private int lineWidht = 0;

    public WaterRippleView(Context context){
        init(context);
    }

    public void init(Context context) {
        setWaveInfo(2f, 1f, 2f, 15f, rippleColor);
        mWaveIntervalSize = DisplayUtils.dip2px(context, 40f);
        mWidth = DisplayUtils.dip2px(context, 1.5f);
        //初始化波动最大半径
        mWaveEndWidth = DisplayUtils.dip2px(context, 100f);

        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);

        mWaveCenterShapePaint.setAntiAlias(true);
        mWaveCenterShapePaint.setStyle(Paint.Style.FILL);
        rippleColor = context.getColor(R.color.water_ripple_color);
        buttonColor = context.getColor(R.color.water_ripple_color);
    }

    public void setWaveSize(Context context, float startWidth, float waveSize, float endWidth){
        setWaveInfo(2f, 2f, startWidth, endWidth, rippleColor);
        mWaveIntervalSize = DisplayUtils.dip2px(context, waveSize);
       // pausIcon = Bitmap.createScaledBitmap(pausIcon, (int)startWidth, (int)startWidth, true);
        buttonSize = startWidth;
        radiusRect.left = (int) (mViewCenterX - buttonSize/2);
        radiusRect.top = (int) (mViewCenterY - buttonSize/2);
        radiusRect.right = (int) (mViewCenterX + buttonSize/2);
        radiusRect.bottom = (int) (mViewCenterY + buttonSize/2);
        lineWidht = (int) (buttonSize/8);
        line1.left = (int) (mViewCenterX - lineWidht*1.2f);
        line1.top = (int) (mViewCenterY - buttonSize/5);
        line1.right =  line1.left;
        line1.bottom = (int) (mViewCenterY + buttonSize/5);

        line2.left = (int) (mViewCenterX + lineWidht*1.2f);
        line2.top = (int) (mViewCenterY - buttonSize/5);
        line2.right = line2.left;
        line2.bottom =  (int) (mViewCenterY + buttonSize/5);
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
        mViewCenterY = height / 5 * 3;

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
        mWavePaint.setStyle(Paint.Style.STROKE);
        for (Wave w : mWaves) {
            mWavePaint.setColor(w.color);
            mWavePaint.setStrokeWidth(mWidth);
            mWavePaint.setAlpha((int) w.alpha);
            //canvas.drawCircle(mViewCenterX, mViewCenterY, w.radius, mWavePaint);
            RectF rectF = new RectF(mViewCenterX-w.radius/2, mViewCenterY-w.radius/2,
                    mViewCenterX+w.radius/2, mViewCenterY+w.radius/2);
            canvas.drawOval(rectF,mWavePaint);
        }
        drawBtn(canvas);
        //canvas.drawBitmap(pausIcon,mViewCenterX-pausIcon.getWidth()/2, mViewCenterY-pausIcon.getHeight()/2,paint);
    }

    private void drawBtn(Canvas canvas){
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(buttonColor);
        mWavePaint.setStrokeWidth(2);
        mWavePaint.setAlpha(255);
        canvas.drawOval(radiusRect,mWavePaint);

        mWavePaint.setColor(lineColor);
        mWavePaint.setStrokeCap(Paint.Cap.ROUND);
        mWavePaint.setStrokeWidth(lineWidht);
        canvas.drawLine(line1.left,line1.top,line1.right,line1.bottom,mWavePaint);
        canvas.drawLine(line2.left,line2.top,line2.right,line2.bottom,mWavePaint);
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
            w.radius = mWaveStartWidth - mStirStep;
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
