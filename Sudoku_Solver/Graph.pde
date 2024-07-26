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
    void addScore(int score) {
        if (score > maxScore) {
            maxScore = score;
        }

        scores.add(score);
    }

    /**
     * Adds the passed temperature to the list of temperatures to be displayed on the graph.
     */
    void addTemperature(float temperature) {
        temperatures.add(temperature);
    }

    /**
     * Resets the annealing graph for the next visualisation.
     */
    void reset() {
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
    void cover() {
        noStroke();
        fill(225);
        rect(x - 10, y - 10, width, height - (y - 10));
    }

    /**
     * Draws the graph on the canvas.
     */
    void Draw() {
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
    void drawNextPoint(int speed) {
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
    void addPoint(int point) {
        points.add(point);
        numberOfPoints++;
    }

    /**
     * Resets the graph for the next visualisation.
     */
    void reset() {
        this.currentPoint = 0;
        this.numberOfPoints = 0;
        this.points = new ArrayList();

        Draw();
    }

    /**
     * Draws the graph on the canvas.
     */
    void Draw() {
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
    void drawNextPoint(int speed) {
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
