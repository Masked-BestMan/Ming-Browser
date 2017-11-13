package com.example.myapplication.Components;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.myapplication.Toolkit.MyUtil;
import com.nineoldandroids.view.ViewHelper;


public class DragLayout extends FrameLayout {
    private Context context;
    private View mLeftContent;
    private View mMainContent;
    private int mWidth;
    private int mDragRange;
    private ViewDragHelper mDragHelper;
    private int mMainLeft;
    private int mHeight;
    private Status mStatus = Status.Close;
    private GestureDetectorCompat mDetectorCompat;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context=context;
        //ViewDragHelper.create(forParent, sensitivity, cb);
        //对应参数：父布局、敏感度、回调
        mDragHelper = ViewDragHelper.create(this, mCallBack);
        mDetectorCompat = new GestureDetectorCompat(getContext(),
                mGestureListener);

    }

    private boolean isDrag = false;

    public void setDrag(boolean isDrag) {
        this.isDrag = isDrag;
        if(isDrag){
            //这里有个Bug,当isDrag从false变为true是，mDragHelper的mCallBack在
            //首次滑动时不响应，再次滑动才响应，只好在此调用下，让mDragHelper恢复下状态
            mDragHelper.abort();
        }
    }

    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if(Math.abs(distanceX) > Math.abs(distanceY)&&distanceX<0&&isDrag&&mStatus==Status.Close){
                return true;
            }else if(Math.abs(distanceX) > Math.abs(distanceY)&&isDrag&&mStatus==Status.Open){
                return true;
            }else {
                return false;
            }
        }
    };

    ViewDragHelper.Callback mCallBack = new ViewDragHelper.Callback() {

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragHelper.captureChildView(mMainContent, pointerId);
        }
        // 决定child是否可被拖拽。返回true则进行拖拽。
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==mLeftContent;
        }

        // 当capturedChild被拖拽时
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        // 横向拖拽的范围，大于0时可拖拽，等于0无法拖拽
        // 此方法只用于计算如view释放速度，敏感度等
        // 实际拖拽范围由clampViewPositionHorizontal方法设置
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragRange;
        }

        // 此处设置view的拖拽范围。（实际移动还未发生）
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // 拖动前oldLeft + 变化量dx == left
            if (mMainLeft + dx < 0) {
                return 0;
            } else if (mMainLeft + dx > mDragRange) {
                return mDragRange;
            }
            return left;
        }

        // 决定了当View位置改变时，希望发生的其他事情。（此时移动已经发生）
        // 高频实时的调用，在这里设置左右面板的联动
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            //如果拖动的是主面板
            if (changedView == mMainContent) {
                mMainLeft = left;
            } else {
                mMainLeft += dx;
            }

            // 进行值的修正
            if (mMainLeft < 0) {
                mMainLeft = 0;
            } else if (mMainLeft > mDragRange) {
                mMainLeft = mDragRange;
            }
            // 如果拖拽的是左面板，强制在指定位置绘制Content
            if (changedView == mLeftContent) {
                layoutContent();
            }

            dispatchDragEvent(mMainLeft);

        }

        // View被释放时,侧滑打开或恢复
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel > 0) {
                open();
            } else if (xvel == 0 && mMainLeft > mDragRange * 0.5f) {
                open();
            } else {
                close();
            }

        }

        //当拖拽状态改变的时，IDLE/DRAGGING/SETTLING
        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

    };

    private void layoutContent() {
        mMainContent.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
        mLeftContent.layout(0, 0, mWidth, mHeight);
    }

    /**
     * 每次更新都会调用 根据当前执行的位置计算百分比percent
     */
    protected void dispatchDragEvent(int mainLeft) {
        float percent = mainLeft / (float) mDragRange;
        animViews(percent);

        if (mListener != null) {
            mListener.onDraging(percent);
        }

        Status lastStatus = mStatus;
        if (updateStatus(mainLeft) != lastStatus) {
            if (mListener == null) {
                return;
            }
            if (lastStatus == Status.Draging) {
                if (mStatus == Status.Close) {
                    mListener.onClose();
                } else if (mStatus == Status.Open) {
                    mListener.onOpen();
                }

            }
        }
    }

    public interface OnLayoutDragingListener {
        void onOpen();

        void onClose();

        void onDraging(float percent);
    }

    private OnLayoutDragingListener mListener;

    public void setOnLayoutDragingListener(OnLayoutDragingListener l) {
        mListener = l;
    }

    private Status updateStatus(int mainLeft) {
        if (mainLeft == 0) {
            mStatus = Status.Close;
        } else if (mainLeft == mDragRange) {
            mStatus = Status.Open;
        } else {
            mStatus = Status.Draging;
        }
        return mStatus;
    }

    public enum Status {
        Open, Close, Draging
    }

    public Status getStatus() {
        return mStatus;
    }


    private void animViews(float percent) {
        // 主面板：缩放
        float inverse = 1 - percent * 0.2f;
        ViewHelper.setScaleX(mMainContent, inverse);
        ViewHelper.setScaleY(mMainContent, inverse);

        // 左面板：缩放、平移、透明度
        ViewHelper.setScaleX(mLeftContent, 0.5f + 0.5f * percent);
        ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f * percent);
        ViewHelper.setTranslationX(mLeftContent, -mWidth / 2.0f + mWidth / 2.0f * percent);
        ViewHelper.setAlpha(mLeftContent, percent);
        // 背景：颜色渐变
        getBackground().setColorFilter(
                evaluate(percent, Color.BLACK, Color.TRANSPARENT),
                PorterDuff.Mode.SRC_OVER);
    }

    private int evaluate(float fraction, int startInt, int endInt) {

        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24)
                | ((startR + (int) (fraction * (endR - startR))) << 16)
                | ((startG + (int) (fraction * (endG - startG))) << 8)
                | ((startB + (int) (fraction * (endB - startB))));
    }
    float oldX,oldY,newX,newY;
    @Override
    public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
            return mDragHelper.shouldInterceptTouchEvent(ev) &&mDetectorCompat.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                oldX=event.getRawX();
                oldY=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                newX=event.getRawX();
                newY=event.getRawY();
                if(Math.abs(newX-oldX)-Math.abs(newY-oldY)< MyUtil.dip2px(context,10)){
                    return true;
                }
        }
        try {
            //将Touch事件传递给ViewDragHelper
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public void close() {
        mMainLeft = 0;
        // 执行动画，返回true代表有未完成的动画, 需要继续执行
        if (mDragHelper.smoothSlideViewTo(mMainContent, mMainLeft, 0)) {
            // 注意：参数传递根ViewGroup
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void open() {
        mMainLeft = mDragRange;
        if (mDragHelper.smoothSlideViewTo(mMainContent, mMainLeft, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        // 高频率调用，决定是否有下一个变动等待执行
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        mMainContent.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
        mLeftContent.layout(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //拿到宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        //设置拖动范围
        mDragRange = (int) (mWidth * 0.7f);
    }

    /**
     * 填充结束时获得两个子布局的引用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        // 必要的检验
        if (childCount < 2) {
            throw new IllegalStateException(
                    "You need two children in your content");
        }

        if (!(getChildAt(0) instanceof ViewGroup)
                || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException(
                    "Your children must be an instance of ViewGroup");
        }

        mLeftContent = getChildAt(0);
        mMainContent = getChildAt(1);
    }
}
