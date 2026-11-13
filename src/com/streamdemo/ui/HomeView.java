package com.streamdemo.ui;

import com.streamdemo.model.Category;
import com.streamdemo.model.Movie;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** The scrolling home page: hero banner followed by one row per category. */
public class HomeView extends ScrollPane {

    private final VBox content = new VBox();
    private final List<CategoryRow> rows = new ArrayList<>();
    private Timeline scrollAnimation;

    public HomeView(List<Category> categories, Movie featured, Consumer<Movie> onSelect) {
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        content.setStyle("-fx-background-color: #141414;");
        content.getChildren().add(new HeroBanner(featured, widthProperty(), onSelect));
        for (Category category : categories) {
            CategoryRow row = new CategoryRow(category, onSelect);
            rows.add(row);
            content.getChildren().add(row);
        }

        Label footer = new Label("StreamBox demo  •  Trailers are streamed from YouTube and are the property of their owners.");
        footer.setStyle("-fx-text-fill: #5a5a5a; -fx-font-size: 12px;");
        footer.setPadding(new Insets(40, 48, 40, 48));
        content.getChildren().add(footer);

        setContent(content);
    }

    public void scrollToTop() {
        animateTo(0);
    }

    public void scrollToCategory(int index) {
        double scrollable = content.getHeight() - getViewportBounds().getHeight();
        if (index < 0 || index >= rows.size() || scrollable <= 0) {
            return;
        }
        animateTo(Math.min(1, rows.get(index).getBoundsInParent().getMinY() / scrollable));
    }

    private void animateTo(double vvalue) {
        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }
        scrollAnimation = new Timeline(new KeyFrame(Duration.millis(500),
                new KeyValue(vvalueProperty(), vvalue, Interpolator.EASE_BOTH)));
        scrollAnimation.play();
    }
}
