package com.messagerie.test;

import com.messagerie.dao.*;
import com.messagerie.model.*;

public class TestDAOs {
    public static void main(String[] args) {
        System.out.println("=== TEST DES DAO ===\n");
        
        try {
            UserDAO userDAO = new UserDAOImpl();
            MessageDAO messageDAO = new MessageDAOImpl();
            LogDAO logDAO = new LogDAOImpl();
            
            // Test 1: Recherche d'utilisateur
            System.out.println("1. Recherche d'utilisateurs :");
            User admin = userDAO.findByUsername("admin");
            if (admin != null) {
                System.out.println("   ✅ Admin trouvé : " + admin.getUsername());
                // Utiliser l'énumération pour obtenir la description
                UserPermission permission = UserPermission.fromLevel(admin.getPermission());
                System.out.println("   Permission : " + permission.getDescription());
                System.out.println("   Status : " + admin.getStatus());
            } else {
                System.out.println("   ❌ Admin non trouvé");
            }
            
            // Test 2: Liste des utilisateurs
            System.out.println("\n2. Liste de tous les utilisateurs :");
            for (User user : userDAO.findAll()) {
                UserPermission permission = UserPermission.fromLevel(user.getPermission());
                System.out.println("   - " + user.getUsername() + 
                                 " (" + permission.getDescription() + 
                                 ") - Status: " + user.getStatus());
            }
            
            // Test 3: Mise à jour du statut
            System.out.println("\n3. Mise à jour du statut :");
            if (admin != null) {
                boolean updated = userDAO.updateStatus(admin.getId(), "online");
                if (updated) {
                    System.out.println("   ✅ Statut de admin mis à jour en 'online'");
                    admin = userDAO.findById(admin.getId());
                    System.out.println("   Nouveau statut : " + admin.getStatus());
                }
            }
            
            // Test 4: Messages récents
            System.out.println("\n4. Derniers messages :");
            for (Message message : messageDAO.findLastMessages(3)) {
                System.out.println("   [" + message.getFormattedTime() + "] " + 
                                 message.getUsername() + " : " + message.getContent());
            }
            
            // Test 5: Logs
            System.out.println("\n5. Logs récents :");
            for (Log log : logDAO.findRecent(5)) {
                String userInfo = log.getUsername() != null ? log.getUsername() : "Système";
                System.out.println("   [" + log.getTimestamp() + "] " + 
                                 userInfo + " - " + log.getType() + " : " + log.getDescription());
            }
            
            // Test 6: Utilisateurs en ligne
            System.out.println("\n6. Utilisateurs en ligne :");
            for (User user : userDAO.getOnlineUsers()) {
                System.out.println("   - " + user.getUsername() + " (" + user.getStatus() + ")");
            }
            
            // Test 7: Test de bannissement avec motif
            System.out.println("\n7. Test de bannissement avec motif :");
            User testUser = userDAO.findByUsername("utilisateur1");
            if (testUser != null) {
                boolean banned = userDAO.banUser(testUser.getId(), "Test de bannissement DAO");
                if (banned) {
                    System.out.println("   ✅ Utilisateur banni avec succès");
                    boolean isBanned = userDAO.isUserBanned("utilisateur1");
                    System.out.println("   Vérification bannissement : " + isBanned);
                    
                    // Vérifier le motif
                    String banReason = userDAO.getBanReason(testUser.getId());
                    System.out.println("   Motif de bannissement : " + banReason);
                }
            }
            
            // Test 8: Test de débannissement
            System.out.println("\n8. Test de débannissement :");
            if (testUser != null) {
                boolean unbanned = userDAO.unbanUser(testUser.getId());
                if (unbanned) {
                    System.out.println("   ✅ Utilisateur débanni avec succès");
                    boolean isBanned = userDAO.isUserBanned("utilisateur1");
                    System.out.println("   Vérification bannissement : " + isBanned);
                    
                    // Vérifier que le motif a été effacé
                    String banReason = userDAO.getBanReason(testUser.getId());
                    System.out.println("   Motif après débannissement : " + banReason);
                }
            }
            
            System.out.println("\n✅ TESTS TERMINÉS AVEC SUCCÈS !");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR CRITIQUE : " + e.getMessage());
            e.printStackTrace();
        }
    }
}