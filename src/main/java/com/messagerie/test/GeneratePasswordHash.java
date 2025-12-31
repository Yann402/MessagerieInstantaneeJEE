package com.messagerie.test;

import org.mindrot.jbcrypt.BCrypt;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        String password = "admin123";
        
        // Générer un hash pour l'admin
        String hashAdmin = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Admin hash: " + hashAdmin);
        
        // Générer un hash pour le modérateur
        String hashModerateur = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Moderateur hash: " + hashModerateur);
        
        // Générer un hash pour utilisateur1
        String hashUser1 = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Utilisateur1 hash: " + hashUser1);
        
        // Générer un hash pour utilisateur2
        String hashUser2 = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Utilisateur2 hash: " + hashUser2);
    }
}