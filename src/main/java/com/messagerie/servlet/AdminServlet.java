package com.messagerie.servlet;

import com.messagerie.service.UserService;
import com.messagerie.util.SessionUtil;
import com.messagerie.model.User;
import com.messagerie.websocket.ChatWebSocketEndpoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;

@WebServlet("/admin")
@MultipartConfig
public class AdminServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!SessionUtil.isUserLoggedIn(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        request.setAttribute("allUsers", userService.getAllUsers());
        request.getRequestDispatcher("/admin-form.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // DÉTECTION : Requête AJAX ou formulaire normal ?
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);
        
        System.out.println("========== AdminServlet.doPost ==========");
        System.out.println("X-Requested-With header: " + ajaxHeader);
        System.out.println("Is AJAX: " + isAjax);
        System.out.println("Content-Type: " + request.getContentType());
        
        if (!SessionUtil.isUserLoggedIn(request.getSession(false))) {
            System.out.println("❌ Session expirée !");
            if (isAjax) {
                sendJsonResponse(response, false, "Session expirée", null);
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }
        
        User currentUser = SessionUtil.getUserFromSession(request.getSession(false));
        String action = request.getParameter("action");
        
        System.out.println("👤 Utilisateur: " + currentUser.getUsername());
        System.out.println("🎯 Action demandée: " + action);
        
        String message = "";
        boolean success = false;
        
        try {
            if ("ban".equals(action)) {
                String targetUserIdStr = request.getParameter("targetUserId");
                String reason = request.getParameter("reason");
                
                System.out.println("🚫 Bannissement - Target ID: " + targetUserIdStr + ", Reason: " + reason);
                
                if (targetUserIdStr != null && !targetUserIdStr.isEmpty()) {
                    int targetUserId = Integer.parseInt(targetUserIdStr);
                    User targetUser = userService.getUserById(targetUserId);
                    
                    if (reason == null || reason.trim().isEmpty()) {
                        reason = "Aucun motif spécifié";
                    }
                    
                    success = userService.banUserWithModeratorAndReason(
                        targetUserId, currentUser.getId(), reason.trim());
                    
                    System.out.println("✅ Résultat bannissement: " + success);
                    
                    if (success && targetUser != null) {
                        message = "Utilisateur " + targetUser.getUsername() + " banni avec succès";
                        
                        // DÉCLENCHER LE WEBSOCKET
                        System.out.println("📡 Déclenchement du broadcast WebSocket...");
                        ChatWebSocketEndpoint.broadcastSystemMessageStatic(
                            targetUser.getUsername() + " a été banni par " + currentUser.getUsername()
                        );
                        ChatWebSocketEndpoint.broadcastUserListUpdateStatic();
                    } else {
                        message = "Échec du bannissement";
                    }
                }
            } 
            else if ("unban".equals(action)) {
                String targetUserIdStr = request.getParameter("targetUserId");
                
                System.out.println("✅ Débannissement - Target ID: " + targetUserIdStr);
                
                if (targetUserIdStr != null && !targetUserIdStr.isEmpty()) {
                    int targetUserId = Integer.parseInt(targetUserIdStr);
                    User targetUser = userService.getUserById(targetUserId);
                    
                    success = userService.unbanUserWithModerator(targetUserId, currentUser.getId());
                    
                    System.out.println("✅ Résultat débannissement: " + success);
                    
                    if (success && targetUser != null) {
                        message = "Utilisateur " + targetUser.getUsername() + " débanni avec succès";
                        
                        // DÉCLENCHER LE WEBSOCKET
                        System.out.println("📡 Déclenchement du broadcast WebSocket...");
                        ChatWebSocketEndpoint.broadcastSystemMessageStatic(
                            targetUser.getUsername() + " a été débanni par " + currentUser.getUsername()
                        );
                        ChatWebSocketEndpoint.broadcastUserListUpdateStatic();
                    } else {
                        message = "Échec du débannissement";
                    }
                }
            } 
            else if ("changeType".equals(action)) {
                String targetUserIdStr = request.getParameter("targetUserId");
                String newPermissionStr = request.getParameter("newPermission");
                
                System.out.println("🔄 Changement type - Target ID: " + targetUserIdStr + ", New Permission: " + newPermissionStr);
                
                if (targetUserIdStr != null && newPermissionStr != null) {
                    int targetUserId = Integer.parseInt(targetUserIdStr);
                    int newPermission = Integer.parseInt(newPermissionStr);
                    User targetUser = userService.getUserById(targetUserId);
                    
                    if (targetUser == null) {
                        message = "Utilisateur introuvable";
                        success = false;
                    }
                    // ✅ VÉRIFIER SI C'EST DÉJÀ LE MÊME TYPE
                    else if (targetUser.getPermission() == newPermission) {
                        String permName = getPermissionName(newPermission);
                        message = targetUser.getUsername() + " est déjà " + permName;
                        success = false;
                        System.out.println("⚠️ L'utilisateur a déjà ce type");
                    } else {
                        success = userService.changeUserType(targetUserId, newPermission, currentUser.getId());
                        
                        System.out.println("✅ Résultat changement type: " + success);
                        
                        if (success) {
                            String permName = getPermissionName(newPermission);
                            message = "Type de " + targetUser.getUsername() + " changé en " + permName;
                            
                            // DÉCLENCHER LE WEBSOCKET
                            System.out.println("📡 Déclenchement du broadcast WebSocket...");
                            ChatWebSocketEndpoint.broadcastSystemMessageStatic(
                                targetUser.getUsername() + " est maintenant " + permName
                            );
                            ChatWebSocketEndpoint.broadcastUserListUpdateStatic();
                        } else {
                            message = "Échec du changement de type";
                        }
                    }
                }
            } else {
                System.out.println("⚠️ Action inconnue ou null: " + action);
                message = "Action inconnue";
            }
            
        } catch (NumberFormatException e) {
            success = false;
            message = "Erreur: ID utilisateur invalide";
            System.err.println("❌ NumberFormatException dans AdminServlet: " + e.getMessage());
        } catch (Exception e) {
            success = false;
            message = "Erreur: " + e.getMessage();
            System.err.println("❌ Exception dans AdminServlet: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("📤 Réponse finale - Success: " + success + ", Message: " + message);
        System.out.println("=========================================\n");
        
        // RÉPONSE selon le type de requête
        if (isAjax) {
            sendJsonResponse(response, success, message, action);
        } else {
            // Mode classique avec rechargement
            if (success) {
                request.getSession().setAttribute("adminMessage", message);
            } else {
                request.getSession().setAttribute("adminError", message);
            }
            response.sendRedirect(request.getContextPath() + "/chat");
        }
    }
    
    /**
     * Envoie une réponse JSON au client
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, String action) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        System.out.println("📨 Envoi JSON - Success: " + success + ", Message: " + message);
        
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"success\":" + success + ",");
        out.print("\"message\":\"" + escapeJson(message) + "\"");
        if (action != null) {
            out.print(",\"action\":\"" + escapeJson(action) + "\"");
        }
        out.print("}");
        out.flush();
        
        System.out.println("✅ JSON envoyé avec succès");
    }
    
    /**
     * Retourne le nom lisible d'une permission
     */
    private String getPermissionName(int permission) {
        switch (permission) {
            case 1: return "Administrateur";
            case 2: return "Modérateur";
            case 3: return "Utilisateur normal";
            default: return "Inconnu";
        }
    }
    
    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}