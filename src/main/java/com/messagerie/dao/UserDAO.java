package com.messagerie.dao;

import com.messagerie.model.User;
import java.util.List;

public interface UserDAO {
    User findById(int id);
    User findByUsername(String username);
    List<User> findAll();
    boolean save(User user);
    boolean update(User user);
    boolean delete(int id);
    boolean updateStatus(int userId, String status);
    boolean updateLastConnectionTime(int userId);
    List<User> getOnlineUsers();
    boolean unbanUser(int userId); // AJOUTÉ
    boolean isUserBanned(String username);
    boolean banUser(int userId, String reason);
    boolean updateBanReason(int userId, String reason);
    String getBanReason(int userId);
}