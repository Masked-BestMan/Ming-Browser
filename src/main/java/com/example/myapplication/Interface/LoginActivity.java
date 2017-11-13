package com.example.myapplication.Interface;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Toolkit.HttpUtil;


/**
 * Created by Zbm阿铭 on 2017/5/10.
 */

public class LoginActivity extends Activity {
    private final int SUCCESS = 1;
    private final int FAIL = 0;
    private final int NOTNETWORK = -1;
    private Bitmap touxiang;
    private RelativeLayout background;
    private Button button, login, register;
    private EditText account, password;
    private InputMethodManager inputMethodManager;
    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    touxiang = HttpUtil.getD();
                    Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                    break;
                case FAIL:
                    Toast.makeText(LoginActivity.this, "登陆失败！", Toast.LENGTH_SHORT).show();
                    break;
                case NOTNETWORK:
                    Toast.makeText(LoginActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        background = (RelativeLayout) findViewById(R.id.anim_login_layout);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.in_from_up);
        background.startAnimation(animation);
        button = (Button) findViewById(R.id.back_login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("touxiang", touxiang);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.checkLogin(account.getText().toString(), password.getText().toString(), new HttpUtil.HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        if (response.equals("1"))
                            mHandler.sendEmptyMessage(SUCCESS);
                        else if (response.equals("0")) {
                            mHandler.sendEmptyMessage(FAIL);
                        } else {
                            mHandler.sendEmptyMessage(NOTNETWORK);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        mHandler.sendEmptyMessage(NOTNETWORK);
                    }
                });

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        Log.d("gg","调用");
        Intent intent = new Intent();
        intent.putExtra("touxiang", touxiang);
        setResult(RESULT_OK, intent);
        finish();
        //super.onBackPressed();
    }
}