package com.wordbrain2.service.game;

import com.wordbrain2.model.game.Grid;
import com.wordbrain2.model.game.Shape;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GridGeneratorService {
    
    private final Random random = new Random();
    
    public Grid generateGrid(int rows, int cols, int levelNumber) {
        Grid grid = new Grid(rows, cols);
        
        // Select shape based on level
        Shape.ShapeType shapeType = selectShapeType(levelNumber);
        Shape shape = new Shape(rows, cols, shapeType);
        grid.applyShape(shape);
        
        return grid;
    }
    
    private Shape.ShapeType selectShapeType(int level) {
        Shape.ShapeType[] types = Shape.ShapeType.values();
        
        // Early levels use simpler shapes
        if (level <= 2) {
            return Shape.ShapeType.SQUARE;
        } else if (level <= 4) {
            return random.nextBoolean() ? Shape.ShapeType.CIRCLE : Shape.ShapeType.DIAMOND;
        } else {
            // Random shape for higher levels
            return types[random.nextInt(types.length - 1)]; // Exclude CUSTOM
        }
    }
}