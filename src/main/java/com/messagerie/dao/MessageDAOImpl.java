package com.messagerie.dao;

import com.messagerie.model.Message;
import com.messagerie.util.DatabaseConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAOImpl implements MessageDAO {

    @Override
    public Message findById(int id) {
        String sql = "SELECT m.*, u.username FROM message m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "WHERE m.id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM message m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "ORDER BY m.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<Message> findByUserId(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM message m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "WHERE m.user_id = ? " +
                     "ORDER BY m.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<Message> findRecent(int limit) {
        return findLastMessages(limit);
    }

    @Override
    public List<Message> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM message m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "WHERE m.timestamp BETWEEN ? AND ? " +
                     "ORDER BY m.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<Message> findLastMessages(int count) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM message m " +
                     "JOIN user u ON m.user_id = u.id " +
                     "ORDER BY m.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, count);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public boolean save(Message message) {
        String sql = "INSERT INTO message (user_id, content) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, message.getUserId());
            stmt.setString(2, message.getContent());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getInt(1));
                        
                        // Récupérer le timestamp généré par la base
                        String selectSql = "SELECT timestamp FROM message WHERE id = ?";
                        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                            selectStmt.setInt(1, message.getId());
                            ResultSet rs = selectStmt.executeQuery();
                            if (rs.next()) {
                                Timestamp timestamp = rs.getTimestamp("timestamp");
                                if (timestamp != null) {
                                    message.setTimestamp(timestamp.toLocalDateTime());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Message message) {
        String sql = "UPDATE message SET content = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, message.getContent());
            stmt.setInt(2, message.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM message WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setUserId(rs.getInt("user_id"));
        message.setUsername(rs.getString("username"));
        
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            message.setTimestamp(timestamp.toLocalDateTime());
        }
        
        message.setContent(rs.getString("content"));
        return message;
    }
}