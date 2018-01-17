package com.example.myapplication.MyPreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2018/1/11.
 */

public class PreferenceHead extends Preference {
    private View.OnClickListener onBackButtonClickListener;
    public PreferenceHead(Context context,AttributeSet attrs){
        this(context,attrs,0);
    }
    public PreferenceHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.preference_head, parent, false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Button btBack = (Button) view.findViewById(R.id.config_back);
        btBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onBackButtonClickListener != null) {
                    onBackButtonClickListener.onClick(v);
                }
            }
        });
    }
    public void setOnBackButtonClickListener(View.OnClickListener onClickListener) {
        this.onBackButtonClickListener = onClickListener;
    }
}
