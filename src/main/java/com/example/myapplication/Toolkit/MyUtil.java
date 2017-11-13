package com.example.myapplication.Toolkit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;


public class MyUtil {
    public static String wendu;
    public static String fengxiang;
    public static String low;
    public static String high;
    public static String date;
    public static String type;
    public static String ganmao;
    public static int dip2px(Context context,float dpValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
    public  static int px2dip(Context context,float pxValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }
    public static void handleWeatherResponse(Context context,String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherInfo=jsonObject.getJSONObject("data");
            wendu=weatherInfo.getString("wendu");
            ganmao=weatherInfo.getString("ganmao");
            String forecast=weatherInfo.getString("forecast");
            JSONArray jsonArray=new JSONArray(forecast);
            JSONObject jsonObject1=jsonArray.getJSONObject(0);
            fengxiang=jsonObject1.getString("fengxiang");
            low=jsonObject1.getString("low");
            high=jsonObject1.getString("high");
            date=jsonObject1.getString("date");
            type=jsonObject1.getString("type");
            SharedPreferences.Editor pref= PreferenceManager.getDefaultSharedPreferences(context).edit();
            pref.putString("wendu",wendu);
            pref.putString("ganmao",ganmao);
            pref.apply();
            context.sendBroadcast(new Intent("weather_refresh"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
