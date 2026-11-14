package com.streamdemo;

import com.streamdemo.model.Catalog;
import com.streamdemo.model.Category;
import com.streamdemo.model.Movie;
import com.streamdemo.player.PlayerServer;
import com.streamdemo.ui.HomeView;
import com.streamdemo.ui.NavBar;
import com.streamdemo.ui.PlayerView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

public class MainApp extends Application {

    private final PlayerServer server = new PlayerServer();
    private PlayerView player;
    private HomeView home;

    @Override
    public void start(Stage stage) throws Exception {
        server.start();

        List<Category> categories = Catalog.load();
        List<String> names = categories.stream().map(Category::name).toList();

        player = new PlayerView(server, getHostServices());
        home = new HomeView(categories, Catalog.featured(), this::play);
        
        NavBar nav = new NavBar(names, home::scrollToTop, home::scrollToCategory, home::filter);

        BorderPane browse = new BorderPane();
        browse.setTop(nav);
        browse.setCenter(home);

        StackPane root = new StackPane(browse, player);
        root.setStyle("-fx-background-color: #141414;");

        Screen screen = Screen.getPrimary();
        double windowWidth = Math.min(1100, screen.getVisualBounds().getWidth() * 0.9);
        double windowHeight = Math.min(700, screen.getVisualBounds().getHeight() * 0.9);

        Scene scene = new Scene(root, windowWidth, windowHeight);
        
        URL css = getClass().getResource("/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (player.isOpen()) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    player.close();
                    e.consume();
                }
            }
        });

        stage.setTitle("StreamBox");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private void play(Movie movie) {
        // THIS IS THE FIX: It opens the YouTube trailer in your default web browser
        getHostServices().showDocument(movie.watchUrl());
    }

    @Override
    public void stop() {
        if (player != null) {
            player.dispose();
        }
        server.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}