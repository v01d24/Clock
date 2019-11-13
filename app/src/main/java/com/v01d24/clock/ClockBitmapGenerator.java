package com.v01d24.clock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class ClockBitmapGenerator {

    private static final int AXES_COUNT = 6;
    private static final int CIRCLES_COUNT = 11;

    private static ClockBitmapGenerator instance;

    private int width;
    private int height;

    private PointF center = new PointF();
    private float radius;

    private LineF[] axes = new LineF[AXES_COUNT];

    private final Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
    private final Canvas canvas;
    private final Paint paint;

    public static ClockBitmapGenerator getInstance(int width, int height) {
        if (instance == null) instance = new ClockBitmapGenerator();
        instance.setSize(width, height);
        return instance;
    }

    private ClockBitmapGenerator() {
        canvas = new Canvas();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    private void setSize(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            updateLayout();
        }
    }

    private void updateLayout() {
        center.x = width / 2f;
        center.y = height / 2f;
        radius = Math.min(width, height) / 2f;
        updateAxes();
    }

    private void updateAxes() {
        float minRadius = radius * 2 / CIRCLES_COUNT;
        int axeAngle = 30;
        for (int i = 0; i < AXES_COUNT; ++i) {
            LineF axe = new LineF();
            axe.x1 = (float) (center.x + minRadius * Math.cos(Math.toRadians(axeAngle)));
            axe.y1 = (float) (center.y + minRadius * Math.sin(Math.toRadians(axeAngle)));
            axe.x2 = (float) (center.x + radius * Math.cos(Math.toRadians(axeAngle)));
            axe.y2 = (float) (center.y + radius * Math.sin(Math.toRadians(axeAngle)));
            axes[i] = axe;
            axeAngle += 60;
        }
    }

    public Bitmap generate() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        canvas.setBitmap(bitmap);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(center.x, center.y, radius, paint);

        paint.setColor(Color.WHITE);
        for (LineF axe: axes) {
            canvas.drawLine(axe.x1, axe.y1, axe.x2, axe.y2, paint);
        }
        return bitmap;
    }

    private static class LineF {

        float x1;
        float y1;
        float x2;
        float y2;

    }
}
