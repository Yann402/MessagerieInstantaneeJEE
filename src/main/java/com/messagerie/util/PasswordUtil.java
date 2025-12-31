package com.messagerie.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Coût du hachage : 10 est un bon compromis entre sécurité et performance
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hache un mot de passe en clair.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Vérifie si un mot de passe en clair correspond au hash stocké.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}