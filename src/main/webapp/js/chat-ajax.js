// Système de mise à jour AJAX pour le chat
class ChatUpdater {
    constructor() {
        this.lastMessageId = 0;
        this.lastUpdateTime = 0;
        this.pollingInterval = 3000; // 3 secondes
        this.isPolling = false;
        this.pollingTimer = null;
        this.contextPath = window.contextPath || '';
    }
    
    // Démarrer le polling
    startPolling() {
        if (this.isPolling) return;
        
        console.log('Démarrage du polling AJAX...');
        this.isPolling = true;
        
        // Première mise à jour immédiate
        this.fetchUpdates();
        
        // Puis toutes les X secondes
        this.pollingTimer = setInterval(() => {
            this.fetchUpdates();
        }, this.pollingInterval);
    }
    
    // Arrêter le polling
    stopPolling() {
        if (!this.isPolling) return;
        
        console.log('Arrêt du polling AJAX...');
        this.isPolling = false;
        
        if (this.pollingTimer) {
            clearInterval(this.pollingTimer);
            this.pollingTimer = null;
        }
    }
    
    // Récupérer les mises à jour
    async fetchUpdates() {
        try {
            const url = `${this.contextPath}/chat/updates?lastMessageId=${this.lastMessageId}&lastUpdate=${this.lastUpdateTime}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Cache-Control': 'no-cache',
                    'Pragma': 'no-cache'
                }
            });
            
            if (!response.ok) {
                if (response.status === 401) {
                    console.warn('Session expirée, redirection...');
                    window.location.href = `${this.contextPath}/login`;
                    return;
                }
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            
            if (data.success) {
                this.processUpdates(data);
            } else {
                console.error('Erreur dans la réponse:', data.error);
            }
            
        } catch (error) {
            console.error('Erreur lors de la récupération des mises à jour:', error);
            // Réessayer plus tard en cas d'erreur
        }
    }
    
    // Traiter les mises à jour reçues
    processUpdates(data) {
        // 1. Mettre à jour le dernier message ID
        if (data.lastMessageId) {
            this.lastMessageId = data.lastMessageId;
        }
        
        // 2. Ajouter les nouveaux messages
        if (data.newMessages && data.newMessages.length > 0) {
            this.addNewMessages(data.newMessages);
        }
        
        // 3. Mettre à jour les listes d'utilisateurs
        if (data.usersUpdated) {
            this.updateUserLists(data);
            if (data.lastUpdate) {
                this.lastUpdateTime = data.lastUpdate;
            }
        }
        
        // 4. Mettre à jour le statut courant
        if (data.currentStatus) {
            this.updateCurrentUserStatus(data.currentStatus);
        }
    }
    
    // Ajouter de nouveaux messages au chat
    addNewMessages(messages) {
        const chatMessages = document.getElementById('chat-messages');
        if (!chatMessages) return;
        
        messages.forEach(message => {
            // Vérifier si le message n'existe pas déjà
            const existingMessage = chatMessages.querySelector(`[data-message-id="${message.id}"]`);
            if (existingMessage) return;
            
            // Créer le message
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message';
            messageDiv.setAttribute('data-message-id', message.id);
            
            messageDiv.innerHTML = `
                <div class="message-header">
                    <strong>${this.escapeHtml(message.username)}</strong>
                    <span class="message-time">${message.timestamp || ''}</span>
                </div>
                <div class="message-content">
                    ${this.escapeHtml(message.content)}
                </div>
            `;
            
            chatMessages.appendChild(messageDiv);
            
            // Animation
            messageDiv.style.opacity = '0';
            setTimeout(() => {
                messageDiv.style.transition = 'opacity 0.3s';
                messageDiv.style.opacity = '1';
            }, 10);
        });
        
        // Faire défiler vers le bas si on est près du bas
        this.scrollToBottomIfNeeded();
    }
    
    // Mettre à jour les listes d'utilisateurs
    updateUserLists(data) {
        // Mettre à jour la liste "En ligne"
        const onlineList = document.querySelector('.sidebar-section:first-child .user-list');
        if (onlineList && data.onlineUsers) {
            onlineList.innerHTML = '';
            
            if (data.onlineUsers.length > 0) {
                data.onlineUsers.forEach(user => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <span class="status-indicator ${user.status}"></span>
                        ${this.escapeHtml(user.username)}
                        ${user.permission == 1 ? '<span class="badge admin">Admin</span>' : ''}
                        ${user.permission == 2 ? '<span class="badge moderator">Mod</span>' : ''}
                    `;
                    onlineList.appendChild(li);
                });
            }
            
            // Mettre à jour le titre avec le compteur
            const onlineTitle = document.querySelector('.sidebar-section:first-child h3');
            if (onlineTitle) {
                onlineTitle.textContent = `En ligne (${data.onlineUsers.length})`;
            }
        }
        
        // Mettre à jour la liste "Tous les utilisateurs"
        const allUsersList = document.querySelector('.sidebar-section:nth-child(2) .user-list');
        if (allUsersList && data.allUsers) {
            allUsersList.innerHTML = '';
            
            if (data.allUsers.length > 0) {
                data.allUsers.forEach(user => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <span class="status-indicator ${user.status}"></span>
                        ${this.escapeHtml(user.username)}
                        ${user.permission == 1 ? '<span class="badge admin">Admin</span>' : ''}
                        ${user.permission == 2 ? '<span class="badge moderator">Mod</span>' : ''}
                        ${user.status === 'banned' ? '<span class="badge banned">Banni</span>' : ''}
                    `;
                    allUsersList.appendChild(li);
                });
            }
        }
    }
    
    // Mettre à jour le statut de l'utilisateur courant dans l'interface
    updateCurrentUserStatus(status) {
        // Mettre à jour l'indicateur dans la sidebar
        const userStatusIndicator = document.querySelector('.user-status .status-indicator');
        if (userStatusIndicator) {
            userStatusIndicator.className = 'status-indicator';
            userStatusIndicator.classList.add(status);
        }
        
        // Mettre à jour le select de statut
        const statusSelect = document.querySelector('select[name="status"]');
        if (statusSelect) {
            statusSelect.value = status;
        }
    }
    
    // Faire défiler vers le bas si nécessaire
    scrollToBottomIfNeeded() {
        const chatMessages = document.getElementById('chat-messages');
        if (!chatMessages) return;
        
        // Si l'utilisateur est près du bas (à moins de 100px), faire défiler
        const scrollThreshold = 100;
        const distanceFromBottom = chatMessages.scrollHeight - chatMessages.scrollTop - chatMessages.clientHeight;
        
        if (distanceFromBottom <= scrollThreshold) {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
    }
    
    // Échapper le HTML pour la sécurité
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // Envoyer un message via AJAX
    async sendMessage(content) {
        try {
            const response = await fetch(`${this.contextPath}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `message=${encodeURIComponent(content)}`
            });
            
            if (response.ok) {
                // Le message sera récupéré lors de la prochaine mise à jour
                return true;
            } else {
                console.error('Erreur lors de l\'envoi du message');
                return false;
            }
        } catch (error) {
            console.error('Erreur réseau:', error);
            return false;
        }
    }
}

// Initialisation globale
let chatUpdater = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Initialisation du chat AJAX...');
    
    // Créer et démarrer le système de mise à jour
    chatUpdater = new ChatUpdater();
    chatUpdater.startPolling();
    
    // Intercepter l'envoi du formulaire de message
    const messageForm = document.getElementById('message-form');
    if (messageForm) {
        messageForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const input = this.querySelector('input[name="message"]');
            if (!input) return;
            
            const message = input.value.trim();
            if (message && chatUpdater) {
                // Désactiver temporairement le bouton
                const submitBtn = this.querySelector('button[type="submit"]');
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.textContent = 'Envoi...';
                
                // Envoyer le message
                const success = await chatUpdater.sendMessage(message);
                
                if (success) {
                    input.value = ''; // Vider le champ
                    input.focus();
                } else {
                    alert('Erreur lors de l\'envoi du message');
                }
                
                // Réactiver le bouton
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
            }
        });
    }
    
    // Gérer la déconnexion (arrêter le polling)
    const logoutLink = document.querySelector('a[href*="logout"]');
    if (logoutLink) {
        logoutLink.addEventListener('click', function() {
            if (chatUpdater) {
                chatUpdater.stopPolling();
            }
        });
    }
    
    // Arrêter le polling quand la page n'est plus visible
    document.addEventListener('visibilitychange', function() {
        if (chatUpdater) {
            if (document.hidden) {
                chatUpdater.stopPolling();
            } else {
                chatUpdater.startPolling();
            }
        }
    });
});