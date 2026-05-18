package com.vplayer.ui.controllers;

import com.vplayer.database.repository.VideoRepository;
import com.vplayer.models.Video;
import com.vplayer.services.VaultService;
import com.vplayer.services.ViewService;
import com.vplayer.ui.controllers.components.VideoCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class VaultController {
    private static final Logger logger = LoggerFactory.getLogger(VaultController.class);
    private final VideoRepository videoRepository = new VideoRepository();
    private final VaultService vaultService = VaultService.getInstance();

    @FXML
    private Button btnLockVault;

    @FXML
    private FlowPane videoGrid;

    @FXML
    public void initialize() {
        btnLockVault.setOnAction(e -> lockVault());
        loadVaultedVideos();
    }

    public void loadVaultedVideos() {
        videoGrid.getChildren().clear();
        List<Video> videos = videoRepository.getAllVaulted();
        
        double delay = 0;
        for (Video video : videos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/video_card.fxml"));
                Parent card = loader.load();
                VideoCardController controller = loader.getController();
                controller.setVideo(video);
                
                // Add card and fade it in smoothly
                card.setOpacity(0.0);
                videoGrid.getChildren().add(card);
                
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), card);
                ft.setToValue(1.0);
                ft.setDelay(javafx.util.Duration.millis(delay));
                ft.play();
                
                delay += 35; // Stagger delay
            } catch (IOException e) {
                logger.error("Failed to load video card component in Vault", e);
            }
        }
    }

    private void lockVault() {
        vaultService.lock();
        MainController mainController = ViewService.getInstance().getController("/fxml/main.fxml");
        if (mainController != null) {
            mainController.loadVault(); // This will now show login because it's locked
        }
    }
}
