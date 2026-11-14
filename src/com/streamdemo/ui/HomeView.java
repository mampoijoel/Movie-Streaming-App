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
import java.util.prefs.Preferences;

public class HomeView extends ScrollPane {

    private final VBox content = new VBox();
    private final List<CategoryRow> rows = new ArrayList<>();
    private final List<Category> originalCategories;
    private final Movie featured;
    private final Consumer<Movie> onSelect;
    private Timeline scrollAnimation;

    public HomeView(List<Category> categories, Movie featured, Consumer<Movie> onSelect) {
        this.originalCategories = categories;
        this.featured = featured;
        this.onSelect = onSelect;
        
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        content.setStyle("-fx-background-color: #141414;");
        setContent(content);
        
        buildHome(false, "");
    }

    private void buildHome(boolean isSearching, String query) {
        content.getChildren().clear();
        rows.clear();

        if (!isSearching) {
            String lastWatched = Preferences.userNodeForPackage(HomeView.class).get("lastWatched", null);
            if (lastWatched != null && !lastWatched.isEmpty()) {
                for (Category c : originalCategories) {
                    boolean found = false;
                    for (Movie m : c.movies()) {
                        if (m.title().equals(lastWatched)) {
                            content.getChildren().add(new CategoryRow(new Category("Continue Watching", List.of(m)), onSelect));
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }

            content.getChildren().add(new HeroBanner(featured, widthProperty(), onSelect));
            for (Category category : originalCategories) {
                CategoryRow row = new CategoryRow(category, onSelect);
                rows.add(row);
                content.getChildren().add(row);
            }
        } else {
            List<Movie> matches = new ArrayList<>();
            for (Category c : originalCategories) {
                for (Movie m : c.movies()) {
                    if (m.title().toLowerCase().contains(query)) {
                        matches.add(m);
                    }
                }
            }
            
            if (!matches.isEmpty()) {
                CategoryRow row = new CategoryRow(new Category("Search Results", matches), onSelect);
                rows.add(row);
                content.getChildren().add(row);
            } else {
                Label noResult = new Label("No results found for \"" + query + "\"");
                noResult.setStyle("-fx-text-fill: #888888; -fx-font-size: 24px; -fx-font-weight: bold;");
                noResult.setPadding(new Insets(60, 48, 40, 48));
                content.getChildren().add(noResult);
            }
        }

        if (!isSearching) {
            Label footer = new Label("StreamBox demo  •  Trailers are streamed from YouTube and are the property of their owners.");
            footer.setStyle("-fx-text-fill: #5a5a5a; -fx-font-size: 12px;");
            footer.setPadding(new Insets(40, 48, 40, 48));
            content.getChildren().add(footer);
        }
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            buildHome(false, "");
        } else {
            buildHome(true, query);
        }
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