package com.messagerie.websocket;

import com.messagerie.model.User;
import com.messagerie.model.Message;
import com.messagerie.service.MessageService;
import com.messagerie.service.UserService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@ServerEndpoint(value = "/chat-websocket", configurator = HttpSessionConfigurator.class)
public class ChatWebSocketEndpoint {
    
    // Sessions WebSocket actives (thread-safe)
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    
    // Services (instance pour les méthodes non-statiques)
    private MessageService messageService = new MessageService();
    private UserService userService = new UserService();
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // Récupérer l'utilisateur depuis la session HTTP
        User user = (User) config.getUserProperties().get("user");
        
        if (user == null) {
            System.err.println("❌ Tentative de connexion WebSocket sans utilisateur authentifié");
            try {
                session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, 
                    "Non authentifié"
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        // Stocker l'utilisateur dans les propriétés de la session WebSocket
        session.getUserProperties().put("user", user);
        sessions.add(session);
        
        System.out.println("✅ WebSocket connecté : " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("📊 Nombre total de sessions WebSocket : " + sessions.size());
        
        // Mettre à jour le statut en "online" dans la DB
        userService.changeUserStatus(user.getId(), "online");
        
        // IMPORTANT: Notifier TOUS les clients de la connexion
        broadcastUserListUpdateInstance();
        broadcastSystemMessageInstance(user.getUsername() + " vient de se connecter");
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        User user = (User) session.getUserProperties().get("user");
        if (user == null) {
            System.err.println("❌ Message reçu sans utilisateur authentifié");
            return;
        }
        
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            System.out.println("📨 Message reçu - Type: " + type + " de " + user.getUsername());
            
            switch (type) {
                case "message":
                    handleChatMessage(json, user);
                    break;
                case "status":
                    handleStatusChange(json, user);
                    break;
                case "ban":
                    handleBanUser(json, user);
                    break;
                case "changePermission":
                    handleChangePermission(json, user);
                    break;
                default:
                    System.out.println("⚠️ Type de message inconnu: " + type);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement du message");
            e.printStackTrace();
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        User user = (User) session.getUserProperties().get("user");
        sessions.remove(session);
        
        if (user != null) {
            System.out.println("✅ WebSocket déconnecté : " + user.getUsername() + 
                             " - Raison: " + reason.getReasonPhrase());
            System.out.println("📊 Nombre total de sessions WebSocket : " + sessions.size());
            
            // Mettre à jour le statut en "offline" dans la DB
            userService.changeUserStatus(user.getId(), "offline");
            
            // IMPORTANT: Notifier TOUS les clients de la déconnexion
            broadcastUserListUpdateInstance();
            broadcastSystemMessageInstance(user.getUsername() + " s'est déconnecté");
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        User user = (User) session.getUserProperties().get("user");
        String username = (user != null) ? user.getUsername() : "Inconnu";
        
        System.err.println("❌ Erreur WebSocket pour " + username + ": " + error.getMessage());
        error.printStackTrace();
    }
    
    // ========== HANDLERS ==========
    
    private void handleChatMessage(JSONObject json, User user) {
        String content = json.getString("content");
        
        if (content == null || content.trim().isEmpty()) {
            System.out.println("⚠️ Message vide ignoré");
            return;
        }
        
        System.out.println("💬 Nouveau message de " + user.getUsername() + ": " + content);
        
        // Sauvegarder en base de données
        boolean saved = messageService.sendMessage(user.getId(), content.trim());
        
        if (saved) {
            // Récupérer le message avec son ID et timestamp
            List<Message> recentMessages = messageService.getRecentMessages(1);
            if (!recentMessages.isEmpty()) {
                Message savedMessage = recentMessages.get(0);
                
                // Créer la réponse JSON
                JSONObject response = new JSONObject();
                response.put("type", "message");
                response.put("id", savedMessage.getId());
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("content", content.trim());
                response.put("timestamp", savedMessage.getFormattedTime());
                response.put("permission", user.getPermission());
                
                // Broadcaster à tous les clients
                broadcast(response.toString());
                System.out.println("✅ Message diffusé à " + sessions.size() + " clients");
            }
        } else {
            System.err.println("❌ Échec de sauvegarde du message");
        }
    }
    
    private void handleStatusChange(JSONObject json, User user) {
        String newStatus = json.getString("status");
        
        System.out.println("🔄 Changement de statut de " + user.getUsername() + " vers " + newStatus);
        
        if (userService.changeUserStatus(user.getId(), newStatus)) {
            user.setStatus(newStatus);
            
            // IMPORTANT: Notifier tous les clients du changement de statut
            broadcastUserListUpdateInstance();
            
            // Message système pour le changement de statut
            String statusText = getStatusText(newStatus);
            broadcastSystemMessageInstance(user.getUsername() + " est maintenant " + statusText);
        }
    }
    
    private void handleBanUser(JSONObject json, User moderator) {
        int targetUserId = json.getInt("targetUserId");
        String reason = json.optString("reason", "Aucun motif spécifié");
        
        System.out.println("🚫 Bannissement de l'utilisateur ID " + targetUserId + 
                         " par " + moderator.getUsername());
        
        if (userService.banUserWithModeratorAndReason(targetUserId, moderator.getId(), reason)) {
            User target = userService.getUserById(targetUserId);
            
            if (target != null) {
                // Déconnecter l'utilisateur banni
                disconnectUser(targetUserId, "Vous avez été banni. Raison: " + reason);
                
                // Notifier tous les clients
                broadcastSystemMessageInstance(target.getUsername() + " a été banni par " + moderator.getUsername());
                broadcastUserListUpdateInstance();
            }
        }
    }
    
    private void handleChangePermission(JSONObject json, User admin) {
        int targetUserId = json.getInt("targetUserId");
        int newPermission = json.getInt("permission");
        
        System.out.println("🔑 Changement de permission pour l'utilisateur ID " + targetUserId);
        
        if (userService.changeUserType(targetUserId, newPermission, admin.getId())) {
            User target = userService.getUserById(targetUserId);
            
            if (target != null) {
                broadcastSystemMessageInstance(target.getUsername() + " est maintenant " + 
                                     getPermissionText(newPermission));
                broadcastUserListUpdateInstance();
            }
        }
    }
    
    // ========== BROADCAST (méthodes d'instance) ==========
    
    private void broadcast(String message) {
        broadcastStatic(message);
    }
    
    private void broadcastUserListUpdateInstance() {
        broadcastUserListUpdateStatic();
    }
    
    private void broadcastSystemMessageInstance(String message) {
        broadcastSystemMessageStatic(message);
    }
    
    // ========== BROADCAST STATIQUE (appelable depuis l'extérieur) ==========
    
    /**
     * ✅ MÉTHODE STATIQUE pour broadcaster un message
     * Peut être appelée depuis AdminServlet
     */
    private static void broadcastStatic(String message) {
        synchronized (sessions) {
            int successCount = 0;
            int failCount = 0;
            
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                        successCount++;
                    } catch (IOException e) {
                        failCount++;
                        System.err.println("❌ Erreur d'envoi à une session: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("📡 Broadcast: " + successCount + " réussis, " + failCount + " échecs");
        }
    }
    
    /**
     * ✅ MÉTHODE STATIQUE pour mettre à jour la liste des utilisateurs
     * Peut être appelée depuis AdminServlet
     */
    public static void broadcastUserListUpdateStatic() {
        UserService userService = new UserService();
        
        JSONObject response = new JSONObject();
        response.put("type", "userListUpdate");
        
        JSONArray usersArray = new JSONArray();
        List<User> allUsers = userService.getAllUsers();
        
        for (User u : allUsers) {
            JSONObject userObj = new JSONObject();
            userObj.put("id", u.getId());
            userObj.put("username", u.getUsername());
            userObj.put("status", u.getStatus());
            userObj.put("permission", u.getPermission());
            usersArray.put(userObj);
        }
        
        response.put("users", usersArray);
        
        System.out.println("👥 Mise à jour de la liste utilisateurs (" + allUsers.size() + " utilisateurs) envoyée à " + sessions.size() + " clients");
        broadcastStatic(response.toString());
    }
    
    /**
     * ✅ MÉTHODE STATIQUE pour envoyer un message système
     * Peut être appelée depuis AdminServlet
     */
    public static void broadcastSystemMessageStatic(String message) {
        JSONObject response = new JSONObject();
        response.put("type", "system");
        response.put("message", message);
        
        System.out.println("📢 Message système: " + message + " envoyé à " + sessions.size() + " clients");
        broadcastStatic(response.toString());
    }
    
    private void disconnectUser(int userId, String reason) {
        synchronized (sessions) {
            for (Session session : sessions) {
                User user = (User) session.getUserProperties().get("user");
                if (user != null && user.getId() == userId) {
                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("type", "disconnect");
                        msg.put("reason", reason);
                        
                        session.getBasicRemote().sendText(msg.toString());
                        session.close(new CloseReason(
                            CloseReason.CloseCodes.NORMAL_CLOSURE, 
                            "Banni"
                        ));
                        
                        System.out.println("🚫 Utilisateur ID " + userId + " déconnecté");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    // ========== UTILS ==========
    
    private String getStatusText(String status) {
        switch (status) {
            case "online": return "en ligne";
            case "away": return "absent";
            case "offline": return "hors ligne";
            default: return status;
        }
    }
    
    private String getPermissionText(int permission) {
        switch (permission) {
            case 1: return "Administrateur";
            case 2: return "Modérateur";
            case 3: return "Utilisateur";
            default: return "Inconnu";
        }
    }
    
    public static int getActiveSessionsCount() {
        return sessions.size();
    }
}