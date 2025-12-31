package com.messagerie.util;

import com.messagerie.model.User;
import com.messagerie.service.AuthenticationService;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Listener pour gérer la création et destruction des sessions HTTP
 * Permet de déconnecter automatiquement les utilisateurs après timeout
 */
@WebListener
public class SessionListener implements HttpSessionListener {
    
    private AuthenticationService authService = new AuthenticationService();
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        
        // Définir le timeout de session à 30 minutes (1800 secondes)
        session.setMaxInactiveInterval(1800);
        
        System.out.println("✓ Session créée : " + session.getId() + 
                         " (Timeout: " + session.getMaxInactiveInterval() + "s)");
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        
        System.out.println("🔴 Session détruite : " + session.getId());
        
        // Récupérer l'utilisateur de la session
        User user = (User) session.getAttribute("user");
        
        if (user != null) {
            System.out.println("👤 Déconnexion automatique de : " + user.getUsername() + 
                             " (ID: " + user.getId() + ")");
            
            // Déconnecter l'utilisateur dans la base de données
            authService.logout(user.getId());
            
            // Note: Le WebSocket sera fermé automatiquement car la session HTTP est détruite
        }
    }
}