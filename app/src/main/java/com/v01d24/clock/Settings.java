package com.v01d24.clock;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String PREFS_NAME = "settings";

    private static final String PREF_WIDGET_WIDTH = "widget_width";
    private static final String PREF_WIDGET_HEIGHT = "widget_height";
    private static final int DEFAULT_SIZE = 144;

    private final SharedPreferences prefs;

    Settings(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    int getWidgetWidth() {
        return prefs.getInt(PREF_WIDGET_WIDTH, DEFAULT_SIZE);
    }

    int getWidgetHeight() {
        return prefs.getInt(PREF_WIDGET_HEIGHT, DEFAULT_SIZE);
    }

    void setWidgetSize(int width, int height) {
        prefs.edit()
            .putInt(PREF_WIDGET_WIDTH, width)
            .putInt(PREF_WIDGET_HEIGHT, height)
            .apply();
    }
}
