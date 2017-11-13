package com.example.myapplication.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.myapplication.R;


public class WebViewFragment extends android.support.v4.app.Fragment{
    private static OnWebViewListener wl;
    private static Context context;
    private WebView webView;
    private View cache;


    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(Context c,OnWebViewListener onWebViewListener) {
        context=c;
        WebViewFragment fragment = new WebViewFragment();
        wl=onWebViewListener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(cache==null){
            cache= inflater.inflate(R.layout.webview_fragment, container, false);
            webView= (WebView) cache.findViewById(R.id.fragment_web_view);
            WebSettings setting = webView.getSettings();
            setSettings(setting);
            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    wl.onReceivedTitle(view,title);
                }
            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    wl.onPageFinished(view,url);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    wl.onPageStarted(view,url,favicon);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    wl.onReceivedError(view,errorCode,description,failingUrl);
                    String data = "Page NO FOUND！";
                    view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
                }
            });
            webView.loadUrl("http://wwww.baidu.com");
            wl.onGetWebView(webView);  //新添加的fragment


        }else{
            webView= (WebView) cache.findViewById(R.id.fragment_web_view);

        }

        return cache;
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        //暂停播放
        webView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }


    private void setSettings(WebSettings setting) {
        setting.setJavaScriptEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setDomStorageEnabled(true);
        setting.setDatabaseEnabled(true);
        setting.setSaveFormData(false);
        setting.setAppCacheEnabled(true);
        setting.setPluginState(WebSettings.PluginState.ON);
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 全屏显示
        setting.setLoadWithOverviewMode(false);
        setting.setUseWideViewPort(true);
    }

    public interface OnWebViewListener{
        void onGetWebView(WebView webView);
        void onReceivedTitle(WebView view, String title);
        void onPageFinished(WebView view, String url);
        void onPageStarted(WebView view, String url, Bitmap favicon);
        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
    }
    public WebView getInnerWebView(){
        return webView;
    }

}
