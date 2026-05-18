package com.vplayer.ui.controllers;

import com.vplayer.database.repository.VideoRepository;
import com.vplayer.models.Video;
import com.vplayer.services.LibraryScanner;
import com.vplayer.services.ViewService;
import com.vplayer.ui.controllers.components.VideoCardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LibraryController {
    private static final Logger logger = LoggerFactory.getLogger(LibraryController.class);
    private final VideoRepository videoRepository = new VideoRepository();
    private final LibraryScanner libraryScanner = new LibraryScanner();

    @FXML
    private Button btnOpenFile;

    @FXML
    private Button btnOpenFolder;

    @FXML
    private FlowPane videoGrid;

    @FXML
    public void initialize() {
        btnOpenFile.setOnAction(e -> openFile());
        btnOpenFolder.setOnAction(e -> openFolder());
        loadVideos();
    }

    public void loadVideos() {
        videoGrid.getChildren().clear();
        List<Video> videos = videoRepository.getAllNonVaulted();
        
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
                logger.error("Failed to load video card component", e);
            }
        }
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Video File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi", "*.mov", "*.webm", "*.flv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(btnOpenFile.getScene().getWindow());
        if (selectedFile != null) {
            logger.info("Opening file: " + selectedFile.getAbsolutePath());
            MainController mainController = ViewService.getInstance().getController("/fxml/main.fxml");
            if (mainController != null) {
                mainController.playVideo(selectedFile.getAbsolutePath());
            }
        }
    }

    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Video Folder");
        File selectedFolder = directoryChooser.showDialog(btnOpenFolder.getScene().getWindow());
        
        if (selectedFolder != null) {
            new Thread(() -> {
                libraryScanner.scanFolder(selectedFolder);
                Platform.runLater(this::loadVideos);
            }).start();
        }
    }
}
