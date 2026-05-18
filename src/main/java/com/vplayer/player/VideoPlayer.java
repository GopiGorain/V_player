package com.vplayer.player;

import com.vplayer.services.ScreenSleepService;
import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
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

        setupPowerManagement();
    }

    private void setupPowerManagement() {
        this.mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                ScreenSleepService.preventSleep();
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                ScreenSleepService.allowSleep();
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                ScreenSleepService.allowSleep();
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                ScreenSleepService.allowSleep();
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                ScreenSleepService.allowSleep();
            }
        });
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
        ScreenSleepService.allowSleep();
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }
}
