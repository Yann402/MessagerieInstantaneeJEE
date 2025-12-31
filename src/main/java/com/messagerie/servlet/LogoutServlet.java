package com.messagerie.servlet;

import com.messagerie.service.AuthenticationService;
import com.messagerie.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serial;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private AuthenticationService authService;
    
    @Override
    public void init() throws ServletException {
        this.authService = new AuthenticationService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            com.messagerie.model.User user = SessionUtil.getUserFromSession(session);
            if (user != null) {
                authService.logout(user.getId());
            }
            SessionUtil.invalidateSession(session);
        }
        
        // Rediriger vers la page de login
        response.sendRedirect(request.getContextPath() + "/login");
    }
}