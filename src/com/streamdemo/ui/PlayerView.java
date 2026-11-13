package com.streamdemo.ui;

import com.streamdemo.audio.SoundManager;
import com.streamdemo.model.Movie;
import com.streamdemo.player.PlayerServer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
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
import netscape.javascript.JSObject;


public class PlayerView extends StackPane {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";
    private static final double LOAD_TIMEOUT_SECONDS = 20;

    private final PlayerServer server;
    private final HostServices hostServices;
    private final Bridge bridge = new Bridge(); // field keeps a strong reference: WebView only holds it weakly

    private final Label titleLabel = new Label();
    private final StackPane stage = new StackPane();
    private final StackPane loadingOverlay = new StackPane();
    private final LoadingAnimation spinner = new LoadingAnimation();
    private final ImageView backdrop = new ImageView();
    private final Label statusLabel = new Label();
    private final VBox overlayBox = new VBox(18);
    private FadeTransition fadeOut;
    private final CustomButton fallbackButton;
    private final CustomButton playPause;
    private final SeekBar seekBar = new SeekBar();
    private final Timeline dots;
    private final Timeline progressPoller;
    private final PauseTransition loadTimeout = new PauseTransition(Duration.seconds(LOAD_TIMEOUT_SECONDS));

    private WebView webView;
    private WebEngine engine;
    private Movie current;
    private boolean playing;
    private int dotCount;

    public PlayerView(PlayerServer server, HostServices hostServices) {
        this.server = server;
        this.hostServices = hostServices;
        setStyle("-fx-background-color: rgba(0,0,0,0.96);");
        setVisible(false);

       
        CustomButton back = new CustomButton("Back", Icons.chevronLeft(16, Color.WHITE),
                CustomButton.Kind.SECONDARY, this::close);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        CustomButton openOnYoutube = new CustomButton("Open on YouTube", CustomButton.Kind.GHOST, this::openInBrowser);
        HBox topBar = new HBox(18, back, titleLabel, spacer, openOnYoutube);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18, 32, 8, 32));

        
        StackPane stageHolder = new StackPane(stage);
        DoubleBinding stageWidth = Bindings.createDoubleBinding(
                () -> Math.max(320, Math.min(stageHolder.getWidth() - 40, (stageHolder.getHeight() - 10) * 16.0 / 9.0)),
                stageHolder.widthProperty(), stageHolder.heightProperty());
        stage.prefWidthProperty().bind(stageWidth);
        stage.maxWidthProperty().bind(stageWidth);
        stage.prefHeightProperty().bind(stageWidth.multiply(9.0 / 16.0));
        stage.maxHeightProperty().bind(stageWidth.multiply(9.0 / 16.0));
        stage.setStyle("-fx-background-color: black;");

        buildLoadingOverlay(stageWidth);
        fallbackButton = new CustomButton("Watch on YouTube", Icons.play(16, Color.BLACK),
                CustomButton.Kind.PRIMARY, this::openInBrowser);
        fallbackButton.setVisible(false);
        overlayBox.getChildren().add(fallbackButton);
        stage.getChildren().add(loadingOverlay);

        
        playPause = new CustomButton("Pause", Icons.pause(16, Color.BLACK), CustomButton.Kind.PRIMARY, this::togglePlayback);
        seekBar.setOnSeek(fraction -> run("window.streamApi && window.streamApi.seek(" + fraction + ")"));
        VBox bottom = new VBox(14, seekBar, playPause);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.maxWidthProperty().bind(stageWidth);
        bottom.setPadding(new Insets(6, 0, 22, 0));
        StackPane bottomHolder = new StackPane(bottom);

        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(stageHolder);
        layout.setBottom(bottomHolder);
        getChildren().add(layout);

       
        dots = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            dotCount = (dotCount + 1) % 4;
            statusLabel.setText("Loading trailer" + ".".repeat(dotCount));
        }));
        dots.setCycleCount(Timeline.INDEFINITE);

        progressPoller = new Timeline(new KeyFrame(Duration.millis(500), e -> pollProgress()));
        progressPoller.setCycleCount(Timeline.INDEFINITE);

        loadTimeout.setOnFinished(e -> showProblem("This trailer is taking too long to load."));
    }

    private void buildLoadingOverlay(DoubleBinding stageWidth) {
        backdrop.setOpacity(0.28);
        backdrop.setPreserveRatio(false);
        backdrop.fitWidthProperty().bind(stageWidth);
        backdrop.fitHeightProperty().bind(stageWidth.multiply(9.0 / 16.0));

        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        overlayBox.getChildren().addAll(spinner, statusLabel);
        overlayBox.setAlignment(Pos.CENTER);
        loadingOverlay.setStyle("-fx-background-color: #000000;");
        loadingOverlay.getChildren().addAll(backdrop, overlayBox); // dimmed poster behind the spinner
        backdrop.setMouseTransparent(true);
    }

    

    public boolean isOpen() {
        return isVisible();
    }

    public void open(Movie movie) {
        ensureWebView();
        if (fadeOut != null) {
            fadeOut.stop(); // reopened while still fading out
        }
        current = movie;
        playing = false;
        titleLabel.setText(movie.title());
        backdrop.setImage(new Image(movie.thumbnailUrl(), true));
        seekBar.setProgress(0);
        playPause.update("Pause", Icons.pause(16, Color.BLACK));
        showLoading();

        setOpacity(0);
        setVisible(true);
        FadeTransition in = new FadeTransition(Duration.millis(260), this);
        in.setToValue(1);
        in.play();
        SoundManager.play(SoundManager.Effect.OPEN);

        engine.load(server.urlFor(movie.youtubeId()));
        loadTimeout.playFromStart();
        progressPoller.play();
    }

    public void close() {
        if (!isVisible()) {
            return;
        }
        SoundManager.play(SoundManager.Effect.CLOSE);
        loadTimeout.stop();
        progressPoller.stop();
        dots.stop();
        spinner.stop();
        current = null;
        playing = false;
        if (engine != null) {
            engine.load("about:blank"); // stops the video and the sound immediately
        }
        fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> setVisible(false));
        fadeOut.play();
    }

    
    public void dispose() {
        if (engine != null) {
            engine.load("about:blank");
        }
    }

   

    public void togglePlayback() {
        if (engine == null || current == null) {
            return;
        }
        if (playing) {
            run("window.streamApi && window.streamApi.pause()");
            setPlayingIcon(false);
        } else {
            run("window.streamApi && window.streamApi.play()");
            setPlayingIcon(true);
        }
    }

    private void setPlayingIcon(boolean isPlaying) {
        playing = isPlaying;
        if (isPlaying) {
            playPause.update("Pause", Icons.pause(16, Color.BLACK));
        } else {
            playPause.update("Play", Icons.play(16, Color.BLACK));
        }
    }

    private void openInBrowser() {
        if (current != null) {
            hostServices.showDocument(current.watchUrl());
        }
    }

    private void pollProgress() {
        if (!playing || engine == null) {
            return;
        }
        try {
            Object result = engine.executeScript("window.streamApi ? window.streamApi.progress() : 0");
            if (result instanceof Number number) {
                seekBar.setProgress(number.doubleValue());
            }
        } catch (Exception ignored) {
            // page not ready yet
        }
    }

    private void run(String script) {
        if (engine == null) {
            return;
        }
        try {
            engine.executeScript(script);
        } catch (Exception ignored) {
            // page not ready yet
        }
    }

    
    private void showLoading() {
        fallbackButton.setVisible(false);
        loadingOverlay.setOpacity(1);
        loadingOverlay.setVisible(true);
        dotCount = 0;
        statusLabel.setText("Loading trailer");
        spinner.start();
        dots.play();
    }

    private void hideLoading() {
        loadTimeout.stop();
        dots.stop();
        FadeTransition fade = new FadeTransition(Duration.millis(450), loadingOverlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            loadingOverlay.setVisible(false);
            spinner.stop();
        });
        fade.play();
    }

    private void showProblem(String message) {
        dots.stop();
        loadTimeout.stop();
        spinner.stop();
        loadingOverlay.setOpacity(1);
        loadingOverlay.setVisible(true);
        statusLabel.setText(message);
        fallbackButton.setVisible(true);
    }

    private void ensureWebView() {
        if (webView != null) {
            return;
        }
        webView = new WebView();
        webView.setContextMenuEnabled(false);
        engine = webView.getEngine();
        engine.setUserAgent(USER_AGENT);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (current == null) {
                return;
            }
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", bridge);
                engine.executeScript("window.flushBridge && window.flushBridge()");
            } else if (newState == Worker.State.FAILED) {
                showProblem("Could not reach the trailer. Check your internet connection.");
            }
        });
        stage.getChildren().add(0, webView); // below the loading overlay
    }

    
    public final class Bridge {

        public void onReady(String ignored) {
            // nothing to do - autoplay starts the video
        }

        public void onState(String state) {
            Platform.runLater(() -> handleState(parse(state)));
        }

        public void onError(String code) {
            Platform.runLater(() -> handleError(parse(code)));
        }

        private int parse(String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                return -99;
            }
        }
    }

   
    private void handleState(int state) {
        if (current == null) {
            return;
        }
        switch (state) {
            case 1 -> {
                setPlayingIcon(true);
                if (loadingOverlay.isVisible() && !fallbackButton.isVisible()) {
                    hideLoading();
                }
            }
            case 2, 0 -> setPlayingIcon(false);
            default -> { }
        }
    }

    private void handleError(int code) {
        if (current == null) {
            return;
        }
        switch (code) {
            case 100 -> showProblem("This trailer is no longer available.");
            case 101, 150, 153 -> showProblem("The owner does not allow this trailer to be played inside other apps.");
            default -> showProblem("The trailer could not be played (error " + code + ").");
        }
    }
}
