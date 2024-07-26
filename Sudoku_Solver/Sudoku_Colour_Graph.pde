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

    void drawConnections() {
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

    void reset() {
        for (Node_Group group: groups) {
            for (Node node: group.nodes) {
                node.value = 0;
            }
        }
    }

    void Draw() {
        for (Node_Group group: groups) {
            group.Draw();
        }
    }

    void intialiseRowConnections() {
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

    void intitaliseColumnCommections() {
        for (Node_Group group: groups) {
            for (int i = 0; i < size - 1; i++) {
                group.nodes.get(i).addConnected(group.nodes.get(i + 1), 0);
            }
        }
    }

    void intitialiseBlockConnections() {
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

    ArrayList < Node > getBlock(int xCo, int yCo) {
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

        void addConnected(Node node, int type) {
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

        void Draw() {
            stroke(0);
            strokeWeight(1);
            if (value != 0) {
                colorMode(HSB);
                fill(map(value, 1, size, 0, 255), 255, 255);
                colorMode(RGB);
            } else {
                fill(255);
            }
            circle(x, y, 0.015 * height);
        }

        void DrawColumnConnections() {
            for (Node node: columnConnectedNodes) {
                stroke(0, 0, 255);
                strokeWeight(2);
                line(x, y, node.x, node.y);
            }
        }

        void DrawRowConnections() {
            for (Node node: rowsConnectedNodes) {
                stroke(255, 0, 0);
                strokeWeight(0.5);
                line(x, y, node.x, node.y);
            }
        }

        void DrawBlockConnections() {
            for (Node node: blockConnectedNodes) {
                stroke(0, 255, 0);
                strokeWeight(0.5);
                line(x, y, node.x, node.y);
            }
        }
    }

    class Node_Group {
        ArrayList < Node > nodes;
        int x, y;
        float angle;
        int columnNumber;
        int totalLength = (int) (0.2 * height);

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

        void DrawColumnConnections() {
            for (Node node: nodes) {
                node.DrawColumnConnections();
            }
        }

        void DrawRowConnections() {
            for (Node node: nodes) {
                node.DrawRowConnections();
            }
        }

        void DrawBlockConnections() {
            for (Node node: nodes) {
                node.DrawBlockConnections();
            }
        }

        void Draw() {
            for (Node node: nodes) {
                node.Draw();
            }
        }
    }
}
