// Main Application Controller
class App {
    constructor() {
        this.currentScreen = 'menu';
        this.playerInfo = null;
        this.roomInfo = null;
        this.gameState = null;
        
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.loadTopics();
        this.showScreen('menu');
    }
    
    async loadTopics() {
        try {
            const response = await fetch('/api/topics');
            if (response.ok) {
                const topics = await response.json();
                const topicSelect = document.getElementById('topic');
                
                // Clear existing options except the first placeholder
                topicSelect.innerHTML = '<option value="">Select Topic</option>';
                
                // Add topics from API
                topics.forEach(topic => {
                    const option = document.createElement('option');
                    option.value = topic.id;
                    option.textContent = `${topic.name} (${topic.difficulty})`;
                    topicSelect.appendChild(option);
                });
                
                console.log(`Loaded ${topics.length} topics from server`);
            } else {
                console.error('Failed to load topics:', response.status);
            }
        } catch (error) {
            console.error('Error loading topics:', error);
            // Keep hardcoded topics as fallback
        }
    }
    
    setupEventListeners() {
        // Menu buttons
        document.getElementById('create-room-btn').addEventListener('click', () => {
            this.showScreen('create-room');
        });
        
        document.getElementById('join-room-btn').addEventListener('click', () => {
            this.showScreen('join-room');
        });
        
        // Back buttons
        document.querySelectorAll('.back-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                this.showScreen('menu');
            });
        });
        
        // Create room form
        document.getElementById('create-room-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.createRoom();
        });
        
        // Join room form
        document.getElementById('join-room-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.joinRoom();
        });
        
        // Lobby buttons
        document.getElementById('ready-btn').addEventListener('click', () => {
            this.toggleReady();
        });
        
        document.getElementById('start-game-btn').addEventListener('click', () => {
            this.startGame();
        });
        
        document.getElementById('leave-room-btn').addEventListener('click', () => {
            this.leaveRoom();
        });
        
        // Game controls
        document.getElementById('submit-word-btn').addEventListener('click', () => {
            this.submitWord();
        });
        
        document.getElementById('clear-selection-btn').addEventListener('click', () => {
            if (window.gridRenderer) {
                window.gridRenderer.clearSelection();
            }
        });
        
        // Results screen
        document.getElementById('play-again-btn').addEventListener('click', () => {
            this.playAgain();
        });
        
        document.getElementById('main-menu-btn').addEventListener('click', () => {
            this.returnToMenu();
        });
    }
    
    showScreen(screenName) {
        // Hide all screens
        document.querySelectorAll('.screen').forEach(screen => {
            screen.classList.remove('active');
        });
        
        // Show requested screen
        const screen = document.getElementById(`${screenName}-screen`);
        if (screen) {
            screen.classList.add('active');
            this.currentScreen = screenName;
        }
    }
    
    async createRoom() {
        const hostName = document.getElementById('host-name').value;
        const topic = document.getElementById('topic').value;
        const levelCount = document.getElementById('level-count').value || 10;
        const levelDuration = document.getElementById('level-duration').value || 30;
        
        if (!hostName || !topic) {
            alert('Please enter your name and select a topic');
            return;
        }
        
        // Store temporarily to use after connection
        this.pendingCreateRoom = {
            playerName: hostName,
            topic: topic,
            levelCount: parseInt(levelCount),
            levelDuration: parseInt(levelDuration)
        };
        
        // Connect WebSocket first
        if (!window.websocketClient || !window.websocketClient.connected) {
            window.websocketClient = new WebSocketClient();
            window.websocketClient.connect();
            
            // Wait for connection then send create room message
            setTimeout(() => {
                this.sendCreateRoomMessage();
            }, 500);
        } else {
            this.sendCreateRoomMessage();
        }
    }
    
    sendCreateRoomMessage() {
        if (this.pendingCreateRoom) {
            window.websocketClient.send({
                type: 'CREATE_ROOM',
                data: this.pendingCreateRoom
            });
            
            // Store player name for later use
            this.playerInfo = {
                name: this.pendingCreateRoom.playerName,
                isHost: true
            };
            
            delete this.pendingCreateRoom;
        }
    }
    
    async joinRoom() {
        const playerName = document.getElementById('player-name').value;
        const roomCode = document.getElementById('room-code').value.toUpperCase();
        
        if (!playerName || !roomCode) {
            alert('Please enter your name and room code');
            return;
        }
        
        // Store temporarily to use after connection
        this.pendingJoinRoom = {
            playerName: playerName,
            roomCode: roomCode
        };
        
        // Connect WebSocket first
        if (!window.websocketClient || !window.websocketClient.connected) {
            window.websocketClient = new WebSocketClient();
            window.websocketClient.connect();
            
            // Wait for connection then send join room message
            setTimeout(() => {
                this.sendJoinRoomMessage();
            }, 500);
        } else {
            this.sendJoinRoomMessage();
        }
    }
    
    sendJoinRoomMessage() {
        if (this.pendingJoinRoom) {
            window.websocketClient.send({
                type: 'JOIN_ROOM',
                data: this.pendingJoinRoom
            });
            
            // Store player name for later use
            this.playerInfo = {
                name: this.pendingJoinRoom.playerName,
                isHost: false
            };
            
            delete this.pendingJoinRoom;
        }
    }
    
    showLobby() {
        this.showScreen('lobby');
        const codeEl = document.getElementById('lobby-room-code');
        if (codeEl) codeEl.textContent = this.roomInfo.roomCode;
        const topicEl = document.getElementById('lobby-topic');
        if (topicEl) topicEl.textContent = this.roomInfo.topic;
        
        // Reset ready button to initial state (not ready)
        const readyBtn = document.getElementById('ready-btn');
        if (readyBtn) {
            readyBtn.textContent = 'Ready';
            readyBtn.classList.remove('btn-success', 'btn-danger');
            readyBtn.classList.add('btn-primary');
        }
        
        // Hide Start by default; refreshRoomInfo will toggle based on hostId
        const startBtn = document.getElementById('start-game-btn');
        if (startBtn) {
            startBtn.style.display = 'none';
        }

        // Refresh players count/max and ownership
        this.refreshRoomInfo();
    }
    
    async refreshRoomInfo() {
        if (!this.roomInfo || !this.roomInfo.roomCode) return;
        try {
            const resp = await fetch(`/api/rooms/${this.roomInfo.roomCode}`);
            if (!resp.ok) return;
            const info = await resp.json();
            // Persist details
            this.roomInfo.maxPlayers = info.maxPlayers;
            this.roomInfo.hostId = info.hostId;
            const playersCountEl = document.getElementById('player-count');
            const playersMaxEl = document.getElementById('player-max');
            if (playersCountEl) playersCountEl.textContent = info.players;
            if (playersMaxEl) playersMaxEl.textContent = info.maxPlayers;
            // Optional combined label
            const playersLabel = document.getElementById('players-label');
            if (playersLabel) playersLabel.textContent = `Players: ${info.players}/${info.maxPlayers}`;
            
            // Toggle Start button visibility based on current host
            const startBtn = document.getElementById('start-game-btn');
            if (startBtn) {
                const isOwner = this.playerInfo && info.hostId && this.playerInfo.id === info.hostId;
                startBtn.style.display = isOwner ? 'inline-block' : 'none';
            }
        } catch (e) {
            console.warn('Failed to refresh room info', e);
        }
    }
    
    toggleReady() {
        const btn = document.getElementById('ready-btn');
        // Check if currently showing "Ready" (meaning player is NOT ready yet)
        const isCurrentlyNotReady = btn && btn.textContent === 'Ready';
        
        // Send the new ready state
        window.websocketClient.send({
            type: 'PLAYER_READY',
            data: {
                ready: isCurrentlyNotReady  // If showing "Ready", we want to become ready (true)
            }
        });
        
        if (btn) {
            // Update button to show opposite state
            if (isCurrentlyNotReady) {
                // Player is becoming ready
                btn.textContent = 'Cancel Ready';
                btn.classList.remove('btn-primary');
                btn.classList.add('btn-success');
            } else {
                // Player is canceling ready
                btn.textContent = 'Ready';
                btn.classList.remove('btn-success');
                btn.classList.add('btn-primary');
            }
        }
    }
    
    startGame() {
        if (this.playerInfo && this.playerInfo.id && this.roomInfo && this.roomInfo.hostId) {
            const isOwner = this.playerInfo.id === this.roomInfo.hostId;
            if (!isOwner) {
                this.showNotification('Only the room owner can start the game.', 'warning');
                return;
            }
        }
        
        if (window.websocketClient) {
            window.websocketClient.send({
                type: 'START_GAME',
                data: {}
            });
        }
    }
    
    leaveRoom() {
        window.websocketClient.send({
            type: 'LEAVE_ROOM',
            data: {}
        });
        
        this.showScreen('menu');
        this.playerInfo = null;
        this.roomInfo = null;
    }
    
    submitWord() {
        // Use game controller's submit method
        if (window.gameController) {
            window.gameController.submitWord();
        }
    }
    
    playAgain() {
        if (this.roomInfo) {
            this.showLobby();
        } else {
            this.showScreen('menu');
        }
    }
    
    returnToMenu() {
        if (window.websocketClient) {
            window.websocketClient.disconnect();
        }
        this.showScreen('menu');
        this.playerInfo = null;
        this.roomInfo = null;
        this.gameState = null;
    }
    
    updatePlayerList(players) {
        const playerList = document.getElementById('player-list');
        if (!playerList) return;
        playerList.innerHTML = '';
        
        players.forEach(player => {
            const playerItem = document.createElement('div');
            playerItem.className = 'player-item';
            if (player.ready) {
                playerItem.classList.add('ready');
            }
            const isHost = this.roomInfo && this.roomInfo.hostId === player.id;
            const isSelf = this.playerInfo && this.playerInfo.id === player.id;
            
            playerItem.innerHTML = `
                <span>${player.name}${isHost ? ' 👑' : ''}${isSelf ? ' (You)' : ''}</span>
                <span style="color: ${player.ready ? '#28a745' : '#6c757d'}; font-weight: ${player.ready ? 'bold' : 'normal'}">
                    ${player.ready ? '✅ Ready' : '⏳ Waiting'}
                </span>
            `;
            
            playerList.appendChild(playerItem);
        });
        
        const countEl = document.getElementById('player-count');
        if (countEl) countEl.textContent = players.length;
        const playersLabel = document.getElementById('players-label');
        if (playersLabel && this.roomInfo) {
            const max = this.roomInfo.maxPlayers || '';
            playersLabel.textContent = `Players: ${players.length}${max ? '/' + max : ''}`;
        }
        // Toggle Start button based on updated host info
        const startBtn = document.getElementById('start-game-btn');
        if (startBtn && this.playerInfo && this.roomInfo) {
            startBtn.style.display = this.playerInfo.id === this.roomInfo.hostId ? 'inline-block' : 'none';
        }
    }
    
    startGameCountdown(countdown) {
        // Show countdown overlay
        const overlay = document.createElement('div');
        overlay.className = 'countdown-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.8);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            color: white;
            font-size: 72px;
            font-weight: bold;
        `;
        overlay.textContent = countdown;
        document.body.appendChild(overlay);
        
        let count = countdown;
        const interval = setInterval(() => {
            count--;
            if (count > 0) {
                overlay.textContent = count;
            } else {
                clearInterval(interval);
                if (overlay.parentNode) {
                    document.body.removeChild(overlay);
                }
            }
        }, 1000);
    }
    
    showGame(levelData) {
        this.showScreen('game');
        
        // Initialize game components
        if (!window.gameController) {
            window.gameController = new GameController();
        }
        
        if (!window.gridRenderer) {
            const canvas = document.getElementById('game-grid');
            window.gridRenderer = new GridRenderer(canvas);
        }
        
        // Connect game controller with grid renderer
        window.gameController.setGridRenderer(window.gridRenderer);
        
        // Load level data
        window.gameController.loadLevel(levelData);
        
        // Update UI
        const currentLevelEl = document.getElementById('current-level');
        if (currentLevelEl) currentLevelEl.textContent = levelData.level;
        const totalLevelsEl = document.getElementById('total-levels');
        if (totalLevelsEl) totalLevelsEl.textContent = this.roomInfo.levelCount || 10;
        
        // Start timer
        this.startTimer(levelData.duration);
    }
    
    startTimer(duration) {
        let timeRemaining = duration;
        const timerDisplay = document.getElementById('timer-display');
        
        const updateTimer = () => {
            if (timerDisplay) timerDisplay.textContent = timeRemaining;
            
            if (timerDisplay) {
                if (timeRemaining <= 5) {
                    timerDisplay.style.color = '#dc3545';
                } else if (timeRemaining <= 10) {
                    timerDisplay.style.color = '#ffc107';
                } else {
                    timerDisplay.style.color = '#28a745';
                }
            }
            
            if (timeRemaining > 0) {
                timeRemaining--;
                setTimeout(updateTimer, 1000);
            }
        };
        
        updateTimer();
    }
    
    updateLeaderboard(leaderboardData) {
        const leaderboardList = document.getElementById('leaderboard-list');
        if (!leaderboardList) return;
        leaderboardList.innerHTML = '';
        
        leaderboardData.leaderboard.forEach(entry => {
            const item = document.createElement('div');
            item.className = 'leaderboard-item';
            
            if (entry.playerId === (this.playerInfo && this.playerInfo.id)) {
                item.classList.add('current-player');
            }
            
            item.innerHTML = `
                <span>${entry.rank}. ${entry.name}</span>
                <span>${entry.score}</span>
            `;
            
            leaderboardList.appendChild(item);
        });
        
        // Update player score
        const playerEntry = leaderboardData.leaderboard.find(e => e.playerId === (this.playerInfo && this.playerInfo.id));
        const scoreEl = document.getElementById('player-score');
        if (playerEntry && scoreEl) {
            scoreEl.textContent = playerEntry.score;
        }
    }
    
    showResults(resultsData) {
        this.showScreen('results');
        
        const finalLeaderboard = document.getElementById('final-leaderboard');
        if (!finalLeaderboard) return;
        finalLeaderboard.innerHTML = '<h3>Final Rankings</h3>';
        
        resultsData.leaderboard.forEach((entry, index) => {
            const item = document.createElement('div');
            item.className = 'leaderboard-item';
            
            let medal = '';
            if (index === 0) medal = '🥇 ';
            else if (index === 1) medal = '🥈 ';
            else if (index === 2) medal = '🥉 ';
            
            item.innerHTML = `
                <span>${medal}${entry.rank}. ${entry.name}</span>
                <span>${entry.score} points</span>
            `;
            
            finalLeaderboard.appendChild(item);
        });
    }
    
    showNotification(message, type) {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            padding: 15px 30px;
            border-radius: 8px;
            color: white;
            font-weight: bold;
            z-index: 10000;
            animation: slideDown 0.3s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
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
        
        // Remove notification after 4 seconds
        setTimeout(() => {
            notification.style.animation = 'slideUp 0.3s ease';
            setTimeout(() => {
                if (notification.parentNode) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 4000);
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new App();
});