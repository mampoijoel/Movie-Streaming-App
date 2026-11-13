package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.function.DoubleConsumer;

/** Custom progress bar built from two Regions; click anywhere on it to seek. */
public class SeekBar extends StackPane {

    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private DoubleConsumer onSeek;

    public SeekBar() {
        setMinHeight(20);
        setPrefHeight(20);
        setMaxHeight(20);
        setCursor(Cursor.HAND);

        Region track = new Region();
        track.setMinHeight(6);
        track.setPrefHeight(6);
        track.setMaxHeight(6);
        track.setStyle("-fx-background-color: #4d4d4d; -fx-background-radius: 3;");

        Region fill = new Region();
        fill.setMinHeight(6);
        fill.setPrefHeight(6);
        fill.setMaxHeight(6);
        fill.setMaxWidth(Region.USE_PREF_SIZE);
        fill.setStyle("-fx-background-color: #e50914; -fx-background-radius: 3;");
        fill.prefWidthProperty().bind(widthProperty().multiply(progress));
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        getChildren().addAll(track, fill);

        setOnMouseClicked(e -> {
            double fraction = Math.max(0, Math.min(1, e.getX() / getWidth()));
            progress.set(fraction);
            SoundManager.play(SoundManager.Effect.CLICK);
            if (onSeek != null) {
                onSeek.accept(fraction);
            }
        });
    }

    public void setProgress(double fraction) {
        progress.set(Math.max(0, Math.min(1, fraction)));
    }

    public void setOnSeek(DoubleConsumer onSeek) {
        this.onSeek = onSeek;
    }
}
