package com.vplayer.services;

import com.vplayer.database.repository.SettingsRepository;
import com.vplayer.database.repository.VideoRepository;
import com.vplayer.models.Video;
import org.apache.commons.io.FileUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class VaultService {
    private static final Logger logger = LoggerFactory.getLogger(VaultService.class);
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.vplayer/vault";
    private static final String PIN_KEY = "vault_pin";
    private final VideoRepository videoRepository;
    private final SettingsRepository settingsRepository;
    private static VaultService instance;
    private boolean isUnlocked = false;

    private VaultService() {
        this.videoRepository = new VideoRepository();
        this.settingsRepository = new SettingsRepository();
        File dir = new File(VAULT_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public static VaultService getInstance() {
        if (instance == null) {
            instance = new VaultService();
        }
        return instance;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void lock() {
        this.isUnlocked = false;
    }

    public boolean isPinSet() {
        return settingsRepository.get(PIN_KEY) != null;
    }

    public void setPin(String pin) {
        String hashed = BCrypt.hashpw(pin, BCrypt.gensalt());
        settingsRepository.set(PIN_KEY, hashed);
    }

    public boolean unlock(String pin) {
        String savedHash = settingsRepository.get(PIN_KEY);
        if (savedHash != null && BCrypt.checkpw(pin, savedHash)) {
            isUnlocked = true;
            return true;
        }
        return false;
    }

    public boolean vaultVideo(Video video) {
        try {
            File source = new File(video.getPath());
            File dest = new File(VAULT_DIR, source.getName());
            
            FileUtils.moveFile(source, dest);
            
            video.setPath(dest.getAbsolutePath());
            video.setVaulted(true);
            videoRepository.upsert(video);
            
            return true;
        } catch (IOException e) {
            logger.error("Failed to vault video: " + video.getTitle(), e);
            return false;
        }
    }

    public boolean unvaultVideo(Video video, String destFolderPath) {
        try {
            File source = new File(video.getPath());
            File dest = new File(destFolderPath, source.getName());
            
            FileUtils.moveFile(source, dest);
            
            video.setPath(dest.getAbsolutePath());
            video.setVaulted(false);
            videoRepository.upsert(video);
            
            return true;
        } catch (IOException e) {
            logger.error("Failed to unvault video: " + video.getTitle(), e);
            return false;
        }
    }
}
