package com.vplayer.services;

import com.vplayer.database.repository.VideoRepository;
import com.vplayer.models.Video;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LibraryScanner {
    private static final Logger logger = LoggerFactory.getLogger(LibraryScanner.class);
    private final VideoRepository videoRepository;
    private final Set<String> videoExtensions = new HashSet<>(Arrays.asList(
            "mp4", "mkv", "avi", "mov", "webm", "flv", "hevc", "av1", "mpeg"
    ));

    public LibraryScanner() {
        this.videoRepository = new VideoRepository();
    }

    public void scanFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return;

        logger.info("Scanning folder: " + folder.getAbsolutePath());
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanFolder(file);
            } else {
                String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
                if (videoExtensions.contains(ext)) {
                    processVideoFile(file);
                }
            }
        }
    }

    private void processVideoFile(File file) {
        // Here we could use VLCJ to get the duration, but for now we'll just save the path
        Video video = new Video(file.getAbsolutePath(), file.getName(), 0);
        videoRepository.upsert(video);
        logger.debug("Found video: " + file.getName());
    }
}
