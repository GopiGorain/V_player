package com.vplayer.ui.controllers;

import com.vplayer.services.ViewService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox sidebar;

    @FXML
    private Button btnLibrary;

    @FXML
    private Button btnPlaylists;

    @FXML
    private Button btnVault;

    @FXML
    private Button btnSettings;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        logger.info("MainController initialized");
        
        btnLibrary.setOnAction(e -> loadLibrary());
        btnPlaylists.setOnAction(e -> loadPlaylists());
        btnVault.setOnAction(e -> loadVault());
        btnSettings.setOnAction(e -> loadSettings());
        
        // Load library by default
        loadLibrary();

        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((wObs, oldWindow, newWindow) -> {
                    if (newWindow instanceof javafx.stage.Stage) {
                        javafx.stage.Stage stage = (javafx.stage.Stage) newWindow;
                        stage.fullScreenProperty().addListener((fObs, oldVal, isFullScreen) -> {
                            sidebar.setVisible(!isFullScreen);
                            sidebar.setManaged(!isFullScreen);
                        });
                    }
                });
            }
        });
    }

    private void setView(String fxmlPath) {
        // If we are navigating away from the player, pause the video
        if (!"/fxml/player_view.fxml".equals(fxmlPath)) {
            PlayerController playerController = ViewService.getInstance().getController("/fxml/player_view.fxml");
            if (playerController != null) {
                playerController.pauseMedia();
            }
        }

        Parent view = ViewService.getInstance().loadView(fxmlPath);
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    private void loadLibrary() {
        logger.info("Loading Library view");
        // For now, load player as a placeholder or library if it exists
        setView("/fxml/library_view.fxml");
    }

    private void loadPlaylists() {
        logger.info("Loading Playlists view");
    }

    public void loadVault() {
        logger.info("Loading Vault view");
        if (com.vplayer.services.VaultService.getInstance().isUnlocked()) {
            setView("/fxml/vault_view.fxml");
        } else {
            setView("/fxml/vault_login.fxml");
        }
    }

    public void reloadCurrentView() {
        // Simple way to refresh: check what buttons are active or just reload based on some state
        // For now, let's just reload Library or Vault if they are the current views
        // This is a bit naive but works for the current use case
        loadLibrary(); 
    }

    private void loadSettings() {
        logger.info("Loading Settings view");
    }

    public void playVideo(String path) {
        logger.info("Playing video: {}", path);
        setView("/fxml/player_view.fxml");
        PlayerController playerController = ViewService.getInstance().getController("/fxml/player_view.fxml");
        if (playerController != null) {
            playerController.playMedia(path);
        } else {
            logger.error("PlayerController not found for /fxml/player_view.fxml");
        }
    }
}
