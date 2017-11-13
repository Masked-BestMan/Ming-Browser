package com.example.myapplication.Toolkit;

import android.webkit.WebView;

import com.example.myapplication.Components.WebViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zbm阿铭 on 2017/11/2.
 */

public class WebPage {
    public static List<WebViewFragment> webpagelist=new ArrayList<>();  //fraglist和fraghashcode成对出现
    public static List<WebView> webViews=new ArrayList<>();
    public static int page_interval=40;  //界面之间的间隔
    public static boolean isFullScreen=true;
    public static int deleteItem=-1;  //当前的界面

}