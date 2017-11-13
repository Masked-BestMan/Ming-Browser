package com.example.myapplication.Components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;


import com.example.myapplication.Assistance.MessageEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Zbm阿铭 on 2017/11/1.
 */

public class MyViewPager extends ViewPager implements OnGestureListener{
    private boolean isFullScreen=true;
    private OnLayoutClickListener lc;
    private GestureDetector gestureDetector;
    private Context context;
    private boolean canDelete=true;

    public MyViewPager(Context context) {
        this(context,null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
            gestureDetector=new GestureDetector(context,this);
        this.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //防止viewpager在滚动中item仍可以上下滑动
                if(state==SCROLL_STATE_IDLE){

                    canDelete=true;
                }else{
                    canDelete=false;
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isFullScreen){
            return true;
        }
        return false;
    }
    private FrameLayout frameLayout;
    protected float point_x, point_y; //手指按下的位置
    private int left, right, top, bottom;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            frameLayout= (FrameLayout) getChildAt(getCurrentItem());
            point_x = ev.getRawX();
            point_y = ev.getRawY();
            left = frameLayout.getLeft();
            right = frameLayout.getRight();
            top = frameLayout.getTop();
            bottom = frameLayout.getBottom();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE){

            float mov_x = ev.getRawX() - point_x;
            float mov_y = ev.getRawY() - point_y;
            if(Math.abs(mov_x) < Math.abs(mov_y)&&canDelete)
                frameLayout.layout(left, (int) mov_y, right, bottom + (int) mov_y);
        }
        if (ev.getAction() == MotionEvent.ACTION_UP){
            if(Math.abs(frameLayout.getTop())>frameLayout.getWidth()/2){
                EventBus.getDefault().post(new MessageEvent(frameLayout.getTop()));
            }else {
                frameLayout.layout(left,0,right,bottom);
            }
        }
        gestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }


    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public void setOnLayoutClickListener(OnLayoutClickListener lc){
        this.lc=lc;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        lc.onLayoutClick();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if(Math.abs(velocityY)>7000){
            EventBus.getDefault().post(new MessageEvent(frameLayout.getTop()));
            return true;
        }
        if(Math.abs(frameLayout.getTop())>frameLayout.getWidth()/2){
            EventBus.getDefault().post(new MessageEvent(frameLayout.getTop()));
        }else {
            frameLayout.layout(left,0,right,bottom);
        }
        return true;
    }
    public interface OnLayoutClickListener{
        void onLayoutClick();
    }

}
