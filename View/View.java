package View;

import Controller.*;
import skills.Text.*;
import views.Turtle.*;

import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class View implements IView {
    private final int height = 600;
    private final int width = 600;
    int tileSize;
    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int margin = 20;
    int topMargin = 50;
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

        int xMin = controller.getXY().get(0);
        int xMax = controller.getXY().get(2);
        int yMin = controller.getXY().get(1);
        int yMax = controller.getXY().get(3);

        int plotWidth = width - 2 * margin;
        int plotHeight = height - topMargin - margin;

        // Rahmen zeichnen
        t.moveTo(margin, topMargin);
        t.lineTo(margin + plotWidth, topMargin); // oben
        t.lineTo(margin + plotWidth, topMargin + plotHeight); // rechts
        t.lineTo(margin, topMargin + plotHeight); // unten
        t.lineTo(margin, topMargin); // links

        int xSteps = xMax - xMin;
        int ySteps = yMax - yMin;

        int maxSteps = Math.max(xSteps, ySteps); // Wir wollen den größten Wert als Referenz verwenden
        tileSize = Math.min(plotWidth / maxSteps, plotHeight / maxSteps);

        // Achsenmittelpunkte
        halfWidth = margin + plotWidth / 2;
        halfHeight = topMargin + plotHeight / 2; // Anpassen für topMargin

        // Zeichnen der Achsen
        t.moveTo(halfWidth, topMargin);
        t.lineTo(halfWidth, topMargin + plotHeight);
        t.moveTo(margin, halfHeight);
        t.lineTo(margin + plotWidth, halfHeight);

        drawGrid(plotWidth, plotHeight);
        labelAxis(plotWidth, plotHeight);

        Button resetButton = new Button(Clerk.view(), controller, "Reset");

        IntegerInput inputXMin = new IntegerInput(Clerk.view(), "xMin");
        IntegerInput inputYMin = new IntegerInput(Clerk.view(), "yMin");
        IntegerInput inputXMax = new IntegerInput(Clerk.view(), "xMax");
        IntegerInput inputYMax = new IntegerInput(Clerk.view(), "yMax");
    }

    void drawGrid(int plotWidth, int plotHeight) {
        t.color(211, 211, 211);
        for (int x = margin; x < margin + plotWidth; x += tileSize) {
            t.moveTo(x, topMargin);
            t.lineTo(x, topMargin + plotHeight);
        }
        for (int y = topMargin + tileSize; y < topMargin + plotHeight; y += tileSize) {
            t.moveTo(margin, y);
            t.lineTo(margin + plotWidth, y);
        }
    }


    void labelAxis(int plotWidth, int plotHeight) {
        t.color(0);
        t.left(90);

        // Beschriftung der X-Achse (unten am Rand)
        int temp = tileSize;

        // Positive X-Beschriftung (rechts vom Ursprung)
        for (int i = 1; i <= controller.getXY().get(2); i++) {
            t.moveTo(halfWidth + temp, topMargin + plotHeight + 15);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        temp = tileSize;

        int negativeX = controller.getXY().get(0);
        if (negativeX < 0) {
            negativeX = -negativeX;
        }
        // Negative X-Beschriftung (links vom Ursprung)
        for (int i = 1; i <= negativeX; i++) {
            t.moveTo(halfWidth - temp, topMargin + plotHeight + 15);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        // Beschriftung der Y-Achse (links vom Koordinatensystem)
        temp = tileSize;

        // Positive Y-Beschriftung (oberhalb des Ursprungs)
        for (int i = 1; i <= controller.getXY().get(3); i++) {
            t.moveTo(margin - 10, halfHeight - temp + 3);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        temp = tileSize;
        int negativeY = controller.getXY().get(1);
        if (negativeY < 0) {
            negativeY = -negativeY;
        }
        // Negative Y-Beschriftung
        for (int i = 1; i <= negativeY; i++) {
            t.moveTo(margin - 10, halfHeight + temp + 3);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        // Beschriftung des Ursprungs (0,0)
        t.moveTo(margin - 10, halfHeight);
        t.text("0");
        t.moveTo(halfWidth, topMargin + plotHeight + 15);
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
                    delegate.accept(value);
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
