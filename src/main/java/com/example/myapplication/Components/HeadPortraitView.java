package com.example.myapplication.Components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2017/10/30.
 */

public class HeadPortraitView extends View {
    private Context context;
    private int src,width,height,imageWidth,imageHeight;
    private Paint mPaint;
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;

    public HeadPortraitView(Context context) {
        super(context);
        this.context=context;
        initView(context,null);
    }

    public HeadPortraitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        initView(context,attrs);
    }

    public HeadPortraitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initView(context,attrs);
    }

    private void initView(Context context,AttributeSet attributeSet){
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.HeadPortraitView);
        src=ta.getResourceId(R.styleable.HeadPortraitView_src,0);
        ta.recycle();
        mBitmap=BitmapFactory.decodeResource(context.getResources(),src);
        imageWidth=mBitmap.getWidth();
        imageHeight=mBitmap.getHeight();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {

            width = widthSize;

        } else {

            width = imageWidth;

        }

        if (heightMode == MeasureSpec.EXACTLY) {

            height = heightSize;

        } else {

            height = imageHeight;

        }

        Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap,width,height,true);
        if(newBitmap!=mBitmap){
            mBitmap.recycle();
            mBitmap=newBitmap;
        }
        mBitmapShader=new BitmapShader(mBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        mPaint=new Paint();
        mPaint.setShader(mBitmapShader);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius=Math.min(width,height)/2.0f;
        canvas.drawCircle(width/2.0f,height/2.0f,radius,mPaint);
    }

    public void setBitmap(int resourceId) {
        Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),resourceId);
        handleBitmap(bitmap);
    }
    public void setBitmap(Bitmap bitmap){
        if(bitmap!=null)
            handleBitmap(bitmap);
    }
    private void handleBitmap(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,width,height,true);
        if(newBitmap!=mBitmap){
            mBitmap.recycle();
            mBitmap=newBitmap;
        }
        mBitmapShader=new BitmapShader(mBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        mPaint=new Paint();
        mPaint.setShader(mBitmapShader);
        invalidate();
    }
}
