package com.vplayer.database.repository;

import com.vplayer.database.DatabaseManager;
import com.vplayer.models.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoRepository {
    private final Connection connection;

    public VideoRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void upsert(Video video) {
        String sql = "INSERT INTO videos(path, title, duration, last_position, last_played_at, is_vaulted) " +
                "VALUES(?,?,?,?,?,?) ON CONFLICT(path) DO UPDATE SET " +
                "last_position=excluded.last_position, last_played_at=excluded.last_played_at, is_vaulted=excluded.is_vaulted";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, video.getPath());
            pstmt.setString(2, video.getTitle());
            pstmt.setLong(3, video.getDuration());
            pstmt.setFloat(4, video.getLastPosition());
            pstmt.setLong(5, video.getLastPlayedAt());
            pstmt.setInt(6, video.isVaulted() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Video> getAllNonVaulted() {
        return getVideosByVaultStatus(false);
    }

    public List<Video> getAllVaulted() {
        return getVideosByVaultStatus(true);
    }

    private List<Video> getVideosByVaultStatus(boolean vaulted) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos WHERE is_vaulted = ? ORDER BY last_played_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, vaulted ? 1 : 0);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Video v = new Video(rs.getString("path"), rs.getString("title"), rs.getLong("duration"));
                    v.setLastPosition(rs.getFloat("last_position"));
                    v.setLastPlayedAt(rs.getLong("last_played_at"));
                    v.setVaulted(rs.getInt("is_vaulted") == 1);
                    videos.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return videos;
    }

    public void updatePosition(String path, float position) {
        String sql = "UPDATE videos SET last_position = ?, last_played_at = ? WHERE path = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setFloat(1, position);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, path);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
