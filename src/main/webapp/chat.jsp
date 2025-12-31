<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat - Messagerie Instantanée</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // Définir le contextPath pour JavaScript
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
</head>
<body>
    <div class="chat-container">
        <!-- Sidebar avec les utilisateurs -->
        <div class="chat-sidebar">
            <div class="sidebar-header">
                <h2>Utilisateurs</h2>
                <div class="user-status">
                    <span class="status-indicator ${sessionScope.user.status}"></span>
                    <span>${sessionScope.user.username}</span>
                </div>
            </div>
            
            <!-- Contenu scrollable de la sidebar -->
            <div class="sidebar-content">
                <div class="sidebar-section">
                    <h3>En ligne (<c:out value="${onlineUsers.size()}" default="0" />)</h3>
                    <ul class="user-list" id="online-users-list">
                        <c:forEach var="user" items="${onlineUsers}">
                            <li>
                                <span class="status-indicator online"></span>
                                <c:out value="${user.username}" />
                                <c:if test="${user.permission == 1}">
                                    <span class="badge admin">Admin</span>
                                </c:if>
                                <c:if test="${user.permission == 2}">
                                    <span class="badge moderator">Mod</span>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
                
                <div class="sidebar-section">
                    <h3>Tous les utilisateurs</h3>
                    <ul class="user-list" id="all-users-list">
                        <c:forEach var="user" items="${allUsers}">
                            <li>
                                <span class="status-indicator ${user.status}"></span>
                                <c:out value="${user.username}" />
                                <c:if test="${user.permission == 1}">
                                    <span class="badge admin">Admin</span>
                                </c:if>
                                <c:if test="${user.permission == 2}">
                                    <span class="badge moderator">Mod</span>
                                </c:if>
                                <c:if test="${user.status == 'banned'}">
                                    <span class="badge banned">Banni</span>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
                
                <!-- Section d'administration (visible seulement pour admin/modo) -->
                <c:if test="${sessionScope.user.permission == 1 or sessionScope.user.permission == 2}">
                    <div class="sidebar-section">
                        <h3>Administration</h3>
                        
                        <!-- Bannir un utilisateur avec motif -->
                        <form class="admin-form" data-action="ban">
                            <input type="hidden" name="action" value="ban">
                            <select name="targetUserId" class="admin-select" required>
                                <option value="">Bannir un utilisateur</option>
                                <c:forEach var="user" items="${allUsers}">
                                    <c:if test="${user.id != sessionScope.user.id and user.status != 'banned'}">
                                        <c:if test="${sessionScope.user.permission == 1 or (sessionScope.user.permission == 2 and user.permission == 3)}">
                                            <option value="${user.id}">${user.username} (${user.permission == 1 ? 'Admin' : user.permission == 2 ? 'Modo' : 'User'})</option>
                                        </c:if>
                                    </c:if>
                                </c:forEach>
                            </select>
                            
                            <textarea name="reason" class="admin-select" 
                                      placeholder="Motif du bannissement (optionnel)" 
                                      rows="2" style="resize: vertical; margin-top: 5px;"></textarea>
                            
                            <button type="submit" class="btn btn-ban">Bannir</button>
                        </form>
                        
                        <!-- Débannir un utilisateur -->
                        <form class="admin-form" data-action="unban">
                            <input type="hidden" name="action" value="unban">
                            <select name="targetUserId" class="admin-select" required>
                                <option value="">Débannir un utilisateur</option>
                                <c:forEach var="user" items="${allUsers}">
                                    <c:if test="${user.status == 'banned'}">
                                        <option value="${user.id}">${user.username}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                            <button type="submit" class="btn btn-unban">Débannir</button>
                        </form>
                        
                        <!-- Changer le type (admin seulement) -->
						<c:if test="${sessionScope.user.permission == 1}">
						    <form class="admin-form" data-action="changeType">
						        <input type="hidden" name="action" value="changeType">
						        <select name="targetUserId" class="admin-select" required>
						            <option value="">Changer le type de</option>
						            <c:forEach var="user" items="${allUsers}">
						                <c:if test="${user.id != sessionScope.user.id}">
						                    <option value="${user.id}">${user.username}</option>
						                </c:if>
						            </c:forEach>
						        </select>
						        <select name="newPermission" class="admin-select" required>
						            <option value="">Sélectionner un type</option>  <!-- ✅ LIGNE AJOUTÉE -->
						            <option value="1">Administrateur</option>
						            <option value="2">Modérateur</option>
						            <option value="3">Utilisateur normal</option>
						        </select>
						        <button type="submit" class="btn btn-change-type">Changer</button>
						    </form>
						</c:if>
                    </div>
                </c:if>
            </div>
            
            <div class="sidebar-footer">
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-logout">Déconnexion</a>
            </div>
        </div>
        
        <!-- Zone principale du chat -->
        <div class="chat-main">
            <div class="chat-header">
                <h1>Messagerie Instantanée</h1>
                <div class="chat-actions">
                    <!-- Le bouton de thème sera ajouté ici par JavaScript -->
                    <form action="${pageContext.request.contextPath}/chat" method="post" class="status-form">
                        <select name="status" onchange="event.preventDefault();">
                            <option value="online" ${sessionScope.user.status == 'online' ? 'selected' : ''}>En ligne</option>
                            <option value="away" ${sessionScope.user.status == 'away' ? 'selected' : ''}>Absent</option>
                            <option value="offline" ${sessionScope.user.status == 'offline' ? 'selected' : ''}>Hors ligne</option>
                        </select>
                    </form>
                </div>
            </div>
            
            <!-- Zone des messages -->
            <div class="chat-messages" id="chat-messages">
                <c:forEach var="message" items="${messages}">
                    <div class="message" data-message-id="${message.id}">
                        <div class="message-header">
                            <strong>
                                <c:out value="${message.username}" />
                                <c:if test="${message.userId != null}">
                                    <c:set var="msgUser" value="${null}" />
                                    <c:forEach var="u" items="${allUsers}">
                                        <c:if test="${u.id == message.userId}">
                                            <c:set var="msgUser" value="${u}" />
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${msgUser != null}">
                                        <c:if test="${msgUser.permission == 1}">
                                            <span class="badge admin">Admin</span>
                                        </c:if>
                                        <c:if test="${msgUser.permission == 2}">
                                            <span class="badge moderator">Mod</span>
                                        </c:if>
                                    </c:if>
                                </c:if>
                            </strong>
                            <span class="message-time"><c:out value="${message.formattedTime}" /></span>
                        </div>
                        <div class="message-content">
                            <c:out value="${message.content}" />
                        </div>
                    </div>
                </c:forEach>
            </div>
            
            <!-- Zone de saisie -->
            <div class="chat-input">
                <form id="message-form">
                    <input type="text" name="message" placeholder="Tapez votre message ici..." autocomplete="off" required>
                    <button type="submit" class="btn btn-send">Envoyer</button>
                </form>
            </div>
        </div>
    </div>
    
    <!-- Scripts -->
    <script src="${pageContext.request.contextPath}/js/toast-notifications.js"></script>
    <script src="${pageContext.request.contextPath}/js/chat.js"></script>
    
    <script>
// ========== GESTION DES FORMULAIRES ADMIN EN AJAX ==========
document.addEventListener('DOMContentLoaded', function() {
    
    const adminForms = document.querySelectorAll('.admin-form');
    
    adminForms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const action = this.querySelector('input[name="action"]').value;
            console.log('🎯 Action:', action);
            
            // Variables pour le message de confirmation
            let username = '';
            let confirmMessage = '';
            
            // ✅ TRAITEMENT SELON L'ACTION
            if (action === 'ban') {
                const targetUserSelect = this.querySelector('select[name="targetUserId"]');
                const selectedOption = targetUserSelect ? targetUserSelect.options[targetUserSelect.selectedIndex] : null;
                
                if (!selectedOption || !selectedOption.value) {
                    toast.warning('Veuillez sélectionner un utilisateur');
                    return;
                }
                
                username = selectedOption.textContent.trim();
                // Nettoyer le format "utilisateur1 (User)"
                if (username.includes('(')) {
                    username = username.split('(')[0].trim();
                }
                
                confirmMessage = `Êtes-vous sûr de vouloir bannir ${username} ?`;
            } 
            else if (action === 'unban') {
                const targetUserSelect = this.querySelector('select[name="targetUserId"]');
                const selectedOption = targetUserSelect ? targetUserSelect.options[targetUserSelect.selectedIndex] : null;
                
                if (!selectedOption || !selectedOption.value) {
                    toast.warning('Veuillez sélectionner un utilisateur');
                    return;
                }
                
                username = selectedOption.textContent.trim();
                confirmMessage = `Êtes-vous sûr de vouloir débannir ${username} ?`;
            } 
            else if (action === 'changeType') {
                // 1. Récupérer l'utilisateur
                const targetUserSelect = this.querySelector('select[name="targetUserId"]');
                const selectedUserOption = targetUserSelect ? targetUserSelect.options[targetUserSelect.selectedIndex] : null;
                
                if (!selectedUserOption || !selectedUserOption.value) {
                    toast.warning('Veuillez sélectionner un utilisateur');
                    return;
                }
                
                username = selectedUserOption.textContent.trim();
                console.log('👤 Username:', username);
                
                // 2. Récupérer le nouveau type
                const newTypeSelect = this.querySelector('select[name="newPermission"]');
                
                if (!newTypeSelect || !newTypeSelect.value) {
                    toast.warning('Veuillez sélectionner un nouveau type');
                    return;
                }
                
                const permissionValue = newTypeSelect.value;
                console.log('🔑 Permission value:', permissionValue);
                
                // 3. Construire le nom du type
                let newTypeName = '';
                if (permissionValue === '1') {
                    newTypeName = 'Administrateur';
                } else if (permissionValue === '2') {
                    newTypeName = 'Modérateur';
                } else if (permissionValue === '3') {
                    newTypeName = 'Utilisateur normal';
                }
                
                console.log('🔛 Type name:', newTypeName);
                
                if (!newTypeName) {
                    toast.error('Erreur: type non reconnu');
                    return;
                }
                
                // 4. Construire le message
                confirmMessage = 'Changer ' + username + ' en ' + newTypeName + ' ?';
                console.log('💬 Message final:', confirmMessage);
            }
            
            // Afficher la confirmation
            if (!confirmMessage) {
                toast.error('Action inconnue');
                return;
            }
            
            console.log('🔔 Affichage confirm:', confirmMessage);
            
            if (!confirm(confirmMessage)) {
                return;
            }
            
            // Désactiver le bouton pendant le traitement
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Traitement...';
            
            try {
                const formData = new FormData(this);
                const params = new URLSearchParams();
                
                for (const [key, value] of formData.entries()) {
                    params.append(key, value);
                }
                
                console.log('📤 Envoi requête AJAX vers /admin...');
                
                const response = await fetch(window.contextPath + '/admin', {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: params.toString()
                });
                
                console.log('📥 Réponse reçue, status:', response.status);
                
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }
                
                const result = await response.json();
                console.log('📋 Résultat:', result);
                
                if (result.success) {
                    toast.success(result.message);
                    this.reset();
                } else {
                    if (result.message.includes('est déjà')) {
                        toast.warning(result.message);
                    } else {
                        toast.error(result.message);
                    }
                }
                
            } catch (error) {
                console.error('❌ Erreur:', error);
                toast.error('Erreur de communication avec le serveur');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = originalBtnText;
            }
        });
    });
});
</script>
</body>
</html>