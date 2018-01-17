package com.example.myapplication.Interface;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.myapplication.Components.SwipeBackLayout;
import com.example.myapplication.MyPreference.SettingsFragment;
import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2017/12/25.
 */

public class ConfigActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private SwipeBackLayout mSwipeBackLayout;
    private boolean isModified=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackLayout = new SwipeBackLayout(this);
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.add(android.R.id.content, new SettingsFragment());
        ft.commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackLayout.attachActivity(this);
    }

    @Override
    public void finish() {
        if (isModified)
            Toast.makeText(this,"修改设置成功",Toast.LENGTH_SHORT).show();
        super.finish();
        overridePendingTransition(0, R.anim.right_out);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        isModified=true;
    }
}
