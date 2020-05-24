package com.wtach.stationremind.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.wtach.stationremind.R;
import com.wtach.stationremind.object.AudioInfo;

import java.util.Random;

import static android.animation.ValueAnimator.INFINITE;

public class AudioWaveView extends View {
    private Paint paint;
    private int viewWidth;
    private int viewHeight;
    /** 每个条的宽度 */
    private int rectWidth;
    /** 条数 */
    private final int columnCount = 5;
    /** 条间距 */
    private final int space = 10;
    private static final int DEFAULT_DURATION = 300;//ms

    private Random random;
    private RectF rect = new RectF();
    private AudioInfo[] heights = new AudioInfo[columnCount];
    private int centerY  = 0;
    private int radius = 8;
    private int step;
    private int maxHiehgt;
    private ValueAnimator mValueAnimator;
    public AudioWaveView(Context context) {
        this(context,null);
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void initController() {
        if(mValueAnimator != null && mValueAnimator.isRunning()){
            mValueAnimator.cancel();
        }
        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.setDuration(DEFAULT_DURATION);
        mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mValueAnimator.setRepeatCount(INFINITE);
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for(int i = 0 ; i < heights.length ; i ++) {
                    if (heights[i].isIncrease) {
                        heights[i].height += heights[i].step;
                    } else {
                        heights[i].height -= heights[i].step;
                    }
                    if(heights[i].height >= (viewHeight - maxHiehgt)){
                        heights[i].isIncrease = false;
                    }
                    if(heights[i].height <= 0){
                        heights[i].isIncrease = true;
                    }
                }
                postInvalidate();
            }
        });
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        centerY = viewHeight / 2;
        rectWidth = (viewWidth - space * (columnCount - 1)) / columnCount;
        resetParameter();
        initController();
        startAnimation();
    }

    private void resetParameter(){
        step = viewHeight / (columnCount+1);
        int middle = columnCount / 2;
        for(int i = 0 ; i < columnCount ; i++){
            heights[i] = new AudioInfo();
            if(i <= middle) {
                heights[i].height = step * (i+1);
            }else{
                heights[i].height = step * ((columnCount-i));
            }

            if(i % 2 == 0){
                //heights[i].isIncrease = true;
            }
            heights[i].isIncrease = true;
            heights[i].step = 1.5f;
            if(maxHiehgt < heights[i].height){
                maxHiehgt = heights[i].height;
            }
        }
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.autdio_wave_color,getContext().getTheme()));
        paint.setStyle(Paint.Style.FILL);
        random = new Random();
    }

    public void startAnimation(){
        if(mValueAnimator != null && !mValueAnimator.isRunning()){
            mValueAnimator.start();
        }
    }
    public void stopAnimation(){
        if(mValueAnimator != null && mValueAnimator.isRunning()){
            mValueAnimator.cancel();
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = rectWidth + space;

        //画每个条之前高度都重新随机生成
        for(int i = 0 ; i < heights.length ; i ++){
            int top = centerY - heights[i].height/2;
            int bottom = centerY + heights[i].height/2;

            rect.set(left * i, top, left * i + rectWidth, bottom);
            canvas.drawRoundRect(rect,radius,radius, paint);
        }

        postInvalidateDelayed(2);
    }

}