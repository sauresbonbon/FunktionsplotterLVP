package View;

import Controller.*;
import skills.Text.*;
import views.Turtle.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

public class View implements IView {
    private final int height = 600;
    private final int width = 600;
    int margin = 50;

    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int plotWidth = width - 2 * margin;
    int plotHeight = plotWidth;
    int tileSize;
    boolean useParameter = false;

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
        t.moveTo(halfWidth, margin / 2);
        t.textSize = 20;
        t.text("Funktionsplotter");
        t.textSize = 10;
        t.right(90);

        generateTileSize();
        drawPlotterArea();
        drawGrid(plotWidth, plotHeight);
        labelAxis(plotWidth, plotHeight);
        drawUI();
    }


    /*
        Fügt Button, Slider und Textfelder der UI hinzu
     */
    void drawUI() {
        // Textfeld und Button für die zu zeichnende Funktion
        TextInput functionInput = new TextInput(Clerk.view(), "f(x)");
        Button functionButton = new Button(Clerk.view(), controller, "Generate");
        functionButton.attachTo(() -> {
            System.out.println(functionInput.getValue());
            controller.setFunction(x -> (x * x) + 1);
            drawFunction();
        });

        // Input und Button für das Setzen von den Grenzen des Koordinatensystems
        Clerk.write(Clerk.view(), "Grenzen setzen");
        IntegerInput inputXMin = new IntegerInput(Clerk.view(), "xMin", controller.getXY().get(0));
        inputXMin.attachTo(value -> {
            controller.setXY(value, controller.getXY().get(1), controller.getXY().get(2), controller.getXY().get(3));
            System.out.println("Eingegebener Wert: " + value);
        });
        IntegerInput inputXMax = new IntegerInput(Clerk.view(), "xMax", controller.getXY().get(2));
        IntegerInput inputYMin = new IntegerInput(Clerk.view(), "yMin", controller.getXY().get(1));
        IntegerInput inputYMax = new IntegerInput(Clerk.view(), "yMax", controller.getXY().get(3));
        Button setBoundsButton = new Button(Clerk.view(), controller, "Set bounds");
        setBoundsButton.attachTo(() -> {

        });

        // Checkbox und Input für Interval der Parameter
        Checkbox parameterCheckbox = new Checkbox(Clerk.view(), "Solve function as f(x;a) with");
        IntegerInput parameterFrom = new IntegerInput(Clerk.view(), "from", 1);
        IntegerInput parameterTo = new IntegerInput(Clerk.view(), "to", 5);
        parameterCheckbox.attachTo(value -> {
            value = useParameter;
            useParameter = !value;
        });

        // Slider für den Zoom
        Slider s = new Slider(Clerk.view(),0,10, "Zoom");

    }

    /*
        Zeichnet den Rahmen und die Achsen
     */
    public void drawPlotterArea() {
        // Achsenmittelpunkte
        halfWidth = margin + plotWidth / 2;
        halfHeight = margin + plotHeight / 2;

        //Achsen
        t.moveTo(halfWidth, margin);
        t.lineTo(halfWidth, margin + plotHeight); //y-Achse
        t.moveTo(margin, halfHeight);
        t.lineTo(margin + plotWidth, halfHeight); //x-Achse

        // Rahmen
        t.moveTo(margin, margin);
        t.lineTo(margin + plotWidth, margin); // oben
        t.lineTo(margin + plotWidth, margin + plotHeight); // rechts
        t.lineTo(margin, margin + plotHeight); // unten
        t.lineTo(margin, margin); // links
    }

    /*
        Methode zum berechnen der Kästchengröße
     */
    void generateTileSize() {
        List<Integer> coordinates = controller.getXY();
        xMin = coordinates.get(0);
        xMax = coordinates.get(2);
        yMin = coordinates.get(1);
        yMax = coordinates.get(3);

        // Anzahl Schritte berechnen
        int xSteps = xMax - xMin;
        int ySteps = yMax - yMin;

        // Schrittgrößen berechnen
        int xStepSize = plotWidth / xSteps;
        int yStepSize = plotHeight / ySteps;

        tileSize = xStepSize > yStepSize ? xStepSize : yStepSize;
    }

    /*
        Zeichnet die Kästchen des Koordinatensystems
     */
    void drawGrid(int plotWidth, int plotHeight) {
        t.color(211, 211, 211);

        int right = margin + plotWidth;
        int bottom = margin + plotHeight;

        // Gitterlinien zeichnen
        for (int i = margin + tileSize; i < Math.max(right, bottom); i += tileSize) {
            if (i < right) { // Vertikale Linie
                t.moveTo(i, margin);
                t.lineTo(i, bottom);
            }
            if (i < bottom) { // Horizontale Linie
                t.moveTo(margin, i);
                t.lineTo(right, i);
            }
        }
    }

    /*
        Zeichnet die Funktionen in das Koordinatensystem
     */
    void drawFunction() {
        DoubleUnaryOperator function = controller.getFunction();
        if (controller.getFunction() == null) return;

        t.color(0, 0, 255);

        double step = 0.1; // Schrittweite für das Zeichnen
        double scaleX = (double) plotWidth / (controller.getXY().get(2) - controller.getXY().get(0));
        double scaleY = (double) plotHeight / (controller.getXY().get(3) - controller.getXY().get(1));

        double xMin = controller.getXY().get(0);
        double xMax = controller.getXY().get(2);
        double yMin = controller.getXY().get(1);
        double yMax = controller.getXY().get(3);

        for (double x = xMin; x <= xMax; x += step) {
            double y = function.applyAsDouble(x);

            if (y < yMin || y > yMax) continue; // Punkt außerhalb des sichtbaren Bereichs

            int screenX = (int) (halfWidth + x * scaleX);
            int screenY = (int) (halfHeight - y * scaleY);

            double nextY = function.applyAsDouble(x + step);
            if (nextY >= yMin && nextY <= yMax) {
                int nextScreenX = (int) (halfWidth + (x + step) * scaleX);
                int nextScreenY = (int) (halfHeight - nextY * scaleY);

                t.moveTo(screenX, screenY);
                t.lineTo(nextScreenX, nextScreenY);
            }
        }
    }

    /*

     */
    void labelAxis(int plotWidth, int plotHeight) {
        t.color(0); // Textfarbe Schwarz
        t.left(90); // Textausrichtung nach links rotieren

        // X-Achse
        int temp = tileSize;
        int xMin = controller.getXY().get(0);
        int xMax = controller.getXY().get(2);

        // Positive X-Beschriftung (rechts vom Ursprung)
        for (int i = 1; i <= xMax; i++) {
            t.moveTo(halfWidth + temp, margin + plotHeight + 15);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        // Negative X-Beschriftung (links vom Ursprung)
        int negativeX = Math.abs(xMin); // Betrag der negativen X-Werte
        temp = tileSize;
        for (int i = 1; i <= negativeX; i++) {
            t.moveTo(halfWidth - temp, margin + plotHeight + 15);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        // Y-Achse
        temp = tileSize;
        int yMin = controller.getXY().get(1);
        int yMax = controller.getXY().get(3);

        // Positive Y-Beschriftung (oberhalb des Ursprungs)
        for (int i = 1; i <= yMax; i++) {
            t.moveTo(margin - 10, halfHeight - temp + 3);
            t.text(String.valueOf(i));
            temp += tileSize;
        }

        // Negative Y-Beschriftung
        int negativeY = Math.abs(yMin); // Betrag der negativen Y-Werte
        temp = tileSize;
        for (int i = 1; i <= negativeY; i++) {
            t.moveTo(margin - 10, halfHeight + temp + 3);
            t.text(String.valueOf(-i));
            temp += tileSize;
        }

        // Ursprung (0,0)
        t.moveTo(margin - 10, halfHeight);
        t.text("0");
        t.moveTo(halfWidth, margin + plotHeight + 15);
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

class Checkbox implements Clerk {
    final String ID;
    LiveView view;

    Checkbox(LiveView view, String label) {
        this.view = view;
        ID = Clerk.getHashID(this);

        // HTML für Checkbox hinzufügen
        Clerk.write(view, "<div>" +
                "<input type='checkbox' id='checkbox" + ID + "'/>" +
                "<label for='checkbox" + ID + "'>" + label + "</label>" +
                "</div>");

        // JavaScript für Checkbox hinzufügen
        Clerk.script(view, "const checkbox" + ID + " = document.getElementById('checkbox" + ID + "');");
    }

    Checkbox attachTo(Consumer<Boolean> delegate) {
        this.view.createResponseContext("/checkbox" + ID, (body) -> {
            try {
                boolean checked = Boolean.parseBoolean(body); // Den Status der Checkbox auslesen (true/false)
                delegate.accept(checked); // Den Wert an den Delegate übergeben
            } catch (Exception e) {
                System.err.println("Fehler beim Verarbeiten des Checkbox-Status: " + body);
            }
        }, ID);

        // JavaScript-Code, der bei einer Änderung der Checkbox den Status sendet
        Clerk.script(view, Text.fillOut(
                """
                checkbox${0}.addEventListener('change', () => {
                    if (locks.includes('${0}')) return;
                    locks.push('${0}');
                    const checked = checkbox${0}.checked; // Status der Checkbox ermitteln
                    console.log('checkbox${0}: checked changed to ' + checked);
                    fetch('checkbox${0}', {
                        method: 'post',
                        body: checked.toString(), // Den Status als String (true/false) senden
                    }).catch(console.error).finally(() => {
                        locks = locks.filter(lock => lock !== '${0}');
                    });
                });
                """, Map.of("0", ID)));

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

    IntegerInput(LiveView view, String placeholder, int defaultValue) {
        this.view = view;
        ID = Clerk.getHashID(this);

        Clerk.write(view,
                "<div>" +
                        "<input id='input" + ID +
                        "' type='number' placeholder='" + placeholder +
                        "' value='" + defaultValue + "' size='5'>" +
                        "</div>");

        Clerk.script(view,
                "const input" + ID + " = document.getElementById('input" + ID + "');");
    }

    IntegerInput attachTo(Consumer<Integer> delegate) {
        this.view.createResponseContext("/input" + ID, (body) -> {
            try {
                int value = Integer.parseInt(body);
                delegate.accept(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer input: " + body);
            }
        }, ID);

        Clerk.script(view,
                "const input" + ID + " = document.getElementById('input" + ID + "');" +
                        "if (input" + ID + ") {" +
                        "    input" + ID + ".addEventListener('change', () => {" +
                        "        const value = input" + ID + ".value;" +
                        "        console.log('input" + ID + ": value changed to ' + value);" +
                        "        fetch('input" + ID + "', {" +
                        "            method: 'post'," +
                        "            body: value," +  // Der Wert wird direkt als Body gesendet
                        "        }).catch(console.error);" +
                        "    });" +
                        "} else {" +
                        "    console.error('Input field not found for ID: ' + '" + ID + "');" +
                        "}"
        );

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