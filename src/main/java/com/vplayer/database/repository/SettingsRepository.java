package com.vplayer.database.repository;

import com.vplayer.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsRepository {
    private static final Logger logger = LoggerFactory.getLogger(SettingsRepository.class);
    private final Connection connection;

    public SettingsRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void set(String key, String value) {
        String sql = "INSERT INTO settings(key, value) VALUES(?, ?) " +
                     "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to set setting: " + key, e);
        }
    }

    public String get(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get setting: " + key, e);
        }
        return null;
    }
}
