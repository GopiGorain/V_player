package com.vplayer.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewService {
    private static final Logger logger = LoggerFactory.getLogger(ViewService.class);
    private static ViewService instance;
    private final Map<String, Parent> viewCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();

    private ViewService() {}

    public static ViewService getInstance() {
        if (instance == null) {
            instance = new ViewService();
        }
        return instance;
    }

    public Parent loadView(String fxmlPath) {
        if (viewCache.containsKey(fxmlPath)) {
            return viewCache.get(fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            viewCache.put(fxmlPath, root);
            controllerCache.put(fxmlPath, loader.getController());
            return root;
        } catch (IOException e) {
            logger.error("Failed to load FXML: " + fxmlPath, e);
            return null;
        }
    }

    public <T> T getController(String fxmlPath) {
        return (T) controllerCache.get(fxmlPath);
    }
}
