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
import javafx.stage.Stage;

import java.util.List;

/**
 * StreamBox - a demo movie-trailer streaming app.
 *
 * Structure:  model/ (data)  ->  ui/ (screens + custom widgets)  ->  player/ (local page server)
 *             audio/ (sound effects)
 */
public class MainApp extends Application {

    private final PlayerServer server = new PlayerServer();
    private PlayerView player;

    @Override
    public void start(Stage stage) throws Exception {
        server.start();

        List<Category> categories = Catalog.load();
        List<String> names = categories.stream().map(Category::name).toList();

        player = new PlayerView(server, getHostServices());
        HomeView home = new HomeView(categories, Catalog.featured(), this::play);
        NavBar nav = new NavBar(names, home::scrollToTop, home::scrollToCategory);

        BorderPane browse = new BorderPane();
        browse.setTop(nav);
        browse.setCenter(home);

        StackPane root = new StackPane(browse, player); // the player covers the browse screen
        root.setStyle("-fx-background-color: #141414;");

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!player.isOpen()) {
                return;
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                player.close();
                e.consume();
            } else if (e.getCode() == KeyCode.SPACE) {
                player.togglePlayback();
                e.consume();
            }
        });

        stage.setTitle("StreamBox");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    private void play(Movie movie) {
        player.open(movie);
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
