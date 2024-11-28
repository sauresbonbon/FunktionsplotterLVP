/o lvp.java
/o View.java

import views.Turtle.*;
public class Main {

    int heigth = 600;
    int width = 600;

    Main() {

    }

    public static void main(String[] args) {
        Main main = new Main();
        main.drawPlotter();
    }

    public void drawPlotter() {
        Turtle t = new Turtle(width, heigth);
        t.moveTo(width/2,0);
        t.lineTo(width/2,heigth);
        t.moveTo(0,heigth/2);
        t.lineTo(width,heigth/2);

        t.moveTo(width/2,0);
        t.lineTo((width/2)-10,10);
        t.moveTo(width/2,0);
        t.lineTo((width/2)+10,10);

        t.moveTo(width,heigth/2);
        t.lineTo(width-10,(heigth/2)-10);
        t.moveTo(width,heigth/2);
        t.lineTo(width-10,(heigth/2)+10);
    }
}