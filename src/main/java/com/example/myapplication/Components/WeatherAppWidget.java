package com.example.myapplication.Components;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.Assistance.WeatherService;

/**
 * Created by Zbm阿铭 on 2017/5/1.
 */

public class WeatherAppWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context,WeatherService.class));
    }
}
