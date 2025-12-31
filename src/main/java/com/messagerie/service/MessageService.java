package com.messagerie.service;

import com.messagerie.dao.MessageDAO;
import com.messagerie.dao.MessageDAOImpl;
import com.messagerie.model.Message;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {
    private MessageDAO messageDAO;

    public MessageService() {
        this.messageDAO = new MessageDAOImpl();
    }

    /**
     * Envoie un nouveau message.
     */
    public boolean sendMessage(int userId, String content) {
        Message message = new Message(userId, content);
        return messageDAO.save(message);
    }

    /**
     * Récupère les derniers messages.
     */
    public List<Message> getRecentMessages(int limit) {
        return messageDAO.findLastMessages(limit);
    }

    /**
     * Récupère tous les messages.
     */
    public List<Message> getAllMessages() {
        return messageDAO.findAll();
    }

    /**
     * Récupère les messages entre deux dates.
     */
    public List<Message> getMessagesBetween(LocalDateTime start, LocalDateTime end) {
        return messageDAO.findBetweenDates(start, end);
    }

    /**
     * Supprime un message.
     */
    public boolean deleteMessage(int messageId) {
        return messageDAO.delete(messageId);
    }

    /**
     * Récupère les messages d'un utilisateur.
     */
    public List<Message> getMessagesByUser(int userId) {
        return messageDAO.findByUserId(userId);
    }
}