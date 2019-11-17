package com.v01d24.clock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class ClockService extends Service {

    private static final String TAG = ClockService.class.getSimpleName();

    private static final String ACTION_UPDATE_WIDGET = "UPDATE_WIDGET";
    private static final String ACTION_RESIZE_WIDGET = "RESIZE_WIDGET";

    private static final String ARG_WIDGET_WIDTH = "WIDGET_WIDTH";
    private static final String ARG_WIDGET_HEIGHT = "WIDGET_HEIGHT";

    private BroadcastReceiver screenOnOffReceiver;
    private BroadcastReceiver timeChangedReceiver;

    private ClockBitmapGenerator bitmapGenerator;

    static void updateWidget(Context context) {
        Intent intent = new Intent(context, ClockService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    static void resizeWidget(Context context, int width, int height) {
        Intent intent = new Intent(context, ClockService.class);
        intent.setAction(ACTION_RESIZE_WIDGET);
        intent.putExtra(ARG_WIDGET_WIDTH, width);
        intent.putExtra(ARG_WIDGET_HEIGHT, height);
        context.startService(intent);
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, ClockService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");

        Settings settings = new Settings(this);
        int width = settings.getWidgetWidth();
        int height = settings.getWidgetHeight();
        bitmapGenerator = new ClockBitmapGenerator(width, height);

        registerScreenOnOffReceiver();
        registerTimeChangedReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action;
        if (intent != null) action = intent.getAction();
        else action = ACTION_UPDATE_WIDGET;
        Log.e(TAG, "onStartCommand action=" + action);

        if (ACTION_UPDATE_WIDGET.equals(action)) {
            updateWidget();
        }
        else if (ACTION_RESIZE_WIDGET.equals(action)) {
            int width = intent.getIntExtra(ARG_WIDGET_WIDTH, 0);
            int height = intent.getIntExtra(ARG_WIDGET_HEIGHT, 0);
            if (width > 0 && height > 0) {
                new Settings(this).setWidgetSize(width, height);
                bitmapGenerator.setSize(width, height);
                updateWidget();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        unregisterScreenOnOffReceiver();
        unregisterTimeChangedReceiver();
    }

    private void registerScreenOnOffReceiver() {
        if (screenOnOffReceiver == null) {
            screenOnOffReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean screenOff = Intent.ACTION_SCREEN_OFF.equals(intent.getAction());
                    Log.e(TAG, "screenOnOffReceiver.onReceive screenOff=" + screenOff);
                    if (screenOff) {
                        unregisterTimeChangedReceiver();
                    }
                    else {
                        updateWidget();
                        registerTimeChangedReceiver();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenOnOffReceiver, filter);
        }
    }

    private void unregisterScreenOnOffReceiver() {
        if (screenOnOffReceiver != null) {
            unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
    }

    private void registerTimeChangedReceiver() {
        if (timeChangedReceiver == null) {
            timeChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e(TAG, "timeChangedReceiver.onReceive");
                    updateWidget();
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(timeChangedReceiver, filter);
        }
    }

    private void unregisterTimeChangedReceiver() {
        if (timeChangedReceiver != null) {
            unregisterReceiver(timeChangedReceiver);
            timeChangedReceiver = null;
        }
    }

    private void updateWidget() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
        Bitmap bitmap = bitmapGenerator.generate();
        views.setBitmap(R.id.image, "setImageBitmap", bitmap);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, WidgetProvider.class);
        appWidgetManager.updateAppWidget(provider, views);
    }
}
