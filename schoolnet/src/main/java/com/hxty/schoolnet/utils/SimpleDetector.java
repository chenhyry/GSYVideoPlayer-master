package com.hxty.schoolnet.utils;

import android.content.Context;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 手势监听
 */
public abstract class SimpleDetector extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private final GestureDetector mDetector;
    private final int mSlop;//slop晃荡的意思
    private boolean mIgnore;//是否忽略监听上下滚动
    private float mDownY;

    public abstract void onScrollDown();

    public abstract void onScrollUp();

    public SimpleDetector(Context context) {
        mDetector = new GestureDetector(context, this);
        mSlop = getSlop(context);
    }

    public boolean isIgnore() {
        return mIgnore;
    }

    public void setIgnore(boolean mIgnore) {
        this.mIgnore = mIgnore;
    }

    protected int getSlop(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            return ViewConfiguration.getTouchSlop() * 2;
        } else {
            return ViewConfiguration.get(context).getScaledPagingTouchSlop();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        mDownY = e.getY();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        if (mIgnore)
            return false;
        if (distanceY == 0) {
            mDownY = e2.getY();
        }

        float distance = mDownY - e2.getY();

        if (distance < -mSlop) {
            onScrollDown();
        } else if (distance > mSlop) {
            onScrollUp();
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        mDetector.onTouchEvent(event);
//        return false;
        return true;
    }

}