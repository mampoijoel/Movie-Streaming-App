package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import com.streamdemo.model.Category;
import com.streamdemo.model.Movie;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * A labelled genre row. Exactly {@value #VISIBLE} thumbnails fit in the viewport
 * (card width is calculated from the window width); the remaining movies are
 * reached by the arrow buttons, a horizontal trackpad swipe or Shift + mouse wheel.
 */
public class CategoryRow extends VBox {

    private static final int VISIBLE = 5;
    private static final double GAP = 16;
    private static final double H_PAD = 48;     // side padding; also leaves room for the arrows
    private static final double V_PAD = 24;     // room for the hover "grow" effect
    private static final double META_HEIGHT = 54; // title + year under each thumbnail

    private final ScrollPane scroller = new ScrollPane();
    private final HBox track = new HBox(GAP);
    private final DoubleBinding cardWidth;
    private Timeline scrollAnimation;

    public CategoryRow(Category category, Consumer<Movie> onSelect) {
        setPadding(new Insets(14, 0, 0, 0));

        // --- header: red accent bar + label + count -------------------------------------
        Region accent = new Region();
        accent.setMinSize(4, 24);
        accent.setPrefSize(4, 24);
        accent.setMaxSize(4, 24);
        accent.setStyle("-fx-background-color: #e50914; -fx-background-radius: 2;");
        Label name = new Label(category.name());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
        Label count = new Label(category.movies().size() + " trailers");
        count.setStyle("-fx-text-fill: #808080; -fx-font-size: 13px;");
        HBox header = new HBox(12, accent, name, count);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, H_PAD, 0, H_PAD));

        // --- thumbnails -----------------------------------------------------------------
        cardWidth = Bindings.createDoubleBinding(
                () -> Math.max(140, (scroller.getWidth() - 2 * H_PAD - (VISIBLE - 1) * GAP) / VISIBLE),
                scroller.widthProperty());

        track.setPadding(new Insets(V_PAD, H_PAD, V_PAD, H_PAD));
        track.setAlignment(Pos.TOP_LEFT);
        for (Movie movie : category.movies()) {
            track.getChildren().add(new MovieCard(movie, cardWidth, onSelect));
        }

        scroller.setContent(track);
        scroller.setFitToHeight(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setPannable(false);
        DoubleBinding rowHeight = cardWidth.multiply(9.0 / 16.0).add(META_HEIGHT + 2 * V_PAD);
        scroller.prefHeightProperty().bind(rowHeight);
        scroller.minHeightProperty().bind(rowHeight);
        scroller.maxHeightProperty().bind(rowHeight);
        scroller.addEventFilter(ScrollEvent.SCROLL, this::handleHorizontalScroll);

        // --- arrows (visible only while the mouse is over the row) ----------------------
        StackPane left = arrow(true);
        StackPane right = arrow(false);
        StackPane stack = new StackPane(scroller, left, right);
        StackPane.setAlignment(left, Pos.TOP_LEFT);
        StackPane.setAlignment(right, Pos.TOP_RIGHT);
        StackPane.setMargin(left, new Insets(V_PAD, 0, 0, 0));
        StackPane.setMargin(right, new Insets(V_PAD, 0, 0, 0));
        left.visibleProperty().bind(stack.hoverProperty().and(scroller.hvalueProperty().greaterThan(0.001)));
        right.visibleProperty().bind(stack.hoverProperty().and(scroller.hvalueProperty().lessThan(0.999)));

        getChildren().addAll(header, stack);
    }

    /** Builds a custom arrow button (a styled pane with a drawn chevron - no Button class). */
    private StackPane arrow(boolean isLeft) {
        Node chevron = isLeft
                ? Icons.chevronLeft(26, Color.WHITE)
                : Icons.chevronRight(26, Color.WHITE);
        StackPane pane = new StackPane(chevron);
        pane.setMinWidth(H_PAD);
        pane.setPrefWidth(H_PAD);
        pane.setMaxWidth(H_PAD);
        pane.prefHeightProperty().bind(cardWidth.multiply(9.0 / 16.0));
        pane.maxHeightProperty().bind(cardWidth.multiply(9.0 / 16.0));
        pane.setCursor(Cursor.HAND);

        String direction = isLeft ? "to right" : "to left";
        String idle = "-fx-background-color: linear-gradient(" + direction + ", rgba(20,20,20,0.85), rgba(20,20,20,0));";
        String hover = "-fx-background-color: linear-gradient(" + direction + ", rgba(0,0,0,0.95), rgba(0,0,0,0.25));";
        pane.setStyle(idle);
        pane.setOnMouseEntered(e -> {
            pane.setStyle(hover);
            SoundManager.play(SoundManager.Effect.HOVER);
        });
        pane.setOnMouseExited(e -> pane.setStyle(idle));
        pane.setOnMouseClicked(e -> {
            SoundManager.play(SoundManager.Effect.CLICK);
            page(isLeft ? -1 : 1);
            e.consume();
        });
        return pane;
    }

    /** Smoothly scrolls one "page" (5 thumbnails) to the left or right. */
    private void page(int direction) {
        double max = maxScroll();
        if (max <= 0) {
            return;
        }
        double current = scroller.getHvalue() * max;
        double step = VISIBLE * (cardWidth.get() + GAP);
        double target = Math.max(0, Math.min(max, current + direction * step));
        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }
        scrollAnimation = new Timeline(new KeyFrame(Duration.millis(450),
                new KeyValue(scroller.hvalueProperty(), target / max, Interpolator.EASE_BOTH)));
        scrollAnimation.play();
    }

    /** Horizontal trackpad swipe / Shift+wheel scrolls the row; vertical wheel still scrolls the page. */
    private void handleHorizontalScroll(ScrollEvent e) {
        double max = maxScroll();
        if (max > 0 && Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY())) {
            double value = scroller.getHvalue() - e.getDeltaX() / max;
            scroller.setHvalue(Math.max(0, Math.min(1, value)));
            e.consume();
        }
    }

    private double maxScroll() {
        return track.getWidth() - scroller.getViewportBounds().getWidth();
    }
}
