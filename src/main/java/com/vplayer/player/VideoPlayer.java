package com.vplayer.player;

import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VideoPlayer {
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;
    private final ImageView videoSurface;

    public VideoPlayer() {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.videoSurface = new ImageView();
        this.videoSurface.setPreserveRatio(true);
        
        // Use custom JavaFX callback video surface with synchronous setup
        this.mediaPlayer.videoSurface().set(new JavaFXVideoSurface(this.videoSurface));
    }

    public void play(String path) {
        if (path == null) {
            mediaPlayer.controls().play();
            return;
        }
        
        System.out.println("VideoPlayer: Attempting to play " + path);
        mediaPlayer.media().play(path);
    }

    public void pause() {
        mediaPlayer.controls().pause();
    }

    public void stop() {
        mediaPlayer.controls().stop();
    }

    public void seek(float percentage) {
        mediaPlayer.controls().setPosition(percentage);
    }

    public void setRate(float rate) {
        mediaPlayer.controls().setRate(rate);
    }

    public void skip(long millis) {
        long currentTime = mediaPlayer.status().time();
        long length = mediaPlayer.status().length();
        if (currentTime != -1) {
            long newTime = currentTime + millis;
            if (newTime < 0) {
                newTime = 0;
            } else if (length > 0 && newTime >= length) {
                // If skipping past the end, skip to 100ms before the end so it naturally finishes
                newTime = length - 100;
            }
            mediaPlayer.controls().setTime(newTime);
        }
    }

    public ImageView getView() {
        return videoSurface;
    }

    public EmbeddedMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void release() {
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }
}
