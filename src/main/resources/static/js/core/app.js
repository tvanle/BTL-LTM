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
        this.showScreen('menu');
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
        
        try {
            const response = await fetch('/api/rooms/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    playerName: hostName,
                    topic: topic,
                    levelCount: parseInt(levelCount),
                    levelDuration: parseInt(levelDuration)
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                this.playerInfo = {
                    id: data.playerId,
                    name: data.playerName,
                    isHost: true
                };
                this.roomInfo = {
                    roomCode: data.roomCode,
                    topic: data.topic
                };
                
                // Connect WebSocket
                if (!window.websocketClient) {
                    window.websocketClient = new WebSocketClient();
                }
                window.websocketClient.connect();
                
                // Show lobby
                this.showLobby();
            } else {
                alert('Failed to create room');
            }
        } catch (error) {
            console.error('Error creating room:', error);
            alert('Error creating room');
        }
    }
    
    async joinRoom() {
        const playerName = document.getElementById('player-name').value;
        const roomCode = document.getElementById('room-code').value.toUpperCase();
        
        try {
            const response = await fetch('/api/rooms/join', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    playerName: playerName,
                    roomCode: roomCode
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                this.playerInfo = {
                    id: data.playerId,
                    name: data.playerName,
                    isHost: false
                };
                this.roomInfo = {
                    roomCode: data.roomCode,
                    topic: data.topic
                };
                
                // Connect WebSocket
                if (!window.websocketClient) {
                    window.websocketClient = new WebSocketClient();
                }
                window.websocketClient.connect();
                
                // Send join message
                setTimeout(() => {
                    window.websocketClient.send({
                        type: 'JOIN_ROOM',
                        data: {
                            roomCode: roomCode,
                            playerName: playerName
                        }
                    });
                }, 1000);
                
                // Show lobby
                this.showLobby();
            } else {
                alert('Failed to join room');
            }
        } catch (error) {
            console.error('Error joining room:', error);
            alert('Error joining room');
        }
    }
    
    showLobby() {
        this.showScreen('lobby');
        document.getElementById('lobby-room-code').textContent = this.roomInfo.roomCode;
        document.getElementById('lobby-topic').textContent = this.roomInfo.topic;
        
        if (this.playerInfo.isHost) {
            document.getElementById('start-game-btn').style.display = 'inline-block';
        }
    }
    
    toggleReady() {
        const btn = document.getElementById('ready-btn');
        const isReady = btn.textContent === 'Ready';
        
        window.websocketClient.send({
            type: 'PLAYER_READY',
            data: {
                ready: !isReady
            }
        });
        
        btn.textContent = isReady ? 'Not Ready' : 'Ready';
        btn.classList.toggle('btn-success', !isReady);
    }
    
    startGame() {
        if (this.playerInfo.isHost) {
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
        if (window.gridRenderer && window.gridRenderer.selectedPath.length > 0) {
            const word = window.gridRenderer.getSelectedWord();
            const path = window.gridRenderer.selectedPath;
            
            window.websocketClient.send({
                type: 'SUBMIT_WORD',
                data: {
                    word: word,
                    path: path.map(cell => ({
                        row: cell.row,
                        col: cell.col,
                        char: cell.character
                    }))
                }
            });
            
            window.gridRenderer.clearSelection();
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
        playerList.innerHTML = '';
        
        players.forEach(player => {
            const playerItem = document.createElement('div');
            playerItem.className = 'player-item';
            if (player.ready) {
                playerItem.classList.add('ready');
            }
            
            playerItem.innerHTML = `
                <span>${player.name}</span>
                <span>${player.ready ? '✓ Ready' : 'Not Ready'}</span>
            `;
            
            playerList.appendChild(playerItem);
        });
        
        document.getElementById('player-count').textContent = players.length;
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
                document.body.removeChild(overlay);
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
        
        // Load level data
        window.gameController.loadLevel(levelData);
        window.gridRenderer.setGrid(levelData.grid);
        
        // Update UI
        document.getElementById('current-level').textContent = levelData.level;
        document.getElementById('total-levels').textContent = this.roomInfo.levelCount || 10;
        
        // Start timer
        this.startTimer(levelData.duration);
    }
    
    startTimer(duration) {
        let timeRemaining = duration;
        const timerDisplay = document.getElementById('timer-display');
        
        const updateTimer = () => {
            timerDisplay.textContent = timeRemaining;
            
            if (timeRemaining <= 5) {
                timerDisplay.style.color = '#dc3545';
            } else if (timeRemaining <= 10) {
                timerDisplay.style.color = '#ffc107';
            } else {
                timerDisplay.style.color = '#28a745';
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
        leaderboardList.innerHTML = '';
        
        leaderboardData.leaderboard.forEach(entry => {
            const item = document.createElement('div');
            item.className = 'leaderboard-item';
            
            if (entry.playerId === this.playerInfo.id) {
                item.classList.add('current-player');
            }
            
            item.innerHTML = `
                <span>${entry.rank}. ${entry.name}</span>
                <span>${entry.score}</span>
            `;
            
            leaderboardList.appendChild(item);
        });
        
        // Update player score
        const playerEntry = leaderboardData.leaderboard.find(e => e.playerId === this.playerInfo.id);
        if (playerEntry) {
            document.getElementById('player-score').textContent = playerEntry.score;
        }
    }
    
    showResults(resultsData) {
        this.showScreen('results');
        
        const finalLeaderboard = document.getElementById('final-leaderboard');
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
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new App();
});