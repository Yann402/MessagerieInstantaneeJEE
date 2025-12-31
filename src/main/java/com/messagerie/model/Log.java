package com.messagerie.model;

import java.time.LocalDateTime;

public class Log {
    private int id;
    private Integer userId; // Changé en Integer pour permettre null
    private String username; // Ajouté pour l'affichage
    private LocalDateTime timestamp;
    private String type;
    private String description; // Ajouté pour stocker la description

    // Types de logs prédéfinis
    public static final String TYPE_LOGIN = "CONNEXION";
    public static final String TYPE_LOGOUT = "DECONNEXION";
    public static final String TYPE_MESSAGE = "MESSAGE";
    public static final String TYPE_BAN = "BANNISSEMENT";
    public static final String TYPE_STATUS_CHANGE = "CHANGEMENT_STATUT";
    public static final String TYPE_USER_CREATED = "CREATION_UTILISATEUR";
    public static final String TYPE_USER_UPDATED = "MODIFICATION_UTILISATEUR";

    // Constructeurs
    public Log() {
        this.timestamp = LocalDateTime.now();
    }

    public Log(String type, String description) {
        this();
        this.type = type;
        this.description = description;
    }

    public Log(int userId, String type, String description) {
        this(type, description);
        this.userId = userId;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}