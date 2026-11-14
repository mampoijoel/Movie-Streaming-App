package com.streamdemo.ui;

import com.streamdemo.model.Movie;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public class HeroBanner extends StackPane {

    public HeroBanner(Movie movie, ObservableDoubleValue windowWidth, Consumer<Movie> onPlay) {
        DoubleBinding height = Bindings.createDoubleBinding(
                () -> Math.max(320, Math.min(520, windowWidth.get() * 0.42)), windowWidth);
        prefHeightProperty().bind(height);
        minHeightProperty().bind(height);
        maxHeightProperty().bind(height);
        setStyle("-fx-background-color: #000000;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        Image backdrop = new Image(movie.heroUrl(), true);
        ImageView picture = new ImageView(backdrop);
        picture.setPreserveRatio(true);
        picture.fitWidthProperty().bind(widthProperty());

        Image fallback = new Image(movie.fallbackHeroUrl(), true);
        fallback.errorProperty().addListener((obs, was, isError) -> {
            if (isError) {
                picture.setImage(null);
                setStyle("-fx-background-color: #1a1a1a;");
            }
        });
        backdrop.errorProperty().addListener((obs, was, isError) -> {
            if (isError) {
                picture.setImage(fallback);
            }
        });

        Region fadeLeft = new Region();
        fadeLeft.setStyle("-fx-background-color: linear-gradient(to right, rgba(20,20,20,0.95) 0%, "
                + "rgba(20,20,20,0.6) 45%, rgba(20,20,20,0) 100%);");
        Region fadeBottom = new Region();
        fadeBottom.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(20,20,20,0) 55%, #141414 100%);");

        Label tag = new Label("FEATURED TRAILER");
        tag.setStyle("-fx-text-fill: #e50914; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label title = new Label(movie.title());
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 52px; -fx-font-weight: bold;");
        title.setEffect(new DropShadow(12, Color.BLACK));
        Label blurb = new Label(movie.year() + "  •  " + movie.blurb());
        blurb.setWrapText(true);
        blurb.setStyle("-fx-text-fill: #e5e5e5; -fx-font-size: 18px;");
        blurb.setEffect(new DropShadow(8, Color.BLACK));

        CustomButton play = new CustomButton("Play Trailer", Icons.play(16, Color.BLACK),
                CustomButton.Kind.PRIMARY, () -> onPlay.accept(movie));
        HBox buttons = new HBox(12, play);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        VBox text = new VBox(10, tag, title, blurb, buttons);
        text.setMaxWidth(560);
        text.setPadding(new Insets(0, 0, 0, 48));
        text.setAlignment(Pos.CENTER_LEFT);
        StackPane.setAlignment(text, Pos.CENTER_LEFT);

        getChildren().addAll(picture, fadeLeft, fadeBottom, text);
    }
}