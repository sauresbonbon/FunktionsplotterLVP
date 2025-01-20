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
    int tileSizeX, tileSizeY;
    boolean useParameter = false;
    int zoomValue;

    int xMin, xMax, yMin, yMax;

    DoubleUnaryOperator function;

    private Turtle t;
    private IController controller;

    public void setController(IController controller) {
        this.controller = controller;
    }

    public void draw() {
        Clerk.view();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        controller.initializePlotter();
        t = new Turtle(width, height);

        t.left(90);
        t.moveTo(halfWidth, (double) margin / 2);
        t.textSize = 20;
        t.text("Funktionsplotter");
        t.textSize = 10;
        t.right(90);

        drawPlotter();
        drawUI();
    }

    void drawPlotter() {
        generateTileSizes();
        drawGrid(plotWidth, plotHeight);
        drawPlotterArea();
        labelAxis();
        drawFunction();
    }

    /*
        Fügt Button, Slider und Textfelder der UI hinzu
     */
    void drawUI() {
        setupFunctionInput();
        setupBoundsInput();
        setupParameterInput();
        setupZoomSlider();
    }

    /*
        Initialisiert das Eingabefeld und den Button für die Funktionseingabe,
        um eine mathematische Funktion zu definieren und anzuzeigen.
     */
    private void setupFunctionInput() {
        TextInput functionInput = new TextInput(Clerk.view(), "f(x)");
        Button functionButton = new Button(Clerk.view(), controller, "Generate");
        functionButton.attachTo(() -> {
            System.out.println(functionInput.getValue());
            controller.setFunction(x -> -(x * x));
            drawFunction();
        });
    }

    /*
        Initialisiert die Eingabe- und Steuerungselemente,
        um die Begrenzungen (xMin, xMax, yMin, yMax) des Koordinatensystems
        festzulegen und diese auf das Koordinatensystem anzuwenden.
     */
    private void setupBoundsInput() {
        Clerk.write(Clerk.view(), "Grenzen setzen");

        createIntegerInput("xMin", controller.getXY().get(0), value -> xMin = Integer.parseInt(value));
        createIntegerInput("xMax", controller.getXY().get(2), value -> xMax = Integer.parseInt(value));
        createIntegerInput("yMin", controller.getXY().get(1), value -> yMin = Integer.parseInt(value));
        createIntegerInput("yMax", controller.getXY().get(3), value -> yMax = Integer.parseInt(value));

        Button setBoundsButton = new Button(Clerk.view(), controller, "Set bounds");
        setBoundsButton.attachTo(() -> {
            controller.setBounds(xMin, yMin, xMax, yMax);
            t.reset();
            drawPlotter();
        });
    }

    /*
        Erstellt ein IntegerInput
     */
    private IntegerInput createIntegerInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, defaultValue);
        input.attachTo(onChange);
        return input;
    }

    /*
        Erstellt eine Checkbox und Eingabefelder für die Definition eines Parameter-Intervalls.
        Wenn die Checkbox aktiviert wird, wird der Parameterbereich für die Funktion f(x;a) gesetzt.
     */
    private void setupParameterInput() {
        Checkbox parameterCheckbox = new Checkbox(Clerk.view(), "Solve function as f(x;a) with");
        IntegerInput parameterFrom = new IntegerInput(Clerk.view(), "from", 1);
        IntegerInput parameterTo = new IntegerInput(Clerk.view(), "to", 5);
        parameterCheckbox.attachTo(value -> {
            value = useParameter;
            useParameter = !value;
        });
    }

    /*
        Erstellt einen Slider zur Einstellung des Zooms und ruft die zoom() Methode auf.
     */
    private void setupZoomSlider() {
        SliderStufen s = new SliderStufen(Clerk.view(), 0, 5, "Zoom",0, zoomValue);
        zoom(s);
    }

    /*
        Der Slider passt die Grenzen der Achsen an und aktualisiert die Darstellung des Diagramms.
     */
    void zoom(SliderStufen s) {
        s.attachTo(response -> {
            int delta = (Integer.parseInt(response) - zoomValue);

            if (delta > 0) {
                // Wenn der Slider-Wert steigt (Rauszoomen),
                // "vergrößern" wir xMin, yMin und verkleinern xMax, yMax
                xMin += (xMin == 0 ? 0 : 1);
                yMin += (yMin == 0 ? 0 : 1);
                xMax -= (xMax == 0 ? 0 : 1);
                yMax -= (yMax == 0 ? 0 : 1);
            } else if (delta < 0) {
                // Wenn der Slider-Wert sinkt (Reinzoomen),
                // verkleinern wir xMin, yMin und "vergrößern" xMax, yMax
                xMin -= (xMin == 0 ? 0 : 1);
                yMin -= (yMin == 0 ? 0 : 1);
                xMax += (xMax == 0 ? 0 : 1);
                yMax += (yMax == 0 ? 0 : 1);
            }

            controller.setBounds(xMin, yMin, xMax, yMax);

            t.reset();
            drawPlotter();

            zoomValue = Integer.parseInt(response);
        });
    }

    /*
        Zeichnet den Rahmen und die Achsen
     */
    public void drawPlotterArea() {
        t.color(0);
        // Grenzen abrufen
        xMin = controller.getXY().get(0);
        xMax = controller.getXY().get(2);
        yMin = controller.getXY().get(1);
        yMax = controller.getXY().get(3);

        // Dynamisch den Ursprung berechnen
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        // Ursprungskoordinaten basierend auf den Grenzen
        halfWidth = margin + (int) ((-xMin / xRange) * plotWidth);
        halfHeight = margin + plotHeight - (int) ((-yMin / yRange) * plotHeight);

        // Achsen zeichnen
        t.lineWidth(2);
        t.moveTo(halfWidth, margin); // y-Achse
        t.lineTo(halfWidth, margin + plotHeight);
        t.moveTo(margin, halfHeight); // x-Achse
        t.lineTo(margin + plotWidth, halfHeight);
        t.lineWidth(1);

        // Rahmen zeichnen
        t.moveTo(margin, margin); // oben
        t.lineTo(margin + plotWidth, margin); // rechts
        t.lineTo(margin + plotWidth, margin + plotHeight); // unten
        t.lineTo(margin, margin + plotHeight); // links
        t.lineTo(margin, margin); // zurück zum Startpunkt
    }


    /*
        Methode zum berechnen der Kästchengröße
     */
    void generateTileSizes() {
        List<Integer> coordinates = controller.getXY();
        xMin = coordinates.get(0);
        xMax = coordinates.get(2);
        yMin = coordinates.get(1);
        yMax = coordinates.get(3);

        // Anzahl der Schritte berechnen
        double xSteps = xMax - xMin;
        double ySteps = yMax - yMin;

        // Schrittgrößen berechnen
        tileSizeX = (int) (plotWidth / xSteps);
        tileSizeY = (int) (plotHeight / ySteps);
    }


    /*
        Zeichnet die Kästchen des Koordinatensystems
    */
    void drawGrid(int plotWidth, int plotHeight) {
        t.color(211, 211, 211);

        int right = margin + plotWidth; // Rechte Grenze
        int bottom = margin + plotHeight; // Untere Grenze

        // Vertikale Linien zeichnen (X-Richtung)
        for (int x = margin + tileSizeX; x < right; x += tileSizeX) {
            t.moveTo(x, margin);      // Startpunkt der Linie
            t.lineTo(x, bottom);      // Endpunkt der Linie
        }

        // Horizontale Linien zeichnen (Y-Richtung)
        for (int y = margin + tileSizeY; y < bottom; y += tileSizeY) {
            t.moveTo(margin, y);      // Startpunkt der Linie
            t.lineTo(right, y);       // Endpunkt der Linie
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
        Beschriftet die Achsen
    */
    void labelAxis() {
        t.color(0); // Textfarbe Schwarz
        t.left(90); // Textausrichtung nach links rotieren

        // X-Achse
        int xMin = controller.getXY().get(0);
        int xMax = controller.getXY().get(2);

        // Positive X-Beschriftung (rechts vom Ursprung)
        for (int i = 1; i <= xMax; i++) {
            int xPos = halfWidth + i * tileSizeX; // Position der Beschriftung
            t.moveTo(xPos, margin + plotHeight + 15); // Verschieben unterhalb der X-Achse
            t.text(String.valueOf(i)); // Beschriftung zeichnen
        }

        // Negative X-Beschriftung (links vom Ursprung)
        for (int i = 1; i <= Math.abs(xMin); i++) {
            int xPos = halfWidth - i * tileSizeX; // Position der Beschriftung
            t.moveTo(xPos, margin + plotHeight + 15); // Verschieben unterhalb der X-Achse
            t.text(String.valueOf(-i)); // Beschriftung zeichnen
        }

        // Y-Achse
        int yMin = controller.getXY().get(1);
        int yMax = controller.getXY().get(3);

        // Positive Y-Beschriftung (oberhalb des Ursprungs)
        for (int i = 1; i <= yMax; i++) {
            int yPos = halfHeight - i * tileSizeY; // Position der Beschriftung
            t.moveTo(margin - 10, yPos + 3); // Verschieben links von der Y-Achse
            t.text(String.valueOf(i)); // Beschriftung zeichnen
        }

        // Negative Y-Beschriftung (unterhalb des Ursprungs)
        for (int i = 1; i <= Math.abs(yMin); i++) {
            int yPos = halfHeight + i * tileSizeY; // Position der Beschriftung
            t.moveTo(margin - 10, yPos + 3); // Verschieben links von der Y-Achse
            t.text(String.valueOf(-i)); // Beschriftung zeichnen
        }

        // Ursprung (0,0)
        t.moveTo(margin - 10, halfHeight); // Beschriftung links neben der Y-Achse
        t.text("0");
        t.moveTo(halfWidth, margin + plotHeight + 15); // Beschriftung unterhalb der X-Achse
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
        this.view.createResponseContext("/text" + ID, delegate, ID);
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

    IntegerInput attachTo(Consumer<String> delegate) {
        this.view.createResponseContext("/input" + ID, delegate, ID);

        Clerk.script(view, Text.fillOut(
                """
                input${0}.addEventListener('input', (event) => {
                    const value = event.target.value;
                    console.log("input${0}: value = " + value);
                    fetch('input${0}', {
                        method: 'post',
                        body: value.toString()
                    }).catch(console.error);
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

    SliderStufen(LiveView view, double min, double max, String label, int defaultValue, int zoomValue) {
        this.view = view;
        ID = Clerk.getHashID(this);

        // HTML: Label, Slider und Wertanzeige
        Clerk.write(view,
                "<label for='slider" + ID + "'>" + label + "</label>" +
                        "<div>" +
                        "  <input type='range' id='slider" + ID + "' min='" + min + "' max='" + max + "' step='1' value='" + defaultValue + "'/>" +
                        "  <span id='value" + ID + "'>" + defaultValue + "</span>" +
                        "</div>");

        // JavaScript: Slider und Wertanzeige initialisieren
        Clerk.script(view, "const slider" + ID + " = document.getElementById('slider" + ID + "');");
        Clerk.script(view, "const valueDisplay" + ID + " = document.getElementById('value" + ID + "');");
    }

    SliderStufen attachTo(Consumer<String> delegate) {
        // Server-seitige Verbindung erstellen
        this.view.createResponseContext("/slider" + ID, delegate, ID);

        // JavaScript: Event-Listener für den Slider
        Clerk.script(view, Text.fillOut(
                """
                slider${0}.addEventListener('input', (event) => {
                    const value = event.target.value;
                    console.log("slider${0}: value = " + value);
                    valueDisplay${0}.textContent = value; // Aktualisiere Wertanzeige
                    fetch('slider${0}', {
                        method: 'post',
                        body: value.toString()
                    }).catch(console.error);
                });
                """, Map.of("0", ID)));
        return this;
    }
}
