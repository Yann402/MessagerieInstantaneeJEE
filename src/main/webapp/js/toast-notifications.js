// ===============================================
// SYSTÈME DE NOTIFICATIONS TOAST
// ===============================================

class ToastNotification {
    constructor() {
        this.createContainer();
    }
    
    createContainer() {
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
    }
    
    show(message, type = 'info', duration = 4000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        const icons = {
            success: '✓',
            error: '✗',
            warning: '⚠',
            info: 'ℹ'
        };
        
        const titles = {
            success: 'Succès',
            error: 'Erreur',
            warning: 'Attention',
            info: 'Information'
        };
        
        toast.innerHTML = `
            <div class="toast-icon">${icons[type] || icons.info}</div>
            <div class="toast-content">
                <div class="toast-title">${titles[type] || titles.info}</div>
                <div class="toast-message">${message}</div>
            </div>
        `;
        
        const container = document.getElementById('toast-container');
        container.appendChild(toast);
        
        // Auto-remove après la durée spécifiée
        setTimeout(() => {
            toast.classList.add('toast-hide');
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.remove();
                }
            }, 300);
        }, duration);
    }
    
    success(message, duration) {
        this.show(message, 'success', duration);
    }
    
    error(message, duration) {
        this.show(message, 'error', duration);
    }
    
    warning(message, duration) {
        this.show(message, 'warning', duration);
    }
    
    info(message, duration) {
        this.show(message, 'info', duration);
    }
}

// Instance globale
const toast = new ToastNotification();

// ===============================================
// SYSTÈME DE MODE SOMBRE/CLAIR
// ===============================================

class ThemeManager {
    constructor() {
        this.theme = localStorage.getItem('theme') || 'light';
        this.applyTheme();
        this.createToggleButton();
    }
    
    applyTheme() {
        document.documentElement.setAttribute('data-theme', this.theme);
    }
    
    toggleTheme() {
        this.theme = this.theme === 'light' ? 'dark' : 'light';
        localStorage.setItem('theme', this.theme);
        this.applyTheme();
        this.updateButtonIcon();
        // PAS DE NOTIFICATION TOAST ICI
    }
    
    createToggleButton() {
        // Vérifier si on est sur la page de chat
        const chatActions = document.querySelector('.chat-actions');
        if (!chatActions) return;
        
        const button = document.createElement('button');
        button.id = 'theme-toggle';
        button.type = 'button';
        button.title = 'Changer de thème';
        this.updateButtonIcon(button);
        
        button.addEventListener('click', () => this.toggleTheme());
        
        chatActions.insertBefore(button, chatActions.firstChild);
    }
    
    updateButtonIcon(button) {
        const btn = button || document.getElementById('theme-toggle');
        if (btn) {
            btn.textContent = this.theme === 'light' ? '🌙' : '☀️';
        }
    }
}

// Initialiser le thème au chargement
const themeManager = new ThemeManager();

// ===============================================
// EXPORT POUR UTILISATION GLOBALE
// ===============================================
window.toast = toast;
window.themeManager = themeManager;