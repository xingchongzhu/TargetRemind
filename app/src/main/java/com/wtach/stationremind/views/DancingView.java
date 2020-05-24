package com.wtach.stationremind.views;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.wtach.stationremind.Interpolator.DancingInterpolator;
import com.wtach.stationremind.R;
import com.wtach.stationremind.utils.DisplayUtils;

import static android.animation.ValueAnimator.INFINITE;

public class DancingView extends View{

    public static final int DEFAULT_POINT_RADIUS = 10;
    public static final int DEFAULT_BALL_RADIUS = 13;
    public static final int DEFAULT_LINE_HEIGHT = 2;
    public static final int DEFAULT_LINE_COLOR = Color.parseColor("#de4848");
    public static final int DEFAULT_FREEDOWN_DURATION = 1000;//ms

    public int BALL_RADIUS = DEFAULT_BALL_RADIUS;//小球半径

    private Paint mPaint;
    private int mBallColor;

    private int mLineHeight;

    private float freeBallDistance;

    private ValueAnimator mFreeDownController;//自由落体控制器
    private WaterRippleView waterRippleView;
    public DancingView(Context context) {
        super(context);
        init(context, null);
    }

    public DancingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DancingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        waterRippleView = new WaterRippleView(getContext());
        waterRippleView.setFillWaveSourceShapeRadius(DisplayUtils.dip2px(getContext(),25f));
        waterRippleView.setFillAllView(true);
        mPaint.setColor(mBallColor);
        setRotationX(30);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.DancingView);
        mBallColor = typeArray.getColor(R.styleable.DancingView_ballColor, DEFAULT_LINE_COLOR);
        mLineHeight = typeArray.getDimensionPixelOffset(R.styleable.DancingView_lineHeight, DEFAULT_LINE_HEIGHT);
        typeArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        waterRippleView.onLayout(changed, left, top, right, bottom,getWidth(),getHeight());
    }

    private void initController() {
        final float upHeight = getHeight()/3;
        mFreeDownController = ValueAnimator.ofFloat(0, 1);
        mFreeDownController.setDuration(DEFAULT_FREEDOWN_DURATION);
        mFreeDownController.setRepeatMode(ValueAnimator.REVERSE);
        mFreeDownController.setRepeatCount(INFINITE);
        mFreeDownController.setInterpolator(new DecelerateInterpolator());
        mFreeDownController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //该公式解决上升减速 和 下降加速
                float t = (float) animation.getAnimatedValue();
                freeBallDistance = upHeight * t;
                postInvalidate();
            }
        });
        BALL_RADIUS = getWidth() / 50;
        waterRippleView.resetWave();
    }

    public void startAnimation(){
        initController();
        mFreeDownController.start();
    }

    public void stopAnimation(){
        if(mFreeDownController != null) {
            mFreeDownController.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 一条绳子用左右两部分的二阶贝塞尔曲线组成
        float cy = getHeight() / 2 - freeBallDistance - BALL_RADIUS*2;
        if(mFreeDownController != null && mFreeDownController.isRunning()){
            waterRippleView.onDraw(canvas);
            canvas.drawCircle(getWidth() / 2, cy, BALL_RADIUS, mPaint);
        }
    }
}
