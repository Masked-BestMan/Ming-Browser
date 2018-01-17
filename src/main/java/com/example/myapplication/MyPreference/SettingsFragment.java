package com.example.myapplication.MyPreference;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2017/12/25.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        ((PreferenceHead)findPreference("configHead")).setOnBackButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment.this.getActivity().finish();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.WHITE);

        /*
        去除preference两边的空白
         */
        View listView=view.findViewById(android.R.id.list);
        listView.setPadding(0,listView.getPaddingTop(),0,listView.getPaddingBottom());
        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference!=null&&preference.getKey().equals("restore_default")){
            PreferenceManager.setDefaultValues(getActivity().getApplicationContext(),R.xml.pref_settings,true);
            getActivity().finish();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
