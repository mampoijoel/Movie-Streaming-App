package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class CustomButton extends StackPane {

    public enum Kind { PRIMARY, SECONDARY, GHOST }

    private final Label label = new Label();
    private final HBox content = new HBox(10);
    private final Kind kind;
    private final ScaleTransition scale = new ScaleTransition(Duration.millis(110), this);
    private Runnable onAction;

    public CustomButton(String text, Node icon, Kind kind, Runnable onAction) {
        this.kind = kind;
        this.onAction = onAction;

        content.setAlignment(Pos.CENTER);
        content.setMouseTransparent(true);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        update(text, icon);
        getChildren().add(content);

        boolean ghost = kind == Kind.GHOST;
        setPadding(ghost ? new Insets(8, 12, 8, 12) : new Insets(10, 26, 10, 22));
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setCursor(Cursor.HAND);
        applyStyle(false);

        setOnMouseEntered(e -> {
            applyStyle(true);
            animateTo(1.06);
            SoundManager.play(SoundManager.Effect.HOVER);
        });
        setOnMouseExited(e -> {
            applyStyle(false);
            animateTo(1.0);
        });
        setOnMousePressed(e -> animateTo(0.94));
        setOnMouseReleased(e -> animateTo(isHover() ? 1.06 : 1.0));
        setOnMouseClicked(e -> {
            SoundManager.play(SoundManager.Effect.CLICK);
            if (this.onAction != null) {
                this.onAction.run();
            }
        });
    }

    public CustomButton(String text, Kind kind, Runnable onAction) {
        this(text, null, kind, onAction);
    }

    public void update(String text, Node icon) {
        label.setText(text);
        content.getChildren().clear();
        if (icon != null) {
            content.getChildren().add(icon);
        }
        if (text != null && !text.isEmpty()) {
            content.getChildren().add(label);
        }
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setOnAction(Runnable onAction) {
        this.onAction = onAction;
    }

    private void animateTo(double target) {
        scale.stop();
        scale.setToX(target);
        scale.setToY(target);
        scale.play();
    }

    private void applyStyle(boolean hover) {
        String background;
        String text;
        switch (kind) {
            case PRIMARY -> {
                background = hover ? "rgba(255,255,255,0.75)" : "white";
                text = "black";
            }
            case SECONDARY -> {
                background = hover ? "rgba(109,109,110,0.45)" : "rgba(109,109,110,0.7)";
                text = "white";
            }
            default -> {
                background = hover ? "rgba(255,255,255,0.12)" : "transparent";
                text = hover ? "white" : "#d2d2d2";
            }
        }
        label.setStyle("-fx-font-size: " + (kind == Kind.GHOST ? 15 : 17)
                + "px; -fx-font-weight: bold; -fx-text-fill: " + text + ";");
        setStyle("-fx-background-color: " + background + "; -fx-background-radius: 6;");
    }
}