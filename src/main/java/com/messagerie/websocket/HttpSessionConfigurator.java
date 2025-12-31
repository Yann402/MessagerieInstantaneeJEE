package com.messagerie.websocket;

import com.messagerie.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Configurateur pour passer la session HTTP au WebSocket
 * CRITIQUE : Sans ce fichier, le WebSocket ne peut pas accéder à l'utilisateur connecté
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        
        System.out.println("🔧 Configuration du handshake WebSocket...");
        
        // Récupérer la session HTTP
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        
        if (httpSession != null) {
            System.out.println("✓ Session HTTP trouvée : " + httpSession.getId());
            
            // Récupérer l'utilisateur de la session
            User user = (User) httpSession.getAttribute("user");
            
            if (user != null) {
                System.out.println("✓ Utilisateur récupéré : " + user.getUsername() + " (ID: " + user.getId() + ")");
                // Passer l'utilisateur au WebSocket
                config.getUserProperties().put("user", user);
            } else {
                System.err.println("❌ Aucun utilisateur dans la session HTTP");
            }
        } else {
            System.err.println("❌ Aucune session HTTP trouvée");
        }
    }
}