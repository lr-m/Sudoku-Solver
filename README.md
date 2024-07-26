# Sudoku Solver Visualisation

This is a Processing sketch this visualised backtracking and simulated annealing methods for solving sudoku puzzles, as well as a [dancing-links implementation](https://github.com/rafalio/dancing-links-java/tree/master).

![interface.png](/images/interface.png)

It has the following:
- Sudoku generator of various difficulty (generates a puzzle and deletes squares from it)
- Backtracker implementation (visualised)
- Simulated annealing implementation (visualised)
- Ported [dancing links implementation](https://github.com/rafalio/dancing-links-java/tree/master) (not visualised)
- Speed control (iterations per frame)
- Represenation as a graph colouring problem (because why not)

## Backtracking

This is a visualisation of a pretty basic backtracking solver, you can control the direction to one of the four available.

![backtracker.gif](/images/backtracker.gif)

## Simulated Annealing

This is a visualisation of a simulated annealing solver, you can change the behaviour of the solver with the sliders.

![simulated_annealing.gif](/images/simulated_annealing.gif)

## Colouring Problem

This basically just represents the sudoku puzzle as a graph colouring problem (i.e. any connected dot cannot be the same colour).

![graph.gif](/images/graph.gif)
