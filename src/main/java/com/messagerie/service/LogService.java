package com.messagerie.service;

import com.messagerie.dao.LogDAO;
import com.messagerie.dao.LogDAOImpl;
import com.messagerie.model.Log;

import java.util.List;

public class LogService {
    private LogDAO logDAO;

    public LogService() {
        this.logDAO = new LogDAOImpl();
    }

    /**
     * Enregistre un log.
     */
    public boolean logAction(Integer userId, String type, String description) {
        Log log = new Log(userId, type, description);
        return logDAO.save(log);
    }

    /**
     * Récupère les logs récents.
     */
    public List<Log> getRecentLogs(int limit) {
        return logDAO.findRecent(limit);
    }

    /**
     * Récupère les logs d'un utilisateur.
     */
    public List<Log> getLogsByUser(int userId) {
        return logDAO.findByUserId(userId);
    }

    /**
     * Récupère les logs par type.
     */
    public List<Log> getLogsByType(String type) {
        return logDAO.findByType(type);
    }
}