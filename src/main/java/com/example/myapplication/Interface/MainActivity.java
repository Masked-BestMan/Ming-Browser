package com.example.myapplication.Interface;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Components.DragLayout;
import com.example.myapplication.Components.HeadPortraitView;
import com.example.myapplication.Components.MainContentLayout;
import com.example.myapplication.Assistance.MessageEvent;
import com.example.myapplication.Components.MyViewPager;
import com.example.myapplication.R;
import com.example.myapplication.Toolkit.SQLiteHelper;
import com.example.myapplication.Assistance.WeatherService;
import com.example.myapplication.Toolkit.WebPage;
import com.example.myapplication.Assistance.WebPageAdapter;
import com.example.myapplication.Components.WebViewFragment;
import com.nineoldandroids.view.ViewHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    boolean first=true;  //有两种含义：第一次运行app时或标签页最后一页被删后需要重新定位当前webview对象
    private int login_flag=0;       //为1表示准备启动登陆界面
    private boolean isExit=false;    //是否在侧滑窗口关闭时退出程序
    private long mExitTime;    //按下返回键退出时的时间
    private ProgressBar progressBar;
    private WebView webView;
    private MyViewPager mViewPager;
    private WebPageAdapter webpageAdapter;
    private DragLayout mDragLayout;
    private TextView now_temperature,describe,city;
    private View toolbarBackground,webPageControlBackground,title_bar;
    private Button titleLeftButton,webBack,addWebPage,next,exit,history,webStopLoading,multiwindow,webRefresh;
    private SQLiteOpenHelper mOpenHelper;
    private InputMethodManager mInputMethodManager;
    private HeadPortraitView headPortrait;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpenHelper=new SQLiteHelper(this,"historyDB",null,1);
        mInputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_main);
        initView();

        startService(new Intent(MainActivity.this,WeatherService.class));

        //网络状态变化广播监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChange,mFilter);

        //天气结果广播监听
        IntentFilter mFilter2 =new IntentFilter();
        mFilter2.addAction("weather_refresh");
        registerReceiver(refresh,mFilter2);
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        now_temperature.setText(preferences.getString("wendu","")+"°");
        describe.setText(preferences.getString("ganmao","无天气信息"));
        city.setText(preferences.getString("cityName"," "));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebPage.webpagelist.clear();
        WebPage.webViews.clear();
        WebPage.frameLayouts.clear();
        unregisterReceiver(networkChange);
        unregisterReceiver(refresh);
    }

    public void onBackPressed() {

        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出浏览器", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_left_button:
                if(mInputMethodManager.isActive()){
                    mInputMethodManager.hideSoftInputFromWindow(webView.getWindowToken(),0);
                }
                mDragLayout.open();
                break;

            case R.id.web_history:
                startActivityForResult(new Intent(this, HistoryActivity.class), 1);
                overridePendingTransition(R.anim.left_in,0);
                break;

            case R.id.web_refresh:
                webView.reload();
                break;
            case R.id.web_stopLoading:
                webView.stopLoading();
                break;
            case R.id.web_back:
                webView.goBack();
                break;

            case R.id.web_next:
                webView.goForward();
                break;
           case R.id.multi_window:
                ZoomChange(0);
                break;

            case R.id.add_web_page:
                if(WebPage.webpagelist.size()>=10){
                    Toast.makeText(this,"窗口数量超过最大值",Toast.LENGTH_SHORT).show();
                }else{
                    WebPage.webpagelist.add(WebViewFragment.newInstance(initWebView()));
                    webpageAdapter.notifyDataSetChanged(WebPageAdapter.ADDWEBPAGE);
                    fixWebPage(WebPage.webpagelist.size()-1);
                    ZoomChange(1);
                }
                break;
            case R.id.app_exit:
                isExit=true;
                mDragLayout.close();
                break;
            case R.id.head_portrait:
                login_flag=1;
                mDragLayout.close();

                break;
        }
    }

    private void ZoomChange(int flag) {
        //0为缩小，1为放大
        if(flag==0){
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.7f);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.7f);
            ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(mViewPager, pvhX, pvhY);
            scale.setDuration(70);
            scale.start();

            toolbarBackground.setVisibility(View.INVISIBLE);
            webPageControlBackground.setVisibility(View.VISIBLE);
            title_bar.setVisibility(View.INVISIBLE);
            mViewPager.setFullScreen(false);

        }else{
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 0.7f, 1f);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 0.7f, 1f);
            ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(mViewPager, pvhX, pvhY);
            scale.setDuration(70);
            scale.start();

            webPageControlBackground.setVisibility(View.INVISIBLE);
            toolbarBackground.setVisibility(View.VISIBLE);
            title_bar.setVisibility(View.VISIBLE);
            mViewPager.setFullScreen(true);

            webView=WebPage.webpagelist.get(mViewPager.getCurrentItem()).getInnerWebView(); //定位当前的webview对象

            //防止viewpager滑动错位
            fixWebPage(mViewPager.getCurrentItem());

            //检测当前的webview对象是否可以向前或前后浏览
            if(!webView.canGoBack()){
                webBack.setEnabled(false);
            }else {
                webBack.setEnabled(true);
            }
            if(!webView.canGoForward()){
                next.setEnabled(false);
            }else {
                next.setEnabled(true);
            }
        }

    }

    public BroadcastReceiver networkChange=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startService(new Intent(context, WeatherService.class));
        }
    };
    public BroadcastReceiver refresh=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
            now_temperature.setText(preferences.getString("wendu","")+"°");
            describe.setText(preferences.getString("ganmao","无天气信息"));
            city.setText(preferences.getString("cityName"," "));
        }
    };
    private void initView(){
        now_temperature=(TextView)findViewById(R.id.now_wendu);
        now_temperature.setTypeface (Typeface.createFromAsset (getAssets(),"fonts/FZYTK.TTF" ));
        describe=(TextView)findViewById(R.id.miao_shu);
        describe.setTypeface (Typeface.createFromAsset (getAssets(),"fonts/FZYTK.TTF" ));
        city=(TextView)findViewById(R.id.city);
        mDragLayout = (DragLayout) findViewById(R.id.dl);
        mDragLayout.setDrag(false);
        ((MainContentLayout) findViewById(R.id.mainContent)).setDragLayout(mDragLayout);
        mDragLayout.setOnLayoutDragingListener(new DragLayout.OnLayoutDragingListener() {
            @Override
            public void onOpen() {
            }

            @Override
            public void onClose(){
                if(login_flag==1){
                    login_flag=0;
                    startActivityForResult(new Intent(MainActivity.this,LoginActivity.class),2);
                    overridePendingTransition(R.anim.left_in,0);
                }
                if(isExit)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },100);

            }
            @Override
            public void onDraging(float percent) {
                ViewHelper.setAlpha(titleLeftButton, 1 - percent);
            }
        });


        title_bar=findViewById(R.id.title_bar);

        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        //左上角侧滑菜单按钮
        titleLeftButton = (Button) findViewById(R.id.title_left_button);
        titleLeftButton.setOnClickListener(this);

        mViewPager= (MyViewPager) findViewById(R.id.viewpager);
        mViewPager.setOnLayoutClickListener(new MyViewPager.OnLayoutClickListener() {
            @Override
            public void onLayoutClick() {
                ZoomChange(1);
            }
        });
        ((ViewGroup)mViewPager.getParent()).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });
        mViewPager.setPageMargin(WebPage.page_interval);
        webpageAdapter=new WebPageAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(webpageAdapter);

        WebPage.webpagelist.add(WebViewFragment.newInstance(initWebView()));
        webpageAdapter.notifyDataSetChanged(WebPageAdapter.ADDWEBPAGE);
        mViewPager.setOffscreenPageLimit(10);


        toolbarBackground=findViewById(R.id.bottom_toolbar);
        webPageControlBackground=findViewById(R.id.web_page_control_bar);
        addWebPage= (Button) webPageControlBackground.findViewById(R.id.add_web_page);
        addWebPage.setOnClickListener(this);

        webBack = (Button)findViewById(R.id.web_back);
        webBack.setOnClickListener(this);

        next = (Button) findViewById(R.id.web_next);
        next.setOnClickListener(this);

        webRefresh= (Button) findViewById(R.id.web_refresh);
        webRefresh.setOnClickListener(this);

        webStopLoading= (Button) findViewById(R.id.web_stopLoading);
        webStopLoading.setOnClickListener(this);

        multiwindow= (Button) findViewById(R.id.multi_window);
        multiwindow.setOnClickListener(this);

        history= (Button)findViewById(R.id.web_history);
        history.setOnClickListener(this);

        exit= (Button) findViewById(R.id.app_exit);
        exit.setOnClickListener(this);

        headPortrait= (HeadPortraitView) findViewById(R.id.head_portrait);
        headPortrait.setOnClickListener(this);
    }


    private WebViewFragment.OnWebViewListener initWebView(){


        return new WebViewFragment.OnWebViewListener() {
            @Override
            public void onGetWebView(WebView webView) {
                //调用代表为新添加的webview
                if(first){
                    MainActivity.this.webView=webView;
                    first=false;
                }
                WebPage.webViews.add(webView);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //会加载几次
                if(title.equals("")||title.contains("https")||title.contains("http"))
                    return;
                else
                    insertTable(view.getUrl(),title);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                webRefresh.setVisibility(View.VISIBLE);
                webStopLoading.setVisibility(View.INVISIBLE);
                if(!webView.canGoBack()){
                    webBack.setEnabled(false);
                }else {
                    webBack.setEnabled(true);
                }
                if(!webView.canGoForward()){
                    next.setEnabled(false);
                }else {
                    next.setEnabled(true);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                webBack.setEnabled(false);
                next.setEnabled(false);
                webRefresh.setVisibility(View.INVISIBLE);
                webStopLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    webView.loadUrl(data.getStringExtra("currentUri"));
                }
            break;
            case 2:
                if (resultCode==RESULT_OK){
                    headPortrait.setBitmap((Bitmap) data.getParcelableExtra("touxiang"));
                }
                break;
        }
    }

    private void insertTable(String url, String title){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String historyTime=format.format(new Date(System.currentTimeMillis()));
        Log.d("rrr",""+historyTime);
        SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        Cursor mCursor=db.query(SQLiteHelper.TB_NAME,null,"historyNAME=?",new String[]{title},null,null,null);
        String sql,tip;
        if(mCursor.moveToFirst()){
            sql="update "+SQLiteHelper.TB_NAME+" set historyTIME='"+historyTime+"' where historyNAME='"+title+"'";
            tip="更新";
        }else{
            sql="insert into "+SQLiteHelper.TB_NAME+"(historyURL,historyTIME,historyNAME) values('"+url+"','"+historyTime+"','"+title+"')";
            tip="插入";
        }
        try{
            db.execSQL(sql);
        }catch (SQLException e){
            Toast.makeText(this,tip+"记录出错",Toast.LENGTH_SHORT).show();
            mCursor.close();
            return;
        }
        mCursor.close();
        Toast.makeText(this,tip+"了记录",Toast.LENGTH_SHORT).show();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //删除动画
        int viewTop=event.getViewTop();
        int value;
        if(viewTop>0){
            value=2500;
        }else{
            value=-2500;
        }
        View selectedView=WebPage.frameLayouts.get(mViewPager.getCurrentItem());
        Log.d("appo","Left:"+selectedView.getLeft());
        Animation animation = new TranslateAnimation(selectedView.getLeft(),selectedView.getLeft(),viewTop,value);
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                WebPage.webpagelist.remove(mViewPager.getCurrentItem());
                WebPage.frameLayouts.remove(mViewPager.getCurrentItem());
                WebPage.webViews.remove(mViewPager.getCurrentItem());
                WebPage.deleteItem=mViewPager.getCurrentItem();
                webpageAdapter.notifyDataSetChanged(WebPageAdapter.DELETEWEBPAGE);
                if (WebPage.webpagelist.size() == 0) {
                    first=true;
                    WebPage.webpagelist.add(WebViewFragment.newInstance(initWebView()));
                    webpageAdapter.notifyDataSetChanged(WebPageAdapter.ADDWEBPAGE);
                    ZoomChange(1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        selectedView.startAnimation(animation);


    }

    private void fixWebPage(int position){
        Log.d("appo","position:"+position);
        try {
            Field field = mViewPager.getClass().getField("mCurItem");
            field.setAccessible(true);
            field.setInt(mViewPager, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        webpageAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(position);
    }
}
