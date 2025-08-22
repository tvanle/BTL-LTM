class GameWebSocket {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.messageHandlers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }

    connect() {
        try {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = `${protocol}//${window.location.host}/game`;
            
            this.socket = new WebSocket(wsUrl);
            
            this.socket.onopen = (event) => {
                this.isConnected = true;
                this.reconnectAttempts = 0;
                console.log('WebSocket connected');
                this.handleOpen(event);
            };
            
            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (error) {
                    console.error('Error parsing WebSocket message:', error);
                }
            };
            
            this.socket.onclose = (event) => {
                this.isConnected = false;
                console.log('WebSocket disconnected');
                this.handleClose(event);
                this.attemptReconnect();
            };
            
            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.handleError(error);
            };
            
        } catch (error) {
            console.error('Failed to connect WebSocket:', error);
            this.attemptReconnect();
        }
    }

    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
            this.isConnected = false;
        }
    }

    send(message) {
        if (this.isConnected && this.socket) {
            try {
                const jsonMessage = JSON.stringify(message);
                this.socket.send(jsonMessage);
                return true;
            } catch (error) {
                console.error('Error sending WebSocket message:', error);
                return false;
            }
        }
        return false;
    }

    onMessage(type, handler) {
        this.messageHandlers.set(type, handler);
    }

    offMessage(type) {
        this.messageHandlers.delete(type);
    }

    handleOpen(event) {
        // Override in subclass or use event listeners
        this.emit('open', event);
    }

    handleClose(event) {
        // Override in subclass or use event listeners
        this.emit('close', event);
    }

    handleError(error) {
        // Override in subclass or use event listeners
        this.emit('error', error);
    }

    handleMessage(message) {
        const handler = this.messageHandlers.get(message.type);
        if (handler) {
            handler(message);
        } else {
            console.warn('No handler for message type:', message.type);
        }
        
        // Also emit generic message event
        this.emit('message', message);
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * this.reconnectAttempts;
            
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${delay}ms`);
            
            setTimeout(() => {
                this.connect();
            }, delay);
        } else {
            console.error('Max reconnection attempts reached');
            this.emit('maxReconnectAttemptsReached');
        }
    }

    // Simple event emitter functionality
    emit(eventName, data) {
        if (this.eventListeners && this.eventListeners[eventName]) {
            this.eventListeners[eventName].forEach(listener => {
                try {
                    listener(data);
                } catch (error) {
                    console.error('Error in event listener:', error);
                }
            });
        }
    }

    on(eventName, listener) {
        if (!this.eventListeners) {
            this.eventListeners = {};
        }
        if (!this.eventListeners[eventName]) {
            this.eventListeners[eventName] = [];
        }
        this.eventListeners[eventName].push(listener);
    }

    off(eventName, listener) {
        if (this.eventListeners && this.eventListeners[eventName]) {
            const index = this.eventListeners[eventName].indexOf(listener);
            if (index !== -1) {
                this.eventListeners[eventName].splice(index, 1);
            }
        }
    }

    // Utility methods
    joinGame(gameId, playerName) {
        return this.send({
            type: 'JOIN_GAME',
            gameId: gameId,
            playerName: playerName
        });
    }

    placeCharacter(gameId, playerId, row, col, character) {
        return this.send({
            type: 'PLACE_CHAR',
            gameId: gameId,
            playerId: playerId,
            row: row,
            col: col,
            character: character
        });
    }

    sendPing(playerId) {
        return this.send({
            type: 'PING',
            playerId: playerId
        });
    }
}

// Export for use in other scripts
window.GameWebSocket = GameWebSocket;