// WebSocket Client
class WebSocketClient {
    constructor() {
        this.ws = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }
    
    connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host;
        const url = `${protocol}//${host}/game-websocket`;
        
        console.log('Connecting to WebSocket:', url);
        
        try {
            this.ws = new WebSocket(url);
            
            this.ws.onopen = () => {
                console.log('WebSocket connected');
                this.connected = true;
                this.reconnectAttempts = 0;
            };
            
            this.ws.onmessage = (event) => {
                this.handleMessage(event.data);
            };
            
            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
            
            this.ws.onclose = () => {
                console.log('WebSocket disconnected');
                this.connected = false;
                this.attemptReconnect();
            };
            
        } catch (error) {
            console.error('Failed to create WebSocket:', error);
        }
    }
    
    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.connected = false;
    }
    
    send(message) {
        if (this.connected && this.ws && this.ws.readyState === WebSocket.OPEN) {
            if (window.app && window.app.playerInfo) {
                message.playerId = window.app.playerInfo.id;
            }
            if (window.app && window.app.roomInfo) {
                message.roomCode = window.app.roomInfo.roomCode;
            }
            
            const jsonMessage = JSON.stringify(message);
            console.log('Sending message:', jsonMessage);
            this.ws.send(jsonMessage);
        } else {
            console.warn('WebSocket not connected');
        }
    }
    
    handleMessage(data) {
        try {
            const message = JSON.parse(data);
            console.log('Received message:', message);
            
            switch (message.type) {
                case 'PLAYER_JOINED':
                    this.handlePlayerJoined(message.data);
                    break;
                    
                case 'PLAYER_LEFT':
                    this.handlePlayerLeft(message.data);
                    break;
                    
                case 'PLAYER_READY':
                    this.handlePlayerReady(message.data);
                    break;
                    
                case 'ROOM_STATE':
                    this.handleRoomState(message.data);
                    break;
                    
                case 'GAME_STARTING':
                    this.handleGameStarting(message.data);
                    break;
                    
                case 'LEVEL_START':
                    this.handleLevelStart(message.data);
                    break;
                    
                case 'WORD_ACCEPTED':
                    this.handleWordAccepted(message.data);
                    break;
                    
                case 'WORD_REJECTED':
                    this.handleWordRejected(message.data);
                    break;
                    
                case 'OPPONENT_SCORED':
                    this.handleOpponentScored(message.data);
                    break;
                    
                case 'LEADERBOARD_UPDATE':
                    this.handleLeaderboardUpdate(message.data);
                    break;
                    
                case 'LEVEL_END':
                    this.handleLevelEnd(message.data);
                    break;
                    
                case 'GAME_END':
                    this.handleGameEnd(message.data);
                    break;
                    
                case 'EFFECT_RECEIVED':
                    this.handleEffectReceived(message.data);
                    break;
                    
                case 'ERROR':
                    this.handleError(message.data);
                    break;

                case 'INVALID_ACTION':
                    this.handleInvalidAction(message.data);
                    break;
                    
                default:
                    console.log('Unknown message type:', message.type);
            }
            
        } catch (error) {
            console.error('Error handling message:', error);
        }
    }
    
    handlePlayerJoined(data) {
        console.log('Player joined:', data);
        // Refresh lobby info for all clients to update counts and possibly host changes
        if (window.app && window.app.refreshRoomInfo) {
            window.app.refreshRoomInfo();
        }
    }
    
    handlePlayerLeft(data) {
        console.log('Player left:', data);
        // Refresh lobby info for all clients to update counts and possibly host changes
        if (window.app && window.app.refreshRoomInfo) {
            window.app.refreshRoomInfo();
        }
    }
    
    handlePlayerReady(data) {
        console.log('Player ready status:', data);
        // Refresh lobby info for all clients (counts may be shown in combined label)
        if (window.app && window.app.refreshRoomInfo) {
            window.app.refreshRoomInfo();
        }
    }

    handleRoomState(data) {
        console.log('Room state:', data);
        if (!window.app) return;
        // Update host in app state for Start button toggling
        if (!window.app.roomInfo) window.app.roomInfo = {};
        window.app.roomInfo.hostId = data.hostId;
        window.app.roomInfo.maxPlayers = data.maxPlayers;
        // Update player count/label
        const playersCountEl = document.getElementById('player-count');
        const playersMaxEl = document.getElementById('player-max');
        if (playersCountEl) playersCountEl.textContent = data.playersCount ?? (data.players ? data.players.length : '');
        if (playersMaxEl) playersMaxEl.textContent = data.maxPlayers;
        const playersLabel = document.getElementById('players-label');
        if (playersLabel) playersLabel.textContent = `Players: ${(data.playersCount ?? data.players.length)}/${data.maxPlayers}`;
        // Toggle Start button visibility based on ownership
        const startBtn = document.getElementById('start-game-btn');
        if (startBtn && window.app.playerInfo) {
            startBtn.style.display = window.app.playerInfo.id === data.hostId ? 'inline-block' : 'none';
        }
        // Update player list UI
        if (Array.isArray(data.players) && window.app.updatePlayerList) {
            window.app.updatePlayerList(data.players.map(p => ({
                id: p.id,
                name: p.name,
                ready: !!p.ready
            })));
        }
    }
    
    handleGameStarting(data) {
        console.log('Game starting:', data);
        if (window.app) {
            window.app.startGameCountdown(data.countdown || 5);
        }
    }
    
    handleLevelStart(data) {
        console.log('Level started:', data);
        if (window.app) {
            window.app.showGame(data);
        }
    }
    
    handleWordAccepted(data) {
        console.log('Word accepted:', data);
        this.showNotification(`✓ Correct! +${data.points} points`, 'success');
        
        // Update current word display
        const currentWordDiv = document.getElementById('current-word');
        if (currentWordDiv) {
            currentWordDiv.style.color = '#28a745';
            setTimeout(() => {
                currentWordDiv.textContent = '';
                currentWordDiv.style.color = '#333';
            }, 1000);
        }
    }
    
    handleWordRejected(data) {
        console.log('Word rejected:', data);
        this.showNotification(`✗ ${data.reason || 'Incorrect'}`, 'error');
        
        // Update current word display
        const currentWordDiv = document.getElementById('current-word');
        if (currentWordDiv) {
            currentWordDiv.style.color = '#dc3545';
            setTimeout(() => {
                currentWordDiv.textContent = '';
                currentWordDiv.style.color = '#333';
            }, 1000);
        }
    }
    
    handleOpponentScored(data) {
        console.log('Opponent scored:', data);
        this.showNotification(`${data.playerName || 'Opponent'} scored ${data.points} points!`, 'info');
    }
    
    handleLeaderboardUpdate(data) {
        console.log('Leaderboard update:', data);
        if (window.app) {
            window.app.updateLeaderboard(data);
        }
    }
    
    handleLevelEnd(data) {
        console.log('Level ended:', data);
        this.showNotification('Level Complete!', 'info');
        
        // Show level results
        if (data.results) {
            // Display level results overlay
        }
    }
    
    handleGameEnd(data) {
        console.log('Game ended:', data);
        if (window.app) {
            window.app.showResults(data);
        }
    }
    
    handleEffectReceived(data) {
        console.log('Effect received:', data);
        
        if (data.effect === 'FREEZE') {
            this.applyFreezeEffect(data.duration);
        }
    }
    
    handleError(data) {
        console.error('Server error:', data);
        this.showNotification(data.error || 'An error occurred', 'error');
    }

    handleInvalidAction(data) {
        console.warn('Invalid action:', data);
        const reason = (data && (data.reason || data.error)) || 'Hành động không hợp lệ';
        this.showNotification(reason, 'warning');
    }
    
    applyFreezeEffect(duration) {
        const canvas = document.getElementById('game-grid');
        if (canvas) {
            canvas.style.pointerEvents = 'none';
            canvas.style.opacity = '0.5';
            
            this.showNotification(`Frozen for ${duration / 1000} seconds!`, 'warning');
            
            setTimeout(() => {
                canvas.style.pointerEvents = 'auto';
                canvas.style.opacity = '1';
                this.showNotification('Unfrozen!', 'success');
            }, duration);
        }
    }
    
    showNotification(message, type) {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: bold;
            z-index: 10000;
            animation: slideIn 0.3s ease;
        `;
        
        // Set background color based on type
        switch (type) {
            case 'success':
                notification.style.background = '#28a745';
                break;
            case 'error':
                notification.style.background = '#dc3545';
                break;
            case 'warning':
                notification.style.background = '#ffc107';
                break;
            case 'info':
            default:
                notification.style.background = '#17a2b8';
        }
        
        notification.textContent = message;
        document.body.appendChild(notification);
        
        // Remove notification after 3 seconds
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }
    
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
            this.showNotification('Connection lost. Please refresh the page.', 'error');
        }
    }
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);