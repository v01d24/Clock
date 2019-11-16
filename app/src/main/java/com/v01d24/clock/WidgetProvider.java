package com.v01d24.clock;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();

    private static ClockBitmapGenerator bitmapGenerator;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.e(TAG, "onUpdate");
        updateWidgets(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, ClockService.class));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        int width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        Log.e(TAG, "onAppWidgetOptionsChanged " + width + " " + height);
        new Settings(context).setWidgetSize(width, height);
        if (bitmapGenerator != null) {
            bitmapGenerator.setSize(width, height);
        }
        updateWidgets(context, appWidgetManager, new int[]{ appWidgetId });
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.e(TAG, "onDeleted");
        context.stopService(new Intent(context, ClockService.class));
    }

    private void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (bitmapGenerator == null) {
            Settings settings = new Settings(context);
            int width = settings.getWidgetWidth();
            int height = settings.getWidgetHeight();
            bitmapGenerator = new ClockBitmapGenerator(width, height);
            Log.e(TAG, "Create bitmap generator " + width + " " + height);
        }
        Bitmap bmp = bitmapGenerator.generate();
        for (int appWidgetId: appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setBitmap(R.id.image, "setImageBitmap", bmp);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}