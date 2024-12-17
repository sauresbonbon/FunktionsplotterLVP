package View;

import Controller.*;
import skills.Text.*;
import views.Turtle.*;

import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class View implements IView {
    private int height = 600;
    private int width = 600;
    int tileSize = 30;
    int halfWidth = width / 2;
    int halfHeight = height / 2;
    Turtle t;
    private IController controller;

    public void setController(IController controller) {
        this.controller = controller;
    }


    public void drawPlotter() {
        Clerk.view();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        controller.initializePlotter();
        t = new Turtle(width, height);

        t.moveTo(halfWidth,0);
        t.lineTo(halfWidth,height);
        t.moveTo(0,halfHeight);
        t.lineTo(width,halfHeight);

        t.moveTo(halfWidth,0);
        t.lineTo(halfWidth-10,10);
        t.moveTo(halfWidth,0);
        t.lineTo(halfWidth+10,10);

        t.moveTo(width,halfHeight);
        t.lineTo(width-10,halfHeight-10);
        t.moveTo(width,halfHeight);
        t.lineTo(width-10,halfHeight+10);

        drawGrid();
        labelAxis();

        String bName = "Werte Zurücksetzen";
        Button b = new Button(Clerk.view(), controller, bName);

        IntegerInput inputXMin = new IntegerInput(Clerk.view(), "xMin");
        IntegerInput inputYMin = new IntegerInput(Clerk.view(), "yMin");
        IntegerInput inputXMax = new IntegerInput(Clerk.view(), "xMax");
        IntegerInput inputYMax = new IntegerInput(Clerk.view(), "yMax");

//        String sName = "slider";
//        String sStName = "slider mit Stufen";
//        Slider s = new Slider(Clerk.view(), 0,10, sName);
//        SliderStufen sSt = new SliderStufen(Clerk.view(), 1, 5, sStName);
    }
    void drawGrid() {
        t.color(211, 211, 211);
        for (int x = tileSize; x < width; x += tileSize) {
            t.moveTo(halfWidth + x, 0);
            t.lineTo(halfWidth + x, height);
            t.moveTo(halfWidth - x, 0);
            t.lineTo(halfWidth - x, height);
        }
        for (int y = tileSize; y < height; y += tileSize) {
            t.moveTo(0, halfHeight + y);
            t.lineTo(width, halfHeight + y);
            t.moveTo(0, halfHeight - y);
            t.lineTo(width, halfHeight - y);
        }
    }
    void labelAxis() {
        t.color(0); // Schwarz für Text
        t.left(90);

        // Beschriftung der X-Achse (unten am Rand)
        int temp = tileSize;

        // Positive X-Beschriftung
        for (int i = 1; i <= controller.getXY().get(2); i++) {
            t.moveTo(halfWidth + temp - 5, height - 10);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        temp = tileSize;
        // Negative X-Beschriftung
        for (int i = 1; i <= controller.getXY().get(0); i++) { // Startet bei 1
            t.moveTo(halfWidth - temp - 5, height - 10);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        temp = tileSize;
        // Positive Y-Beschriftung
        for (int i = 1; i <= controller.getXY().get(3); i++) {
            t.moveTo(10, halfHeight - temp + 5);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        temp = tileSize;
        // Negative Y-Beschriftung
        for (int i = 1; i <= controller.getXY().get(1); i++) { // Startet bei 1
            t.moveTo(10, halfHeight + temp + 5);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        // Beschriftung des Ursprungs (0,0)
        t.moveTo(5, halfHeight); // Links am Rand für Y-Achse
        t.text("0");
        t.moveTo(halfWidth, height - 5); // Unten am Rand für X-Achse
        t.text("0");
    }


}


    /*
    Button
     */
    class Button implements Clerk {
        final String ID;
        LiveView view;
        IController controller;

        Button(LiveView view,IController controller, String label) {
            this.view = view;
            this.controller = controller;
            ID = Clerk.getHashID(this);
            Clerk.write(view, "<div><button id='button" + ID + "'>" + label + "</button></div>");
            Clerk.script(view, "const button" + ID + " = document.getElementById('button" + ID + "');");
        }

        Button attachTo(Runnable delegate) {
            this.view.createResponseContext("/button" + ID, (body) -> {
                delegate.run();

                if(controller != null) {

                }

            }, ID);
            Clerk.script(view, Text.fillOut(
                    """
                    button${0}.addEventListener('click', () => {
                        if (locks.includes('${0}')) return;
                        locks.push('${0}');
                        console.log(`button${0}: clicked`);
                        fetch('button${0}', {
                           method: 'post',
                        }).catch(console.error);
                    });
                    """, Map.of("0", ID)));
            return this;
        }
    }

    class IntegerInput implements Clerk {
        final String ID;
        LiveView view;

        IntegerInput(LiveView view, String placeholder) {
            this.view = view;
            ID = Clerk.getHashID(this);

            // HTML-Input-Feld erzeugen
            Clerk.write(view,
                    "<div><input id='input" + ID + "' type='number' placeholder='" + placeholder + "'></div>");
            Clerk.script(view,
                    "const input" + ID + " = document.getElementById('input" + ID + "');");
        }

        IntegerInput attachTo(java.util.function.Consumer<Integer> delegate) {
            this.view.createResponseContext("/input" + ID, (body) -> {
                try {
                    int value = Integer.parseInt(body);
                    delegate.accept(value); // Delegate mit Integer-Wert ausführen
                } catch (NumberFormatException e) {
                    System.err.println("Invalid integer input: " + body);
                }
            }, ID);

            Clerk.script(view, Text.fillOut(
                    """
                    input${0}.addEventListener('change', () => {
                        if (locks.includes('${0}')) return;
                        locks.push('${0}');
                        const value = input${0}.value;
                        console.log(`input${0}: value changed to ${value}`);
                        fetch('input${0}', {
                            method: 'post',
                            body: value,
                        }).catch(console.error).finally(() => {
                            locks = locks.filter(lock => lock !== '${0}');
                        });
                    });
                    """, Map.of("0", ID)));
            return this;
        }
    }

    /*
    Normaler Slider
     */
    class Slider implements Clerk {
        final String ID;
        LiveView view;

        Slider(LiveView view, double min, double max, String label) {
            this.view = view;
            ID = Clerk.getHashID(this);

            // Korrektur: label wird korrekt eingebettet
            Clerk.write(view, "<label for='slider" + ID + "'>" + label + "</label>" +
                    "<div><input type='range' id='slider" + ID + "' min='" + min + "' max='" + max + "' step='any'/></div>");

            Clerk.script(view, "const slider" + ID + " = document.getElementById('slider" + ID + "');");
            Clerk.script(view, "const valueDisplay" + ID + " = document.getElementById('value" + ID + "');");
        }

        Slider attachTo(Consumer<String> delegate) {
            this.view.createResponseContext("/slider" + ID, delegate, ID);
            Clerk.script(view, Text.fillOut(
                    """
                    slider${0}.addEventListener('input', (event) => {
                        if (locks.includes('${0}')) return;
                        locks.push('${0}');
                        const value = event.target.value;
                        console.log(`slider${0}: value = ${value}`);
                        valueDisplay${0}.textContent = value;
                        fetch('slider${0}', {
                           method: 'post',
                            body: value.toString()
                        }).catch(console.error);
                    });
                    """, Map.of("0", ID)));
            return this;
        }
    }

    /*
    Slider mit Abstufungen
     */
    class SliderStufen implements Clerk {
        final String ID;
        LiveView view;

        SliderStufen(LiveView view, double min, double max, String label) {
            this.view = view;
            ID = Clerk.getHashID(this);

            // Korrektur: label wird korrekt eingebettet
            Clerk.write(view, "<label for='slider" + ID + "'>" + label + "</label>" +
                    "<div><input type='range' id='slider" + ID + "' min='" + min + "' max='" + max + "' step='1'/></div>");

            Clerk.script(view, "const slider" + ID + " = document.getElementById('slider" + ID + "');");
            Clerk.script(view, "const valueDisplay" + ID + " = document.getElementById('value" + ID + "');");
        }

        SliderStufen attachTo(Consumer<String> delegate) {
            this.view.createResponseContext("/slider" + ID, delegate, ID);
            Clerk.script(view, Text.fillOut(
                    """
                    slider${0}.addEventListener('input', (event) => {
                        if (locks.includes('${0}')) return;
                        locks.push('${0}');
                        const value = event.target.value;
                        console.log(`slider${0}: value = ${value}`);
                        valueDisplay${0}.textContent = value;
                        fetch('slider${0}', {
                           method: 'post',
                            body: value.toString()
                        }).catch(console.error);
                    });
                    """, Map.of("0", ID)));
            return this;
        }
    }
