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

    private void executeCommand(List<String> command) {
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
            } catch (Exception e) {
                logger.error("FFmpeg execution failed", e);
            }
        }).start();
    }
}
