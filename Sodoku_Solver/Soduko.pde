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
    ArrayList < ArrayList < Integer >> convertInputTosudoku() {
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
    boolean issudokuValid() {
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
    void initialiseColourGraph() {
        noStroke();
        fill(225);
        rect(0, 0, width / 2, height - 210);
        strokeWeight(1);

        colourGraph.drawConnections();
    }

    /**
     * Performs the dancing links solve on the sudoku.
     */
    void dancingSolve() {
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
    void backtrackerSolve() {
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
    void annealingSolve() {
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
    void performStep(int speed) {
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
    void reset() {
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
    void getSelectedSquare() {
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
    void display() {
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
    void clear() {
        for (ArrayList < Sudoku_Square > rowToClear: squares) {
            for (Sudoku_Square element: rowToClear) {
                element.setValue(0);
            }
        }
    }

    /**
     * Sets the square with the specified coordinates to the specified value.
     */
    void setSquare(int x, int y, int value) {
        squares.get(x).get(y).setValue(value);
        if (colourGraph != null) {
            colourGraph.groups.get(x).nodes.get(y).value = value;
        }
    }

    /**
     * Generates a sudoku.
     */
    void gen() {
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
    int[][] getArray() {
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
        void reset() {
            numberOfUpdates = 0;
        }

        /**
         * Draws the square on the canvas.
         */
        void Draw() {
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
        void DrawSelected() {
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
        void DrawHighlightedRed() {
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
        void setValue(int input) {

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
        int getValue() {
            return value;
        }

        /**
         * Checks if the users mouth is over this square.
         */
        boolean MouseIsOver() {
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
        int getXCo() {
            return xCoord;
        }

        /**
         * Returns the y coordinate of the square on the sudoku.
         */
        int getYCo() {
            return yCoord;
        }
    }
}
