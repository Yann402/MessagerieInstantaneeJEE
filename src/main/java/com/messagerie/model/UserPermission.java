package com.messagerie.model;

public enum UserPermission {
    ADMIN(1, "Administrateur"),
    MODERATOR(2, "Modérateur"),
    USER(3, "Utilisateur normal");

    private final int level;
    private final String description;

    UserPermission(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static UserPermission fromLevel(int level) {
        for (UserPermission perm : values()) {
            if (perm.level == level) {
                return perm;
            }
        }
        throw new IllegalArgumentException("Niveau de permission inconnu: " + level);
    }

    // Méthodes pour vérifier les permissions
    public boolean canBan() {
        return this == ADMIN || this == MODERATOR;
    }

    public boolean canChangeUserType() {
        return this == ADMIN;
    }
}