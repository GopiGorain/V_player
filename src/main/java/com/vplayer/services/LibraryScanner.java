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

    private final FFmpegService ffmpegService = new FFmpegService();
    private final String thumbnailDir = System.getProperty("user.home") + "/.vplayer/thumbnails";

    public LibraryScanner() {
        this.videoRepository = new VideoRepository();
        new File(thumbnailDir).mkdirs();
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
        Video video = new Video(file.getAbsolutePath(), file.getName(), 0);
        videoRepository.upsert(video);
        
        // Generate thumbnail
        String thumbName = Integer.toHexString(file.getAbsolutePath().hashCode()) + ".jpg";
        File thumbFile = new File(thumbnailDir, thumbName);
        if (!thumbFile.exists()) {
            ffmpegService.generateThumbnail(file.getAbsolutePath(), thumbFile.getAbsolutePath());
        }
        
        logger.debug("Found video: " + file.getName());
    }
}
