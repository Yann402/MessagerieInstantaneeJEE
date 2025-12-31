package com.messagerie.service;

import com.messagerie.dao.UserDAO;
import com.messagerie.dao.UserDAOImpl;
import com.messagerie.dao.LogDAO;
import com.messagerie.dao.LogDAOImpl;
import com.messagerie.model.User;
import com.messagerie.model.UserPermission;
import com.messagerie.model.Log;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private UserDAO userDAO;
    private LogDAO logDAO;
    
    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.logDAO = new LogDAOImpl();
    }
    
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    public List<User> getOnlineUsers() {
        return userDAO.getOnlineUsers();
    }
    
    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }
    
    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    public boolean changeUserStatus(int userId, String status) {
        boolean success = userDAO.updateStatus(userId, status);
        if (success) {
            User user = userDAO.findById(userId);
            if (user != null) {
                logDAO.save(new Log(userId, Log.TYPE_STATUS_CHANGE, 
                    user.getUsername() + " a changé son statut en: " + status));
            }
        }
        return success;
    }
    
    // Méthode simple pour bannir (sans motif, pour compatibilité)
    public boolean banUser(int userId) {
        return banUserWithReason(userId, "Aucun motif spécifié");
    }
    
    // Pour bannir avec motif
    public boolean banUserWithReason(int userId, String reason) {
        User user = userDAO.findById(userId);
        if (user == null) return false;
        
        // Valider le motif
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Aucun motif spécifié";
        }
        
        boolean success = userDAO.banUser(userId, reason);
        if (success) {
            logDAO.save(new Log(userId, Log.TYPE_BAN, 
                "Utilisateur " + user.getUsername() + " a été banni. Motif: " + reason));
        }
        return success;
    }
    
    // Pour bannir avec l'ID du modérateur/admin (sans motif, pour compatibilité)
    public boolean banUserWithModerator(int userId, int moderatorId) {
        return banUserWithModeratorAndReason(userId, moderatorId, "Aucun motif spécifié");
    }
    
    // Pour bannir avec modérateur/admin et motif
    public boolean banUserWithModeratorAndReason(int userId, int moderatorId, String reason) {
        User user = userDAO.findById(userId);
        User moderator = userDAO.findById(moderatorId);

        if (user == null || moderator == null) {
            return false;
        }

        // Vérifier les permissions
        UserPermission moderatorPerm = UserPermission.fromLevel(moderator.getPermission());
        if (!moderatorPerm.canBan()) {
            logDAO.save(new Log(moderatorId, Log.TYPE_BAN, 
                "Tentative de bannissement non autorisée sur " + user.getUsername()));
            return false;
        }

        // Empêcher un modérateur de bannir un admin
        // Un modérateur (niveau 2) ne peut bannir que les utilisateurs normaux (niveau 3)
        // Un admin (niveau 1) peut bannir tout le monde (sauf lui-même, mais on le gère dans l'interface)
        if (moderatorPerm == UserPermission.MODERATOR && user.getPermission() <= moderator.getPermission()) {
            logDAO.save(new Log(moderatorId, Log.TYPE_BAN, 
                "Tentative de bannissement d'un utilisateur de rang supérieur ou égal par " + moderator.getUsername()));
            return false;
        }

        // Valider le motif
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Aucun motif spécifié";
        }

        boolean success = userDAO.banUser(userId, reason);
        if (success) {
            logDAO.save(new Log(moderatorId, Log.TYPE_BAN, 
                moderator.getUsername() + " a banni " + user.getUsername() + ". Motif: " + reason));
        }
        return success;
    }
    
    // Pour débannir un utilisateur
    public boolean unbanUserWithModerator(int userId, int moderatorId) {
        User user = userDAO.findById(userId);
        User moderator = userDAO.findById(moderatorId);

        if (user == null || moderator == null) {
            return false;
        }

        // Vérifier les permissions
        UserPermission moderatorPerm = UserPermission.fromLevel(moderator.getPermission());
        if (!moderatorPerm.canBan()) {
            logDAO.save(new Log(moderatorId, Log.TYPE_BAN, 
                "Tentative de débannissement non autorisée sur " + user.getUsername()));
            return false;
        }

        boolean success = userDAO.unbanUser(userId);
        if (success) {
            // Effacer le motif de bannissement
            userDAO.updateBanReason(userId, null);
            
            logDAO.save(new Log(moderatorId, Log.TYPE_BAN, 
                moderator.getUsername() + " a débanni " + user.getUsername()));
        }
        return success;
    }

    // Méthode pour récupérer les utilisateurs bannis
    public List<User> getBannedUsers() {
        List<User> allUsers = userDAO.findAll();
        return allUsers.stream()
                       .filter(user -> "banned".equals(user.getStatus()))
                       .collect(Collectors.toList());
    }
    
    // ✅ Pour changer le type d'utilisateur - AVEC VÉRIFICATION
    public boolean changeUserType(int userId, int newPermission, int adminId) {
        User user = userDAO.findById(userId);
        User admin = userDAO.findById(adminId);
        
        if (user == null || admin == null) {
            return false;
        }
        
        // ✅ VÉRIFIER SI C'EST DÉJÀ LE MÊME TYPE
        if (user.getPermission() == newPermission) {
            UserPermission perm = UserPermission.fromLevel(newPermission);
            logDAO.save(new Log(adminId, Log.TYPE_USER_UPDATED, 
                "Tentative de changement : " + user.getUsername() + 
                " est déjà " + perm.getDescription()));
            return false; // Retourner false pour indiquer qu'aucun changement n'a été fait
        }
        
        // Vérifier que l'admin a bien le droit
        UserPermission adminPerm = UserPermission.fromLevel(admin.getPermission());
        if (!adminPerm.canChangeUserType()) {
            logDAO.save(new Log(adminId, Log.TYPE_USER_UPDATED, 
                "Tentative non autorisée de changement de type pour " + user.getUsername()));
            return false;
        }
        
        user.setPermission(newPermission);
        boolean success = userDAO.update(user);
        
        if (success) {
            logDAO.save(new Log(adminId, Log.TYPE_USER_UPDATED, 
                admin.getUsername() + " a changé le type de " + 
                user.getUsername() + " en " + UserPermission.fromLevel(newPermission).getDescription()));
        }
        
        return success;
    }
    
    public boolean hasPermission(int userId, String requiredPermission) {
        User user = userDAO.findById(userId);
        if (user == null) return false;
        
        UserPermission userPerm = UserPermission.fromLevel(user.getPermission());
        
        switch (requiredPermission) {
            case "BAN":
                return userPerm.canBan();
            case "CHANGE_USER_TYPE":
                return userPerm.canChangeUserType();
            default:
                return false;
        }
    }
    
    // Méthode pour vérifier si un utilisateur est banni
    public boolean isUserBanned(String username) {
        User user = userDAO.findByUsername(username);
        return user != null && "banned".equals(user.getStatus());
    }
    
    // Méthode pour récupérer le motif de bannissement
    public String getBanReason(int userId) {
        return userDAO.getBanReason(userId);
    }
    
    // Méthode pour récupérer le motif de bannissement par nom d'utilisateur
    public String getBanReasonByUsername(String username) {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            return userDAO.getBanReason(user.getId());
        }
        return null;
    }
}