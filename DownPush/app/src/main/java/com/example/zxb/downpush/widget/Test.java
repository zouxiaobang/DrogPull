package com.example.zxb.downpush.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zouxiaobang on 17-9-1.
 */

public class Test extends View {
    public Test(Context context) {
        super(context);
        init();
    }

    public Test(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Test(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private void init(){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(5);

        //一阶
        Path path = mPath;
        path.moveTo(100, 100);
        path.lineTo(300, 300);

        //二阶
//        path.quadTo(500 ,100 ,700, 300);
        path.rQuadTo(200, -200, 400, 300);  //相对的，建议使用这个

        //三阶
        path.moveTo(200, 800);
        path.cubicTo(300, 600, 400, 1000, 500, 800);
        path.rCubicTo(100, -200, 200, 200, 300, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }
}
