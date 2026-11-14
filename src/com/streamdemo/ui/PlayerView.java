package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import com.streamdemo.model.Movie;
import com.streamdemo.player.PlayerServer;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.util.prefs.Preferences;

public class PlayerView extends StackPane {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/124.0.0.0 Safari/537.36 Edg/124.0.0.0";

    private final PlayerServer server;
    private final HostServices hostServices;

    private final Label titleLabel = new Label();
    private final StackPane stage = new StackPane();
    private final StackPane loadingOverlay = new StackPane();
    private final ImageView backdrop = new ImageView();
    private final Label statusLabel = new Label("Loading YouTube trailer...");
    
    private WebView webView;
    private WebEngine engine;

    public PlayerView(PlayerServer server, HostServices hostServices) {
        this.server = server;
        this.hostServices = hostServices;
        
        setStyle("-fx-background-color: #000000;"); 
        setVisible(false);

        // --- Top Bar ---
        CustomButton back = new CustomButton("Back", Icons.chevronLeft(16, Color.WHITE),
                CustomButton.Kind.SECONDARY, this::close);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        CustomButton openOnYoutube = new CustomButton("Open in Browser", CustomButton.Kind.GHOST, this::openInBrowser);
        HBox topBar = new HBox(18, back, titleLabel, spacer, openOnYoutube);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 30, 10, 30));

        // --- Video Stage ---
        StackPane stageHolder = new StackPane(stage);
        DoubleBinding stageWidth = Bindings.createDoubleBinding(
                () -> Math.max(320, Math.min(stageHolder.getWidth() - 60, (stageHolder.getHeight() - 40) * 16.0 / 9.0)),
                stageHolder.widthProperty(), stageHolder.heightProperty());
        stage.prefWidthProperty().bind(stageWidth);
        stage.maxWidthProperty().bind(stageWidth);
        stage.prefHeightProperty().bind(stageWidth.multiply(9.0 / 16.0));
        stage.maxHeightProperty().bind(stageWidth.multiply(9.0 / 16.0));
        stage.setStyle("-fx-background-color: black; -fx-background-radius: 8;");

        // Loading Screen
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        StackPane.setAlignment(statusLabel, Pos.CENTER);
        loadingOverlay.setStyle("-fx-background-color: #000000;");
        loadingOverlay.getChildren().add(statusLabel);
        stage.getChildren().add(loadingOverlay);

        // --- Layout ---
        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(stageHolder);
        getChildren().add(layout);

        ensureWebView();
    }

    private void ensureWebView() {
        if (webView != null) return;
        
        webView = new WebView();
        webView.setContextMenuEnabled(false);
        engine = webView.getEngine();
        engine.setUserAgent(USER_AGENT);
        
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Hide the loading screen once the YouTube iframe loads
                loadingOverlay.setVisible(false);
            }
        });
        
        // Add WebView under the loading screen
        stage.getChildren().add(0, webView);
    }

    public boolean isOpen() {
        return isVisible();
    }

    public void open(Movie movie) {
        Preferences.userNodeForPackage(PlayerView.class).put("lastWatched", movie.title());

        titleLabel.setText(movie.title());
        loadingOverlay.setVisible(true);
        
        toFront();
        setVisible(true);
        
        SoundManager.play(SoundManager.Effect.OPEN);

        // Load the new simple iframe page
        engine.load(server.urlFor(movie.youtubeId()));
    }

    public void close() {
        if (!isVisible()) return;
        
        SoundManager.play(SoundManager.Effect.CLOSE);
        
        if (engine != null) {
            engine.load("about:blank"); // Stops the video
        }
        
        setVisible(false);
    }

    public void togglePlayback() {
        // Not needed for the simple iframe, but keeping it prevents errors in MainApp
    }

    public void dispose() {
        close();
    }

    private void openInBrowser() {
        // Fallback just in case
        if (titleLabel.getText() != null) {
             // (Optional: add logic to open browser)
        }
    }
}