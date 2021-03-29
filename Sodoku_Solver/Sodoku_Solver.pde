import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Random;
import java.util.Collections;
import java.util.List;

int maxHeat = 0;
int selectedX = -1, selectedY = -1;
int highlightedX = -1, highlightedY = -1;
int highlightedX2 = -1, highlightedY2 = -1;
int speed;
int size = 9;
int solver = 0;
int stepI = 0;
int backtrackerPattern = 0;
int iterationLimit, stuckCountReset;
float tempReduction;

int difficulty = 5;

Annealing_Graph annealingGraph;
Backtracker_Graph backtrackerGraph;

Sudoku sudoku;
Number_Input input;

Button solve, reset, generate;
Button topBottom, bottomTop, leftRight, rightLeft;
Button drawAsColourButton;

Slider speedSelector;
Slider temperatureReductionSlider, iterationLimitSlider, stuckCountSlider;

DropList generationDifficulty, solverMethod;

boolean valid = true;
boolean visualisationFin = false;
boolean solvePressed = false;
boolean drawAsColour = false;

ArrayList < ArrayList < PVector >> annealingChanges = new ArrayList();
ArrayList < ArrayList < Integer >> backtrackerChanges = new ArrayList();
ArrayList < ArrayList < Integer >> highlighted = new ArrayList();

void settings() {
    size(1280, 720);
}

void setup() {
    frameRate(60);
    background(225);
    fill(0);
    textAlign(CENTER, CENTER);
    textSize(72);
    text("Sudoku Solver", width - 605, 10, 500, 100);
    textSize(20);
    noStroke();

    sudoku = new Sudoku(100, 30, height - 270);
    input = new Number_Input(width - 635, 200, 45);

    speedSelector = new Slider(width - 450, 150, 200, 25, 1, 2500);
    temperatureReductionSlider = new Slider(width - 405, 450, 125, 25, 9900, 9999);
    iterationLimitSlider = new Slider(width - 625, 450, 125, 25, 1000, 1000000);
    stuckCountSlider = new Slider(width - 195, 450, 125, 25, 500, 100000);

    solve = new Button("Solve", width - 230, 355, 75, 30);
    reset = new Button("Reset", width - 130, 355, 75, 30);
    generate = new Button("Generate", width - 435, 355, 175, 30);

    annealingGraph = new Annealing_Graph(50, height - 200, width - 100, 150);
    backtrackerGraph = new Backtracker_Graph(50, height - 200, width - 100, 150);

    topBottom = new Button("Top -> Bottom", width - 635, 425, 130, 50);
    bottomTop = new Button("Bottom -> Top", width - 485, 425, 130, 50);
    leftRight = new Button("Left -> Right", width - 335, 425, 130, 50);
    rightLeft = new Button("Right -> Left", width - 185, 425, 130, 50);
    drawAsColourButton = new Button("Graph", 10, 15, 65, 30);
    
    generationDifficulty = new DropList(width-435, 200, 175, 30, "Difficulty", new ArrayList(Arrays.asList("Easy", "Medium", "Hard")));
    solverMethod = new DropList(width-230, 200, 175, 30, "Solve Method", new ArrayList(Arrays.asList("Backtracker", "Simulated Annealing", "Dancing Links")));

    topBottom.animationI = 25;
    topBottom.pressed = true;

    tempReduction = temperatureReductionSlider.getValue() / 1000;
    iterationLimit = (int) iterationLimitSlider.getValue();
    stuckCountReset = (int) stuckCountSlider.getValue();
    
    annealingGraph.Draw();
}

void draw() {
    speed = (int) speedSelector.getValue();
    tempReduction = temperatureReductionSlider.getValue() / 10000;
    iterationLimit = (int) iterationLimitSlider.getValue();
    stuckCountReset = (int) stuckCountSlider.getValue();

    // Perform the solve and draw the background of the graph
    if (solvePressed && !sudoku.solveCompleted && valid) {
        if (solver == 0) {
            sudoku.backtrackerSolve();
            backtrackerGraph.Draw();
        } else if (solver == 1) {
            sudoku.annealingSolve();
            annealingGraph.Draw();
        } else if (solver == 2) {
            sudoku.dancingSolve();
        }
    }

    fill(225);
    noStroke();
    rect(width/2 - 50, 120, width/2 + 25, height-350);
    textSize(20);
    fill(0);
    text("Iterations Per Frame: " + speed, width - 350, 125);

    sudoku.display();
    noStroke();
    speedSelector.display();
    strokeWeight(1);

    if (drawAsColour) {
        drawAsColourButton.drawSelected();
    } else {
        drawAsColourButton.Draw();
    }

    // Draw the buttons on the canvas
    if (solver == 0) {
        if (backtrackerPattern == 0) {
            topBottom.drawSelected();
            bottomTop.Draw();
            rightLeft.Draw();
            leftRight.Draw();
        } else if (backtrackerPattern == 1) {
            topBottom.Draw();
            bottomTop.drawSelected();
            rightLeft.Draw();
            leftRight.Draw();
        } else if (backtrackerPattern == 2) {
            topBottom.Draw();
            bottomTop.Draw();
            leftRight.drawSelected();
            rightLeft.Draw();
        } else if (backtrackerPattern == 3) {
            topBottom.Draw();
            bottomTop.Draw();
            leftRight.Draw();
            rightLeft.drawSelected();
        }
    } else if (solver == 1) {
        temperatureReductionSlider.display();
        iterationLimitSlider.display();
        stuckCountSlider.display();

        fill(0);
        text("Temperature Reduction: " + tempReduction, width - 345, 430);
        text("Iteration Limit: " + iterationLimit, width - 575, 430);
        text("Stuck Count: " + stuckCountReset, width - 135, 430);
    }

    // Draw the graph of the solver
    if (solver == 0) {
        backtrackerGraph.drawNextPoint(speed);
    } else if (solver == 1) {
        annealingGraph.drawNextPoint(speed);
    }

    // Perform the steps of the visualisation
    if (!visualisationFin) {
        sudoku.performStep(speed);
    }

    // If entered sudoku is not valid, indicate that the sudoku is invalid
    if (valid == false) {
        fill(0, 50);
        rectMode(CENTER);
        rect(sudoku.x + sudoku.gridSize / 2, sudoku.y + sudoku.gridSize / 2, 300, 100);
        rectMode(CORNER);
        fill(255, 0, 0);
        textSize(56);

        text("INVALID", sudoku.x + sudoku.gridSize / 2, sudoku.y + sudoku.gridSize / 2);
        textSize(20);
    }
    
    generationDifficulty.Draw();
    solverMethod.Draw();
    
    input.Draw();
    solve.Draw();
    reset.Draw();
    generate.Draw();
    fill(225);
    rect(0, 50, 75, 30);
    fill(0);
    text("Clues: " + sudoku.clueCount, 40, 60);
}

/**
 * Resets the elements of the visualisation.
 */
void reset() {
    maxHeat = 0;
    selectedX = -1;
    selectedY = -1;

    highlightedX = -1;
    highlightedY = -1;
    highlightedX2 = -1;
    highlightedY2 = -1;

    solvePressed = false;
    valid = true;
    visualisationFin = false;

    annealingChanges.clear();
    backtrackerChanges.clear();
    highlighted.clear();

    sudoku.display();
    sudoku.reset();

    annealingGraph.reset();
    backtrackerGraph.reset();
}

void keyPressed() {
    // User inputs to sudoku
    if (selectedX != -1 && selectedY != -1) {
        if (key == '1') {
            sudoku.setSquare(selectedX, selectedY, 1);
        }

        if (key == '2') {
            sudoku.setSquare(selectedX, selectedY, 2);
        }

        if (key == '3') {
            sudoku.setSquare(selectedX, selectedY, 3);
        }

        if (key == '4') {
            sudoku.setSquare(selectedX, selectedY, 4);
        }

        if (key == '5') {
            sudoku.setSquare(selectedX, selectedY, 5);
        }

        if (key == '6') {
            sudoku.setSquare(selectedX, selectedY, 6);
        }

        if (key == '7') {
            sudoku.setSquare(selectedX, selectedY, 7);
        }

        if (key == '8') {
            sudoku.setSquare(selectedX, selectedY, 8);
        }

        if (key == '9') {
            sudoku.setSquare(selectedX, selectedY, 9);
        }

        if (key == '0') {
            sudoku.setSquare(selectedX, selectedY, 0);
        }
    }
}

void mousePressed() {
    sudoku.getSelectedSquare();
    speedSelector.press();
    
    int solverValue = solverMethod.checkForPress();
    if (solverValue != -1){
        solver = solverValue-1;
    }
    
    int difficultyValue = generationDifficulty.checkForPress();
    if (difficultyValue != -1){
        difficulty = 4 - (difficultyValue);
    }
     
    // Detect colour graph button press
    if (drawAsColourButton.MouseIsOver()) {
        drawAsColour = !drawAsColour;
        sudoku.initialiseColourGraph();
    }
    
    if (solver == 0) {
        // Detect backtracking configuration button presses
        if (topBottom.MouseIsOver()) {
            backtrackerPattern = 0;
        } else if (bottomTop.MouseIsOver()) {
            backtrackerPattern = 1;
        } else if (leftRight.MouseIsOver()) {
            backtrackerPattern = 2;
        } else if (rightLeft.MouseIsOver()) {
            backtrackerPattern = 3;
        }
    } else if (solver == 1) {
        // Detect annealing configuration slider changes
        temperatureReductionSlider.press();
        iterationLimitSlider.press();
        stuckCountSlider.press();
    }

    // Generate random sudoku if random button pressed
    if (generate.MouseIsOver() && !solvePressed) {
        sudoku.gen();
    }

    // Reset the sudoku if the reset button is pressed
    if (reset.MouseIsOver()) {
        if (valid) {
            sudoku.clear();
        }
        reset();
        return;
    }

    // Set the selected square to the value input to the user
    if (!solvePressed && input.MouseIsOver()) {
        sudoku.setSquare(selectedX, selectedY, input.getPressed());
        return;
    }

    // If solve pressed, check if sudoku is valid and attempt to solve
    if (!solvePressed && solve.MouseIsOver()) {
        solvePressed = true;
        
        valid = sudoku.issudokuValid();

        selectedX = -1;
        selectedY = -1;
    }
}

void mouseReleased() {
    // Release all the sliders to set the values
    speedSelector.release();
    temperatureReductionSlider.release();
    iterationLimitSlider.release();
    stuckCountSlider.release();
}
