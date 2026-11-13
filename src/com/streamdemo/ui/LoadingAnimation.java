package com.streamdemo.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Our own "trailer is loading" animation (no stock ProgressIndicator):
 * two dashed rings spinning in opposite directions around a pulsing play symbol.
 */
public class LoadingAnimation extends StackPane {

    private final RotateTransition outerSpin;
    private final RotateTransition innerSpin;
    private final ScaleTransition pulse;
    private final FadeTransition glowFade;

    public LoadingAnimation() {
        Circle outer = ring(48, 0.75, Icons.NETFLIX_RED, 6);
        Circle inner = ring(33, 0.55, Color.web("#ffffff", 0.7), 3);
        Polygon symbol = Icons.play(24, Icons.NETFLIX_RED);
        symbol.setTranslateX(3);

        setPrefSize(120, 120);
        setMinSize(120, 120);
        setMaxSize(120, 120);
        getChildren().addAll(outer, inner, symbol);

        outerSpin = new RotateTransition(Duration.seconds(1.1), outer);
        outerSpin.setByAngle(360);
        outerSpin.setInterpolator(Interpolator.LINEAR);
        outerSpin.setCycleCount(Animation.INDEFINITE);

        innerSpin = new RotateTransition(Duration.seconds(1.8), inner);
        innerSpin.setByAngle(-360);
        innerSpin.setInterpolator(Interpolator.LINEAR);
        innerSpin.setCycleCount(Animation.INDEFINITE);

        pulse = new ScaleTransition(Duration.millis(650), symbol);
        pulse.setFromX(0.8);
        pulse.setFromY(0.8);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);

        glowFade = new FadeTransition(Duration.millis(650), symbol);
        glowFade.setFromValue(0.5);
        glowFade.setToValue(1.0);
        glowFade.setAutoReverse(true);
        glowFade.setCycleCount(Animation.INDEFINITE);
    }

    /** A circle outline drawn as a dashed stroke so it looks like an arc but rotates about its true centre. */
    private static Circle ring(double radius, double visibleFraction, Color color, double width) {
        Circle c = new Circle(radius);
        c.setFill(Color.TRANSPARENT);
        c.setStroke(color);
        c.setStrokeWidth(width);
        c.setStrokeLineCap(StrokeLineCap.ROUND);
        double circumference = 2 * Math.PI * radius;
        c.getStrokeDashArray().addAll(circumference * visibleFraction - width,
                circumference * (1 - visibleFraction) + width);
        return c;
    }

    public void start() {
        outerSpin.play();
        innerSpin.play();
        pulse.play();
        glowFade.play();
    }

    public void stop() {
        outerSpin.stop();
        innerSpin.stop();
        pulse.stop();
        glowFade.stop();
    }
}
