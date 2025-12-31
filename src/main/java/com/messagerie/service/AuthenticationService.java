package com.messagerie.service;

import com.messagerie.dao.UserDAO;
import com.messagerie.dao.UserDAOImpl;
import com.messagerie.dao.LogDAO;
import com.messagerie.dao.LogDAOImpl;
import com.messagerie.model.User;
import com.messagerie.model.Log;
import com.messagerie.util.PasswordUtil;
import java.time.LocalDateTime;

public class AuthenticationService {
    private UserDAO userDAO;
    private LogDAO logDAO;
    
    public AuthenticationService() {
        this.userDAO = new UserDAOImpl();
        this.logDAO = new LogDAOImpl();
    }
    
    /**
     * Authentifie un utilisateur
     */
    public User authenticate(String username, String password) {
        User user = userDAO.findByUsername(username);
        
        if (user != null) {
            // Vérifier si l'utilisateur est banni
            if ("banned".equals(user.getStatus())) {
                // Récupérer le motif du bannissement
                String banReason = userDAO.getBanReason(user.getId());
                String reasonMessage = (banReason != null && !banReason.isEmpty()) ? 
                    " Motif : " + banReason : " (Aucun motif spécifié)";
                
                // CORRECTION : Utiliser le constructeur correct
                logDAO.save(new Log(
                    Log.TYPE_LOGIN, 
                    "Tentative de connexion refusée - Utilisateur banni: " + username + reasonMessage
                ));
                return null;
            }
            
            // Vérifier le mot de passe
            if (PasswordUtil.checkPassword(password, user.getPassword())) {
                // Mettre à jour le statut et la dernière connexion
                userDAO.updateStatus(user.getId(), "online");
                userDAO.updateLastConnectionTime(user.getId());
                
                // Mettre à jour l'objet user retourné
                user.setStatus("online");
                user.setLastConnectionTime(LocalDateTime.now());
                
                // Logger la connexion
                logDAO.save(new Log(user.getId(), Log.TYPE_LOGIN, 
                    "Connexion réussie de " + username));
                
                return user;
            }
        }
        
        // Logger l'échec
        logDAO.save(new Log(Log.TYPE_LOGIN, 
            "Échec de connexion pour l'utilisateur: " + username));
        
        return null;
    }
    
    /**
     * Déconnecte un utilisateur
     */
    public void logout(int userId) {
        userDAO.updateStatus(userId, "offline");
        userDAO.updateLastConnectionTime(userId);
        
        // Logger la déconnexion
        logDAO.save(new Log(userId, Log.TYPE_LOGOUT, "Déconnexion"));
    }
    
    /**
     * Vérifie si un utilisateur existe
     */
    public boolean userExists(String username) {
        return userDAO.findByUsername(username) != null;
    }
    
    /**
     * Récupère un utilisateur par son nom d'utilisateur
     */
    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
}