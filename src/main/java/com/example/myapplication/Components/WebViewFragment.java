package com.example.myapplication.Components;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.myapplication.R;
import com.example.myapplication.Toolkit.WebPage;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


public class WebViewFragment extends android.support.v4.app.Fragment{
    private static OnWebViewListener wl;
    private WebView webView;
    private View cache;


    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(OnWebViewListener onWebViewListener) {
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

                @Override
                public void onReceivedIcon(WebView view, Bitmap icon) {
                    super.onReceivedIcon(view, icon);
                }

            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    //只有加载重定向网页才会调用
                    Log.d("appo","shouldOverride");
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d("appo","onPageFinished");
                    wl.onPageFinished(view,url);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.d("appo","onPageStarted");
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
            WebPage.frameLayouts.add((FrameLayout) cache.findViewById(R.id.frame_layout));

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
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setAllowFileAccess(true);
        setting.setSupportZoom(true);
        setting.setBuiltInZoomControls(true);
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        setting.setSupportMultipleWindows(false);
        setting.setDomStorageEnabled(true);
        setting.setAppCacheEnabled(true);
        setting.setGeolocationEnabled(true);
        setting.setAppCacheMaxSize(Long.MAX_VALUE);
        setting.setAppCachePath(getActivity().getDir("appcache", 0).getPath());
        setting.setDatabasePath(getActivity().getDir("databases", 0).getPath());
        setting.setGeolocationDatabasePath(getActivity().getDir("geolocation", 0)
                .getPath());
        setting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // 全屏显示
        setting.setUseWideViewPort(true);
        setting.setTextZoom(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("text_size","100")));
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
