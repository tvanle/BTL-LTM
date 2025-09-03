// Grid Renderer
class GridRenderer {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.grid = null;
        this.cellSize = 60;
        this.selectedPath = [];
        this.isSelecting = false;
        this.wordTargets = []; // Target word lengths to find
        this.completedWords = [];
        this.animatingCells = [];
        
        this.setupEventListeners();
    }
    
    setGrid(gridData) {
        this.grid = gridData;
        this.selectedPath = [];
        this.calculateCellSize();
        this.render();
    }
    
    calculateCellSize() {
        if (!this.grid) return;
        
        const maxWidth = this.canvas.width / this.grid.cols;
        const maxHeight = this.canvas.height / this.grid.rows;
        this.cellSize = Math.min(maxWidth, maxHeight, 80);
        
        // Center the grid
        this.offsetX = (this.canvas.width - this.cellSize * this.grid.cols) / 2;
        this.offsetY = (this.canvas.height - this.cellSize * this.grid.rows) / 2;
    }
    
    setupEventListeners() {
        // Click events for selecting cells
        this.canvas.addEventListener('click', (e) => this.handleClick(e));
        
        // Touch events for mobile
        this.canvas.addEventListener('touchend', (e) => this.handleTouch(e));
        
        // Visual feedback on hover
        this.canvas.addEventListener('mousemove', (e) => this.handleHover(e));
    }
    
    render() {
        if (!this.grid) return;
        
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Draw grid cells
        for (let row = 0; row < this.grid.rows; row++) {
            for (let col = 0; col < this.grid.cols; col++) {
                this.drawCell(row, col);
            }
        }
        
        // Draw selection path
        if (this.selectedPath.length > 0) {
            this.drawPath();
        }
    }
    
    drawCell(row, col) {
        const x = this.offsetX + col * this.cellSize;
        const y = this.offsetY + row * this.cellSize;
        const cell = this.grid.cells[row][col];
        const isActive = this.grid.shape.mask[row][col];
        const isHovered = this.hoveredCell && 
                         this.hoveredCell.row === row && 
                         this.hoveredCell.col === col;
        
        // Skip if cell is animating (falling)
        if (this.isAnimatingCell(row, col)) {
            return;
        }
        
        // Cell background
        if (!isActive || !cell) {
            this.ctx.fillStyle = '#2c3e50';
        } else if (this.isInPath(row, col)) {
            this.ctx.fillStyle = '#3498db';
        } else if (isHovered) {
            this.ctx.fillStyle = '#5faee3';
        } else {
            this.ctx.fillStyle = '#ecf0f1';
        }
        
        // Rounded corners
        this.roundRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4, 5);
        this.ctx.fill();
        
        // Cell border
        if (this.isInPath(row, col)) {
            this.ctx.strokeStyle = '#2980b9';
            this.ctx.lineWidth = 3;
        } else if (isHovered) {
            this.ctx.strokeStyle = '#3498db';
            this.ctx.lineWidth = 2;
        } else {
            this.ctx.strokeStyle = '#34495e';
            this.ctx.lineWidth = 1;
        }
        this.roundRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4, 5);
        this.ctx.stroke();
        
        // Draw character
        if (cell && isActive) {
            this.ctx.fillStyle = this.isInPath(row, col) ? '#ffffff' : '#2c3e50';
            this.ctx.font = `bold ${this.cellSize * 0.5}px Arial`;
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(cell.toUpperCase(), x + this.cellSize / 2, y + this.cellSize / 2);
        }
    }
    
    drawPath() {
        // No longer draw lines between cells
        // Selection is now shown only by highlighting cells
    }
    
    handleClick(e) {
        const cell = this.getCellFromCoords(e.offsetX, e.offsetY);
        if (!cell || !this.isValidCell(cell)) return;
        
        // If no selection started, start new selection
        if (this.selectedPath.length === 0) {
            this.isSelecting = true;
            this.selectedPath = [cell];
            this.updateCurrentWord();
            this.render();
        } 
        // If cell already in path, check if it's the last one to allow deselection
        else if (this.isInPath(cell.row, cell.col)) {
            const cellIndex = this.selectedPath.findIndex(c => c.row === cell.row && c.col === cell.col);
            if (cellIndex === this.selectedPath.length - 1 && this.selectedPath.length > 1) {
                // Remove last cell if clicked again
                this.selectedPath.pop();
                this.updateCurrentWord();
                this.render();
            }
        }
        // Add cell to path
        else {
            this.selectedPath.push(cell);
            this.updateCurrentWord();
            this.render();
        }
    }
    
    handleTouch(e) {
        e.preventDefault();
        const touch = e.changedTouches[0];
        const rect = this.canvas.getBoundingClientRect();
        const x = touch.clientX - rect.left;
        const y = touch.clientY - rect.top;
        
        const cell = this.getCellFromCoords(x, y);
        if (!cell || !this.isValidCell(cell)) return;
        
        // Same logic as click
        if (this.selectedPath.length === 0) {
            this.isSelecting = true;
            this.selectedPath = [cell];
            this.updateCurrentWord();
            this.render();
        } else if (this.isInPath(cell.row, cell.col)) {
            const cellIndex = this.selectedPath.findIndex(c => c.row === cell.row && c.col === cell.col);
            if (cellIndex === this.selectedPath.length - 1 && this.selectedPath.length > 1) {
                this.selectedPath.pop();
                this.updateCurrentWord();
                this.render();
            }
        } else {
            // Add cell to path (no adjacency check needed)
            this.selectedPath.push(cell);
            this.updateCurrentWord();
            this.render();
        }
    }
    
    handleHover(e) {
        const cell = this.getCellFromCoords(e.offsetX, e.offsetY);
        if (cell && this.isValidCell(cell)) {
            this.canvas.style.cursor = 'pointer';
            
            // Check if cell is selectable (not already in path)
            if (this.selectedPath.length > 0) {
                if (!this.isInPath(cell.row, cell.col)) {
                    this.hoveredCell = cell;
                    this.render();
                    return;
                }
            }
        } else {
            this.canvas.style.cursor = 'default';
        }
        
        if (this.hoveredCell) {
            this.hoveredCell = null;
            this.render();
        }
    }
    
    getCellFromCoords(x, y) {
        if (!this.grid) return null;
        
        const col = Math.floor((x - this.offsetX) / this.cellSize);
        const row = Math.floor((y - this.offsetY) / this.cellSize);
        
        if (row >= 0 && row < this.grid.rows && col >= 0 && col < this.grid.cols) {
            return {
                row: row,
                col: col,
                character: this.grid.cells[row][col]
            };
        }
        
        return null;
    }
    
    isValidCell(cell) {
        return this.grid && 
               this.grid.shape.mask[cell.row][cell.col];
    }
    
    isInPath(row, col) {
        return this.selectedPath.some(cell => cell.row === row && cell.col === col);
    }
    
    isAdjacent(cell1, cell2) {
        const rowDiff = Math.abs(cell1.row - cell2.row);
        const colDiff = Math.abs(cell1.col - cell2.col);
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    clearSelection() {
        this.selectedPath = [];
        this.isSelecting = false;
        this.updateCurrentWord();
        this.render();
    }
    
    getSelectedWord() {
        return this.selectedPath
            .map(cell => cell.character)
            .join('');
    }
    
    updateCurrentWord() {
        const wordDisplay = document.getElementById('current-word');
        if (wordDisplay) {
            wordDisplay.textContent = this.getSelectedWord();
        }
    }
    
    // Helper method for rounded rectangles
    roundRect(x, y, width, height, radius) {
        this.ctx.beginPath();
        this.ctx.moveTo(x + radius, y);
        this.ctx.lineTo(x + width - radius, y);
        this.ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
        this.ctx.lineTo(x + width, y + height - radius);
        this.ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
        this.ctx.lineTo(x + radius, y + height);
        this.ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
        this.ctx.lineTo(x, y + radius);
        this.ctx.quadraticCurveTo(x, y, x + radius, y);
        this.ctx.closePath();
    }
    
    // Check if cell is animating
    isAnimatingCell(row, col) {
        return this.animatingCells.some(cell => cell.row === row && cell.col === col);
    }
    
    // Animate word removal with falling effect
    removeWord() {
        if (this.selectedPath.length === 0) return;
        
        // Mark cells for removal
        const cellsToRemove = [...this.selectedPath];
        
        // Start falling animation
        this.animateFallingCells(cellsToRemove);
        
        // Clear selection
        this.selectedPath = [];
        this.updateCurrentWord();
    }
    
    animateFallingCells(cellsToRemove) {
        // Create animation data for each cell
        cellsToRemove.forEach(cell => {
            this.animatingCells.push({
                row: cell.row,
                col: cell.col,
                character: cell.character,
                y: this.offsetY + cell.row * this.cellSize,
                targetY: this.canvas.height + 100,
                velocity: 0,
                acceleration: 0.5
            });
        });
        
        // Start animation loop
        this.startFallingAnimation();
    }
    
    startFallingAnimation() {
        const animate = () => {
            // Clear and redraw grid
            this.render();
            
            // Update and draw animating cells
            let stillAnimating = false;
            
            this.animatingCells.forEach(cell => {
                // Apply gravity
                cell.velocity += cell.acceleration;
                cell.y += cell.velocity;
                
                // Draw falling cell
                const x = this.offsetX + cell.col * this.cellSize;
                
                this.ctx.fillStyle = '#3498db';
                this.roundRect(x + 2, cell.y + 2, this.cellSize - 4, this.cellSize - 4, 5);
                this.ctx.fill();
                
                this.ctx.fillStyle = '#ffffff';
                this.ctx.font = `bold ${this.cellSize * 0.5}px Arial`;
                this.ctx.textAlign = 'center';
                this.ctx.textBaseline = 'middle';
                this.ctx.fillText(cell.character.toUpperCase(), 
                                 x + this.cellSize / 2, 
                                 cell.y + this.cellSize / 2);
                
                if (cell.y < cell.targetY) {
                    stillAnimating = true;
                }
            });
            
            if (stillAnimating) {
                requestAnimationFrame(animate);
            } else {
                // Animation complete, update grid
                this.animatingCells = [];
                this.updateGridAfterRemoval();
            }
        };
        
        animate();
    }
    
    updateGridAfterRemoval() {
        // This will trigger grid update with new cells falling down
        // The backend should handle the actual grid state update
        if (window.gameController) {
            window.gameController.onWordComplete();
        }
        
        this.render();
    }
    
    // Set word targets for current level
    setWordTargets(targets) {
        this.wordTargets = targets;
        this.updateWordTargetsDisplay();
    }
    
    updateWordTargetsDisplay() {
        const targetDisplay = document.getElementById('word-targets');
        if (targetDisplay && this.wordTargets) {
            targetDisplay.innerHTML = this.wordTargets
                .map(length => `<div class="word-target">Word: ${length} letters</div>`)
                .join('');
        }
    }
}