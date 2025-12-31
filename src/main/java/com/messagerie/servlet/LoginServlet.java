package com.messagerie.servlet;

import com.messagerie.service.AuthenticationService;
import com.messagerie.service.UserService;
import com.messagerie.model.User;
import com.messagerie.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serial;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private AuthenticationService authService;
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        this.authService = new AuthenticationService();
        this.userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        // Si déjà connecté, rediriger vers le chat
        if (session != null && SessionUtil.isUserLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/chat");
            return;
        }
        
        // Sinon afficher la page de login
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        User user = authService.authenticate(username, password);
        
        if (user != null) {
            HttpSession session = request.getSession();
            SessionUtil.setUserInSession(session, user);
            response.sendRedirect(request.getContextPath() + "/chat");
        } else {
            // Vérifier si l'utilisateur existe mais est banni
            User bannedUser = userService.getUserByUsername(username);
            if (bannedUser != null && "banned".equals(bannedUser.getStatus())) {
                String banReason = userService.getBanReason(bannedUser.getId());
                String errorMessage = "Votre compte a été banni.";
                
                if (banReason != null && !banReason.isEmpty() && !banReason.equals("Aucun motif spécifié")) {
                    errorMessage += " Motif : " + banReason;
                }
                
                request.setAttribute("errorMessage", errorMessage);
            } else {
                request.setAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect");
            }
            
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}