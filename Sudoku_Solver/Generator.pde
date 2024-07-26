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
  
  int[][] generate(){
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
  
  void unique(){
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
       randX = Math.round(random(0, 9) - 0.5);
       randY = Math.round(random(0, 9) - 0.5);
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
         randX = Math.round(random(elem.get(0) * 3, (elem.get(0) * 3) + 3) - 0.5);
         randY = Math.round(random(elem.get(1) * 3, (elem.get(1) * 3) + 3) - 0.5);
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
  
  void nonUnique(){
    for (int i = 0; i < Math.pow(size, 2)/1.5; i++){
      int randX, randY;
      do{
       randX = Math.round(random(0, 9) - 0.5);
       randY = Math.round(random(0, 9) - 0.5);
      } while (board[randX][randY] == 0);
       
       board[randX][randY] = 0;
    }
  }
}
