// Configuration WebSocket
let ws;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 3000;

// Éléments DOM
const messageInput = document.querySelector('#message-form input[name="message"]');
const messageForm = document.getElementById('message-form');
const messagesContainer = document.getElementById('chat-messages');
const statusSelect = document.querySelector('select[name="status"]');

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Initialisation du chat WebSocket...');
    initWebSocket();
    scrollToBottom();
    setupEventListeners();
});

/**
 * Initialise la connexion WebSocket
 */
function initWebSocket() {
    // Construire l'URL WebSocket
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const contextPath = window.contextPath || '';
    const wsUrl = `${protocol}//${host}${contextPath}/chat-websocket`;
    
    console.log('🔌 Connexion WebSocket à:', wsUrl);
    
    try {
        ws = new WebSocket(wsUrl);
        
        ws.onopen = function(event) {
            console.log('✅ WebSocket CONNECTÉ !');
            reconnectAttempts = 0;
            toast.success('Connecté au chat en temps réel');
        };
        
        ws.onmessage = function(event) {
            console.log('📨 Message WebSocket reçu:', event.data);
            handleWebSocketMessage(event.data);
        };
        
        ws.onerror = function(error) {
            console.error('❌ Erreur WebSocket:', error);
            toast.error('Erreur de connexion');
        };
        
        ws.onclose = function(event) {
            console.log('🔌 WebSocket FERMÉ. Code:', event.code, 'Raison:', event.reason);
            
            // Tentative de reconnexion
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                console.log(`🔄 Tentative de reconnexion ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS} dans ${RECONNECT_DELAY/1000}s...`);
                
                toast.warning(`Reconnexion en cours (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`);
                
                setTimeout(() => {
                    initWebSocket();
                }, RECONNECT_DELAY);
            } else {
                console.error('❌ Nombre maximum de tentatives de reconnexion atteint');
                toast.error('Connexion perdue. Veuillez recharger la page.');
            }
        };
        
    } catch (error) {
        console.error('❌ Erreur lors de la création du WebSocket:', error);
        toast.error('Impossible de se connecter au serveur');
    }
}

/**
 * Configure les écouteurs d'événements
 */
function setupEventListeners() {
    // Envoi de message
    if (messageForm) {
        messageForm.addEventListener('submit', function(e) {
            e.preventDefault();
            sendMessage();
        });
    }
    
    // Changement de statut
    if (statusSelect) {
        statusSelect.addEventListener('change', function(e) {
            const newStatus = this.value;
            console.log('🔄 Changement de statut vers:', newStatus);
            
            if (ws && ws.readyState === WebSocket.OPEN) {
                const message = {
                    type: 'status',
                    status: newStatus
                };
                ws.send(JSON.stringify(message));
            }
            
            // NE PAS soumettre le formulaire pour éviter le rechargement
            e.preventDefault();
        });
    }
    
    // Fermeture propre du WebSocket
    window.addEventListener('beforeunload', function() {
        if (ws) {
            ws.close(1000, 'Page fermée');
        }
    });
}

/**
 * Envoie un message via WebSocket
 */
function sendMessage() {
    if (!messageInput) return;
    
    const content = messageInput.value.trim();
    
    if (!content) {
        console.log('⚠️ Message vide, pas d\'envoi');
        return;
    }
    
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        console.error('❌ WebSocket non connecté');
        toast.error('Non connecté au serveur');
        return;
    }
    
    console.log('📤 Envoi du message:', content);
    
    const message = {
        type: 'message',
        content: content
    };
    
    try {
        ws.send(JSON.stringify(message));
        messageInput.value = '';
        messageInput.focus();
    } catch (error) {
        console.error('❌ Erreur lors de l\'envoi:', error);
        toast.error('Erreur lors de l\'envoi du message');
    }
}

/**
 * Traite les messages WebSocket reçus
 */
function handleWebSocketMessage(data) {
    try {
        const message = JSON.parse(data);
        console.log('📬 Message traité - Type:', message.type);
        
        switch (message.type) {
            case 'message':
                displayMessage(message);
                break;
                
            case 'system':
                // Afficher comme notification toast au lieu du chat
                displaySystemNotification(message.message);
                break;
                
            case 'userListUpdate':
                updateUsersList(message.users);
                break;
                
            case 'disconnect':
                handleDisconnect(message.reason);
                break;
                
            default:
                console.log('⚠️ Type de message inconnu:', message.type);
        }
    } catch (error) {
        console.error('❌ Erreur lors du parsing du message:', error);
    }
}

/**
 * Affiche un message de chat
 */
function displayMessage(msg) {
    if (!messagesContainer) {
        console.error('❌ Container de messages non trouvé');
        return;
    }
    
    console.log('💬 Affichage du message:', msg.username, '-', msg.content);
    
    // Vérifier si le message existe déjà
    if (document.querySelector(`[data-message-id="${msg.id}"]`)) {
        console.log('⚠️ Message déjà affiché, ignoré');
        return;
    }
    
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    messageDiv.setAttribute('data-message-id', msg.id);
    
    let badge = '';
    if (msg.permission === 1) {
        badge = '<span class="badge admin">Admin</span>';
    } else if (msg.permission === 2) {
        badge = '<span class="badge moderator">Mod</span>';
    }
    
    messageDiv.innerHTML = `
        <div class="message-header">
            <strong>${escapeHtml(msg.username)}${badge}</strong>
            <span class="message-time">${msg.timestamp || ''}</span>
        </div>
        <div class="message-content">${escapeHtml(msg.content)}</div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

/**
 * Affiche un message système comme notification toast
 */
function displaySystemNotification(text) {
    console.log('🔔 Notification système:', text);
    
    // Déterminer le type de notification
    if (text.includes('connecter') || text.includes('connecté')) {
        toast.info(text, 3000);
    } else if (text.includes('déconnecté') || text.includes('hors ligne')) {
        toast.info(text, 3000);
    } else if (text.includes('banni')) {
        toast.warning(text, 4000);
    } else {
        toast.info(text, 3000);
    }
}

/**
 * Met à jour la liste des utilisateurs
 */
function updateUsersList(users) {
    console.log('👥 Mise à jour de la liste:', users.length, 'utilisateurs');
    
    // Liste "Tous les utilisateurs"
    const allUsersList = document.querySelector('.sidebar-section:nth-child(2) .user-list');
    if (allUsersList) {
        allUsersList.innerHTML = '';
        
        users.forEach(user => {
            const li = document.createElement('li');
            
            let badge = '';
            if (user.permission === 1) badge = '<span class="badge admin">Admin</span>';
            else if (user.permission === 2) badge = '<span class="badge moderator">Mod</span>';
            if (user.status === 'banned') badge += '<span class="badge banned">Banni</span>';
            
            li.innerHTML = `
                <span class="status-indicator ${user.status}"></span>
                ${escapeHtml(user.username)}
                ${badge}
            `;
            
            allUsersList.appendChild(li);
        });
    }
    
    // Liste "En ligne"
    const onlineUsersList = document.querySelector('.sidebar-section:first-child .user-list');
    if (onlineUsersList) {
        onlineUsersList.innerHTML = '';
        
        const onlineUsers = users.filter(u => u.status === 'online');
        
        onlineUsers.forEach(user => {
            const li = document.createElement('li');
            
            let badge = '';
            if (user.permission === 1) badge = '<span class="badge admin">Admin</span>';
            else if (user.permission === 2) badge = '<span class="badge moderator">Mod</span>';
            
            li.innerHTML = `
                <span class="status-indicator online"></span>
                ${escapeHtml(user.username)}
                ${badge}
            `;
            
            onlineUsersList.appendChild(li);
        });
        
        // Mettre à jour le compteur
        const onlineTitle = document.querySelector('.sidebar-section:first-child h3');
        if (onlineTitle) {
            onlineTitle.textContent = `En ligne (${onlineUsers.length})`;
        }
    }
    
    // ✅ NOUVEAU : Mettre à jour les selects des formulaires d'administration
    updateAdminSelects(users);
}

/**
 * ✅ NOUVELLE FONCTION : Met à jour les options des selects d'administration
 */
function updateAdminSelects(users) {
    console.log('🔄 Mise à jour des selects d\'administration...');
    
    // Récupérer l'ID de l'utilisateur courant (depuis la session)
    const currentUserElement = document.querySelector('.user-status span:last-child');
    const currentUsername = currentUserElement ? currentUserElement.textContent.trim() : '';
    const currentUser = users.find(u => u.username === currentUsername);
    const currentUserId = currentUser ? currentUser.id : null;
    const currentUserPermission = currentUser ? currentUser.permission : 3;
    
    console.log('👤 Utilisateur courant:', currentUsername, 'ID:', currentUserId, 'Permission:', currentUserPermission);
    
    // 1. Mettre à jour le select "Bannir un utilisateur"
    const banSelect = document.querySelector('form[data-action="ban"] select[name="targetUserId"]');
    if (banSelect) {
        const selectedValue = banSelect.value; // Sauvegarder la sélection
        banSelect.innerHTML = '<option value="">Bannir un utilisateur</option>';
        
        users.forEach(user => {
            // Ne pas inclure l'utilisateur courant ni les utilisateurs déjà bannis
            if (user.id !== currentUserId && user.status !== 'banned') {
                // Si l'utilisateur est modérateur (permission 2), ne montrer que les utilisateurs normaux (permission 3)
                if (currentUserPermission === 1 || (currentUserPermission === 2 && user.permission === 3)) {
                    const option = document.createElement('option');
                    option.value = user.id;
                    const permLabel = user.permission === 1 ? 'Admin' : user.permission === 2 ? 'Modo' : 'User';
                    option.textContent = `${user.username} (${permLabel})`;
                    banSelect.appendChild(option);
                }
            }
        });
        
        if (selectedValue) banSelect.value = selectedValue; // Restaurer la sélection
        console.log('✅ Select "Bannir" mis à jour:', banSelect.options.length - 1, 'options');
    }
    
    // 2. Mettre à jour le select "Débannir un utilisateur"
    const unbanSelect = document.querySelector('form[data-action="unban"] select[name="targetUserId"]');
    if (unbanSelect) {
        const selectedValue = unbanSelect.value;
        unbanSelect.innerHTML = '<option value="">Débannir un utilisateur</option>';
        
        const bannedUsers = users.filter(u => u.status === 'banned');
        bannedUsers.forEach(user => {
            const option = document.createElement('option');
            option.value = user.id;
            option.textContent = user.username;
            unbanSelect.appendChild(option);
        });
        
        if (selectedValue) unbanSelect.value = selectedValue;
        console.log('✅ Select "Débannir" mis à jour:', unbanSelect.options.length - 1, 'options');
    }
    
    // 3. Mettre à jour le select "Changer le type" (uniquement pour les admins)
    const changeTypeSelect = document.querySelector('form[data-action="changeType"] select[name="targetUserId"]');
    if (changeTypeSelect && currentUserPermission === 1) {
        const selectedValue = changeTypeSelect.value;
        changeTypeSelect.innerHTML = '<option value="">Changer le type de</option>';
        
        users.forEach(user => {
            // Ne pas inclure l'utilisateur courant
            if (user.id !== currentUserId) {
                const option = document.createElement('option');
                option.value = user.id;
                option.textContent = user.username;
                changeTypeSelect.appendChild(option);
            }
        });
        
        if (selectedValue) changeTypeSelect.value = selectedValue;
        console.log('✅ Select "Changer type" mis à jour:', changeTypeSelect.options.length - 1, 'options');
    }
}

/**
 * Gère la déconnexion forcée
 */
function handleDisconnect(reason) {
    console.log('🚫 Déconnexion forcée:', reason);
    toast.error(reason, 5000);
    
    setTimeout(() => {
        window.location.href = (window.contextPath || '') + '/login';
    }, 2000);
}

/**
 * Scroll vers le bas
 */
function scrollToBottom() {
    if (messagesContainer) {
        messagesContainer.scrollTo({
            top: messagesContainer.scrollHeight,
            behavior: 'smooth'
        });
    }
}

/**
 * Échappe le HTML pour éviter XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ✅ EXPORT DE LA FONCTION pour utilisation dans chat.jsp
window.forceUpdateAdminSelects = function() {
    console.log('🔄 Forçage de la mise à jour des selects...');
    if (ws && ws.readyState === WebSocket.OPEN) {
        // Le WebSocket enverra automatiquement une mise à jour de la liste
        console.log('WebSocket actif, attente de la mise à jour automatique...');
    }
};