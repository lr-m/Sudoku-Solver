import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.Stack; 
import java.util.LinkedList; 
import java.util.Random; 
import java.util.Collections; 
import java.util.List; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Sodoku_Solver extends PApplet {









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

public void settings() {
    size(1280, 720);
}

public void setup() {
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

public void draw() {
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
public void reset() {
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

public void keyPressed() {
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

public void mousePressed() {
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

public void mouseReleased() {
    // Release all the sliders to set the values
    speedSelector.release();
    temperatureReductionSlider.release();
    iterationLimitSlider.release();
    stuckCountSlider.release();
}
/**
 * Class that implements the simulated annealing solver for the visualisation.
 */
public class Annealing_Solver {
    boolean[] fixedSquares;
    int[][] sudokuArr;

    boolean solveCompleted;
    boolean solutionFound;

    int iteration = 0;
    int stuckCount = 0;
    float temperature = 1;

    /**
     * Produces a list of the 9 numbers that can exist in a valid sudoku square.
     */
    public ArrayList < Integer > getNumberList(ArrayList < Integer > numbers) {
        numbers.clear();

        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }

        return numbers;
    }

    /**
     * Attempts to solve the passed 2d array representing a sudoku.
     */
    public void solve(int[][] toSolve) {
        fixedSquares = new boolean[81];

        // Create the list that indicates if a square is changeable or not
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (toSolve[i][j] != 0)
                    fixedSquares[i * 9 + j] = true;
            }
        }

        // Fill in each empty square in each block with random numbers (without duplicates)
        ArrayList < Integer > numbers = new ArrayList < Integer > ();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                numbers = getNumberList(numbers);

                for (int row = i * 3; row < (i + 1) * 3; row++) {
                    for (int col = j * 3; col < (j + 1) * 3; col++) {
                        if (toSolve[row][col] != 0) {
                            numbers.remove(new Integer(toSolve[row][col]));
                        }
                    }
                }

                Collections.shuffle(numbers);

                for (int row = i * 3; row < (i + 1) * 3; row++) {
                    for (int col = j * 3; col < (j + 1) * 3; col++) {
                        if (toSolve[row][col] == 0) {
                            toSolve[row][col] = numbers.remove(0);
                        }
                    }
                }
            }
        }

        sudokuArr = toSolve;

        solveCompleted = false;
        solutionFound = false;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku.setSquare(i, j, sudokuArr[i][j]);
            }
        }

        while (!solveCompleted) {
            solveIteration();
            annealingGraph.addTemperature(temperature);
        }
    }

    /**
     * Returns a count of the number of conflicts that arrise from the current board configuration
     */
    public int score(int[][] board) {
        int num = 0;
        HashMap < Integer, Integer > numbers = new HashMap < Integer, Integer > ();

        // Rows
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (numbers.get(board[i][j]) == null) {
                    numbers.put(board[i][j], 1);
                } else {
                    numbers.put(board[i][j], numbers.get(board[i][j]) + 1);
                }
            }

            for (int j = 1; j <= 9; j++) {
                if (numbers.get(j) != null && numbers.get(j) > 1) {
                    num += numbers.get(j) - 1;
                }
                numbers.put(j, null);
            }
        }

        // Columns
        for (int col = 0; col < 9; col++) {
            for (int row = 0; row < 9; row++) {
                if (numbers.get(board[row][col]) == null)
                    numbers.put(board[row][col], 1);
                else
                    numbers.put(board[row][col], numbers.get(board[row][col]) + 1);
            }

            for (int j = 1; j <= 9; j++) {
                if (numbers.get(j) != null && numbers.get(j) > 1) {
                    num += numbers.get(j) - 1;
                }
                numbers.put(j, null); //reset map for next column
            }
        }
        return num;
    }

    /**
     * Performs the solve on the board array
     */
    public void solveIteration() {
        int initConflicts = score(sudokuArr);
        annealingGraph.addScore(initConflicts);
        int xOffset = 3 * Math.round(random(-0.5f, 2.5f));
        int yOffset = 3 * Math.round(random(-0.5f, 2.5f));

        if (initConflicts == 0) {
            sudoku.solveCompleted = true;
            solutionFound = true;
            solveCompleted = true;
            return;
        }

        int x1, y1, x2, y2;

        int loopLimit = 0;
        do {
            x1 = (int)(Math.random() * 3);
            y1 = (int)(Math.random() * 3);
            x2 = (int)(Math.random() * 3);
            y2 = (int)(Math.random() * 3);
            loopLimit++;

            if (loopLimit > 10 && !((fixedSquares[(xOffset + x1) * 9 + (yOffset + y1)] || fixedSquares[(xOffset + x2) * 9 + (yOffset + y2)]))) {
                break;
            }
        } while (((x1 == x2) && (y1 == y2)) || ((fixedSquares[(xOffset + x1) * 9 + (yOffset + y1)] || fixedSquares[(xOffset + x2) * 9 + (yOffset + y2)])));

        iteration++;

        int[][] boardCandidate = new int[9][9];

        copy(sudokuArr, boardCandidate);

        boardCandidate[xOffset + x1][yOffset + y1] = sudokuArr[xOffset + x2][yOffset + y2];
        boardCandidate[xOffset + x2][yOffset + y2] = sudokuArr[xOffset + x1][yOffset + y1];

        ArrayList < PVector > toAddToChanges = new ArrayList();

        toAddToChanges.add(new PVector(xOffset + x1, yOffset + y1));
        toAddToChanges.add(new PVector(xOffset + x2, yOffset + y2));

        int newConflicts = score(boardCandidate);

        if (newConflicts < initConflicts) {
            copy(boardCandidate, sudokuArr);
            annealingChanges.add(toAddToChanges);
            stuckCount = 0;
        } else {
            stuckCount++;
            double probability = Math.exp((initConflicts - newConflicts) / temperature);
            double random = Math.random();
            if (random <= probability) {
                copy(boardCandidate, sudokuArr);
                annealingChanges.add(toAddToChanges);
            } else {
                annealingChanges.add(new ArrayList());
            }
        }

        if (stuckCount == stuckCountReset) {
            temperature = 1;
            stuckCount = 0;
        }

        if (iteration == iterationLimit) {
            solveCompleted = true;
            solutionFound = false;
            sudoku.solveCompleted = true;
            return;
        }

        float nextTemperature = updateTemp(temperature);
        temperature = nextTemperature;
    }

    /**
     * Copies the source array to the destination array.
     */
    public void copy(int[][] source, int[][] destination) {
        for (int a = 0; a < source.length; a++) {
            System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
        }
    }

    /**
     * Updates the temperature for the annealing solve.
     */
    public float updateTemp(float temperature) {
        temperature *= tempReduction;
        return temperature;
    }
}
/**
 * Class that implements the backtracking solver for the visualisation.
 */
class Backtracking_Solver {
    boolean[][] changeable;
    ArrayList < ArrayList < ArrayList < Integer >>> rowIntegerPossibleValues;
    ArrayList < Integer > order;
    boolean generatingOrSolving;

    Backtracking_Solver(boolean generatingOrSolving) {
        this.changeable = new boolean[9][9];
        this.rowIntegerPossibleValues = new ArrayList();
        this.generatingOrSolving = generatingOrSolving;

        order = new ArrayList();

        if (backtrackerPattern == 0) {
            for (int i = 0; i < 81; i++) {
                order.add(i);
            }
        } else if (backtrackerPattern == 1) {
            for (int i = 0; i < 81; i++) {
                order.add(80 - i);
            }
        } else if (backtrackerPattern == 2) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    order.add((j * size) + i);
                }
            }
        } else if (backtrackerPattern == 3) {
            for (int i = 8; i >= 0; i--) {
                for (int j = 8; j >= 0; j--) {
                    order.add((j * size) + i);
                }
            }
        }
    }
    
    /**
     * Performs the initialisation and solve to be visualised.
     */
    public boolean solve(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            ArrayList < ArrayList < Integer >> toAddToPossible = new ArrayList();
            int[] row = board[i];

            for (int j = 0; j < row.length; j++) {
                toAddToPossible.add(new ArrayList());
                if (row[i] == 0) {
                    changeable[i][j] = true;
                } else {
                    changeable[i][i] = false;
                }
            }

            rowIntegerPossibleValues.add(toAddToPossible);
        }

        backtrack(board);
        return true;
    }

    /**
     * Performs the backtracking solve to be visualised.
     */
    public void backtrack(int[][] board) {
        int currentIndex = 0;
        int currentSquare = 0;
        boolean backtracking = false;

        while (currentIndex < (int) Math.pow(size, 2)) {
            currentSquare = order.get(currentIndex);

            int currentX = currentSquare % size;
            int currentY = (int) Math.floor(currentSquare / size);

            if (!generatingOrSolving){
              backtrackerGraph.addPoint(currentIndex);
            }

            if (!backtracking) {
                // If value already filled, cont
                if (board[currentY][currentX] != 0) {
                    if (!generatingOrSolving){
                        backtrackerChanges.add(new ArrayList());
                        highlighted.add(new ArrayList(Arrays.asList(currentX, currentY)));
                    }
                    currentIndex++;
                    continue;
                } else {
                    // Get the possible values this square can take and pick the first one to try           
                    rowIntegerPossibleValues.get(currentX).set(currentY, getPossibleValues(currentX, currentY, board));

                    if (rowIntegerPossibleValues.get(currentX).get(currentY).size() > 0) {
                        // Set the value of the square to the first possible value
                        board[currentY][currentX] = rowIntegerPossibleValues.get(currentX).get(currentY).get(0);
                        
                        if (!generatingOrSolving){
                            backtrackerChanges.add(new ArrayList(Arrays.asList(currentX, currentY, rowIntegerPossibleValues.get(currentX).get(currentY).get(0))));
                            highlighted.add(new ArrayList(Arrays.asList(currentX, currentY)));
                        }

                        currentIndex++;
                    } else {
                        backtracking = true;

                        if (!generatingOrSolving){
                            backtrackerChanges.add(new ArrayList());
                            highlighted.add(new ArrayList(Arrays.asList(currentX, currentY)));
                        }
                    }
                }
            } else {
                currentIndex--;

                int lastX = currentSquare % size;
                int lastY = (int) Math.floor(currentSquare / size);
                int lastValue = board[lastY][lastX];

                ArrayList < Integer > lastPossibleValues = rowIntegerPossibleValues.get(lastX).get(lastY);

                if (lastPossibleValues.size() == 0) {
                    if (!generatingOrSolving){
                        backtrackerChanges.add(new ArrayList());
                        highlighted.add(new ArrayList(Arrays.asList(lastX, lastY)));
                    }
                    continue;
                }

                int indexOfLastAttempt = lastPossibleValues.indexOf(lastValue);

                if (indexOfLastAttempt == lastPossibleValues.size() - 1) {
                    board[lastY][lastX] = 0;

                    if (!generatingOrSolving){
                        backtrackerChanges.add(new ArrayList(Arrays.asList(lastX, lastY, 0)));
                        highlighted.add(new ArrayList(Arrays.asList(lastX, lastY)));
                    }

                    continue;
                } else {
                    board[lastY][lastX] = lastPossibleValues.get(indexOfLastAttempt + 1);
                    backtracking = false;
                    currentIndex++;

                    if (!generatingOrSolving){
                        backtrackerChanges.add(new ArrayList(Arrays.asList(lastX, lastY, lastPossibleValues.get(indexOfLastAttempt + 1))));
                        highlighted.add(new ArrayList(Arrays.asList(lastX, lastY)));
                    }
                }
            }
        }
    }

    /**
     * Gets the possible values that can be placed in the square with the passed coordinates on the passed board.
     */
    public ArrayList < Integer > getPossibleValues(int currentX, int currentY, int[][] board) {
        ArrayList < Integer > valuesToCheck = new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

        // Check column
        for (Integer toCheck: board[currentY]) {
            if (valuesToCheck.contains(toCheck)) {
                valuesToCheck.remove(toCheck);
            }
        }

        // Check row
        for (int[] col: board) {
            Integer toCheck = col[currentX];

            if (valuesToCheck.contains(toCheck)) {
                valuesToCheck.remove(toCheck);
            }
        }

        // Check block
        int blockX = (int)(Math.floor(currentX / Math.sqrt(size))) * (int) Math.sqrt(size);
        int blockY = (int)(Math.floor(currentY / Math.sqrt(size))) * (int) Math.sqrt(size);

        for (int i = blockX; i < blockX + (int) Math.sqrt(size); i++) {
            for (int j = blockY; j < blockY + (int) Math.sqrt(size); j++) {
                Integer valueToCheck = board[j][i];

                if (valuesToCheck.contains(valueToCheck)) {
                    valuesToCheck.remove(valueToCheck);
                }
            }
        }
        return valuesToCheck;
    }
}
class Dancing_Links_Solver {
    private static final int BOARD_SIZE = 9;
    private static final int SUBSECTION_SIZE = 3;
    private static final int NO_VALUE = 0;
    private static final int CONSTRAINTS = 4;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 9;
    private static final int COVER_START_INDEX = 1;

    public void solve(int[][] board) {
        boolean[][] cover = initializeExactCoverBoard(board);
        DancingLinks dlx = new DancingLinks(cover);
        dlx.runSolver();
    }
    
    public boolean checkForUniqueSolution(int[][] board){
       boolean[][] cover = initializeExactCoverBoard(board);
        DancingLinks dlx = new DancingLinks(cover);
        dlx.runCounter();
        if (dlx.count == 1){
            return true;
        } else {
           return false; 
        }
    }

    public int getIndex(int row, int column, int num) {
        return (row - 1) * BOARD_SIZE * BOARD_SIZE + (column - 1) * BOARD_SIZE + (num - 1);
    }

    public boolean[][] createExactCoverBoard() {
        boolean[][] coverBoard = new boolean[BOARD_SIZE * BOARD_SIZE * MAX_VALUE][BOARD_SIZE * BOARD_SIZE * CONSTRAINTS];

        int hBase = 0;
        hBase = checkCellConstraint(coverBoard, hBase);
        hBase = checkRowConstraint(coverBoard, hBase);
        hBase = checkColumnConstraint(coverBoard, hBase);
        checkSubsectionConstraint(coverBoard, hBase);

        return coverBoard;
    }

    public int checkSubsectionConstraint(boolean[][] coverBoard, int hBase) {
        for (int row = COVER_START_INDEX; row <= BOARD_SIZE; row += SUBSECTION_SIZE) {
            for (int column = COVER_START_INDEX; column <= BOARD_SIZE; column += SUBSECTION_SIZE) {
                for (int n = COVER_START_INDEX; n <= BOARD_SIZE; n++, hBase++) {
                    for (int rowDelta = 0; rowDelta < SUBSECTION_SIZE; rowDelta++) {
                        for (int columnDelta = 0; columnDelta < SUBSECTION_SIZE; columnDelta++) {
                            int index = getIndex(row + rowDelta, column + columnDelta, n);
                            coverBoard[index][hBase] = true;
                        }
                    }
                }
            }
        }
        return hBase;
    }

    public int checkColumnConstraint(boolean[][] coverBoard, int hBase) {
        for (int column = COVER_START_INDEX; column <= BOARD_SIZE; column++) {
            for (int n = COVER_START_INDEX; n <= BOARD_SIZE; n++, hBase++) {
                for (int row = COVER_START_INDEX; row <= BOARD_SIZE; row++) {
                    int index = getIndex(row, column, n);
                    coverBoard[index][hBase] = true;
                }
            }
        }
        return hBase;
    }

    public int checkRowConstraint(boolean[][] coverBoard, int hBase) {
        for (int row = COVER_START_INDEX; row <= BOARD_SIZE; row++) {
            for (int n = COVER_START_INDEX; n <= BOARD_SIZE; n++, hBase++) {
                for (int column = COVER_START_INDEX; column <= BOARD_SIZE; column++) {
                    int index = getIndex(row, column, n);
                    coverBoard[index][hBase] = true;
                }
            }
        }
        return hBase;
    }

    public int checkCellConstraint(boolean[][] coverBoard, int hBase) {
        for (int row = COVER_START_INDEX; row <= BOARD_SIZE; row++) {
            for (int column = COVER_START_INDEX; column <= BOARD_SIZE; column++, hBase++) {
                for (int n = COVER_START_INDEX; n <= BOARD_SIZE; n++) {
                    int index = getIndex(row, column, n);
                    coverBoard[index][hBase] = true;
                }
            }
        }
        return hBase;
    }

    public boolean[][] initializeExactCoverBoard(int[][] board) {
        boolean[][] coverBoard = createExactCoverBoard();
        for (int row = COVER_START_INDEX; row <= BOARD_SIZE; row++) {
            for (int column = COVER_START_INDEX; column <= BOARD_SIZE; column++) {
                int n = board[row - 1][column - 1];
                if (n != NO_VALUE) {
                    for (int num = MIN_VALUE; num <= MAX_VALUE; num++) {
                        if (num != n) {
                            Arrays.fill(coverBoard[getIndex(row, column, num)], false);
                        }
                    }
                }
            }
        }
        return coverBoard;
    }
}

public class DancingLinks {

    private ColumnNode header;
    private List < DancingNode > answer;

    private void search(int k) {
        if (!sudoku.solveCompleted) {
            if (header.R == header) {
                handleSolution(answer);
                sudoku.solveCompleted = true;
                return;
            } else {
                ColumnNode c = selectColumnNodeHeuristic();
                c.cover();

                for (DancingNode r = c.D; r != c; r = r.D) {
                    answer.add(r);

                    for (DancingNode j = r.R; j != r; j = j.R) {
                        j.C.cover();
                    }

                    search(k + 1);

                    r = answer.remove(answer.size() - 1);
                    c = r.C;

                    for (DancingNode j = r.L; j != r; j = j.L) {
                        j.C.uncover();
                    }
                }
                c.uncover();
            }
        }
    }
    
    private void count(int k) {
        if (!sudoku.solveCompleted) {
            if (header.R == header) {
                count++;
            } else {
                ColumnNode c = selectColumnNodeHeuristic();
                c.cover();

                for (DancingNode r = c.D; r != c; r = r.D) {
                    answer.add(r);

                    for (DancingNode j = r.R; j != r; j = j.R) {
                        j.C.cover();
                    }

                    count(k + 1);

                    r = answer.remove(answer.size() - 1);
                    c = r.C;

                    for (DancingNode j = r.L; j != r; j = j.L) {
                        j.C.uncover();
                    }
                }
                c.uncover();
            }
        }
    }

    private ColumnNode selectColumnNodeHeuristic() {
        int min = Integer.MAX_VALUE;
        ColumnNode ret = null;
        for (ColumnNode c = (ColumnNode) header.R; c != header; c = (ColumnNode) c.R) {
            if (c.size < min) {
                min = c.size;
                ret = c;
            }
        }
        return ret;
    }

    private ColumnNode makeDLXBoard(boolean[][] grid) {
        final int COLS = grid[0].length;

        ColumnNode headerNode = new ColumnNode("header");
        List < ColumnNode > columnNodes = new ArrayList();

        for (int i = 0; i < COLS; i++) {
            ColumnNode n = new ColumnNode(Integer.toString(i));
            columnNodes.add(n);
            headerNode = (ColumnNode) headerNode.hookRight(n);
        }
        headerNode = headerNode.R.C;

        for (boolean[] aGrid: grid) {
            DancingNode prev = null;
            for (int j = 0; j < COLS; j++) {
                if (aGrid[j]) {
                    ColumnNode col = columnNodes.get(j);
                    DancingNode newNode = new DancingNode(col);
                    if (prev == null)
                        prev = newNode;
                    col.U.hookDown(newNode);
                    prev = prev.hookRight(newNode);
                    col.size++;
                }
            }
        }

        headerNode.size = COLS;

        return headerNode;
    }

    DancingLinks(boolean[][] cover) {
        header = makeDLXBoard(cover);
    }
    
    int count;

    public void runSolver() {
        answer = new LinkedList();
        search(0);
    }
    
    public void runCounter(){
        this.count = 0;
        answer = new LinkedList();
        count(0);
    }

    public void handleSolution(List < DancingNode > answer) {
        int[][] result = parseBoard(answer);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sudoku.setSquare(i, j, result[i][j]);
            }
        }
    }

    public int[][] parseBoard(List < DancingNode > answer) {
        int[][] result = new int[size][size];
        for (DancingNode n: answer) {
            DancingNode rcNode = n;
            int min = Integer.parseInt(rcNode.C.name);
            for (DancingNode tmp = n.R; tmp != n; tmp = tmp.R) {
                int val = Integer.parseInt(tmp.C.name);
                if (val < min) {
                    min = val;
                    rcNode = tmp;
                }
            }
            int ans1 = Integer.parseInt(rcNode.C.name);
            int ans2 = Integer.parseInt(rcNode.R.C.name);
            int r = ans1 / size;
            int c = ans1 % size;
            int num = (ans2 % size) + 1;
            result[r][c] = num;
        }
        return result;
    }
}

class DancingNode {
    DancingNode L, R, U, D;
    ColumnNode C;

    public DancingNode hookDown(DancingNode node) {
        assert(this.C == node.C);
        node.D = this.D;
        node.D.U = node;
        node.U = this;
        this.D = node;
        return node;
    }

    public DancingNode hookRight(DancingNode node) {
        node.R = this.R;
        node.R.L = node;
        node.L = this;
        this.R = node;
        return node;
    }

    public void unlinkLR() {
        this.L.R = this.R;
        this.R.L = this.L;
    }

    public void relinkLR() {
        this.L.R = this.R.L = this;
    }

    public void unlinkUD() {
        this.U.D = this.D;
        this.D.U = this.U;
    }

    public void relinkUD() {
        this.U.D = this.D.U = this;
    }

    DancingNode() {
        L = R = U = D = this;
    }

    DancingNode(ColumnNode c) {
        this();
        C = c;
    }
}

class ColumnNode extends DancingNode {
    int size;
    String name;

    ColumnNode(String n) {
        super();
        size = 0;
        name = n;
        C = this;
    }

    public void cover() {
        unlinkLR();
        for (DancingNode i = this.D; i != this; i = i.D) {
            for (DancingNode j = i.R; j != i; j = j.R) {
                j.unlinkUD();
                j.C.size--;
            }
        }
    }

    public void uncover() {
        for (DancingNode i = this.U; i != this; i = i.U) {
            for (DancingNode j = i.L; j != i; j = j.L) {
                j.C.size++;
                j.relinkUD();
            }
        }
        relinkLR();
    }
}
/**
 * Class that generates Sudokus to be solved for the visualisation.
 */
class Sudoku_Generator{
  Backtracking_Solver backtrackSolver;
  int[][] board;
  
  Sudoku_Generator(){
     this.backtrackSolver = new Backtracking_Solver(true);
     
     this.board = new int[9][9];
     
     for (int i = 0; i < 9; i++){
       for (int j = 0; j < 9; j++){
           this.board[i][j] = 0;
       }
     }  
  }
  
  public int[][] generate(){
    ArrayList<Integer> values = new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    
    Collections.shuffle(values);
    
    for (int i = 0; i < 9; i++){
        int randomValue = values.remove(values.size()-1);
        board[i][0] = randomValue;
    }
    
    values = new ArrayList(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
    
    Collections.shuffle(values);
    
    ArrayList<Integer> newOrder = new ArrayList();
    
    for (int i = 0; i < 9; i++){
        int rowStart = values.remove(values.size()-1);
        for (int j = 0; j < 9; j++){
            newOrder.add((size * rowStart) + j);
        }
    }
    
    backtrackSolver.order = newOrder;
    
    backtrackSolver.solve(board);
    
    unique();

    return board;
  }
  
  public void unique(){
    Dancing_Links_Solver solver = new Dancing_Links_Solver();
    
    int removeCount = (int) (81/difficulty);
    int clueCount = (int) Math.pow(size, 2);
    int[][] removed = new int[3][3];
    
    for (int i = 0; i < 3; i++){
      for (int j = 0; j < 3; j++){
        removed[i][j] = 0;
      }
    }
    
    int i = 0;
    while(i < removeCount){
      int randX, randY, lastVal;

      do{
       randX = Math.round(random(0, 9) - 0.5f);
       randY = Math.round(random(0, 9) - 0.5f);
      } while (board[randX][randY] == 0);
      
      lastVal = board[randX][randY];
      
      board[randX][randY] = 0;
      
      if (!solver.checkForUniqueSolution(board)){
         board[randX][randY] = lastVal;
      } else {
         removed[floor(randX/3)][floor(randY/3)] = 1;
         clueCount--;
      }
        
      i++;
    }
    
    ArrayList<ArrayList<Integer>> toFix = new ArrayList();
    
    for (int k = 0; k < 3; k++){
      for (int j = 0; j < 3; j++){
        if (removed[k][j] == 0){
          toFix.add(new ArrayList<Integer>(Arrays.asList(k, j)));
        }
      }
    }
    
    for (ArrayList<Integer> elem : toFix){
      boolean found = false;
      while(!found){
        int randX, randY, lastVal;
  
        do{
         randX = Math.round(random(elem.get(0) * 3, (elem.get(0) * 3) + 3) - 0.5f);
         randY = Math.round(random(elem.get(1) * 3, (elem.get(1) * 3) + 3) - 0.5f);
        } while (board[randX][randY] == 0);
        
        lastVal = board[randX][randY];
        
        board[randX][randY] = 0;
        
        if (!solver.checkForUniqueSolution(board)){
           board[randX][randY] = lastVal;
        } else {
           found = true;
           removed[floor(randX/3)][floor(randY/3)] = 1;
           clueCount--;
        }
      }
    }
    
    sudoku.clueCount = clueCount;
  }
  
  public void nonUnique(){
    for (int i = 0; i < Math.pow(size, 2)/1.5f; i++){
      int randX, randY;
      do{
       randX = Math.round(random(0, 9) - 0.5f);
       randY = Math.round(random(0, 9) - 0.5f);
      } while (board[randX][randY] == 0);
       
       board[randX][randY] = 0;
    }
  }
}
/**
 * A class that implements the graph that displays the progress of the simulated annealing solver.
 */
class Annealing_Graph {
    int x, y, w, h;
    ArrayList < Integer > scores;
    ArrayList < Float > temperatures;
    int maxScore, minY;
    float maxTemp;
    int currentPoint;

    Annealing_Graph(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.currentPoint = 0;
        this.maxTemp = 1;
        this.maxScore = MIN_INT;

        this.scores = new ArrayList();
        this.temperatures = new ArrayList();
    }
    
    /**
     * Adds the passed score to the list of scores to be displayed on the graph.
     */
    public void addScore(int score) {
        if (score > maxScore) {
            maxScore = score;
        }

        scores.add(score);
    }

    /**
     * Adds the passed temperature to the list of temperatures to be displayed on the graph.
     */
    public void addTemperature(float temperature) {
        temperatures.add(temperature);
    }

    /**
     * Resets the annealing graph for the next visualisation.
     */
    public void reset() {
        this.currentPoint = 0;
        this.scores = new ArrayList();
        this.temperatures = new ArrayList();

        this.maxScore = MIN_INT;

        cover();
        Draw();
    }

    /**
     * Covers up the existing graph on the canvas.
     */
    public void cover() {
        noStroke();
        fill(225);
        rect(x - 10, y - 10, width, height - (y - 10));
    }

    /**
     * Draws the graph on the canvas.
     */
    public void Draw() {
        if (sudoku.solveCompleted) {
            strokeWeight(1);
            stroke(0);
            textSize(12);
            for (float i = 0; i <= temperatures.size(); i += (temperatures.size() / 10)) {
                line(map(i, 0, temperatures.size(), x, x + w), y + h, map(i, 0, temperatures.size(), x, x + w), y + h + 25);
                text((int) i, map(i, 0, temperatures.size(), x, x + w), y + h + 35);
            }

            noStroke();
            fill(255);
            rect(x - 10, y - 10, w + 20, h + 20);

            fill(0);
            textAlign(LEFT);
            text("Solution Found: " + sudoku.solutionFound, x + w - 125, y + 20);
            text("Iterations: " + temperatures.size(), x + w - 125, y + 35);
            textAlign(CENTER, CENTER);
        } else {
            noStroke();
            fill(255);
            rect(x - 10, y - 10, w + 20, h + 20);
        }
    }

    /**
     * Draws the next point on the graph and links to the last point, as well as all of the points that are skipped if the speed is greater than 1.
     */
    public void drawNextPoint(int speed) {
        int end = currentPoint + speed;

        if (currentPoint >= temperatures.size() - 1) {
            return;
        }

        while (currentPoint < end) {
            fill(0);

            strokeWeight(1);
            stroke(lerpColor(color(0, 0, 255), color(255, 0, 0), (float) temperatures.get(currentPoint) / (float) maxTemp));

            line(map(currentPoint, 0, temperatures.size(), x, x + w), map(temperatures.get(currentPoint), minY, maxTemp, y + h/2, y), map(currentPoint + 1, 0, temperatures.size(), x, x + w), map(temperatures.get(currentPoint + 1), minY, maxTemp, y + h/2, y));
            stroke(0);
            line(map(currentPoint, 0, temperatures.size(), x, x + w), map(scores.get(currentPoint), minY, maxScore, y + h, y + h/2), map(currentPoint + 1, 0, temperatures.size(), x, x + w), map(scores.get(currentPoint + 1), minY, maxScore, y + h, y + h/2));
            currentPoint++;

            if (currentPoint >= temperatures.size() - 1) {
                break;
            }

            if (temperatures.get(currentPoint) == MIN_INT) {
                currentPoint++;
            }

            if (currentPoint >= temperatures.size() - 1) {
                break;
            }
        }
    }
}

/**
 * A class that implements the graph that displays the progress of the backtracking solver.
 */
class Backtracker_Graph {
    int x, y, w, h;
    ArrayList < Integer > points;
    int numberOfPoints;
    int maxY, minY;
    int currentPoint;

    Backtracker_Graph(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.currentPoint = 0;
        this.numberOfPoints = 0;
        this.maxY = size * size;

        this.points = new ArrayList();
    }

    /**
     * Adds the passed point to the list of points to be drawn on the graph.
     */
    public void addPoint(int point) {
        points.add(point);
        numberOfPoints++;
    }

    /**
     * Resets the graph for the next visualisation.
     */
    public void reset() {
        this.currentPoint = 0;
        this.numberOfPoints = 0;
        this.points = new ArrayList();

        Draw();
    }

    /**
     * Draws the graph on the canvas.
     */
    public void Draw() {
        if (sudoku.solveCompleted) {
            strokeWeight(1);
            stroke(0);
            fill(0);
            textSize(12);
            for (float i = 0; i <= points.size(); i += (points.size() / 10)) {
                line(map(i, 0, points.size(), x, x + w), y + h, map(i, 0, points.size(), x, x + w), y + h + 25);
                text((int) i, map(i, 0, points.size(), x, x + w), y + h + 35);
            }
        }

        noStroke();
        fill(255);
        rect(x - 10, y - 10, w + 20, h + 20);
    }

    /**
     * Draws the next point on the graph as well as all the points skipped by the visualisation due to the speed.
     */
    public void drawNextPoint(int speed) {
        int end = currentPoint + speed;

        if (currentPoint >= points.size() - 1) {
            return;
        }

        while (currentPoint < end) {
            if (points.get(currentPoint) != MIN_INT) {
                fill(0);

                strokeWeight(1);
                stroke(lerpColor(color(255, 0, 0), color(0, 255, 0), (float) points.get(currentPoint) / (float) maxY));

                line(map(currentPoint, 0, points.size(), x, x + w), map(points.get(currentPoint), minY, maxY, y + h, y), map(currentPoint + 1, 0, points.size(), x, x + w), map(points.get(currentPoint + 1), minY, maxY, y + h, y));
                currentPoint++;
            }

            if (currentPoint >= points.size() - 1) {
                break;
            }

            if (points.get(currentPoint) == MIN_INT) {
                currentPoint++;
            }

            if (currentPoint >= points.size() - 1) {
                break;
            }
        }
    }
}
/**
 * This class implements a drop list that allows the user to select a value from a list.
 */
class DropList {
    int x, y, w, h;
    ArrayList < String > labels;
    Button dropList;
    int currentlySelected;
    String title;
    Boolean dropped = false;
    int animateI;

    DropList(int x, int y, int buttonWidth, int buttonHeight, String defaultLabel, ArrayList < String > labels) {
        this.x = x;
        this.y = y;
        this.w = buttonWidth;
        this.h = buttonHeight;
        this.labels = labels;
        this.title = defaultLabel;

        this.animateI = 0;

        dropList = new Button(">", x + w - 20, y, 20, h);
    }

    // Draws the droplist on the sketch
    public void Draw() {
        noStroke();
        fill(255);
        rect(x, y, w, h, h);
        fill(0);
        textSize(13);
        text(title, x + ((w - 20) / 2), y + ((h) / 2));

        if (dropped) {
            dropList.drawSelected();
        } else {
            dropList.Draw();
        }

        if (dropped) {
            if (animateI < labels.size() - 1) {
                animateI++;
            }

            int currY = y + h;
            int col = 250;
            for (int i = 0; i <= animateI; i++) {
                fill(col);
                rect(x, currY, w - 20, h, h);
                fill(0);
                text(labels.get(i), x + ((w - 20) / 2), currY + ((h) / 2));
                currY += h;
                col -= (100) / labels.size();
            }
        } else {
            if (animateI >= 0) {
                animateI--;
            }

            int currY = y + h;
            int col = 250;
            for (int i = 0; i <= animateI; i++) {
                fill(col);
                rect(x, currY, w - 20, h, h);
                fill(0);
                text(labels.get(i), x + ((w - 20) / 2), currY + ((h) / 2));
                currY += h;
                col -= (100) / labels.size();
            }
        }
    }

    // Checks if the button to drop the list has been pressed, and if an element of the list has been selected
    public int checkForPress() {
        if (dropList.MouseIsOver()) {
            dropped = !dropped;
        }

        int toReturn = -1;
        if (dropped && mouseX > x && mouseX < x + w && mouseY > y + h && mouseY < y + (h * (labels.size() + 1))) {
            toReturn = (mouseY - y) / h;
            title = labels.get(toReturn - 1);
        }
        return toReturn;
    }

    // 'Undrops' the droplist
    public void unShowDropList() {
        stroke(256);
        fill(225);
        rect(x, y + h, w + 1, 1 + (h * labels.size()));
    }
}

/**
 * This class implements a button that can be pressed by the user.
 */
class Button {
    String label;
    float x, y, w, h;

    boolean pressed = false; // indicates if the button has been pressed
    float animationI = 0; // Where the button is in the pressed animation

    // Button constructor
    Button(String label, float x, float y, float w, float h) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Draw the button with default label
    public void Draw() {
        if (pressed) {
            pressed = false;
        }

        if (animationI > 0) {
            fill(lerpColor(color(200), color(255), (25 - animationI) / 25));
            animationI--;
        } else {
            fill(255);
        }

        textSize(13);
        rect(x, y, w, h, h);
        fill(0);
        text(label, x + (w / 2), y + (h / 2));
    }

    // Draw the button with the passed PImage
    public void Draw(PImage image) {
        noStroke();
        fill(225);
        rect(x, y, w, h);
        image(image, x, y, w, h);
        fill(0);
    }

    // Draws the button with a darker fill to signify that it has been selected.
    public void drawSelected() {
        noStroke();
        if (pressed == true) {
            if (animationI < 25) {
                fill(lerpColor(color(255), color(200), animationI / 25));
                animationI++;
            } else {
                fill(200);
            }
        }

        textSize(13);
        rect(x, y, w, h, h);
        fill(0);
        text(label, x + (w / 2), y + (h / 2));
    }

    // Returns a boolean indicating if the mouse was above the button when the mouse was pressed
    public boolean MouseIsOver() {
        if (mouseX > x && mouseX < (x + w) && mouseY > y && mouseY < (y + h)) {
            pressed = true;
            return true;
        }
        return false;
    }
}

/**
 * This class implements a slider that can be used by the user to select a value.
 */
class Slider {
    int startX, startY, sliderWidth, sliderHeight;
    float minVal, maxVal;
    int labelSize;
    float sliderX;
    float currentVal;
    String label;
    boolean sliderPressed = false;
    boolean floatOrInt = false;

    // Constructor
    Slider(int startX, int startY, int sliderWidth, int sliderHeight, float minVal, float maxVal) {
        this.startX = startX;
        this.startY = startY;
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
        this.minVal = minVal;
        this.maxVal = maxVal;

        this.currentVal = (minVal + maxVal) / 2;

        sliderX = startX + sliderWidth / 2;
    }

    // Returns the value of the slider
    public float getValue() {
        return currentVal;
    }

    // Draws the slider on the sketch
    public void display() {
        if (sliderPressed) {
            press();
        }

        fill(255);
        rect(startX - sliderHeight / 2, startY, sliderWidth + sliderHeight, sliderHeight, sliderHeight);

        fill(100);
        rect(sliderX - sliderHeight / 2, startY, sliderHeight, sliderHeight, sliderHeight);
    }

    // Checks if the slider has been clicked
    public void press() {
        if (mouseX > startX && mouseX < startX + sliderWidth) {
            if (mouseY > startY && mouseY < startY + sliderHeight || sliderPressed) {
                sliderPressed = true;
            }
        }

        if (sliderPressed) {
            if (mouseX <= startX + sliderWidth && mouseX >= startX) {
                sliderX = mouseX;
                currentVal = map(mouseX, startX, startX + sliderWidth, minVal, maxVal);
                return;
            } else if (mouseX > startX + sliderWidth) {
                sliderX = startX + sliderWidth;
                currentVal = Math.round(maxVal);
                return;
            } else if (mouseX < startX) {
                sliderX = startX;
                currentVal = Math.round(minVal);
                return;
            }
        }
    }

    // Releases the slider so the value change stops
    public void release() {
        sliderPressed = false;
    }

    // Updates the position of the slider
    public void update() {
        sliderPressed = true;

        currentVal = map(mouseX, sliderX, sliderX + sliderWidth, minVal, maxVal);
        println(currentVal);
        sliderX = mouseX;
    }
}

class Number_Input {
    int x, y, buttonSize;
    int buttonPressed;
    Button num1, num2, num3, num4, num5, num6, num7, num8, num9, clear;

    Number_Input(int x, int y, int buttonSize) {
        this.x = x;
        this.y = y;
        this.buttonSize = buttonSize;
        defineButtons();
    }

    public void defineButtons() {
        num1 = new Button("1", x, y, buttonSize, buttonSize);
        num2 = new Button("2", x + 1.1f * buttonSize, y, buttonSize, buttonSize);
        num3 = new Button("3", x + 2.2f * buttonSize, y, buttonSize, buttonSize);
        num4 = new Button("4", x, y + 1.1f * buttonSize, buttonSize, buttonSize);
        num5 = new Button("5", x + 1.1f * buttonSize, y + 1.1f * buttonSize, buttonSize, buttonSize);
        num6 = new Button("6", x + 2.2f * buttonSize, y + 1.1f * buttonSize, buttonSize, buttonSize);
        num7 = new Button("7", x, y + 2.2f * buttonSize, buttonSize, buttonSize);
        num8 = new Button("8", x + 1.1f * buttonSize, y + 2.2f * buttonSize, buttonSize, buttonSize);
        num9 = new Button("9", x + 2.2f * buttonSize, y + 2.2f * buttonSize, buttonSize, buttonSize);
        clear = new Button("CLEAR", x, y + 3.3f * buttonSize, 3.3f * buttonSize, buttonSize);
    }

    public void Draw() {
        num1.Draw();
        num2.Draw();
        num3.Draw();
        num4.Draw();
        num5.Draw();
        num6.Draw();
        num7.Draw();
        num8.Draw();
        num9.Draw();
        clear.Draw();
    }

    public boolean MouseIsOver() {
        if (mouseX > x && mouseX < (x + 3 * buttonSize)) {
            // Check Y
            if (mouseY > y && mouseY < (y + 4 * buttonSize)) {
                return true;
            }
        }
        return false;
    }

    public int getPressed() {
        if (num1.MouseIsOver()) {
            return 1;
        } else if (num2.MouseIsOver()) {
            return 2;
        } else if (num3.MouseIsOver()) {
            return 3;
        } else if (num4.MouseIsOver()) {
            return 4;
        } else if (num5.MouseIsOver()) {
            return 5;
        } else if (num6.MouseIsOver()) {
            return 6;
        } else if (num7.MouseIsOver()) {
            return 7;
        } else if (num8.MouseIsOver()) {
            return 8;
        } else if (num9.MouseIsOver()) {
            return 9;
        } else {
            return 0;
        }
    }
}
/**
 * The class that implements the sudoku displayed on the canvas.
 */
class Sudoku {
    ArrayList < ArrayList < Sudoku_Square >> squares;
    int x, y, gridSize;
    int stepI = 0;
    int clueCount = 0;

    Annealing_Solver annealingSolver;
    Backtracking_Solver backtrackSolver;
    Dancing_Links_Solver dancingSolver = new Dancing_Links_Solver();

    Sudoku_Colored_Graph colourGraph;

    int[][] board;

    boolean solveCompleted = false;
    boolean solutionFound = false;

    Sudoku(int x, int y, int gridSize) {
        squares = new ArrayList();

        this.x = x;
        this.y = y;
        this.gridSize = gridSize;

        int startY = y;

        for (int i = 0; i < size; i++) {
            int startX = x;
            ArrayList < Sudoku_Square > toAdd = new ArrayList();

            for (int j = 0; j < size; j++) {
                toAdd.add(new Sudoku_Square(startX, startY, i, j, gridSize / size));
                startX += (int) gridSize / size;
            }

            squares.add(toAdd);
            startY += (int) gridSize / size;
        }

        colourGraph = new Sudoku_Colored_Graph(9, x + (gridSize / 2), y + (gridSize / 2), (int)(gridSize / 2), getArray());
    }

    /**
     * Converts the sudoku to an arraylist, used to check validity.
     */
    public ArrayList < ArrayList < Integer >> convertInputTosudoku() {
        ArrayList < ArrayList < Integer >> toReturn = new ArrayList();
        for (ArrayList < Sudoku_Square > row: squares) {
            ArrayList < Integer > toAdd = new ArrayList();
            for (Sudoku_Square element: row) {
                toAdd.add(element.getValue());
            }
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Checks if the current sudoku is valid.
     */
    public boolean issudokuValid() {
        ArrayList < ArrayList < Integer >> rowIntegers = convertInputTosudoku();

        ArrayList < Integer > checkList = new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        // Check rows
        for (ArrayList < Integer > row: rowIntegers) {
            for (Integer number: checkList) {
                if (Collections.frequency(row, number) > 1) {
                    return false;
                }
            }
        }

        // Check columns
        for (int i = 0; i < 9; i++) {
            ArrayList < Integer > column = new ArrayList();
            for (ArrayList < Integer > row: rowIntegers) {
                column.add(row.get(i));
            }

            for (Integer toCheck: checkList) {
                if (Collections.frequency(column, toCheck) > 1) {
                    return false;
                }
            }
        }

        // Check blocks
        for (int i = 0; i < Math.sqrt(size); i++) {
            for (int j = 0; j < Math.sqrt(size); j++) {
                ArrayList < Integer > block = new ArrayList();
                for (int k = (int)(i * (Math.sqrt(size))); k < (int)(i * (Math.sqrt(size))) + (int) Math.sqrt(size); k++) {
                    for (int l = (int)(j * (Math.sqrt(size))); l < (int)(j * (Math.sqrt(size))) + (int) Math.sqrt(size); l++) {
                        block.add(rowIntegers.get(k).get(l));
                    }
                }

                for (Integer toCheck: checkList) {
                    if (Collections.frequency(block, toCheck) > 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Initialises the colour graph, overwrites the old representation and draws the connections.
     */
    public void initialiseColourGraph() {
        noStroke();
        fill(225);
        rect(0, 0, width / 2, height - 210);
        strokeWeight(1);

        colourGraph.drawConnections();
    }

    /**
     * Performs the dancing links solve on the sudoku.
     */
    public void dancingSolve() {
        if (solveCompleted) {
            return;
        }

        board = getArray();

        dancingSolver.solve(board);

        solveCompleted = true;
    }

    /**
     * Performs the backtracking solve on the sudoku.
     */
    public void backtrackerSolve() {
        if (solveCompleted) {
            return;
        }

        board = getArray();

        backtrackSolver = new Backtracking_Solver(false);

        backtrackSolver.solve(board);

        solveCompleted = true;
    }

    /**
     * Performs the simulated annealing solve on the sudoku.
     */
    public void annealingSolve() {
        if (solveCompleted) {
            return;
        }

        annealingSolver = new Annealing_Solver();

        board = getArray();

        annealingSolver.solve(board);

        solutionFound = annealingSolver.solutionFound;

        solveCompleted = true;
    }

    /**
     * Performs the next step of the visualisation when called, as well as all the steps skipped due too speed.
     */
    public void performStep(int speed) {
        if (solveCompleted) {
            if (solver == 0) {
                int end = stepI + speed;

                if (end >= backtrackerChanges.size()) {
                    end = backtrackerChanges.size();
                    visualisationFin = true;

                    highlightedX = -1;
                    highlightedY = -1;
                }

                ArrayList < Integer > sudokuStep = backtrackerChanges.get(stepI);
                ArrayList < Integer > stepHigh = highlighted.get(stepI);

                highlightedY = stepHigh.get(0);
                highlightedX = stepHigh.get(1);

                for (int i = stepI; i < end; i++) {
                    sudokuStep = backtrackerChanges.get(i);
                    if (sudokuStep.size() > 0) {
                        sudoku.setSquare(sudokuStep.get(1), sudokuStep.get(0), sudokuStep.get(2));
                    }
                }

                sudoku.display();
                stepI = end;
            } else if (solver == 1) {
                int end = stepI + speed;

                if (end >= annealingChanges.size()) {
                    end = annealingChanges.size();
                    visualisationFin = true;

                    highlightedX = -1;
                    highlightedY = -1;
                    highlightedX2 = -1;
                    highlightedY2 = -1;
                }

                while (stepI < end) {
                    ArrayList < PVector > currentChanges = annealingChanges.get(stepI);

                    if (currentChanges.size() == 0) {
                        stepI++;

                        highlightedX = -1;
                        highlightedY = -1;
                        highlightedX2 = -1;
                        highlightedY2 = -1;

                        continue;
                    }

                    PVector square1 = currentChanges.get(0);
                    PVector square2 = currentChanges.get(1);

                    highlightedX = (int) square1.x;
                    highlightedY = (int) square1.y;
                    highlightedX2 = (int) square2.x;
                    highlightedY2 = (int) square2.y;

                    int temp = squares.get((int) square1.x).get((int) square1.y).getValue();
                    sudoku.setSquare((int) square1.x, (int) square1.y, squares.get((int) square2.x).get((int) square2.y).getValue());
                    //squares.get((int) square1.x).get((int) square1.y).setValue(squares.get((int) square2.x).get((int) square2.y).getValue());
                    //squares.get((int) square2.x).get((int) square2.y).setValue(temp);
                    sudoku.setSquare((int) square2.x, (int) square2.y, temp);

                    if (stepI >= annealingChanges.size() - 1) {
                        break;
                    }

                    stepI++;
                }
                stepI = end;
            }
        }
    }

    /**
     * Resets the sudoku.
     */
    public void reset() {
        for (ArrayList < Sudoku_Square > squareList: squares) {
            for (Sudoku_Square square: squareList) {
                square.reset();
            }
        }

        solveCompleted = false;
        stepI = 0;

        if (colourGraph != null) {
            colourGraph.reset();
        }
    }

    /**
     * Gets the square selected by the user.
     */
    public void getSelectedSquare() {
        for (ArrayList < Sudoku_Square > row: sudoku.squares) {
            for (Sudoku_Square element: row) {
                if (element.MouseIsOver()) {
                    selectedX = element.getXCo();
                    selectedY = element.getYCo();
                    return;
                }
            }
        }
    }

    /**
     * Displays the sudoku on the canvas in the selected representation.
     */
    public void display() {
        if (!drawAsColour) {
            fill(0);
            stroke(0);
            strokeWeight(30);
            rect(100, 30, height - 270, height - 270);
            strokeWeight(1);

            for (ArrayList < Sudoku_Square > row: squares) {
                for (Sudoku_Square element: row) {
                    if (element.getXCo() == selectedX && element.getYCo() == selectedY) {
                        element.DrawSelected();
                    } else if ((element.getXCo() == highlightedX && element.getYCo() == highlightedY) || (element.getXCo() == highlightedX2 && element.getYCo() == highlightedY2)) {
                        element.DrawHighlightedRed();
                    } else {
                        element.Draw();
                    }
                }
            }

            strokeWeight(3);
            for (int i = 100; i < (height - 170); i += ((height - 270) / (Math.sqrt(size)))) {
                line(i, 30, i, height - 230);
            }

            for (int i = 30; i < (height - 240); i += ((height - 270) / (Math.sqrt(size)))) {
                line(100, i, height - 170, i);
            }
            strokeWeight(1);
        } else {
            colourGraph.Draw();
        }
    }

    /**
     * Clears the sudoku.
     */
    public void clear() {
        for (ArrayList < Sudoku_Square > rowToClear: squares) {
            for (Sudoku_Square element: rowToClear) {
                element.setValue(0);
            }
        }
    }

    /**
     * Sets the square with the specified coordinates to the specified value.
     */
    public void setSquare(int x, int y, int value) {
        squares.get(x).get(y).setValue(value);
        if (colourGraph != null) {
            colourGraph.groups.get(x).nodes.get(y).value = value;
        }
    }

    /**
     * Generates a sudoku.
     */
    public void gen() {
        Sudoku_Generator s = new Sudoku_Generator();
        int[][] generatedBoard = s.generate();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                setSquare(i, j, generatedBoard[i][j]);
            }
        }

        //String[] sudokuArr = loadStrings("expert.txt");

        //int number = Math.round(random(1, 1));

        //int startLineNumber = ((number - 1) * 10) + 1;

        //for (int i = 0; i < 9; i++) {
        //    char[] row = sudokuArr[startLineNumber + i].toCharArray();
        //    for (int j = 0; j < row.length; j++) {
        //        squares.get(i).get(j).setValue(row[j] - 48);
        //    }
        //}
    }

    /**
     * Returns the current sudoku as a 2D array.
     */
    public int[][] getArray() {
        int[][] toReturn;

        toReturn = new int[9][9];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                toReturn[i][j] = squares.get(i).get(j).getValue();
            }
        }

        return toReturn;
    }

    /**
     * Class that implements the sudoku square that the sudoku is comprised of.
     */
    class Sudoku_Square {
        int value, x, y, size;
        int xCoord, yCoord;
        int numberOfUpdates = 0;
        boolean changeable;

        Sudoku_Square(int x, int y, int xCoord, int yCoord, int size) {
            this.x = x;
            this.y = y;
            this.xCoord = xCoord;
            this.yCoord = yCoord;
            this.value = 0;
            this.size = size;

            this.changeable = true;
        }

        /**
         * Resets the square.
         */
        public void reset() {
            numberOfUpdates = 0;
        }

        /**
         * Draws the square on the canvas.
         */
        public void Draw() {
            stroke(0);
            strokeWeight(1);
            
            if (visualisationFin && ((solver == 1 && numberOfUpdates > 1) || (solver == 0 && numberOfUpdates >= 1))) {
                colorMode(HSB);
                fill(color(map(numberOfUpdates, 0, maxHeat, 0, 120), 255, 255));
                rect(x, y, size, size);
                colorMode(RGB);
            } else {
                fill(255);
                rect(x, y, size, size);
            }

            fill(0);
            if (value != 0) {
                textSize(20);
                text(value, x + size / 2, y + size / 2);
            }
        }

        /**
         * Draws the square in a grey colour to indicate that this is the square the user can modify the value of. 
         */
        public void DrawSelected() {
            fill(200);
            rect(x, y, size, size);
            textSize(20);
            if (value != 0) {
                fill(0);
                text(value, x + size / 2, y + size / 2);
            }
        }

        /**
         * Draws the square with a red background to indicate the square is being looked at by the algorithm.
         */
        public void DrawHighlightedRed() {
            fill(255, 0, 0);
            rect(x, y, size, size);
            if (value != 0) {
                fill(0);
                text(value, x + size / 2, y + size / 2);
            }
        }

        /**
         * Sets the value of the square to the passed value.
         */
        public void setValue(int input) {

            if (!solvePressed && input != 0) {
                this.changeable = false;
            }

            this.value = input;
            
            if (solvePressed){
              numberOfUpdates++;
            }

            if (numberOfUpdates > maxHeat) {
                maxHeat = numberOfUpdates;
            }
        }

        /**
         * Returns the value of the square.
         */
        public int getValue() {
            return value;
        }

        /**
         * Checks if the users mouth is over this square.
         */
        public boolean MouseIsOver() {
            // Check X
            if (mouseX > x && mouseX < (x + size)) {
                // Check Y
                if (mouseY > y && mouseY < (y + size)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the x coordinate of the square on the sudoku.
         */
        public int getXCo() {
            return xCoord;
        }

        /**
         * Returns the y coordinate of the square on the sudoku.
         */
        public int getYCo() {
            return yCoord;
        }
    }
}
class Sudoku_Colored_Graph {
    ArrayList < Node_Group > groups;
    int size, centerX, centerY, radius;
    int[][] board;

    Sudoku_Colored_Graph(int size, int centerX, int centerY, int radius, int[][] board) {
        this.size = size;
        this.groups = new ArrayList();
        this.board = board;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;

        for (int i = 0; i < size; i++) {
            groups.add(new Node_Group(centerX, centerY, i, radius));
        }

        intitaliseColumnCommections();

        intialiseRowConnections();

        intitialiseBlockConnections();
    }

    public void drawConnections() {
        fill(50);
        circle(centerX, centerY, 2 * radius);

        for (Node_Group group: groups) {
            group.DrawRowConnections();
        }

        for (Node_Group group: groups) {
            group.DrawBlockConnections();
        }

        for (Node_Group group: groups) {
            group.DrawColumnConnections();
        }
    }

    public void reset() {
        for (Node_Group group: groups) {
            for (Node node: group.nodes) {
                node.value = 0;
            }
        }
    }

    public void Draw() {
        for (Node_Group group: groups) {
            group.Draw();
        }
    }

    public void intialiseRowConnections() {
        for (Node_Group group: groups) {
            for (Node_Group otherGroup: groups) {
                for (int i = 0; i < size; i++) {
                    if (group != otherGroup) {
                        group.nodes.get(i).addConnected(otherGroup.nodes.get(i), 1);
                    }
                }
            }
        }
    }

    public void intitaliseColumnCommections() {
        for (Node_Group group: groups) {
            for (int i = 0; i < size - 1; i++) {
                group.nodes.get(i).addConnected(group.nodes.get(i + 1), 0);
            }
        }
    }

    public void intitialiseBlockConnections() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Node node = groups.get(i).nodes.get(j);
                for (Node otherNode: getBlock(i, j)) {
                    if (node != otherNode) {
                        node.addConnected(otherNode, 2);
                    }
                }
            }
        }
    }

    public ArrayList < Node > getBlock(int xCo, int yCo) {
        int blockStartX = xCo - (int)(xCo % Math.sqrt(size));
        int blockStartY = yCo - (int)(yCo % Math.sqrt(size));

        ArrayList < Node > toReturn = new ArrayList();
        for (int i = blockStartX; i < blockStartX + Math.sqrt(size); i++) {
            for (int j = blockStartY; j < blockStartY + Math.sqrt(size); j++) {
                toReturn.add(groups.get(i).nodes.get(j));
            }
        }

        return toReturn;
    }

    class Node {
        int x, y;
        int value;
        ArrayList < Node > columnConnectedNodes;
        ArrayList < Node > rowsConnectedNodes;
        ArrayList < Node > blockConnectedNodes;

        Node(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.columnConnectedNodes = new ArrayList();
            this.rowsConnectedNodes = new ArrayList();
            this.blockConnectedNodes = new ArrayList();
        }

        public void addConnected(Node node, int type) {
            if (type == 0 && !columnConnectedNodes.contains(node)) {
                columnConnectedNodes.add(node);
                node.addConnected(this, 0);
            }

            if (type == 1 && !rowsConnectedNodes.contains(node)) {
                rowsConnectedNodes.add(node);
                node.addConnected(this, 1);
            }

            if (type == 2 && !blockConnectedNodes.contains(node)) {
                blockConnectedNodes.add(node);
                node.addConnected(this, 2);
            }
        }

        public void Draw() {
            stroke(0);
            strokeWeight(1);
            if (value != 0) {
                colorMode(HSB);
                fill(map(value, 1, size, 0, 255), 255, 255);
                colorMode(RGB);
            } else {
                fill(255);
            }
            circle(x, y, 0.015f * height);
        }

        public void DrawColumnConnections() {
            for (Node node: columnConnectedNodes) {
                stroke(0, 0, 255);
                strokeWeight(2);
                line(x, y, node.x, node.y);
            }
        }

        public void DrawRowConnections() {
            for (Node node: rowsConnectedNodes) {
                stroke(255, 0, 0);
                strokeWeight(0.5f);
                line(x, y, node.x, node.y);
            }
        }

        public void DrawBlockConnections() {
            for (Node node: blockConnectedNodes) {
                stroke(0, 255, 0);
                strokeWeight(0.5f);
                line(x, y, node.x, node.y);
            }
        }
    }

    class Node_Group {
        ArrayList < Node > nodes;
        int x, y;
        float angle;
        int columnNumber;
        int totalLength = (int) (0.2f * height);

        Node_Group(int centerX, int centerY, int columnNumber, int radius) {
            this.nodes = new ArrayList();
            this.angle = (2 * PI / size) * columnNumber;
            this.columnNumber = columnNumber;
            this.x = centerX + (int)(radius * Math.cos(angle - (PI / 2)));
            this.y = centerY + (int)(radius * Math.sin(angle - (PI / 2)));

            for (int i = 0; i < size; i++) {
                int nodeX = (x - (int)(Math.cos(angle) * totalLength / 2)) + (int)(Math.cos(angle) * (i * (totalLength / size)));
                int nodeY = (y - (int)(Math.sin(angle) * totalLength / 2)) + (int)(Math.sin(angle) * (i * (totalLength / size)));
                nodes.add(new Node(nodeX, nodeY, board[i][columnNumber]));
            }
        }

        public void DrawColumnConnections() {
            for (Node node: nodes) {
                node.DrawColumnConnections();
            }
        }

        public void DrawRowConnections() {
            for (Node node: nodes) {
                node.DrawRowConnections();
            }
        }

        public void DrawBlockConnections() {
            for (Node node: nodes) {
                node.DrawBlockConnections();
            }
        }

        public void Draw() {
            for (Node node: nodes) {
                node.Draw();
            }
        }
    }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--hide-stop", "Sodoku_Solver" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
