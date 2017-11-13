package com.example.myapplication.Components;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.myapplication.Components.SwipeBackLayout;
import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2017/5/16.
 */

public class SwipeBackActivity extends AppCompatActivity {
    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackLayout = new SwipeBackLayout(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackLayout.attachActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.right_out);
    }
}
