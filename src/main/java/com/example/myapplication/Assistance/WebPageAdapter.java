package com.example.myapplication.Assistance;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.Components.WebViewFragment;
import com.example.myapplication.Toolkit.WebPage;

/**
 * Created by Zbm阿铭 on 2017/11/2.
 */

public class WebPageAdapter extends FragmentPagerAdapter {
    private FragmentManager fm;
    public static final int ADDWEBPAGE=0;
    public static final int DELETEWEBPAGE=1;
    private int notifyType=1;
    public WebPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.fm=fm;
    }

    @Override
    public WebViewFragment getItem(int position) {
        return WebPage.webpagelist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return WebPage.webpagelist.get(position).hashCode();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return WebPage.webpagelist.size();
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container,position);
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(notifyType==1&&position==WebPage.deleteItem){
            fm.beginTransaction().remove((Fragment) object).commit();
            WebPage.deleteItem=-1;
            return;
        }
        super.destroyItem(container, position, object);
    }

    public void notifyDataSetChanged(int type) {
        if(type==ADDWEBPAGE){
            notifyType=0;
        }else{
            notifyType=1;
        }
        super.notifyDataSetChanged();
    }

}
