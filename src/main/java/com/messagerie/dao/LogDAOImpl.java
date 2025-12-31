package com.messagerie.dao;

import com.messagerie.model.Log;
import com.messagerie.util.DatabaseConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAOImpl implements LogDAO {

    @Override
    public Log findById(int id) {
        String sql = "SELECT l.*, u.username FROM log l " +
                     "LEFT JOIN user u ON l.user_id = u.id " +
                     "WHERE l.id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLog(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Log> findAll() {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                     "LEFT JOIN user u ON l.user_id = u.id " +
                     "ORDER BY l.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public List<Log> findByUserId(int userId) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                     "LEFT JOIN user u ON l.user_id = u.id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public List<Log> findByType(String type) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                     "LEFT JOIN user u ON l.user_id = u.id " +
                     "WHERE l.type = ? " +
                     "ORDER BY l.timestamp DESC";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public List<Log> findRecent(int limit) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                     "LEFT JOIN user u ON l.user_id = u.id " +
                     "ORDER BY l.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public boolean save(Log log) {
        String sql = "INSERT INTO log (user_id, type, description) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (log.getUserId() != null) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, log.getType());
            stmt.setString(3, log.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setId(generatedKeys.getInt(1));
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
    public boolean update(Log log) {
        String sql = "UPDATE log SET user_id = ?, type = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (log.getUserId() != null) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, log.getType());
            stmt.setString(3, log.getDescription());
            stmt.setInt(4, log.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM log WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Log mapResultSetToLog(ResultSet rs) throws SQLException {
        Log log = new Log();
        log.setId(rs.getInt("id"));
        
        // Récupérer userId en tant qu'objet Integer (peut être null)
        Integer userId = (Integer) rs.getObject("user_id");
        log.setUserId(userId);
        
        log.setUsername(rs.getString("username"));
        
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            log.setTimestamp(timestamp.toLocalDateTime());
        }
        
        log.setType(rs.getString("type"));
        log.setDescription(rs.getString("description"));
        return log;
    }
}