package com.messagerie.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int permission; // Stocké comme int (1, 2, 3)
    private LocalDateTime lastConnectionTime;
    private String status; // online, offline, away, banned
    private LocalDateTime createdAt;

    // Constructeurs
    public User() {
        this.createdAt = LocalDateTime.now();
        this.status = "offline";
    }

    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getPermission() { return permission; }
    public void setPermission(int permission) { this.permission = permission; }
    
    // Méthode utilitaire pour obtenir l'énumération
    public UserPermission getPermissionEnum() {
        return UserPermission.fromLevel(permission);
    }
    
    // Méthode pour définir la permission à partir de l'énumération
    public void setPermissionFromEnum(UserPermission permissionEnum) {
        this.permission = permissionEnum.getLevel();
    }

    public LocalDateTime getLastConnectionTime() { return lastConnectionTime; }
    public void setLastConnectionTime(LocalDateTime lastConnectionTime) { this.lastConnectionTime = lastConnectionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Méthodes pratiques
    public boolean isOnline() {
        return "online".equals(status);
    }

    public boolean isBanned() {
        return "banned".equals(status);
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else {
            return username;
        }
    }
}