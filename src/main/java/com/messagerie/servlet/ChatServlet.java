package com.messagerie.servlet;

import com.messagerie.service.MessageService;
import com.messagerie.service.UserService;
import com.messagerie.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private MessageService messageService;
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!SessionUtil.isUserLoggedIn(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Charger les données
        request.setAttribute("messages", messageService.getRecentMessages(50));
        request.setAttribute("onlineUsers", userService.getOnlineUsers());
        request.setAttribute("allUsers", userService.getAllUsers());
        
        request.getRequestDispatcher("/chat.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!SessionUtil.isUserLoggedIn(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        com.messagerie.model.User user = SessionUtil.getUserFromSession(request.getSession(false));
        
        // 1. Gestion des messages
        String messageContent = request.getParameter("message");
        if (messageContent != null && !messageContent.trim().isEmpty()) {
            messageService.sendMessage(user.getId(), messageContent.trim());
        }
        
        // 2. Gestion du changement de statut
        String newStatus = request.getParameter("status");
        if (newStatus != null && !newStatus.isEmpty()) {
            // Mettre à jour dans la base
            userService.changeUserStatus(user.getId(), newStatus);
            
            // Mettre à jour l'utilisateur dans la session
            user.setStatus(newStatus);
            SessionUtil.setUserInSession(request.getSession(), user);
        }
        
        // Rediriger pour recharger la page
        response.sendRedirect(request.getContextPath() + "/chat");
    }
}