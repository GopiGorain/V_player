package com.vplayer.models;

public class Video {
    private String path;
    private String title;
    private long duration;
    private float lastPosition;
    private long lastPlayedAt;
    private boolean isVaulted;

    public Video(String path, String title, long duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    // Getters and Setters
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public float getLastPosition() { return lastPosition; }
    public void setLastPosition(float lastPosition) { this.lastPosition = lastPosition; }

    public long getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(long lastPlayedAt) { this.lastPlayedAt = lastPlayedAt; }

    public boolean isVaulted() { return isVaulted; }
    public void setVaulted(boolean vaulted) { isVaulted = vaulted; }
}
