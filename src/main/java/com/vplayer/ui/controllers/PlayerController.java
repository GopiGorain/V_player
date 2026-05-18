package com.vplayer.ui.controllers;

import com.vplayer.database.repository.VideoRepository;
import com.vplayer.models.Video;
import com.vplayer.player.VideoPlayer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.TrackDescription;

import java.util.List;

public class PlayerController {
    @FXML
    private StackPane playerRoot;
    @FXML
    private StackPane videoContainer;
    @FXML
    private VBox controlsOverlay;
    @FXML
    private Slider seekSlider;
    @FXML
    private Button btnPlayPause;
    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;
    @FXML
    private FontIcon playPauseIcon;
    @FXML
    private Label lblTime;
    @FXML
    private Slider volumeSlider;
    @FXML
    private MenuButton btnSubtitle;
    @FXML
    private MenuButton btnAudio;
    @FXML
    private MenuButton btnSpeed;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnFullscreen;
    @FXML
    private ImageView previewImage;
    
    @FXML
    private StackPane hoverPreviewContainer;
    @FXML
    private ImageView hoverPreviewImage;
    @FXML
    private Label lblHoverTime;

    private final com.vplayer.services.FFmpegService ffmpegService = new com.vplayer.services.FFmpegService();
    private static final int NUM_PREVIEW_THUMBNAILS = 50;
    private final List<javafx.scene.image.Image> previewThumbnails = new java.util.ArrayList<>();
    private boolean previewsLoaded = false;

    private VideoPlayer videoPlayer;
    private boolean isSeeking = false;
    private PauseTransition hideControlsTransition;

    @FXML
    public void initialize() {
        videoPlayer = new VideoPlayer();
        ImageView videoView = videoPlayer.getView();
        videoView.setPreserveRatio(true);
        videoView.setSmooth(true);
        
        videoContainer.getChildren().add(videoView);
        
        // Ensure container can shrink
        playerRoot.setMinSize(0, 0);
        videoContainer.setMinSize(0, 0);
        playerRoot.setPrefSize(0, 0);
        videoContainer.setPrefSize(0, 0);

        // Ensure video fills container
        videoView.fitWidthProperty().bind(videoContainer.widthProperty());
        videoView.fitHeightProperty().bind(videoContainer.heightProperty());
        
        // Ensure preview fills container
        previewImage.fitWidthProperty().bind(videoContainer.widthProperty());
        previewImage.fitHeightProperty().bind(videoContainer.heightProperty());

        setupListeners();
        setupSpeedMenu();
        
        // Set initial volume
        volumeSlider.setValue(50);
        videoPlayer.getMediaPlayer().audio().setVolume(50);
        
        hideControlsTransition = new PauseTransition(Duration.seconds(2));
        hideControlsTransition.setOnFinished(e -> {
            if (videoPlayer.getMediaPlayer().status().isPlaying()) {
                controlsOverlay.setVisible(false);
                playerRoot.setCursor(javafx.scene.Cursor.NONE);
            }
        });

        playerRoot.setOnMouseMoved(e -> {
            controlsOverlay.setVisible(true);
            playerRoot.setCursor(javafx.scene.Cursor.DEFAULT);
            hideControlsTransition.playFromStart();
        });

        playerRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyShortcuts);
                newScene.windowProperty().addListener((wObs, oldWindow, newWindow) -> {
                    if (newWindow instanceof javafx.stage.Stage) {
                        javafx.stage.Stage stage = (javafx.stage.Stage) newWindow;
                        stage.fullScreenProperty().addListener((fObs, oldVal, isFullScreen) -> {
                            if (isFullScreen) {
                                hideControlsTransition.playFromStart();
                            } else {
                                hideControlsTransition.stop();
                                controlsOverlay.setVisible(true);
                                playerRoot.setCursor(javafx.scene.Cursor.DEFAULT);
                            }
                        });
                    }
                });
            }
        });
    }

    private void setupSpeedMenu() {
        float[] speeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f};
        for (float s : speeds) {
            MenuItem item = new MenuItem(s + "x");
            item.setOnAction(e -> {
                videoPlayer.setRate(s);
                btnSpeed.setText(s + "x");
            });
            btnSpeed.getItems().add(item);
        }
    }

    private void handleKeyShortcuts(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE:
                togglePlayPause();
                event.consume();
                break;
            case LEFT:
                videoPlayer.skip(-10000);
                event.consume();
                break;
            case RIGHT:
                videoPlayer.skip(10000);
                event.consume();
                break;
            case UP:
                volumeSlider.setValue(volumeSlider.getValue() + 5);
                event.consume();
                break;
            case DOWN:
                volumeSlider.setValue(volumeSlider.getValue() - 5);
                event.consume();
                break;
            case F:
                toggleFullscreen();
                event.consume();
                break;
            case M:
                volumeSlider.setValue(0);
                event.consume();
                break;
        }
    }

    private void setupListeners() {
        btnPlayPause.setOnAction(e -> togglePlayPause());
        btnPrevious.setOnAction(e -> videoPlayer.skip(-10000));
        btnNext.setOnAction(e -> videoPlayer.skip(10000));
        btnFullscreen.setOnAction(e -> toggleFullscreen());
        btnMinimize.setOnAction(e -> toggleMinimize());
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            videoPlayer.getMediaPlayer().audio().setVolume(newVal.intValue());
        });

        seekSlider.setOnMousePressed(e -> {
            isSeeking = true;
            double mouseX = e.getX();
            double sliderWidth = seekSlider.getWidth();
            if (sliderWidth > 0) {
                double pct = mouseX / sliderWidth;
                pct = Math.max(0.0, Math.min(1.0, pct));
                videoPlayer.seek((float) pct);
                seekSlider.setValue(pct * 100);
            }
        });

        seekSlider.setOnMouseDragged(e -> {
            isSeeking = true;
            double mouseX = e.getX();
            double sliderWidth = seekSlider.getWidth();
            if (sliderWidth > 0) {
                double pct = mouseX / sliderWidth;
                pct = Math.max(0.0, Math.min(1.0, pct));
                videoPlayer.seek((float) pct);
                seekSlider.setValue(pct * 100);
                handleSeekSliderHover(e);
            }
        });

        seekSlider.setOnMouseReleased(e -> {
            isSeeking = false;
            double mouseX = e.getX();
            double sliderWidth = seekSlider.getWidth();
            if (sliderWidth > 0) {
                double pct = mouseX / sliderWidth;
                pct = Math.max(0.0, Math.min(1.0, pct));
                videoPlayer.seek((float) pct);
            }
            handleSeekSliderExit(null);
        });

        seekSlider.setOnMouseMoved(this::handleSeekSliderHover);
        seekSlider.setOnMouseExited(this::handleSeekSliderExit);

        videoPlayer.getMediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                if (!isSeeking) {
                    Platform.runLater(() -> seekSlider.setValue(newPosition * 100));
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                Platform.runLater(() -> updateTimeLabel(newTime, mediaPlayer.status().length()));
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("MediaPlayer: Playing event triggered");
                // Explicitly ensure audio is enabled and volume is set
                mediaPlayer.audio().setMute(false);
                mediaPlayer.audio().setVolume((int) volumeSlider.getValue());
                
                // Debug audio tracks
                int trackCount = mediaPlayer.audio().trackCount();
                System.out.println("Audio track count: " + trackCount);
                if (trackCount > 0) {
                    System.out.println("Current audio track: " + mediaPlayer.audio().track());
                } else {
                    System.err.println("WARNING: No audio tracks detected!");
                }

                long duration = mediaPlayer.status().length();
                if (duration > 0 && currentPath != null) {
                    String hash = Integer.toHexString(currentPath.hashCode());
                    String outputDir = System.getProperty("user.home") + "/.vplayer/previews/" + hash;
                    ffmpegService.generateHoverPreviews(currentPath, outputDir, duration, NUM_PREVIEW_THUMBNAILS, () -> {
                        Platform.runLater(() -> loadPreviewThumbnails(outputDir));
                    });
                }
                
                Platform.runLater(() -> {
                    previewImage.setVisible(false);
                    playPauseIcon.setIconLiteral("fas-pause");
                    populateTracks();
                });
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                System.out.println("MediaPlayer: Paused event triggered");
                Platform.runLater(() -> playPauseIcon.setIconLiteral("fas-play"));
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.err.println("MediaPlayer: Error event triggered");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Playback Error");
                    alert.setHeaderText("Failed to play video");
                    alert.setContentText("The file might be corrupted or in an unsupported format.");
                    alert.showAndWait();
                });
            }

            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, uk.co.caprica.vlcj.media.MediaRef media) {
                System.out.println("MediaPlayer: Media changed");
            }

            @Override
            public void opening(MediaPlayer mediaPlayer) {
                System.out.println("MediaPlayer: Opening...");
                Platform.runLater(() -> previewImage.setVisible(false));
            }

            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
                System.out.println("MediaPlayer: Buffering " + newCache + "%");
                if (newCache > 0) {
                    Platform.runLater(() -> previewImage.setVisible(false));
                }
            }
        });
    }

    private void populateTracks() {
        populateTrackMenu(btnSubtitle, videoPlayer.getMediaPlayer().subpictures().trackDescriptions(), 
                id -> videoPlayer.getMediaPlayer().subpictures().setTrack(id));
        populateTrackMenu(btnAudio, videoPlayer.getMediaPlayer().audio().trackDescriptions(), 
                id -> videoPlayer.getMediaPlayer().audio().setTrack(id));
    }

    private void populateTrackMenu(MenuButton menu, List<TrackDescription> descriptions, java.util.function.Consumer<Integer> onSelect) {
        menu.getItems().clear();
        for (TrackDescription desc : descriptions) {
            MenuItem item = new MenuItem(desc.description());
            item.setOnAction(e -> onSelect.accept(desc.id()));
            menu.getItems().add(item);
        }
    }

    private void togglePlayPause() {
        if (videoPlayer.getMediaPlayer().status().isPlaying()) {
            videoPlayer.pause();
        } else {
            videoPlayer.play(null);
        }
    }

    private void toggleFullscreen() {
        // Toggle fullscreen on the current stage
        javafx.stage.Stage stage = (javafx.stage.Stage) btnFullscreen.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void toggleMinimize() {
        // Toggle window minimization (iconify)
        javafx.stage.Stage stage = (javafx.stage.Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(!stage.isIconified());
    }

    private void updateTimeLabel(long currentTime, long totalTime) {
        lblTime.setText(formatTime(currentTime) + " / " + formatTime(totalTime));
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String currentPath;
    private final VideoRepository videoRepository = new VideoRepository();

    public void playMedia(String path) {
        if (currentPath != null && currentPath.equals(path)) {
            togglePlayPause();
            return;
        }

        if (currentPath != null) {
            saveCurrentPosition();
        }
        
        // Find video in DB to get last position
        Video video = videoRepository.getAllNonVaulted().stream()
                .filter(v -> v.getPath().equals(path))
                .findFirst()
                .orElse(null);

        this.currentPath = path;
        
        // Clear previous previews
        previewsLoaded = false;
        previewThumbnails.clear();
        
        // Pre-load hover previews if they already exist
        String hash = Integer.toHexString(path.hashCode());
        String outputDir = System.getProperty("user.home") + "/.vplayer/previews/" + hash;
        java.io.File dir = new java.io.File(outputDir);
        if (dir.exists()) {
            java.io.File[] files = dir.listFiles((d, name) -> name.startsWith("img_") && name.endsWith(".jpg"));
            if (files != null && files.length >= NUM_PREVIEW_THUMBNAILS) {
                loadPreviewThumbnails(outputDir);
            }
        }
        
        // Show preview image
        String thumbnailDir = System.getProperty("user.home") + "/.vplayer/thumbnails";
        String thumbName = Integer.toHexString(path.hashCode()) + ".jpg";
        java.io.File thumbFile = new java.io.File(thumbnailDir, thumbName);
        if (thumbFile.exists()) {
            previewImage.setImage(new javafx.scene.image.Image(thumbFile.toURI().toString()));
            previewImage.setVisible(true);
        } else {
            previewImage.setVisible(false);
        }

        if (video != null && video.getLastPosition() > 0.01 && video.getLastPosition() < 0.95) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Resume Playback");
            alert.setHeaderText("Resume Video");
            alert.setContentText("Would you like to resume from where you left off?");
            
            ButtonType btnResume = new ButtonType("Resume");
            ButtonType btnStartOver = new ButtonType("Start Over");
            alert.getButtonTypes().setAll(btnResume, btnStartOver);

            java.util.Optional<ButtonType> result = alert.showAndWait();
            
            videoPlayer.play(path);
            if (result.isPresent() && result.get() == btnResume) {
                videoPlayer.getMediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                    @Override
                    public void playing(MediaPlayer mediaPlayer) {
                        mediaPlayer.controls().setPosition(video.getLastPosition());
                        mediaPlayer.events().removeMediaPlayerEventListener(this);
                    }
                });
            }
        } else {
            videoPlayer.play(path);
        }
    }

    private void saveCurrentPosition() {
        if (currentPath != null) {
            float pos = videoPlayer.getMediaPlayer().status().position();
            videoRepository.updatePosition(currentPath, pos);
        }
    }

    public void pauseMedia() {
        if (videoPlayer != null) {
            saveCurrentPosition();
            if (videoPlayer.getMediaPlayer().status().isPlaying()) {
                videoPlayer.pause();
            }
        }
    }

    public void shutdown() {
        if (videoPlayer != null) {
            saveCurrentPosition();
            videoPlayer.stop();
            videoPlayer.release();
        }
    }

    private void loadPreviewThumbnails(String dirPath) {
        java.io.File dir = new java.io.File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) return;
        
        java.io.File[] files = dir.listFiles((d, name) -> name.startsWith("img_") && name.endsWith(".jpg"));
        if (files == null || files.length == 0) return;
        
        java.util.Arrays.sort(files, java.util.Comparator.comparing(java.io.File::getName));
        
        previewThumbnails.clear();
        for (java.io.File file : files) {
            previewThumbnails.add(new javafx.scene.image.Image(file.toURI().toString()));
        }
        previewsLoaded = true;
        System.out.println("Loaded " + previewThumbnails.size() + " hover preview thumbnails");
    }

    private void handleSeekSliderHover(javafx.scene.input.MouseEvent e) {
        double mouseX = e.getX();
        double sliderWidth = seekSlider.getWidth();
        if (sliderWidth <= 0) return;
        
        double pct = mouseX / sliderWidth;
        pct = Math.max(0.0, Math.min(1.0, pct));
        
        hoverPreviewContainer.setVisible(true);
        
        long totalDuration = videoPlayer.getMediaPlayer().status().length();
        long hoveredTime = (long) (pct * totalDuration);
        lblHoverTime.setText(formatTime(hoveredTime));
        
        if (previewsLoaded && !previewThumbnails.isEmpty()) {
            int numImages = previewThumbnails.size();
            int idx = (int) Math.round(pct * (numImages - 1));
            idx = Math.max(0, Math.min(numImages - 1, idx));
            hoverPreviewImage.setImage(previewThumbnails.get(idx));
        } else {
            hoverPreviewImage.setImage(null);
        }
        
        double containerWidth = hoverPreviewContainer.getBoundsInLocal().getWidth();
        if (containerWidth <= 0) {
            containerWidth = 170; // fallback default
        }
        double posX = mouseX - (containerWidth / 2.0);
        posX = Math.max(0, Math.min(sliderWidth - containerWidth, posX));
        hoverPreviewContainer.setTranslateX(posX);
        hoverPreviewContainer.setTranslateY(-120); // Push it up above the slider track
    }

    private void handleSeekSliderExit(javafx.scene.input.MouseEvent e) {
        hoverPreviewContainer.setVisible(false);
    }
}
