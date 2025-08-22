class CrosswordGame {
    constructor() {
        this.gameId = null;
        this.playerId = null;
        this.playerName = null;
        this.gameState = null;
        this.websocket = null;
        this.board = null;
        this.timer = null;
        
        this.initializeComponents();
        this.setupEventListeners();
        this.setupWebSocket();
    }

    initializeComponents() {
        // Initialize crossword board
        this.board = new CrosswordBoard('game-canvas', 15);
        
        // Initialize UI elements
        this.timerDisplay = document.getElementById('timer-display');
        this.statusDisplay = document.getElementById('status-display');
        this.messagesContainer = document.getElementById('messages');
        
        // Player info elements
        this.player1Name = document.getElementById('player1-name');
        this.player1Score = document.getElementById('player1-score');
        this.player1Words = document.getElementById('player1-words');
        this.player2Name = document.getElementById('player2-name');
        this.player2Score = document.getElementById('player2-score');
        this.player2Words = document.getElementById('player2-words');
        
        // Input elements
        this.charInput = document.getElementById('char-input');
        this.submitButton = document.getElementById('submit-char');
        
        // Modal elements
        this.joinModal = document.getElementById('join-modal');
        this.playerNameInput = document.getElementById('player-name-input');
        this.gameIdInput = document.getElementById('game-id-input');
    }

    setupEventListeners() {
        // Game action buttons
        document.getElementById('new-game-btn').addEventListener('click', () => {
            this.createNewGame();
        });
        
        document.getElementById('join-game-btn').addEventListener('click', () => {
            this.showJoinModal();
        });
        
        // Character input and submission
        this.charInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                this.submitCharacter();
            }
        });
        
        this.charInput.addEventListener('input', (event) => {
            // Only allow single letters
            let value = event.target.value.toUpperCase();
            if (value.length > 1) {
                value = value.charAt(value.length - 1);
            }
            event.target.value = value.replace(/[^A-Z]/g, '');
        });
        
        this.submitButton.addEventListener('click', () => {
            this.submitCharacter();
        });
        
        // Modal events
        document.getElementById('join-confirm-btn').addEventListener('click', () => {
            this.joinGame();
        });
        
        document.getElementById('join-cancel-btn').addEventListener('click', () => {
            this.hideJoinModal();
        });
        
        // Close modal on outside click
        this.joinModal.addEventListener('click', (event) => {
            if (event.target === this.joinModal) {
                this.hideJoinModal();
            }
        });
    }

    setupWebSocket() {
        this.websocket = new GameWebSocket();
        
        // Setup message handlers
        this.websocket.onMessage('GAME_STATE', (message) => {
            this.handleGameState(message);
        });
        
        this.websocket.onMessage('CHAR_VALIDATED', (message) => {
            this.handleCharacterValidated(message);
        });
        
        this.websocket.onMessage('CHAR_REJECTED', (message) => {
            this.handleCharacterRejected(message);
        });
        
        this.websocket.onMessage('SCORE_UPDATE', (message) => {
            this.handleScoreUpdate(message);
        });
        
        this.websocket.onMessage('GAME_STARTED', (message) => {
            this.handleGameStarted(message);
        });
        
        this.websocket.onMessage('GAME_ENDED', (message) => {
            this.handleGameEnded(message);
        });
        
        this.websocket.onMessage('PLAYER_JOINED', (message) => {
            this.handlePlayerJoined(message);
        });
        
        this.websocket.onMessage('ERROR', (message) => {
            this.showMessage(message.data, 'error');
        });
        
        // Setup connection events
        this.websocket.on('open', () => {
            this.showMessage('Connected to server', 'success');
        });
        
        this.websocket.on('close', () => {
            this.showMessage('Disconnected from server', 'warning');
        });
        
        this.websocket.on('error', () => {
            this.showMessage('Connection error', 'error');
        });
        
        // Connect to WebSocket
        this.websocket.connect();
    }

    createNewGame() {
        fetch('/api/games', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            this.gameId = data.gameId;
            this.showMessage(`New game created: ${this.gameId}`, 'success');
            this.showJoinModal(this.gameId);
        })
        .catch(error => {
            console.error('Error creating game:', error);
            this.showMessage('Failed to create game', 'error');
        });
    }

    showJoinModal(gameId = '') {
        this.gameIdInput.value = gameId;
        this.joinModal.style.display = 'block';
        this.playerNameInput.focus();
    }

    hideJoinModal() {
        this.joinModal.style.display = 'none';
        this.playerNameInput.value = '';
        this.gameIdInput.value = '';
    }

    joinGame() {
        const playerName = this.playerNameInput.value.trim();
        const gameId = this.gameIdInput.value.trim();
        
        if (!playerName) {
            this.showMessage('Please enter your name', 'error');
            return;
        }
        
        this.playerName = playerName;
        this.gameId = gameId || this.gameId;
        
        if (this.websocket.isConnected) {
            this.websocket.joinGame(this.gameId, playerName);
            this.hideJoinModal();
        } else {
            this.showMessage('Not connected to server', 'error');
        }
    }

    submitCharacter() {
        const selectedCell = this.board.getSelectedCell();
        if (!selectedCell) {
            this.showMessage('Please select a cell first', 'warning');
            return;
        }
        
        const character = this.charInput.value.trim().toUpperCase();
        if (!character) {
            this.showMessage('Please enter a letter', 'warning');
            return;
        }
        
        if (!this.board.canPlaceCharacter(selectedCell.row, selectedCell.col)) {
            this.showMessage('Cannot place character in this cell', 'error');
            return;
        }
        
        if (this.websocket.isConnected && this.gameId && this.playerId) {
            this.board.placeCharacter(selectedCell.row, selectedCell.col, character, this.playerId);
            this.websocket.placeCharacter(this.gameId, this.playerId, selectedCell.row, selectedCell.col, character);
            this.charInput.value = '';
        } else {
            this.showMessage('Not connected to game', 'error');
        }
    }

    handleGameState(message) {
        this.gameState = message.data;
        this.gameId = this.gameState.gameId;
        
        // Find current player
        const players = Object.values(this.gameState.players);
        const currentPlayer = players.find(p => p.name === this.playerName);
        if (currentPlayer) {
            this.playerId = currentPlayer.id;
        }
        
        // Update board
        this.board.updateGrid(this.gameState.grid);
        
        // Update UI
        this.updateGameStatus();
        this.updatePlayerInfo();
        this.updateTimer();
    }

    handleCharacterValidated(message) {
        const moveData = message.data;
        this.board.validateCharacter(moveData.row, moveData.col, moveData.character);
        this.showMessage('Character placed!', 'success');
    }

    handleCharacterRejected(message) {
        const moveData = message.data;
        this.board.rejectCharacter(moveData.row, moveData.col);
        this.showMessage('Invalid character!', 'error');
    }

    handleScoreUpdate(message) {
        this.gameState.players = message.data;
        this.updatePlayerInfo();
    }

    handleGameStarted(message) {
        this.gameState = message.data;
        this.showMessage('Game started!', 'success');
        this.updateGameStatus();
        this.startTimer();
    }

    handleGameEnded(message) {
        this.gameState = message.data;
        this.updateGameStatus();
        this.stopTimer();
        
        const winnerId = this.gameState.winnerId;
        if (winnerId) {
            const winner = this.gameState.players[winnerId];
            if (winner) {
                if (winnerId === this.playerId) {
                    this.showMessage('You won! 🎉', 'success');
                } else {
                    this.showMessage(`${winner.name} won!`, 'info');
                }
            }
        } else {
            this.showMessage('Game ended in a tie!', 'info');
        }
    }

    handlePlayerJoined(message) {
        const player = message.data;
        this.showMessage(`${player.name} joined the game`, 'info');
        
        // Request updated game state
        if (this.gameId) {
            fetch(`/api/games/${this.gameId}/status`)
                .then(response => response.json())
                .then(data => {
                    this.updateGameStatus();
                })
                .catch(error => console.error('Error fetching game status:', error));
        }
    }

    updateGameStatus() {
        if (!this.gameState) return;
        
        let statusText = 'Unknown';
        switch (this.gameState.status) {
            case 'WAITING':
                statusText = `Waiting for players (${Object.keys(this.gameState.players).length}/2)`;
                break;
            case 'IN_PROGRESS':
                statusText = 'Game in progress';
                break;
            case 'FINISHED':
                statusText = 'Game finished';
                break;
        }
        
        this.statusDisplay.textContent = statusText;
    }

    updatePlayerInfo() {
        if (!this.gameState || !this.gameState.players) return;
        
        const players = Object.values(this.gameState.players);
        
        // Reset displays
        this.player1Name.textContent = '-';
        this.player1Score.textContent = '0';
        this.player1Words.textContent = '0';
        this.player2Name.textContent = '-';
        this.player2Score.textContent = '0';
        this.player2Words.textContent = '0';
        
        // Update player 1
        if (players[0]) {
            this.player1Name.textContent = players[0].name;
            this.player1Score.textContent = players[0].totalScore;
            this.player1Words.textContent = players[0].completedWords;
            
            const card1 = document.getElementById('player1-card');
            if (players[0].id === this.playerId) {
                card1.classList.add('active');
            } else {
                card1.classList.remove('active');
            }
        }
        
        // Update player 2
        if (players[1]) {
            this.player2Name.textContent = players[1].name;
            this.player2Score.textContent = players[1].totalScore;
            this.player2Words.textContent = players[1].completedWords;
            
            const card2 = document.getElementById('player2-card');
            if (players[1].id === this.playerId) {
                card2.classList.add('active');
            } else {
                card2.classList.remove('active');
            }
        }
    }

    updateTimer() {
        if (!this.gameState) return;
        
        const remainingTime = this.gameState.remainingTimeSeconds || 0;
        const minutes = Math.floor(remainingTime / 60);
        const seconds = remainingTime % 60;
        
        this.timerDisplay.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // Change color when time is running low
        if (remainingTime <= 30) {
            this.timerDisplay.parentElement.style.backgroundColor = '#ff4757';
        } else if (remainingTime <= 60) {
            this.timerDisplay.parentElement.style.backgroundColor = '#ffa502';
        } else {
            this.timerDisplay.parentElement.style.backgroundColor = '#ff6b6b';
        }
    }

    startTimer() {
        this.stopTimer(); // Clear any existing timer
        
        this.timer = setInterval(() => {
            if (this.gameState) {
                this.gameState.remainingTimeSeconds = Math.max(0, this.gameState.remainingTimeSeconds - 1);
                this.updateTimer();
                
                if (this.gameState.remainingTimeSeconds <= 0) {
                    this.stopTimer();
                }
            }
        }, 1000);
    }

    stopTimer() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
    }

    showMessage(text, type = 'info') {
        const message = document.createElement('div');
        message.className = `message ${type}`;
        message.textContent = text;
        
        this.messagesContainer.appendChild(message);
        
        // Auto-remove message after 5 seconds
        setTimeout(() => {
            if (message.parentNode) {
                message.parentNode.removeChild(message);
            }
        }, 5000);
    }
}

// Initialize game when page loads
document.addEventListener('DOMContentLoaded', () => {
    window.gameInstance = new CrosswordGame();
});