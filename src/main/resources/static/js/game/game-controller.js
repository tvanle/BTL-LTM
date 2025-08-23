// Game Controller
class GameController {
    constructor() {
        this.currentLevel = null;
        this.gridData = null;
        this.timer = null;
        this.boosters = this.initializeBoosters();
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
    
    loadLevel(levelData) {
        this.currentLevel = levelData;
        this.gridData = levelData.grid;
        
        // Initialize booster buttons
        this.setupBoosterButtons();
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
}