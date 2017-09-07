package com.example.zxb.downpush;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.zxb.downpush.widget.TouchPullView;

public class MainActivity extends AppCompatActivity {
    private static final float TOUCH_MOVE_MAX_Y = 400;

    private float mTouchMoveStartY;

    private TouchPullView mTouchPullView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mTouchPullView = (TouchPullView) findViewById(R.id.touch_pull);

        findViewById(R.id.layout_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        mTouchMoveStartY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float y = event.getY();
                        if (y >= mTouchMoveStartY){
                            float moveSize = y - mTouchMoveStartY;
                            float progress = moveSize >= TOUCH_MOVE_MAX_Y ? 1:moveSize/TOUCH_MOVE_MAX_Y;
                            mTouchPullView.setProgress(progress);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        mTouchPullView.release();
                        break;
                    default:

                }

                return false;
            }
        });
    }
}
