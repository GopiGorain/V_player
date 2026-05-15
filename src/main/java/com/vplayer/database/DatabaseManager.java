package com.vplayer.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:vplayer.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            logger.error("Failed to reconnect to database", e);
        }
        return connection;
    }

    private void createTables() {
        String videosTable = "CREATE TABLE IF NOT EXISTS videos (" +
                "path TEXT PRIMARY KEY," +
                "title TEXT," +
                "duration INTEGER," +
                "last_position REAL DEFAULT 0," +
                "last_played_at INTEGER," +
                "is_vaulted INTEGER DEFAULT 0" +
                ");";

        String playlistsTable = "CREATE TABLE IF NOT EXISTS playlists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE" +
                ");";

        String playlistVideosTable = "CREATE TABLE IF NOT EXISTS playlist_videos (" +
                "playlist_id INTEGER," +
                "video_path TEXT," +
                "sort_order INTEGER," +
                "FOREIGN KEY(playlist_id) REFERENCES playlists(id)," +
                "FOREIGN KEY(video_path) REFERENCES videos(path)" +
                ");";

        String settingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(videosTable);
            stmt.execute(playlistsTable);
            stmt.execute(playlistVideosTable);
            stmt.execute(settingsTable);
            logger.info("Database tables created or verified.");
        } catch (SQLException e) {
            logger.error("Failed to create tables", e);
        }
    }
}
