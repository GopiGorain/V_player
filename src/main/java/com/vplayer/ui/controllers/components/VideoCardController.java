package com.vplayer.ui.controllers.components;

import com.vplayer.models.Video;
import com.vplayer.services.VaultService;
import com.vplayer.services.ViewService;
import com.vplayer.ui.controllers.LibraryController;
import com.vplayer.ui.controllers.MainController;
import com.vplayer.ui.controllers.VaultController;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class VideoCardController {
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblDuration;
    @FXML
    private javafx.scene.image.ImageView imgThumbnail;
    @FXML
    private VBox root;

    private Video video;

    public void setVideo(Video video) {
        this.video = video;
        lblTitle.setText(video.getTitle());
        lblDuration.setText(formatDuration(video.getDuration()));
        
        // Load thumbnail
        String thumbnailDir = System.getProperty("user.home") + "/.vplayer/thumbnails";
        String thumbName = Integer.toHexString(video.getPath().hashCode()) + ".jpg";
        java.io.File thumbFile = new java.io.File(thumbnailDir, thumbName);
        if (thumbFile.exists()) {
            imgThumbnail.setImage(new javafx.scene.image.Image(thumbFile.toURI().toString()));
        }
        
        setupContextMenu();
    }

    @FXML
    public void initialize() {
        // Smooth scale-on-hover micro-animations
        root.setOnMouseEntered(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), root);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        root.setOnMouseExited(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), root);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        if (!video.isVaulted()) {
            MenuItem vaultItem = new MenuItem("Move to Private Vault");
            vaultItem.setOnAction(e -> moveToVault());
            contextMenu.getItems().add(vaultItem);
        } else {
            MenuItem unvaultItem = new MenuItem("Restore from Vault");
            unvaultItem.setOnAction(e -> restoreFromVault());
            contextMenu.getItems().add(unvaultItem);
        }

        root.setOnContextMenuRequested(e -> 
            contextMenu.show(root, e.getScreenX(), e.getScreenY())
        );
    }

    private void moveToVault() {
        if (VaultService.getInstance().vaultVideo(video)) {
            refreshViews();
        }
    }

    private void restoreFromVault() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Folder");
        File selectedFolder = directoryChooser.showDialog(root.getScene().getWindow());
        
        if (selectedFolder != null) {
            if (VaultService.getInstance().unvaultVideo(video, selectedFolder.getAbsolutePath())) {
                refreshViews();
            }
        }
    }

    private void refreshViews() {
        // Refresh Library if it's loaded
        LibraryController libraryController = ViewService.getInstance().getController("/fxml/library_view.fxml");
        if (libraryController != null) {
            libraryController.loadVideos();
        }
        
        // Refresh Vault if it's loaded
        VaultController vaultController = ViewService.getInstance().getController("/fxml/vault_view.fxml");
        if (vaultController != null) {
            vaultController.loadVaultedVideos();
        }
    }

    private String formatDuration(long millis) {
        if (millis <= 0) return "Unknown";
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void onCardClicked() {
        MainController mainController = ViewService.getInstance().getController("/fxml/main.fxml");
        if (mainController != null) {
            mainController.playVideo(video.getPath());
        }
    }
}
