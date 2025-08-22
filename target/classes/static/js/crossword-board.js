class CrosswordBoard {
    constructor(canvasId, gridSize = 15) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.gridSize = gridSize;
        this.cellSize = Math.min(this.canvas.width, this.canvas.height) / this.gridSize;
        this.selectedCell = { row: -1, col: -1 };
        this.grid = null;
        this.words = [];
        
        this.setupEventListeners();
        this.initializeGrid();
        this.draw();
    }

    setupEventListeners() {
        this.canvas.addEventListener('click', (event) => {
            const rect = this.canvas.getBoundingClientRect();
            const x = event.clientX - rect.left;
            const y = event.clientY - rect.top;
            
            const col = Math.floor(x / this.cellSize);
            const row = Math.floor(y / this.cellSize);
            
            if (row >= 0 && row < this.gridSize && col >= 0 && col < this.gridSize) {
                this.selectCell(row, col);
            }
        });
    }

    initializeGrid() {
        this.grid = [];
        for (let i = 0; i < this.gridSize; i++) {
            this.grid[i] = [];
            for (let j = 0; j < this.gridSize; j++) {
                this.grid[i][j] = {
                    row: i,
                    col: j,
                    character: '',
                    type: 'EMPTY', // EMPTY, LOCKED, BLOCKED
                    status: 'EMPTY', // EMPTY, PENDING, CORRECT, INCORRECT
                    ownerId: null
                };
            }
        }
    }

    updateGrid(gameGrid) {
        if (gameGrid && gameGrid.grid) {
            for (let i = 0; i < this.gridSize && i < gameGrid.rows; i++) {
                for (let j = 0; j < this.gridSize && j < gameGrid.cols; j++) {
                    if (gameGrid.grid[i] && gameGrid.grid[i][j]) {
                        const serverCell = gameGrid.grid[i][j];
                        this.grid[i][j] = {
                            row: i,
                            col: j,
                            character: serverCell.character || '',
                            type: serverCell.type || 'EMPTY',
                            status: serverCell.status || 'EMPTY',
                            ownerId: serverCell.ownerId || null
                        };
                    }
                }
            }
        }
        
        if (gameGrid && gameGrid.words) {
            this.words = gameGrid.words;
        }
        
        this.draw();
    }

    selectCell(row, col) {
        // Check if cell can be selected
        const cell = this.grid[row][col];
        if (cell.type === 'BLOCKED') {
            return false;
        }
        
        this.selectedCell = { row, col };
        this.draw();
        
        // Update UI
        const selectedCellInfo = document.getElementById('selected-cell');
        if (selectedCellInfo) {
            selectedCellInfo.textContent = `Row ${row + 1}, Col ${col + 1}`;
        }
        
        // Enable character input
        const charInput = document.getElementById('char-input');
        if (charInput) {
            charInput.focus();
        }
        
        return true;
    }

    placeCharacter(row, col, character, playerId) {
        if (row >= 0 && row < this.gridSize && col >= 0 && col < this.gridSize) {
            const cell = this.grid[row][col];
            cell.character = character;
            cell.ownerId = playerId;
            cell.status = 'PENDING';
            this.draw();
        }
    }

    validateCharacter(row, col, character) {
        if (row >= 0 && row < this.gridSize && col >= 0 && col < this.gridSize) {
            const cell = this.grid[row][col];
            cell.character = character;
            cell.status = 'CORRECT';
            this.draw();
        }
    }

    rejectCharacter(row, col) {
        if (row >= 0 && row < this.gridSize && col >= 0 && col < this.gridSize) {
            const cell = this.grid[row][col];
            cell.status = 'INCORRECT';
            this.draw();
            
            // Clear incorrect character after a delay
            setTimeout(() => {
                cell.character = '';
                cell.status = 'EMPTY';
                cell.ownerId = null;
                this.draw();
            }, 1000);
        }
    }

    draw() {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Draw grid
        this.drawGrid();
        
        // Draw cells
        this.drawCells();
        
        // Draw selected cell highlight
        this.drawSelectedCell();
    }

    drawGrid() {
        this.ctx.strokeStyle = '#ddd';
        this.ctx.lineWidth = 1;
        
        // Draw vertical lines
        for (let i = 0; i <= this.gridSize; i++) {
            const x = i * this.cellSize;
            this.ctx.beginPath();
            this.ctx.moveTo(x, 0);
            this.ctx.lineTo(x, this.canvas.height);
            this.ctx.stroke();
        }
        
        // Draw horizontal lines
        for (let i = 0; i <= this.gridSize; i++) {
            const y = i * this.cellSize;
            this.ctx.beginPath();
            this.ctx.moveTo(0, y);
            this.ctx.lineTo(this.canvas.width, y);
            this.ctx.stroke();
        }
    }

    drawCells() {
        for (let i = 0; i < this.gridSize; i++) {
            for (let j = 0; j < this.gridSize; j++) {
                this.drawCell(i, j);
            }
        }
    }

    drawCell(row, col) {
        const cell = this.grid[row][col];
        const x = col * this.cellSize;
        const y = row * this.cellSize;
        
        // Determine cell background color
        let fillColor = '#ffffff';
        if (cell.type === 'BLOCKED') {
            fillColor = '#333333';
        } else if (cell.type === 'LOCKED') {
            fillColor = '#e8f4fd';
        } else if (cell.status === 'CORRECT') {
            fillColor = '#d4edda';
        } else if (cell.status === 'INCORRECT') {
            fillColor = '#f8d7da';
        } else if (cell.status === 'PENDING') {
            fillColor = '#fff3cd';
        }
        
        // Fill cell background
        this.ctx.fillStyle = fillColor;
        this.ctx.fillRect(x + 1, y + 1, this.cellSize - 2, this.cellSize - 2);
        
        // Draw character
        if (cell.character && cell.type !== 'BLOCKED') {
            this.ctx.fillStyle = '#333333';
            this.ctx.font = `bold ${this.cellSize * 0.6}px Arial`;
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            
            const centerX = x + this.cellSize / 2;
            const centerY = y + this.cellSize / 2;
            
            this.ctx.fillText(cell.character, centerX, centerY);
        }
        
        // Draw player ownership indicator
        if (cell.ownerId && cell.status === 'CORRECT') {
            this.ctx.fillStyle = cell.ownerId === window.gameState?.currentPlayerId ? '#4CAF50' : '#2196F3';
            this.ctx.fillRect(x + this.cellSize - 8, y + 2, 6, 6);
        }
    }

    drawSelectedCell() {
        if (this.selectedCell.row >= 0 && this.selectedCell.col >= 0) {
            const x = this.selectedCell.col * this.cellSize;
            const y = this.selectedCell.row * this.cellSize;
            
            // Draw selection border
            this.ctx.strokeStyle = '#4CAF50';
            this.ctx.lineWidth = 3;
            this.ctx.strokeRect(x + 1, y + 1, this.cellSize - 2, this.cellSize - 2);
        }
    }

    getSelectedCell() {
        if (this.selectedCell.row >= 0 && this.selectedCell.col >= 0) {
            return this.selectedCell;
        }
        return null;
    }

    canPlaceCharacter(row, col) {
        if (row < 0 || row >= this.gridSize || col < 0 || col >= this.gridSize) {
            return false;
        }
        
        const cell = this.grid[row][col];
        return cell.type === 'EMPTY' && cell.character === '';
    }

    // Get words that intersect with a given position
    getWordsAtPosition(row, col) {
        return this.words.filter(word => {
            if (!word.positions) return false;
            return word.positions.some(pos => pos.row === row && pos.col === col);
        });
    }

    // Highlight word when hovering or selecting
    highlightWord(word) {
        if (!word || !word.positions) return;
        
        this.ctx.strokeStyle = '#FFC107';
        this.ctx.lineWidth = 2;
        
        word.positions.forEach(pos => {
            const x = pos.col * this.cellSize;
            const y = pos.row * this.cellSize;
            this.ctx.strokeRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4);
        });
    }
}

// Export for use in other scripts
window.CrosswordBoard = CrosswordBoard;