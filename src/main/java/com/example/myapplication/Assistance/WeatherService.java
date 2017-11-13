package com.example.myapplication.Assistance;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.myapplication.Interface.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Toolkit.HttpUtil;
import com.example.myapplication.Toolkit.MyUtil;
import com.example.myapplication.Components.WeatherAppWidget;

/**
 * Created by Zbm阿铭 on 2017/5/1.
 */

public class WeatherService extends Service {
    private AppWidgetManager manager;
    private RemoteViews views;
    private String city="";
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        mLocationClient = new LocationClient(getApplicationContext());
        initLocation();
        mLocationClient.registerLocationListener( myListener );
        views=new RemoteViews(getPackageName(), R.layout.weather_widget_layout);
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        city=sharedPreferences.getString("cityName","");
        if(city!="")
            views.setTextViewText(R.id.city,city);
        PendingIntent refreshIntent=PendingIntent.getService(this,0,new Intent(this,WeatherService.class),0);
        views.setOnClickPendingIntent(R.id.weather_refresh,refreshIntent);
        PendingIntent appIntent=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
        views.setOnClickPendingIntent(R.id.weather_text,appIntent);
        manager=AppWidgetManager.getInstance(this);
        ComponentName widget=new ComponentName(this,WeatherAppWidget.class);
        manager.updateAppWidget(widget,views);
        mLocationClient.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }
    private class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation.getLocType()==63)
                Toast.makeText(WeatherService.this,"请检查网络",Toast.LENGTH_SHORT).show();
            else{
                city=bdLocation.getCity();
                SharedPreferences.Editor pref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                pref.putString("cityName",city);
                pref.commit();
                views=new RemoteViews(getPackageName(),R.layout.weather_widget_layout);
                if(city!=null){
                    HttpUtil.sendHttpRequest("http://wthrcdn.etouch.cn/weather_mini?city=" + city, new HttpUtil.HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            MyUtil.handleWeatherResponse(WeatherService.this,response);
                            views.setTextViewText(R.id.city,city);
                            views.setTextViewText(R.id.weather_text, String.format("%s\n%s , %s\n%s~%s",MyUtil.date,MyUtil.type,MyUtil.fengxiang,MyUtil.low,MyUtil.high));
                            ComponentName widget=new ComponentName(WeatherService.this,WeatherAppWidget.class);
                            manager.updateAppWidget(widget,views);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(WeatherService.this,"网络不通畅，请刷新重试",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
            WeatherService.this.stopSelf();
        }
    }
}
