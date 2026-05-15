package com.vplayer.app;

import com.vplayer.services.ViewService;
import com.vplayer.ui.controllers.PlayerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

public class VPlayerApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(VPlayerApp.class);

    @Override
    public void init() {
        boolean vlcFound = new NativeDiscovery().discover();
        if (!vlcFound) {
            logger.error("VLC not found on the system. Please install VLC 3.x (64-bit).");
            // In a real app, we might show a dialog and exit
        } else {
            logger.info("VLC found successfully.");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = ViewService.getInstance().loadView("/fxml/main.fxml");

        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle("VPlayer - Premium Media Player");
        primaryStage.setScene(scene);
        
        // Load global CSS
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        try {
            PlayerController playerController = ViewService.getInstance().getController("/fxml/player_view.fxml");
            if (playerController != null) {
                playerController.shutdown();
            }
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
        super.stop();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
