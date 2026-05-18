package com.vplayer.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFmpegService {
    private static final Logger logger = LoggerFactory.getLogger(FFmpegService.class);
    private String ffmpegPath = "ffmpeg"; // Assume in PATH

    public void createGif(String input, String output, String start, String duration, String resolution, int fps) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-ss"); command.add(start);
        command.add("-t"); command.add(duration);
        command.add("-i"); command.add(input);
        command.add("-vf"); command.add("fps=" + fps + ",scale=" + resolution + ":-1:flags=lanczos");
        command.add("-c:v"); command.add("gif");
        command.add(output);

        executeCommand(command);
    }

    public void generateThumbnail(String input, String output) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-ss"); command.add("00:00:01"); // Extract at 1 second mark
        command.add("-i"); command.add(input);
        command.add("-vframes"); command.add("1");
        command.add("-q:v"); command.add("2");
        command.add("-f"); command.add("image2");
        command.add(output);
        executeCommand(command);
    }

    public void generateHoverPreviews(String input, String outputDir, long durationMillis, int numThumbnails, Runnable onComplete) {
        java.io.File dir = new java.io.File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Verify if we already have the expected number of files
        boolean alreadyExists = true;
        for (int i = 1; i <= numThumbnails; i++) {
            java.io.File f = new java.io.File(dir, String.format("img_%03d.jpg", i));
            if (!f.exists()) {
                alreadyExists = false;
                break;
            }
        }

        if (alreadyExists) {
            logger.info("Hover preview thumbnails already exist in: " + outputDir);
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        logger.info("Generating hover preview thumbnails in: " + outputDir);
        double durationSeconds = durationMillis / 1000.0;
        if (durationSeconds <= 0) return;
        double fps = numThumbnails / durationSeconds;

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i"); command.add(input);
        command.add("-vf"); command.add("fps=" + fps + ",scale=160:90");
        command.add("-vsync"); command.add("vfr");
        command.add("-q:v"); command.add("5"); // Lower quality = faster generation
        command.add(new java.io.File(dir, "img_%03d.jpg").getAbsolutePath());

        executeCommand(command, onComplete);
    }

    private void executeCommand(List<String> command) {
        executeCommand(command, null);
    }

    private void executeCommand(List<String> command, Runnable onComplete) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug(line);
                    }
                }
                
                int exitCode = process.waitFor();
                logger.info("FFmpeg process exited with code " + exitCode);
                if (exitCode == 0 && onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                logger.error("FFmpeg execution failed", e);
            }
        }).start();
    }
}
