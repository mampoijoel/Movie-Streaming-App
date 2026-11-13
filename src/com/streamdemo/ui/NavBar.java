package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.function.IntConsumer;

/** Top bar: logo, jump-to-category links and the sound on/off switch. */
public class NavBar extends HBox {

    public NavBar(List<String> categoryNames, Runnable onHome, IntConsumer onCategory) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(12, 48, 12, 48));
        setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #141414);");

        Label logoRed = new Label("STREAM");
        logoRed.setStyle("-fx-text-fill: #e50914; -fx-font-size: 30px; -fx-font-weight: 900;");
        Label logoWhite = new Label("BOX");
        logoWhite.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: 900;");
        HBox logo = new HBox(2, logoRed, logoWhite);
        logo.setPadding(new Insets(0, 24, 0, 0));
        getChildren().add(logo);

        getChildren().add(new CustomButton("Home", CustomButton.Kind.GHOST, onHome));
        for (int i = 0; i < categoryNames.size(); i++) {
            final int index = i;
            getChildren().add(new CustomButton(categoryNames.get(i), CustomButton.Kind.GHOST,
                    () -> onCategory.accept(index)));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        CustomButton sound = new CustomButton("Sound: ON", CustomButton.Kind.GHOST, null);
        sound.setOnAction(() -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            sound.update(SoundManager.isMuted() ? "Sound: OFF" : "Sound: ON", null);
        });
        getChildren().add(sound);
    }
}
