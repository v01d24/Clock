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

    private int width;
    private int height;

    private int backgroundColor = Color.argb(125, 0, 0, 0);
    private int[] sectorColors = new int[] {
        Color.DKGRAY, Color.BLUE, Color.GREEN, Color.RED
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
    private final Paint paint;

    ClockBitmapGenerator(int width, int height) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        setSize(width, height);
    }

    void setSize(int width, int height) {
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
        updateSector(segmentMX20, -75, -45);
        updateSector(segmentMX5, -15, 15);
        updateSector(segmentMX1, 45, 75);
        updateSector(segmentHX12, 105, 135);
        updateSector(segmentHX4, 165, 195);
        updateSector(segmentHX1, 225, 255);
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
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        canvas.setBitmap(bitmap);

        drawBackground(canvas);
        drawAxes(canvas);
        drawSectors(canvas);

        return bitmap;
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(backgroundColor);
        canvas.drawCircle(center.x, center.y, radius, paint);
    }

    private void drawAxes(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (LineF axe: axes) {
            canvas.drawLine(axe.x1, axe.y1, axe.x2, axe.y2, paint);
        }
    }

    private void drawSectors(Canvas canvas) {
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);

        drawSector(canvas, segmentMX1, minutes % 5, 1);
        drawSector(canvas, segmentMX5, (minutes % 20) / 5, 2);
        drawSector(canvas, segmentMX20, minutes / 20, 3);
        drawSector(canvas, segmentHX1, hours % 4, 1);
        drawSector(canvas, segmentHX4, (hours % 12) / 4, 2);
        drawSector(canvas, segmentHX12, hours / 12, 3);
    }

    private void drawSector(Canvas canvas, Path[] sector, int segments, int colorIndex) {
        paint.setColor(sectorColors[colorIndex]);
        for (int i = 0; i < segments; ++i) {
            canvas.drawPath(sector[i], paint);
        }
        if (segments < sector.length) {
            paint.setColor(sectorColors[0]);
            for (int i = segments; i < sector.length; ++i) {
                canvas.drawPath(sector[i], paint);
            }
        }
    }

    private static class LineF {

        float x1;
        float y1;
        float x2;
        float y2;

    }
}
