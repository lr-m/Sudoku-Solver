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
    boolean solve(int[][] board) {
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
    void backtrack(int[][] board) {
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
    ArrayList < Integer > getPossibleValues(int currentX, int currentY, int[][] board) {
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
