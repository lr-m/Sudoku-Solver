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
    void Draw() {
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
    int checkForPress() {
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
    void unShowDropList() {
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
    void Draw() {
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
    void Draw(PImage image) {
        noStroke();
        fill(225);
        rect(x, y, w, h);
        image(image, x, y, w, h);
        fill(0);
    }

    // Draws the button with a darker fill to signify that it has been selected.
    void drawSelected() {
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
    boolean MouseIsOver() {
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
    float getValue() {
        return currentVal;
    }

    // Draws the slider on the sketch
    void display() {
        if (sliderPressed) {
            press();
        }

        fill(255);
        rect(startX - sliderHeight / 2, startY, sliderWidth + sliderHeight, sliderHeight, sliderHeight);

        fill(100);
        rect(sliderX - sliderHeight / 2, startY, sliderHeight, sliderHeight, sliderHeight);
    }

    // Checks if the slider has been clicked
    void press() {
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
    void release() {
        sliderPressed = false;
    }

    // Updates the position of the slider
    void update() {
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

    void defineButtons() {
        num1 = new Button("1", x, y, buttonSize, buttonSize);
        num2 = new Button("2", x + 1.1 * buttonSize, y, buttonSize, buttonSize);
        num3 = new Button("3", x + 2.2 * buttonSize, y, buttonSize, buttonSize);
        num4 = new Button("4", x, y + 1.1 * buttonSize, buttonSize, buttonSize);
        num5 = new Button("5", x + 1.1 * buttonSize, y + 1.1 * buttonSize, buttonSize, buttonSize);
        num6 = new Button("6", x + 2.2 * buttonSize, y + 1.1 * buttonSize, buttonSize, buttonSize);
        num7 = new Button("7", x, y + 2.2 * buttonSize, buttonSize, buttonSize);
        num8 = new Button("8", x + 1.1 * buttonSize, y + 2.2 * buttonSize, buttonSize, buttonSize);
        num9 = new Button("9", x + 2.2 * buttonSize, y + 2.2 * buttonSize, buttonSize, buttonSize);
        clear = new Button("CLEAR", x, y + 3.3 * buttonSize, 3.3 * buttonSize, buttonSize);
    }

    void Draw() {
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

    boolean MouseIsOver() {
        if (mouseX > x && mouseX < (x + 3 * buttonSize)) {
            // Check Y
            if (mouseY > y && mouseY < (y + 4 * buttonSize)) {
                return true;
            }
        }
        return false;
    }

    int getPressed() {
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
