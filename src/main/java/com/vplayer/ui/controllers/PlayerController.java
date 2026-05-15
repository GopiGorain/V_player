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

        setupListeners();
        setupSpeedMenu();
        
        hideControlsTransition = new PauseTransition(Duration.seconds(3));
        hideControlsTransition.setOnFinished(e -> {
            if (playerRoot.getScene() != null && playerRoot.getScene().getWindow() instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) playerRoot.getScene().getWindow();
                if (stage.isFullScreen()) {
                    controlsOverlay.setVisible(false);
                    playerRoot.setCursor(javafx.scene.Cursor.NONE);
                }
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

        seekSlider.setOnMousePressed(e -> isSeeking = true);
        seekSlider.setOnMouseReleased(e -> {
            videoPlayer.seek((float) (seekSlider.getValue() / 100.0));
            isSeeking = false;
        });

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
                Platform.runLater(() -> {
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
            }

            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
                System.out.println("MediaPlayer: Buffering " + newCache + "%");
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
}
