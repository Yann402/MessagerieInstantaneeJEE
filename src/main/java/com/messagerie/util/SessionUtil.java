package com.messagerie.util;

import com.messagerie.model.User;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static final String USER_SESSION_KEY = "user";

    public static void setUserInSession(HttpSession session, User user) {
        if (session != null) {
            session.setAttribute(USER_SESSION_KEY, user);
        }
    }

    public static User getUserFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(USER_SESSION_KEY);
    }

    public static boolean isUserLoggedIn(HttpSession session) {
        return getUserFromSession(session) != null;
    }

    public static void invalidateSession(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}