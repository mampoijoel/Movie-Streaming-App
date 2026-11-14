package com.streamdemo.ui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public final class Icons {

    public static final Color NETFLIX_RED = Color.web("#e50914");

    private Icons() {
    }

    public static Polygon play(double size, Paint color) {
        Polygon p = new Polygon(0, 0, size * 0.9, size / 2, 0, size);
        p.setFill(color);
        return p;
    }

    public static Group pause(double size, Paint color) {
        Rectangle a = new Rectangle(0, 0, size * 0.32, size);
        Rectangle b = new Rectangle(size * 0.58, 0, size * 0.32, size);
        a.setFill(color);
        b.setFill(color);
        return new Group(a, b);
    }

    public static Polyline chevronLeft(double size, Paint color) {
        return chevron(new double[]{size * 0.6, 0, 0, size * 0.5, size * 0.6, size}, color);
    }

    public static Polyline chevronRight(double size, Paint color) {
        return chevron(new double[]{0, 0, size * 0.6, size * 0.5, 0, size}, color);
    }

    private static Polyline chevron(double[] points, Paint color) {
        Polyline line = new Polyline(points);
        line.setStroke(color);
        line.setFill(null);
        line.setStrokeWidth(3.5);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return line;
    }
}