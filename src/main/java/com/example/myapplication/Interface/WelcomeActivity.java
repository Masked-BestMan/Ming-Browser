package com.example.myapplication.Interface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.myapplication.R;

/**
 * Created by Zbm阿铭 on 2017/5/5.
 */

public class WelcomeActivity extends Activity {
    private TextView textView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        textView=(TextView)findViewById(R.id.textView);
        textView.setTypeface (Typeface.createFromAsset (getAssets(),"fonts/FZYTK.TTF" ));
        PreferenceManager.setDefaultValues(this,R.xml.pref_settings,true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.welcome_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        textView.startAnimation(animation);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
