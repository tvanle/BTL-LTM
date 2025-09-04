// Game Controller
class GameController {
    constructor() {
        this.currentLevel = null;
        this.gridData = null;
        this.timer = null;
        this.gridRenderer = null;
        this.boosters = this.initializeBoosters();
        this.wordTargets = [];
        this.completedWords = [];
    }
    
    initializeBoosters() {
        return {
            DOUBLE_UP: { available: 1, used: 0 },
            FREEZE: { available: 1, used: 0 },
            REVEAL: { available: 2, used: 0 },
            TIME_PLUS: { available: 2, used: 0 },
            SHIELD: { available: 1, used: 0 },
            STREAK_SAVE: { available: 1, used: 0 },
            SKIP_HALF: { available: 1, used: 0 }
        };
    }
    
    setGridRenderer(renderer) {
        this.gridRenderer = renderer;
    }
    
    loadLevel(levelData) {
        this.currentLevel = levelData;
        this.gridData = levelData.grid;
        this.wordTargets = levelData.wordTargets || []; // Array of word lengths to find
        this.wordSlots = levelData.wordSlots || []; // New word slots with order
        this.completedWords = [];
        this.currentWordIndex = levelData.currentWordIndex || 0;
        
        // Update grid renderer with new level data
        if (this.gridRenderer) {
            this.gridRenderer.setGrid(this.gridData);
            this.gridRenderer.setWordTargets(this.wordTargets);
        }
        
        // Setup word slots UI
        this.setupWordSlots();
        
        // Initialize booster buttons
        this.setupBoosterButtons();
    }
    
    setupWordSlots() {
        const container = document.getElementById('word-slots');
        if (!container || !this.wordSlots) return;
        
        container.innerHTML = '';
        
        this.wordSlots.forEach((slot, index) => {
            const slotDiv = document.createElement('div');
            slotDiv.className = 'word-slot';
            slotDiv.id = `word-slot-${index}`;
            
            // Set status class
            if (slot.completed) {
                slotDiv.classList.add('completed');
            } else if (index === this.currentWordIndex) {
                slotDiv.classList.add('active');
            } else {
                slotDiv.classList.add('blocked');
            }
            
            // Add label
            const label = document.createElement('div');
            label.className = 'word-slot-label';
            label.textContent = `${index + 1}.`;
            slotDiv.appendChild(label);
            
            // Add letter boxes
            const boxesDiv = document.createElement('div');
            boxesDiv.className = 'word-slot-boxes';
            
            for (let i = 0; i < slot.length; i++) {
                const box = document.createElement('div');
                box.className = 'letter-box';
                box.id = `slot-${index}-letter-${i}`;
                
                // If completed, show the letter
                if (slot.completed && slot.word) {
                    box.textContent = slot.word[i];
                    box.classList.add('filled');
                }
                
                boxesDiv.appendChild(box);
            }
            
            slotDiv.appendChild(boxesDiv);
            container.appendChild(slotDiv);
        });
    }
    
    setupBoosterButtons() {
        const boosterPanel = document.getElementById('booster-buttons');
        boosterPanel.innerHTML = '';
        
        Object.entries(this.boosters).forEach(([type, booster]) => {
            const button = document.createElement('button');
            button.className = 'booster-btn';
            button.textContent = this.getBoosterName(type);
            button.disabled = booster.available <= booster.used;
            
            button.addEventListener('click', () => {
                this.useBooster(type);
            });
            
            boosterPanel.appendChild(button);
        });
    }
    
    getBoosterName(type) {
        const names = {
            DOUBLE_UP: '2X',
            FREEZE: 'Freeze',
            REVEAL: 'Reveal',
            TIME_PLUS: '+5s',
            SHIELD: 'Shield',
            STREAK_SAVE: 'Save',
            SKIP_HALF: 'Skip'
        };
        return names[type] || type;
    }
    
    useBooster(type) {
        if (this.boosters[type].available > this.boosters[type].used) {
            window.websocketClient.send({
                type: 'USE_BOOSTER',
                data: {
                    boosterType: type
                }
            });
            
            this.boosters[type].used++;
            this.setupBoosterButtons();
        }
    }
    
    // Called when word is submitted
    submitWord() {
        if (!this.gridRenderer) return;
        
        const word = this.gridRenderer.getSelectedWord();
        const path = this.gridRenderer.selectedPath;
        
        if (word.length > 0) {
            window.websocketClient.send({
                type: 'SUBMIT_WORD',
                data: {
                    word: word,
                    path: path.map(cell => ({ row: cell.row, col: cell.col }))
                }
            });
        }
    }
    
    // Called when word is validated by server
    onWordValidated(result) {
        if (result.valid) {
            // Trigger falling animation
            this.gridRenderer.removeWord();
            this.completedWords.push(result.word);
            
            // Update score
            this.updateScore(result.points);
            
            // Check if level is complete
            this.checkLevelCompletion();
        } else {
            // Show error feedback
            this.showInvalidWordFeedback();
            this.gridRenderer.clearSelection();
        }
    }
    
    // Called after animation completes
    onWordComplete() {
        // Request updated grid from server
        window.websocketClient.send({
            type: 'REQUEST_GRID_UPDATE',
            data: {}
        });
    }
    
    // Update grid with new state from server
    updateGrid(newGridData) {
        this.gridData = newGridData;
        if (this.gridRenderer) {
            this.gridRenderer.setGrid(this.gridData);
        }
    }
    
    updateScore(points) {
        const scoreElement = document.getElementById('player-score');
        if (scoreElement) {
            const currentScore = parseInt(scoreElement.textContent) || 0;
            scoreElement.textContent = currentScore + points;
        }
    }
    
    showInvalidWordFeedback() {
        const wordDisplay = document.getElementById('current-word');
        if (wordDisplay) {
            wordDisplay.style.color = 'red';
            setTimeout(() => {
                wordDisplay.style.color = '';
            }, 500);
        }
    }
    
    checkLevelCompletion() {
        // Check if all word targets have been found
        if (this.completedWords.length === this.wordTargets.length) {
            window.websocketClient.send({
                type: 'LEVEL_COMPLETE',
                data: {}
            });
        }
    }
}