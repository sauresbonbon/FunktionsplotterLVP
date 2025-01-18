package View;

import Controller.*;
import skills.Text.*;
import views.Turtle.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

public class View implements IView {
    private final int height = 600;
    private final int width = 600;
    int margin = 50;
    int topMargin = 50;

    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int plotWidth = width - 2 * topMargin;
    int plotHeight = plotWidth;
    int tileSize;

    int xMin, xMax, yMin, yMax;

    DoubleUnaryOperator function;

    private Turtle t;
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

        t.left(90);
        t.moveTo(halfWidth, topMargin/2);
        t.textSize = 20;
        t.text("Funktionsplotter");
        t.textSize = 10;
        t.right(90);

        controller.setXY(-5, -5, 5, 5);

        generateTileSize();
        drawPlotterArea();
        drawGrid(plotWidth, plotHeight);
        labelAxis(plotWidth, plotHeight);
        drawUI();

        controller.setFunction(Math::cos);
        drawFunction();

        function = controller.getFunction();


    }


    void drawUI() {
        TextInput functionInput = new TextInput(Clerk.view(), "f(x)");

        Button plotButton = new Button(Clerk.view(), controller, "Generate");

        Clerk.write(Clerk.view(),"Zurücksetzen");
        Button resetButton = new Button(Clerk.view(), controller, "Reset");

        Clerk.write(Clerk.view(), "Grenzen setzen");
        IntegerInput inputXMin = new IntegerInput(Clerk.view(), "xMin");
        IntegerInput inputYMin = new IntegerInput(Clerk.view(), "yMin");
        IntegerInput inputXMax = new IntegerInput(Clerk.view(), "xMax");
        IntegerInput inputYMax = new IntegerInput(Clerk.view(), "yMax");

        SliderStufen parameterCount = new SliderStufen(Clerk.view(), 0,5, "Anzahl Parameter");

        Slider s = new Slider(Clerk.view(),0,10, "Zoom");

    }

    public void drawPlotterArea() {


        // Rahmen zeichnen
        t.moveTo(margin, topMargin);
        t.lineTo(margin + plotWidth, topMargin); // oben
        t.lineTo(margin + plotWidth, topMargin + plotHeight); // rechts
        t.lineTo(margin, topMargin + plotHeight); // unten
        t.lineTo(margin, topMargin); // links


        // Achsenmittelpunkte
        halfWidth = margin + plotWidth / 2;
        halfHeight = topMargin + plotHeight / 2;

        // Zeichnen der Achsen
        t.moveTo(halfWidth, topMargin);
        t.lineTo(halfWidth, topMargin + plotHeight);
        t.moveTo(margin, halfHeight);
        t.lineTo(margin + plotWidth, halfHeight);
    }

    void generateTileSize() {
        xMin = controller.getXY().get(0);
        xMax = controller.getXY().get(2);
        yMin = controller.getXY().get(1);
        yMax = controller.getXY().get(3);


        int xSteps = xMax - xMin;
        int ySteps = yMax - yMin;

        int xStepSize = plotWidth/xSteps;
        int yStepSize = plotHeight/ySteps;

        if (xStepSize > yStepSize) {
            tileSize = xStepSize;
        } else tileSize = yStepSize;

//        System.out.println("xMin: " + xMin + ", xMax: " + xMax + ", yMin: " + yMin + ", yMax: " + yMax);
//        System.out.println("xSteps: " + xSteps + ", ySteps: " + ySteps);
//        System.out.println("plotWidth: " + plotWidth + ", plotHeight: " + plotHeight);
//        System.out.println("xStepSize: " + xStepSize + ", yStepSize: " + yStepSize);
//        System.out.println("tileSize: " + tileSize);

    }

    void drawGrid(int plotWidth, int plotHeight) {
        t.color(211, 211, 211);
        //Vertikale linien
        for (int x = margin + tileSize; x < margin + plotWidth; x += tileSize) {
            t.moveTo(x, topMargin);
            t.lineTo(x, topMargin + plotHeight);
        }
        //horizontal linien
        for (int y = topMargin + tileSize; y < topMargin + plotHeight; y += tileSize) {
            t.moveTo(margin, y);
            t.lineTo(margin + plotWidth, y);
        }
    }

    void drawFunction() {
        if (controller.getFunction() == null) {
            return;
        }
        DoubleUnaryOperator function = controller.getFunction();

        t.color(0, 0, 255); // Blau für die Funktion

        double step = 0.1; // Schrittweite für das Zeichnen der Punkte
        double scaleX = (double) plotWidth / (controller.getXY().get(2) - controller.getXY().get(0));
        double scaleY = (double) plotHeight / (controller.getXY().get(3) - controller.getXY().get(1));

        for (double x = controller.getXY().get(0); x <= controller.getXY().get(2); x += step) {
            double y = function.applyAsDouble(x);

            if (y >= controller.getXY().get(1) && y <= controller.getXY().get(3)) {
                int screenX = (int) (halfWidth + x * scaleX);
                int screenY = (int) (halfHeight - y * scaleY);
                t.moveTo(screenX, screenY);

                // Linie zeichnen
                double nextY = function.applyAsDouble(x + step);
                if (nextY >= controller.getXY().get(1) && nextY <= controller.getXY().get(3)) {
                    int nextScreenX = (int) (halfWidth + (x + step) * scaleX);
                    int nextScreenY = (int) (halfHeight - nextY * scaleY);
                    t.lineTo(nextScreenX, nextScreenY);
                }
            }
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

class TextInput implements Clerk {
    final String ID;
    LiveView view;

    TextInput(LiveView view, String placeholder) {
        this.view = view;
        ID = Clerk.getHashID(this);


        Clerk.write(view,
                "<div>" +
                        "<input id='text" + ID +
                        "' type='text' placeholder='" + placeholder + "' size='20'>" +
                        "</div>");

        Clerk.script(view,
                "const text" + ID + " = document.getElementById('text" + ID + "');");
    }

    // Abrufen des Textwerts von der LiveView
    public String getValue() {
        // Hier könnte eine JavaScript-Abruflogik benötigt werden, aber wir müssen dies über den Event-Handler machen
        // Return-Wert muss dynamisch aus der Frontend-Antwort kommen, daher kein direkter Zugriff
        return "";  // Hier könnte ein Mechanismus zum Abrufen des Textwerts implementiert werden
    }

    TextInput attachTo(Consumer<String> delegate) {
        // JavaScript oder Server-Anfrage an LiveView zum Abfragen des Werts
        this.view.createResponseContext("/text" + ID, (body) -> delegate.accept(body), ID);
        return this;
    }
}




/*
Button
 */
class Button implements Clerk {
    final String ID;
    LiveView view;
    View v = new View();
    IController controller;

    Button(LiveView view, IController controller, String label) {
        this.view = view;
        this.controller = controller;
        ID = Clerk.getHashID(this);
        Clerk.write(view, "<div><button id='button" + ID + "'>" + label + "</button></div>");
        Clerk.script(view, "const button" + ID + " = document.getElementById('button" + ID + "');");
    }

    Button attachTo(Runnable delegate) {
        this.view.createResponseContext("/button" + ID, (body) -> {
            controller.setFunction(x -> (x * x) +1);
            v.drawFunction();
            delegate.run();
        }, ID);
        Clerk.script(view, String.format(
                "button%s.addEventListener('click', () => {" +
                        "" +
                        "   fetch('button%s', {method: 'post'})" +
                        "   .catch(console.error);" +
                        "});", ID, ID));
        return this;
    }
}

class IntegerInput implements Clerk {
    final String ID;
    LiveView view;

    IntegerInput(LiveView view, String placeholder) {
        this.view = view;
        ID = Clerk.getHashID(this);

        Clerk.write(view,
                "<div>" +
                        "<input id='input" + ID +
                        "' type='number' placeholder='" + placeholder + "' size = '5'>" +
                        "</div>");

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
                    console.log(input${0}: value changed to ${value});
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
                    console.log(slider${0}: value = ${value});
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
                    console.log(slider${0}: value = ${value});
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