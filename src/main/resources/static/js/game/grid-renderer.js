// Grid Renderer
class GridRenderer {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.grid = null;
        this.cellSize = 60;
        this.selectedPath = [];
        this.isDragging = false;
        
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
        // Mouse events
        this.canvas.addEventListener('mousedown', (e) => this.handleMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.handleMouseMove(e));
        this.canvas.addEventListener('mouseup', (e) => this.handleMouseUp(e));
        
        // Touch events for mobile
        this.canvas.addEventListener('touchstart', (e) => this.handleTouchStart(e));
        this.canvas.addEventListener('touchmove', (e) => this.handleTouchMove(e));
        this.canvas.addEventListener('touchend', (e) => this.handleTouchEnd(e));
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
        
        // Cell background
        if (!isActive) {
            this.ctx.fillStyle = '#2c3e50';
        } else if (this.isInPath(row, col)) {
            this.ctx.fillStyle = '#3498db';
        } else {
            this.ctx.fillStyle = '#ecf0f1';
        }
        
        this.ctx.fillRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4);
        
        // Cell border
        this.ctx.strokeStyle = '#34495e';
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4);
        
        // Draw character
        if (cell && isActive) {
            this.ctx.fillStyle = this.isInPath(row, col) ? '#ffffff' : '#2c3e50';
            this.ctx.font = `bold ${this.cellSize * 0.5}px Arial`;
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(cell, x + this.cellSize / 2, y + this.cellSize / 2);
        }
    }
    
    drawPath() {
        if (this.selectedPath.length < 2) return;
        
        this.ctx.strokeStyle = '#3498db';
        this.ctx.lineWidth = 4;
        this.ctx.lineCap = 'round';
        this.ctx.lineJoin = 'round';
        
        this.ctx.beginPath();
        
        for (let i = 0; i < this.selectedPath.length; i++) {
            const cell = this.selectedPath[i];
            const x = this.offsetX + cell.col * this.cellSize + this.cellSize / 2;
            const y = this.offsetY + cell.row * this.cellSize + this.cellSize / 2;
            
            if (i === 0) {
                this.ctx.moveTo(x, y);
            } else {
                this.ctx.lineTo(x, y);
            }
        }
        
        this.ctx.stroke();
    }
    
    handleMouseDown(e) {
        const cell = this.getCellFromCoords(e.offsetX, e.offsetY);
        if (cell && this.isValidCell(cell)) {
            this.isDragging = true;
            this.selectedPath = [cell];
            this.updateCurrentWord();
            this.render();
        }
    }
    
    handleMouseMove(e) {
        if (!this.isDragging) return;
        
        const cell = this.getCellFromCoords(e.offsetX, e.offsetY);
        if (cell && this.isValidCell(cell) && !this.isInPath(cell.row, cell.col)) {
            const lastCell = this.selectedPath[this.selectedPath.length - 1];
            if (this.isAdjacent(lastCell, cell)) {
                this.selectedPath.push(cell);
                this.updateCurrentWord();
                this.render();
            }
        }
    }
    
    handleMouseUp(e) {
        this.isDragging = false;
    }
    
    handleTouchStart(e) {
        e.preventDefault();
        const touch = e.touches[0];
        const rect = this.canvas.getBoundingClientRect();
        const x = touch.clientX - rect.left;
        const y = touch.clientY - rect.top;
        
        const cell = this.getCellFromCoords(x, y);
        if (cell && this.isValidCell(cell)) {
            this.isDragging = true;
            this.selectedPath = [cell];
            this.updateCurrentWord();
            this.render();
        }
    }
    
    handleTouchMove(e) {
        e.preventDefault();
        if (!this.isDragging) return;
        
        const touch = e.touches[0];
        const rect = this.canvas.getBoundingClientRect();
        const x = touch.clientX - rect.left;
        const y = touch.clientY - rect.top;
        
        const cell = this.getCellFromCoords(x, y);
        if (cell && this.isValidCell(cell) && !this.isInPath(cell.row, cell.col)) {
            const lastCell = this.selectedPath[this.selectedPath.length - 1];
            if (this.isAdjacent(lastCell, cell)) {
                this.selectedPath.push(cell);
                this.updateCurrentWord();
                this.render();
            }
        }
    }
    
    handleTouchEnd(e) {
        e.preventDefault();
        this.isDragging = false;
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
        this.isDragging = false;
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
}