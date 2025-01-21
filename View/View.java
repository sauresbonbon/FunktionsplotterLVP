package View;

import skills.Text.*;
import views.Turtle.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {
    private final int height = 600;
    private final int width = 600;
    int margin = 50;
    List<Integer> bounds = new ArrayList<>();
    private DoubleUnaryOperator f;
    Function function1, function2, function3;;

    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int plotWidth = width - 2 * margin;
    int plotHeight = plotWidth;
    int tileSizeX, tileSizeY;
    boolean useParameter = false;
    int zoomValue;
    List<Integer> ogCoordinates;

    int xMin, xMax, yMin, yMax;

    private Turtle t;

    //-------------------------------Initialize--------------------------//

    public void initializePlotter() {
        //xMin
        bounds.add(-10);
        //yMin
        bounds.add(-10);

        //xMax
        bounds.add(10);
        //yMax
        bounds.add(10);
    }

    void initializeFunctions() {


    }

    //----------------------------draw-methods--------------------------//

    public void draw() {
        Clerk.view();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        initializePlotter();
        initializeFunctions();
        ogCoordinates = getBounds();
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
        Zeichnet das gesamte Koordinatensystem mit der generierten tileSize
     */
    void drawPlotter() {
        generateTileSizes();
        drawGrid(plotWidth, plotHeight);
        drawPlotterArea();
        labelAxis();
        if (function1 != null) drawFunction(function1);
        if (function2 != null) drawFunction(function2);
        if (function3 != null) drawFunction(function3);

    }


    /*
        Zeichnet den Rahmen und die Achsen
     */
    public void drawPlotterArea() {
        t.color(0);
        // Grenzen abrufen
        xMin = getBounds().get(0);
        xMax = getBounds().get(2);
        yMin = getBounds().get(1);
        yMax = getBounds().get(3);

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
        Zeichnet die Kästchen des Koordinatensystems
    */
    void drawGrid(int plotWidth, int plotHeight) {
        t.color(211, 211, 211);

        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        double pixelPerX = plotWidth / xRange;
        double pixelPerY = plotHeight / yRange;

        int right = margin + plotWidth;
        int bottom = margin + plotHeight;

        // Vertikale Linien zeichnen (X-Achse)
        for (int i = 0; i <= xRange; i++) {
            int x = (int) (margin + i * pixelPerX);
            t.moveTo(x, margin);
            t.lineTo(x, bottom);
        }

        // Horizontale Linien zeichnen (Y-Achse)
        for (int i = 0; i <= yRange; i++) {
            int y = (int) (margin + i * pixelPerY);
            t.moveTo(margin, y);
            t.lineTo(right, y);
        }
    }


    /*
        Zeichnet die Funktionen in das Koordinatensystem
     */
    void drawFunction(Function function) {

        t.color(function.color.getRed(), function.color.getGreen(), function.color.getBlue());
        double step = 0.1;
        double scaleX = (double) plotWidth / (getBounds().get(2) - getBounds().get(0));
        double scaleY = (double) plotHeight / (getBounds().get(3) - getBounds().get(1));

        for (double x = xMin; x <= xMax; x += step) {
            double y = function.function.applyAsDouble(x);
            if (y < yMin || y > yMax) continue;

            int screenX = (int) (halfWidth + x * scaleX);
            int screenY = (int) (halfHeight - y * scaleY);

            double nextY = function.function.applyAsDouble(x + step);
            if (nextY >= yMin && nextY <= yMax) {
                int nextScreenX = (int) (halfWidth + (x + step) * scaleX);
                int nextScreenY = (int) (halfHeight - nextY * scaleY);

                t.moveTo(screenX, screenY);
                t.lineTo(nextScreenX, nextScreenY);
            }
        }

    }

    //-----------------------------setup-methods------------------------------//

    /*
        Initialisiert das Eingabefeld und den Button für die Funktionseingabe,
        um eine mathematische Funktion zu definieren und anzuzeigen.
     */
    private void setupFunctionInput() {
        TextInput functionInput1 = new TextInput(Clerk.view(), "f(x) = ","blue" ,"f(x)");
        functionInput1.attachTo(delegate -> {
            DoubleUnaryOperator newFunction = parseFunction(delegate);
            function1 = new Function(newFunction, Color.BLUE);
        });
        TextInput functionInput2 = new TextInput(Clerk.view(), "g(x) = ", "red", "g(x)");
        functionInput2.attachTo(delegate -> {
            DoubleUnaryOperator newFunction = parseFunction(delegate);
            function2 = new Function(newFunction, Color.RED);
        });
        TextInput functionInput3 = new TextInput(Clerk.view(), "h(x) = ", "green", "h(x)");
        functionInput3.attachTo(delegate -> {
           DoubleUnaryOperator newFunction = parseFunction(delegate);
           function3 = new Function(newFunction, Color.GREEN);
        });
        Button functionButton = new Button(Clerk.view(), "Generate");
        functionButton.attachTo(() -> {
            // Überprüfen, ob jede Funktion gesetzt wurde, bevor sie gezeichnet wird
            if (function1 != null && function1.getFunction() != null) {
                drawFunction(function1);
            }
            if (function2 != null && function2.getFunction() != null) {
                drawFunction(function2);
            }
            if (function3 != null && function3.getFunction() != null) {
                drawFunction(function3);
            }
            t.reset();
            drawPlotter();
        });
    }

    /*
        Initialisiert die Eingabe- und Steuerungselemente,
        um die Begrenzungen (xMin, xMax, yMin, yMax) des Koordinatensystems
        festzulegen und diese auf das Koordinatensystem anzuwenden.
     */
    private void setupBoundsInput() {
        Clerk.write(Clerk.view(), "Grenzen setzen");

        createIntegerInput("xMin", getBounds().get(0), value -> xMin = Integer.parseInt(value));
        createIntegerInput("xMax", getBounds().get(2), value -> xMax = Integer.parseInt(value));
        createIntegerInput("yMin", getBounds().get(1), value -> yMin = Integer.parseInt(value));
        createIntegerInput("yMax", getBounds().get(3), value -> yMax = Integer.parseInt(value));

        Button setBoundsButton = new Button(Clerk.view(), "Set bounds");
        setBoundsButton.attachTo(() -> {
            setBounds(xMin, yMin, xMax, yMax);
            ogCoordinates = getBounds();
            t.reset();
            drawPlotter();
        });
    }

    /*
    Erstellt eine Checkbox und Eingabefelder für die Definition eines Parameter-Intervalls.
    Wenn die Checkbox aktiviert wird, wird der Parameterbereich für die Funktion f(x;a) gesetzt.
 */
    private void setupParameterInput() {
        Checkbox parameterCheckbox = new Checkbox(Clerk.view(), "Solve function as f(x;a) with");
        IntegerInput parameterFrom = new IntegerInput(Clerk.view(), "from", "from",1);
        IntegerInput parameterTo = new IntegerInput(Clerk.view(), "to", "to",5);
        parameterCheckbox.attachTo(value -> {
            value = useParameter;
            useParameter = !value;
        });
    }

    /*
       Erstellt einen Slider zur Einstellung des Zooms und ruft die zoom() Methode auf.
    */
    private void setupZoomSlider() {
        SliderStufen zoomSlider = new SliderStufen(Clerk.view(), 0, 5, "Zoom",0, zoomValue);

        zoom(zoomSlider);
    }

    public void setBounds(int xMin, int yMin, int xMax, int yMax) {
        if(xMin >= xMax) {
            throw new IllegalArgumentException("xMin muss kleiner als xMax sein. (xMin = " + xMin + ", xMax = " + xMax + ")");
        }
        if(yMin >= yMax) {
            throw new IllegalArgumentException("yMin muss kleiner als yMax sein. (yMin = " + yMin + ", yMax = " + yMax + ")");
        }
        else {
            bounds.set(0, xMin);
            bounds.set(1, yMin);
            bounds.set(2, xMax);
            bounds.set(3, yMax);
        }
    }


    public List<Integer> getBounds() {
        return bounds;
    }

    public DoubleUnaryOperator parseFunction(String functionInput) {
        functionInput = functionInput.replaceAll("\\s+", ""); // Entfernt alle Leerzeichen

        // Beispiel für das Erkennen von x^n (Potenzfunktionen)
        if (functionInput.matches("^[xX]\\^\\d+$")) {
            int power = Integer.parseInt(functionInput.substring(2));
            return (x) -> Math.pow(x, power);
        }

        // Beispiel für lineare Funktionen der Form 2x + 3
        if (functionInput.matches("^[+-]?\\d*x([+-]?\\d+)?$")) {
            Pattern pattern = Pattern.compile("([+-]?\\d*)x([+-]?\\d*)");
            Matcher matcher = pattern.matcher(functionInput);
            if (matcher.matches()) {
                int coefficient = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
                int constant = matcher.group(2).isEmpty() ? 0 : Integer.parseInt(matcher.group(2));
                return (x) -> coefficient * x + constant;
            }
        }

        // Beispiel für trigonometrische Funktionen wie sin(x), cos(x), tan(x)
        if (functionInput.matches("^(sin|cos|tan)\\(x\\)$")) {
            if (functionInput.startsWith("sin")) {
                return (x) -> Math.sin(x);
            } else if (functionInput.startsWith("cos")) {
                return (x) -> Math.cos(x);
            } else if (functionInput.startsWith("tan")) {
                return (x) -> Math.tan(x);
            }
        }
        return null;
    }


    /*
        Erstellt ein IntegerInput
     */
    private void createIntegerInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, label,  defaultValue);
        input.attachTo(onChange);
    }





    /*
        Der Slider passt die Grenzen der Achsen an und aktualisiert die Darstellung des Diagramms.
     */
    void zoom(SliderStufen s) {
        s.attachTo(response -> {
            // delta gibt an, ob der Zoomwert steigt oder sinkt, also rein oder rauszoomen
            int delta = (Integer.parseInt(response) - zoomValue);
            zoomValue = Integer.parseInt(response);
            if (delta > 0) {
                // Wenn der Slider-Wert steigt (Rauszoomen),
                // "vergrößern" wir xMin, yMin und verkleinern xMax, yMax
                if(ogCoordinates.get(0) <= -1) {
                    xMin += (xMin == 0 ? 0 : 1);
                }
                if(ogCoordinates.get(1) <= -1) {
                    yMin += (yMin == 1 ? 0 : 1);
                }
                if(ogCoordinates.get(2) >= 1) {
                    xMax -= (xMax == 0 ? 0 : 1);
                }
                if(ogCoordinates.get(3) >= 1) {
                    yMax -= (yMax == 1 ? 0 : 1);
                }
            } else if (delta < 0) {
                // Wenn der Slider-Wert sinkt (Reinzoomen),
                // verkleinern wir xMin, yMin und "vergrößern" xMax, yMax
                if(xMin <= -1) {
                    xMin -= (xMin == 0 ? 0 : 1);
                }
                if(yMin <= -1) {
                    yMin -= (yMin == 1 ? 0 : 1);
                }
                if(xMax >= 1) {
                    xMax += (xMax == 0 ? 0 : 1);
                }
                if(yMax >= 1) {
                    yMax += (yMax == 1 ? 0 : 1);
                }
            }
            setBounds(xMin, yMin, xMax, yMax);

            new Thread(() -> {
                t.reset();
                drawPlotter();
            }).start();
        });
    }



    /*
        Methode zum berechnen der Kästchengröße
     */
    void generateTileSizes() {
        List<Integer> coordinates = getBounds();
        xMin = coordinates.get(0);
        xMax = coordinates.get(2);
        yMin = coordinates.get(1);
        yMax = coordinates.get(3);

        // Anzahl der Schritte berechnen
        int xSteps = xMax - xMin;
        int ySteps = yMax - yMin;

        // Schrittgrößen berechnen
        tileSizeX = plotWidth / xSteps;
        tileSizeY = plotHeight / ySteps;
    }








    /*
        Beschriftet die Achsen
    */
    void labelAxis() {
        t.color(0); // Textfarbe Schwarz
        t.left(90); // Textausrichtung nach links rotieren

        // X-Achse
        int xMin = getBounds().get(0);
        int xMax = getBounds().get(2);

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
        int yMin = getBounds().get(1);
        int yMax = getBounds().get(3);

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

//-------------------------------------Klassen---------------------------------------//

/*
    Erstellt ein Texteingabefeld
 */
class TextInput implements Clerk {
    final String ID;
    LiveView view;

    TextInput(LiveView view, String label, String color, String placeholder) {
        this.view = view;
        ID = Clerk.getHashID(this);

        Clerk.write(view,
                "<div>" +
                        "<label for='text" + ID + "' style='color: "+ color + ";'>" + label + "</label>" +
                        "<input id='text" + ID +
                        "' type='text' placeholder='" + placeholder + "' size='20'>" +
                        "</div>");

        Clerk.script(view,
                "const text" + ID + " = document.getElementById('text" + ID + "');");
    }

    TextInput attachTo(Consumer<String> delegate) {
        this.view.createResponseContext("/text" + ID, delegate, ID);

        Clerk.script(view, Text.fillOut(
                """
                text${0}.addEventListener('input', (event) => {
                    const value = event.target.value;
                    console.log("text${0}: value = " + value);
                    fetch('text${0}', {
                        method: 'post',
                        body: value
                    }).catch(console.error);
                });
                """, Map.of("0", ID)));

        return this;
    }
}


/*
    Erstellt eine Checkbox
 */
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
    Erstellt ein Button
 */
class Button implements Clerk {
    final String ID;
    LiveView view;
    View v = new View();

    Button(LiveView view, String label) {
        this.view = view;
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

/*
    Erstellt ein Inputfeld für Ganzzahlen.
 */
class IntegerInput implements Clerk {
    final String ID;
    LiveView view;

    IntegerInput(LiveView view, String placeholder, String label, int defaultValue) {
        this.view = view;
        ID = Clerk.getHashID(this);

        Clerk.write(view,
                "<div>" +
                        "<label for='slider" + ID + "'>" + label + "</label>" +
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
    Erstellt einen Slider mit Abstufungen
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

/*
    Die Enum Color definiert vordefinierte Farben (BLUE, RED, GREEN) mit ihren RGB-Werten.
 */
enum Color {
    BLUE(0,0,255),
    RED(255,0,0),
    GREEN(0,255,0);

    private final int red;
    private final int green;
    private final int blue;

    Color(int red, int green, int blue) {
        this.red = red;
        this.blue = blue;
        this.green = green;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}

/*
    Die Klasse Function speichert eine mathematische Funktion und eine Farbe.
 */
class Function {
    public DoubleUnaryOperator function;
    public Color color;

    public Function(DoubleUnaryOperator function, Color color) {
        this.function = function;
        this.color = color;
    }

    public void setFunction(DoubleUnaryOperator function) {
        this.function = function;
    }
    public DoubleUnaryOperator getFunction() {
        return function;
    }
    public Color getColor() {
        return color;
    }

}

