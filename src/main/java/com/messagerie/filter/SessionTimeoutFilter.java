package com.messagerie.filter;

import com.messagerie.model.User;
import com.messagerie.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtre pour vérifier la validité de la session et rediriger si expirée
 */
@WebFilter("/*")
public class SessionTimeoutFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("✓ SessionTimeoutFilter initialisé");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // URLs accessibles sans authentification
        if (path.startsWith("/login") || 
            path.startsWith("/css/") || 
            path.startsWith("/js/") || 
            path.startsWith("/images/") ||
            path.equals("/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Vérifier l'authentification pour les autres URLs
        HttpSession session = httpRequest.getSession(false);
        
        if (session == null || !SessionUtil.isUserLoggedIn(session)) {
            // Session expirée ou inexistante
            System.out.println("⚠️ Session invalide ou expirée pour le chemin: " + path);
            
            // Si c'est une requête AJAX/WebSocket, retourner 401
            String requestedWith = httpRequest.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith) || path.contains("websocket")) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Sinon rediriger vers login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        
        // Session valide, vérifier le timeout
        User user = SessionUtil.getUserFromSession(session);
        if (user != null) {
            long lastAccessTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            int maxInactiveInterval = session.getMaxInactiveInterval() * 1000; // en millisecondes
            
            if ((currentTime - lastAccessTime) > maxInactiveInterval) {
                // Session expirée
                System.out.println("⏰ Session expirée pour l'utilisateur: " + user.getUsername());
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        System.out.println("✓ SessionTimeoutFilter détruit");
    }
}