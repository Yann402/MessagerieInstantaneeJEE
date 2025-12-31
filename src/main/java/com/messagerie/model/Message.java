package com.messagerie.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int userId;
    private String username; // Ajouté pour faciliter l'affichage
    private LocalDateTime timestamp;
    private String content;

    // Constructeurs
    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(int userId, String content) {
        this();
        this.userId = userId;
        this.content = content;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // Formatage pour affichage - CORRECTION : gérer le cas null
    public String getFormattedTime() {
        if (timestamp != null) {
            return timestamp.toString().replace("T", " ");
        }
        return "";
    }
}