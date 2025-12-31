package com.messagerie.servlet;

import com.messagerie.service.MessageService;
import com.messagerie.service.UserService;
import com.messagerie.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.time.format.DateTimeFormatter;

@WebServlet("/chat/updates")
public class ChatUpdateServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private MessageService messageService;
    private UserService userService;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public void init() throws ServletException {
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!SessionUtil.isUserLoggedIn(request.getSession(false))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Récupérer le dernier message ID connu (pour éviter de tout renvoyer)
        String lastMessageIdParam = request.getParameter("lastMessageId");
        int lastMessageId = 0;
        if (lastMessageIdParam != null && !lastMessageIdParam.isEmpty()) {
            try {
                lastMessageId = Integer.parseInt(lastMessageIdParam);
            } catch (NumberFormatException e) {
                // Garder 0 par défaut
            }
        }
        
        // Récupérer le timestamp de la dernière mise à jour
        String lastUpdateParam = request.getParameter("lastUpdate");
        long lastUpdateTime = 0;
        if (lastUpdateParam != null && !lastUpdateParam.isEmpty()) {
            try {
                lastUpdateTime = Long.parseLong(lastUpdateParam);
            } catch (NumberFormatException e) {
                // Garder 0 par défaut
            }
        }
        
        // Construire la réponse JSON
        JSONObject responseJson = new JSONObject();
        
        try {
            // 1. Nouveaux messages (après le dernier ID connu)
            JSONArray newMessages = new JSONArray();
            var allMessages = messageService.getRecentMessages(100); // Récupérer les 100 derniers
            
            for (var message : allMessages) {
                if (message.getId() > lastMessageId) {
                    JSONObject msgJson = new JSONObject();
                    msgJson.put("id", message.getId());
                    msgJson.put("username", message.getUsername());
                    msgJson.put("content", message.getContent());
                    if (message.getTimestamp() != null) {
                        msgJson.put("timestamp", message.getTimestamp().format(timeFormatter));
                    }
                    newMessages.put(msgJson);
                }
            }
            responseJson.put("newMessages", newMessages);
            
            // 2. Nouveaux messages ID max pour la prochaine requête
            int maxMessageId = allMessages.stream()
                .mapToInt(m -> m.getId())
                .max()
                .orElse(0);
            responseJson.put("lastMessageId", maxMessageId);
            
            // 3. Mise à jour des utilisateurs (seulement si nécessaire, toutes les 10s)
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > 10000) { // 10 secondes
                JSONArray onlineUsers = new JSONArray();
                JSONArray allUsers = new JSONArray();
                
                for (var user : userService.getOnlineUsers()) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("id", user.getId());
                    userJson.put("username", user.getUsername());
                    userJson.put("status", user.getStatus());
                    userJson.put("permission", user.getPermission());
                    onlineUsers.put(userJson);
                }
                
                for (var user : userService.getAllUsers()) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("id", user.getId());
                    userJson.put("username", user.getUsername());
                    userJson.put("status", user.getStatus());
                    userJson.put("permission", user.getPermission());
                    allUsers.put(userJson);
                }
                
                responseJson.put("onlineUsers", onlineUsers);
                responseJson.put("allUsers", allUsers);
                responseJson.put("usersUpdated", true);
                responseJson.put("lastUpdate", currentTime);
            } else {
                responseJson.put("usersUpdated", false);
            }
            
            // 4. Statut de l'utilisateur courant
            var currentUser = SessionUtil.getUserFromSession(request.getSession(false));
            if (currentUser != null) {
                responseJson.put("currentUser", currentUser.getUsername());
                responseJson.put("currentStatus", currentUser.getStatus());
            }
            
            responseJson.put("success", true);
            responseJson.put("timestamp", currentTime);
            
        } catch (Exception e) {
            responseJson.put("success", false);
            responseJson.put("error", e.getMessage());
        }
        
        // Envoyer la réponse
        PrintWriter out = response.getWriter();
        out.print(responseJson.toString());
        out.flush();
    }
}