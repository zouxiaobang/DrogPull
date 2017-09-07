package com.example.zxb.downpush.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.RotateAnimation;

import com.example.zxb.downpush.R;

/**
 * Created by zouxiaobang on 17-9-1.
 */

public class TouchPullView extends View {


    private Paint mCirclePaint;
    private int mCircleRadius = 40;
    private float mCirclePointX, mCirclePointY;
    private float mProgress;
    //可拖动的高度
    private int mDragHeight = 300;

    //目标宽度 -- 起始点
    private int mTargetWidth = 400;
    //贝塞尔曲线的路径和画笔
    private Path mPath = new Path();
    private Paint mPathPaint;
    //控制点的最终高度，决定控制点的y坐标
    private int mTargetGravityHeight = 20;
    //角度变换 0 ～ 120
    private int mTargetAngle = 110;

    private ValueAnimator mValueAnimator;

    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    private Interpolator mAngleInterpolator ;

    private Drawable mContent = null;
    private int mContentMargin = 0;

    private boolean isShowDraw = true;
    private boolean isUp = false;
    private Path mLinePath = new Path();
    private Paint mLinePaint = new Paint();

    /**
     * 释放动画
     */
    public void release(){
        if (mValueAnimator == null){
            ValueAnimator animator = ValueAnimator.ofFloat(mProgress, 0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float){
                        setProgress((Float) val);
                    }
                }
            });
            mValueAnimator = animator;
        } else {
            mValueAnimator.cancel();
            mValueAnimator.setFloatValues(mProgress, 0f);
        }
        mValueAnimator.start();

        isUp = true;

    }

    public TouchPullView(Context context) {
        super(context);
        init(null);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * 初始化
     */
    public void init(AttributeSet attrs) {

        final Context context = getContext();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TouchPullView, 0, 0);
        int color = ta.getColor(R.styleable.TouchPullView_pColor, getResources().getColor(R.color.colorAccent));
        mCircleRadius = (int) ta.getDimension(R.styleable.TouchPullView_pRadius, mCircleRadius);
        mDragHeight = (int) ta.getDimension(R.styleable.TouchPullView_pDragHeight, mDragHeight);
        mTargetAngle = ta.getInteger(R.styleable.TouchPullView_pTargetAngle, mTargetAngle);
        mTargetWidth = ta.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidget, mTargetWidth);
        mTargetGravityHeight = ta.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight, mTargetGravityHeight);
        mContent = ta.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mContentMargin = (int) ta.getDimension(R.styleable.TouchPullView_pContentDrawableMargin, mContentMargin);

        ta.recycle();


        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置抗锯齿
        p.setAntiAlias(true);
        //防止抖动
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        mCirclePaint = p;

        //设置抗锯齿
        p.setAntiAlias(true);
        //防止抖动
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        mPathPaint = p;

        mAngleInterpolator = PathInterpolatorCompat.create((mCircleRadius*2)/mDragHeight, 90/mTargetAngle);

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3);
        mLinePaint = linePaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //进行基础坐标的改变
        int count = canvas.save();
        float tranX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress))/2;
        canvas.translate(tranX, 0);


        //画贝塞尔曲线
        canvas.drawPath(mPath, mPathPaint);

        //圆
        canvas.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint);



        Drawable drawable = mContent;
        if (drawable != null && isShowDraw){
            canvas.save();
            canvas.clipRect(drawable.getBounds());
            drawable.draw(canvas);
            canvas.restore();
        }

        canvas.restoreToCount(count);
        if (isShowDraw)
            canvas.drawPath(mLinePath, mLinePaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int measureWidth, measureHeight;
        //最小的宽度和高度
        int iWidth = 2 * mCircleRadius + getPaddingLeft() + getPaddingRight();
        int iHeight = (int) (mDragHeight * mProgress + 0.5f + getPaddingTop() + getPaddingBottom());

        if (widthMode == MeasureSpec.EXACTLY) {
            measureWidth = width;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            measureWidth = Math.min(width, iWidth);
        } else {
            measureWidth = iWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = height;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            measureHeight = Math.min(height, iHeight);
        } else {
            measureHeight = iHeight;
        }

        //设置宽度和高度
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //当高度变化时进行界面更新
        updatePathLayout();
    }

    /**
     * 设置进度
     *
     * @param progress
     */
    public void setProgress(float progress) {
        Log.i("zouxiaobang", "progress == " + progress);

        mProgress = progress;
        if (mProgress == 1){
            isShowDraw = false;
        }
        if (mProgress == 0){
            isUp = false;
        }
        if (!isUp){
            isShowDraw = true;
        }

        if (isUp && (mProgress == 1)){
            Log.i("zouxiaobang", "this is refreshing .... ");
        }

        //请求重新进行测量
        requestLayout();
    }

    /**
     * 更新
     */
    private void updatePathLayout(){
        final float progress = mProgressInterpolator.getInterpolation(mProgress);

        //获取可绘制区域的高度和宽度
        final float w = getValueByLine(getWidth(), mTargetWidth, mProgress);
        final float h = getValueByLine(0, mDragHeight, mProgress);

        //圆的圆心和半径
        final float cPointX = w/2;
        final float cRadius = mCircleRadius;
        final float cPointY = h - cRadius;

        //控制点结束的Y值
        final float endControlY = mTargetGravityHeight;

        //更新圆的坐标
        mCirclePointX = cPointX;
        mCirclePointY = cPointY;

        //复位
        final Path path = mPath;
        path.reset();
        path.moveTo(0, 0);

        //左边部分的结束点和控制点
        float lEndPointX, lEndPointY;
        float lControlPointX, lControlPointY;

        //弧度的计算,获取当前弧度
        float angle = mAngleInterpolator.getInterpolation(progress) * mTargetAngle;
        double radian = Math.toRadians(angle);
        //结束点的计算
        float a = (float) (Math.sin(radian) * cRadius);
        float b = (float) (Math.cos(radian) * cRadius);
        lEndPointX = cPointX - a;
        lEndPointY = cPointY + b;
        //控制点的计算
        float c = (float) (Math.tan(radian));
        lControlPointY = getValueByLine(0, endControlY, progress);
        float tHeight = lEndPointY - lControlPointY;
        float tWidth = tHeight/c;
        lControlPointX = lEndPointX - tWidth;

        //贝塞尔曲线的路径
        path.quadTo(lControlPointX, lControlPointY, lEndPointX, lEndPointY);
        //链接到右边
        path.lineTo(cPointX + (cPointX- lEndPointX), lEndPointY);
        //画右边的贝塞尔曲线
        path.quadTo(cPointX + cPointX-lControlPointX, lControlPointY, w, 0);

        //更新内容Drawable
        updateContentLayout(cPointX, cPointY, cRadius);



        double radians = Math.toRadians(360 * mProgress);

        Path linePath = mLinePath;
        linePath.reset();
        linePath.moveTo(getWidth()/2, mCirclePointY);
        linePath.lineTo((float) (getWidth()/2 + Math.cos(radians)*mCircleRadius)
                , (float) (mCirclePointY + Math.sin(radians)*mCircleRadius));
    }

    private void updateContentLayout(float cx, float cy, float radius){
        Drawable drawable = mContent;
        if (drawable != null){
            int margin = mContentMargin;
            int l = (int) (cx - radius + margin);
            int r = (int) (cx + radius - margin);
            int t = (int) (cy - radius + margin);
            int b = (int) (cy + radius - margin);
            drawable.setBounds(l,t,r,b);
        }
    }

    /**
     * 获取起点当前值
     * @param start
     * @param end
     * @param progress
     * @return
     */
    private float getValueByLine(float start, float end, float progress){
        return start + (end - start) * progress;
    }
}
