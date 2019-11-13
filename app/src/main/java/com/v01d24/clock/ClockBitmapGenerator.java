package com.v01d24.clock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.Calendar;

class ClockBitmapGenerator {

    private static final int AXES_COUNT = 6;
    private static final int CIRCLES_COUNT = 11;

    private static ClockBitmapGenerator instance;

    private int width;
    private int height;

    private int backgroundColor = Color.argb(125, 0, 0, 0);
    private int[] sectorColors = new int[] {
        Color.BLUE, Color.GREEN, Color.RED
    };

    private PointF center = new PointF();
    private float radius;

    private LineF[] axes = new LineF[AXES_COUNT];

    private Path[] segmentMX1 = createPaths(4);
    private Path[] segmentMX5 = createPaths(3);
    private Path[] segmentMX20 = createPaths(2);
    private Path[] segmentHX1 = createPaths(3);
    private Path[] segmentHX4 = createPaths(2);
    private Path[] segmentHX12 = createPaths(1);

    private static Path[] createPaths(int count) {
        Path[] paths = new Path[count];
        for (int i = 0; i < count; ++i) {
            paths[i] = new Path();
        }
        return paths;
    }

    private final Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
    private final Canvas canvas;
    private final Paint paint;

    static ClockBitmapGenerator getInstance(int width, int height) {
        if (instance == null) {
            instance = new ClockBitmapGenerator();
        }
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
        updateSectors();
    }

    private void updateAxes() {
        float minRadius = radius * 2 / CIRCLES_COUNT;
        double axeAngle = Math.toRadians(30);
        double angleStep = Math.PI * 2 / AXES_COUNT;
        for (int i = 0; i < AXES_COUNT; ++i) {
            LineF axe = new LineF();
            axe.x1 = getX(minRadius, axeAngle);
            axe.y1 = getY(minRadius, axeAngle);
            axe.x2 = getX(radius, axeAngle);
            axe.y2 = getY(radius, axeAngle);
            axes[i] = axe;
            axeAngle += angleStep;
        }
    }

    private void updateSectors() {
        updateSector(segmentMX1, -75, -45);
        updateSector(segmentMX5, -15, 15);
        updateSector(segmentMX20, 45, 75);
        updateSector(segmentHX1, 105, 135);
        updateSector(segmentHX4, 165, 195);
        updateSector(segmentHX12, 225, 255);
    }

    private void updateSector(Path[] sector, int angleFromDeg, int angleToDeg) {
        float radiusStep = radius / CIRCLES_COUNT;
        float radiusFrom = radiusStep * 3;
        float radiusTo = radiusFrom + radiusStep;
        for (Path segment: sector) {
            updateSegment(segment, radiusFrom, radiusTo, angleFromDeg, angleToDeg);
            radiusFrom = radiusTo + radiusStep;
            radiusTo = radiusFrom + radiusStep;
        }
    }

    private void updateSegment(Path segment, float radiusFrom, float radiusTo, int angleFromDeg, int angleToDeg) {
        segment.reset();
        RectF oval = new RectF();
        getOval(radiusFrom, oval);
        segment.addArc(oval, angleFromDeg, angleToDeg - angleFromDeg);
        getOval(radiusTo, oval);
        segment.arcTo(oval, angleToDeg, angleFromDeg - angleToDeg);
        segment.close();
    }

    private void getOval(float radius, RectF oval) {
        oval.left = center.x - radius;
        oval.top = center.y - radius;
        oval.right = center.x + radius;
        oval.bottom = center.y + radius;
    }

    private float getX(float radius, double angleRad) {
        return (float) (center.x + radius * Math.cos(angleRad));
    }

    private float getY(float radius, double angleRad) {
        return (float) (center.y + radius * Math.sin(angleRad));
    }

    Bitmap generate() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        canvas.setBitmap(bitmap);

        drawBackground();
        drawAxes();
        drawSectors();

        return bitmap;
    }

    private void drawBackground() {
        paint.setColor(backgroundColor);
        canvas.drawCircle(center.x, center.y, radius, paint);
    }

    private void drawAxes() {
        paint.setColor(Color.WHITE);
        for (LineF axe: axes) {
            canvas.drawLine(axe.x1, axe.y1, axe.x2, axe.y2, paint);
        }
    }

    private void drawSectors() {
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);

        drawSector(segmentMX1, sectorColors[0], minutes % 5);
        drawSector(segmentMX5, sectorColors[1], (minutes % 20) / 5);
        drawSector(segmentMX20, sectorColors[2], minutes / 20);
        drawSector(segmentHX1, sectorColors[0], hours % 4);
        drawSector(segmentHX4, sectorColors[1], (hours % 12) / 4);
        drawSector(segmentHX12, sectorColors[2], hours / 12);
    }

    private void drawSector(Path[] sector, int color, int segments) {
        paint.setColor(color);
        for (int i = 0; i < segments; ++i) {
            canvas.drawPath(sector[i], paint);
        }
    }

    private static class LineF {

        float x1;
        float y1;
        float x2;
        float y2;

    }
}
