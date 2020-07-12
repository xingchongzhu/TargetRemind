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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.wtach.stationremind.Interpolator.DancingInterpolator;
import com.wtach.stationremind.R;
import com.wtach.stationremind.object.SelectResultInfo;
import com.wtach.stationremind.utils.DisplayUtils;
import com.wtach.stationremind.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.Nullable;

import static android.animation.ValueAnimator.INFINITE;

public class DancingView extends View {

    public static final int DEFAULT_POINT_RADIUS = 10;
    public static final int DEFAULT_BALL_RADIUS = 18;
    public static final int DEFAULT_LINE_HEIGHT = 2;
    public static final int DEFAULT_LINE_COLOR = Color.parseColor("#de4848");
    public static final int DEFAULT_FREEDOWN_DURATION = 1000;//ms

    public int BALL_RADIUS = DEFAULT_BALL_RADIUS;//小球半径

    private Paint mPaint;
    private Paint mTextPaint;
    private int mBallColor;

    private int mLineHeight;

    private float freeBallDistance;

    private ValueAnimator mFreeDownController;//自由落体控制器
    private WaterRippleView waterRippleView;
    private Bitmap bonuceIccn;
    private float centerX = 0;
    private float centerY = 0;
    final float initScale = 1.0f;
    final float finalScale = 0.95f;
    private ValueAnimator animation = new ValueAnimator();
    private boolean isTouchDown = false;
    private View.OnClickListener mOnClickListener;
    private List<Object> LoacationList = new ArrayList<>();
    private Rect rect = new Rect();
    private float textX;
    private float textY;

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
        mTextPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(getContext().getResources().getDimension(R.dimen.hint_title_size));
        mTextPaint.setColor(getResources().getColor(R.color.autdio_wave_color));

        waterRippleView = new WaterRippleView(getContext());
        waterRippleView.setFillWaveSourceShapeRadius(DisplayUtils.dip2px(getContext(),25f));
        waterRippleView.setFillAllView(true);
        mPaint.setColor(mBallColor);
        //setRotationX(30);
        bonuceIccn = ImageUtils.drawableToBitamp(getContext().getDrawable(R.drawable.ic_bounce_location));
        bonuceIccn = Bitmap.createScaledBitmap(bonuceIccn, DEFAULT_BALL_RADIUS*2, DEFAULT_BALL_RADIUS*2, true);
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
        centerX = getWidth() / 2 - bonuceIccn.getWidth()/2;
        float radius = getWidth();
        waterRippleView.setWaveSize(getContext(),radius/4,radius/4,radius);
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

    private void initAnim(final View view){
        animation.setFloatValues(0f, 1f);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setDuration(100);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float r = (Float) animation.getAnimatedValue();
                float s = r * finalScale + (1 - r) * initScale;
                view.setScaleX(s);
                view.setScaleY(s);
            }
        });
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

    public void onClick(View v) {
        if(mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    public void setOnCenterClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    public void addReminderList(List targetStation){
        LoacationList = targetStation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 一条绳子用左右两部分的二阶贝塞尔曲线组成
        centerY = getHeight() / 2 - freeBallDistance - BALL_RADIUS*2;
        if(mFreeDownController != null && mFreeDownController.isRunning()){
            drawText(canvas);
            waterRippleView.onDraw(canvas);
            //canvas.drawBitmap(bonuceIccn,centerX, centerY,mPaint);
            //canvas.drawCircle(getWidth() / 2, cy, BALL_RADIUS, mPaint);
        }
    }

    private void drawText(Canvas canvas){
        Iterator iterator = LoacationList.iterator();
        textY = 35;
        int n = LoacationList.size();
        int count = n;
        while (iterator.hasNext()){
            SelectResultInfo selectResultInfo = (SelectResultInfo) iterator.next();
            String str = selectResultInfo.getKey();
            mTextPaint.getTextBounds(str, 0, str.length(), rect);
            mTextPaint.setAlpha(Math.max(255*count/n,50));
            textX = (getWidth()-rect.width())/2;
            textY += rect.height() * 1.5f;
            canvas.drawText(str,textX,textY,mTextPaint);
            count--;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            isTouchDown = true;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            if(isTouchDown && waterRippleView.radiusRect.contains(event.getX(),event.getY())){
                onClick(this);
            }
            isTouchDown = false;
        }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
            isTouchDown = false;
        }
        return super.onTouchEvent(event);
    }
}
