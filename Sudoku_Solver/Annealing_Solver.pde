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
    ArrayList < Integer > getNumberList(ArrayList < Integer > numbers) {
        numbers.clear();

        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }

        return numbers;
    }

    /**
     * Attempts to solve the passed 2d array representing a sudoku.
     */
    void solve(int[][] toSolve) {
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
    int score(int[][] board) {
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
    void solveIteration() {
        int initConflicts = score(sudokuArr);
        annealingGraph.addScore(initConflicts);
        int xOffset = 3 * Math.round(random(-0.5, 2.5));
        int yOffset = 3 * Math.round(random(-0.5, 2.5));

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
    void copy(int[][] source, int[][] destination) {
        for (int a = 0; a < source.length; a++) {
            System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
        }
    }

    /**
     * Updates the temperature for the annealing solve.
     */
    float updateTemp(float temperature) {
        temperature *= tempReduction;
        return temperature;
    }
}
