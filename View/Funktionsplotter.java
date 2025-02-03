package View;

import skills.Text.*;
import views.Turtle.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Funktionsplotter {
    // Konstanten und allgemeine Größen
    private final int height = 600;
    private final int width = 600;
    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int margin = 50;
    int plotWidth = width - 2 * margin;
    int plotHeight = width - 2 * margin;
    double step = 0.05;

    // Zoom
    Zoom zoom = new Zoom(-10,-10,10,10);

    // Funktionen
    Function function1, function2, function3;
    List<Function> parameterFunctions1 = new ArrayList<>();
    List<Function> parameterFunctions2 = new ArrayList<>();
    List<Function> parameterFunctions3 = new ArrayList<>();
    Map<String, String> functionMap = new HashMap<>();


    // Parameter und Koordinaten
    List<Integer> bounds = new ArrayList<>();
    List<Integer> initialBounds;
    List<Integer> aInterval = new ArrayList<>();

    // Plotter-Status
    boolean useParameter = false;
    int from = 1;
    int to = 5;

    // Tile-Größe
    int tileSizeX, tileSizeY;

    // Turtle
    private Turtle t;

    // Mathematische Bibliothek
    MathBib mathbib = new MathBib();



    //-------------------------------Initialize--------------------------//

    /*
        Initialisiert die Grenzen
     */
    void initializeBounds() {
        bounds.clear();
        //xMin
        bounds.add(-10);
        //yMin
        bounds.add(-10);

        //xMax
        bounds.add(10);
        //yMax
        bounds.add(10);
    }

    /*
        Initialisiert die Parameter
     */
    void initializeParameters() {
        aInterval.clear();
        for (int i = 1; i <= 5; i++) {
            aInterval.add(i);
        }
    }


    //----------------------------draw-methods--------------------------//
    /*
    Initialisiert die LiveView, setzt die Grenzen und Parameter und zeichnet dann den Plotter und Benutzeroberfläche.
    */
    public void draw() {
        Clerk.view();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Clerk.markdown(
                Text.fillOut(
                        """
                # **Funktionsplotter**
                """, Text.cutOut("./View/Funktionsplotter.java", "// Funktionsplotter")));

        t = new Turtle(width, height);

        initializeBounds();
        initializeParameters();
        initialBounds = bounds.stream().mapToInt(Integer::intValue).boxed().collect(Collectors.toList());

        drawPlotter(t);
        drawUI();
    }

    //Anzeigebereich anpassen
    void szenario1(Turtle t) {
        setBounds(0,0,5,5);
        drawPlotter(t);
    }

    //Zoom-Function
    void szenario2(Turtle t) {
        function1 = parseAndCreateFunction(function1,"x^3", Color.BLUE);
        drawPlotter(t);
        zoom(t, 2, zoom);
    }

    //Mehrere Funktionen gleichzeitig darstellen
    void szenario3(Turtle t) {
        function1 = parseAndCreateFunction(function1,"x^2", Color.BLUE);
        function2 = parseAndCreateFunction(function2,"2x-5", Color.RED);
        function3 = parseAndCreateFunction(function3,"sin(x)", Color.GREEN);

        drawFunction(t, function1);
        drawFunction(t, function2);
        drawFunction(t, function3);
    }

    // Parameterabhängige Funktionen darstellen
    void szenario4(Turtle t) {
        useParameter = true;

        function1 = parseAndCreateFunction(function1,"x+a", Color.BLUE);
        drawParameterFunctions(t, function1);
    }

    // Funktionen Speichern und Laden
    void szenario5(Turtle t) {
        function1 = parseAndCreateFunction(function1,"x^3", Color.BLUE);
        function2 = parseAndCreateFunction(function2,"2x-5", Color.RED);
        setBounds(0,-3,7,7);

        drawPlotter(t);
    }

    /*
        Zeichnet das gesamte Koordinatensystem mit der generierten tileSize
     */
    void drawPlotter(Turtle t) {
        t.reset();
        generateTileSizes();
        drawGrid(t, plotWidth, plotHeight);
        drawPlotterArea(t);
        labelAxis(t);

        for (Function func : new Function[]{function1, function2, function3}) {
            if (func != null) {
                if (useParameter) {
                    drawParameterFunctions(t, func);
                } else {
                    drawFunction(t, func);
                }
            }
        }

    }

    /*
        Fügt Button, Slider und Textfelder der UI hinzu
     */
    void drawUI() {
        setupFunctions();
        Clerk.markdown(
                Text.fillOut(
                        """
                ---
                """, Text.cutOut("./View/Funktionsplotter.java", "// Funktionsplotter")));
        setupBoundsInput();
        Clerk.markdown(
                Text.fillOut(
                        """
                ---
                """, Text.cutOut("./View/Funktionsplotter.java", "// Funktionsplotter")));
        setupParameterInput();
        Clerk.markdown(
                Text.fillOut(
                        """
                ---
                """, Text.cutOut("./View/Funktionsplotter.java", "// Funktionsplotter")));
        setupZoomSlider();
        Clerk.markdown(
                Text.fillOut(
                        """
                ---
                """, Text.cutOut("./View/Funktionsplotter.java", "// Funktionsplotter")));
        setupSaveAndLoadButton();
    }

    /*
        Zeichnet den Rahmen und die Achsen
     */
    public void drawPlotterArea(Turtle t) {
        t.color(0);

        // Dynamisch den Ursprung berechnen
        double xRange = bounds.get(2) - bounds.get(0);
        double yRange = bounds.get(3) - bounds.get(1);

        // Ursprungskoordinaten basierend auf den Grenzen
        halfWidth = margin + (int) ((-bounds.get(0) / xRange) * plotWidth);
        halfHeight = margin + plotHeight - (int) ((-bounds.get(1) / yRange) * plotHeight);

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
    void drawGrid(Turtle t, int plotWidth, int plotHeight) {
        t.color(211, 211, 211);

        double xRange = bounds.get(2) - bounds.get(0);
        double yRange = bounds.get(3) - bounds.get(1);

        double pixelPerX = plotWidth / xRange;
        double pixelPerY = plotHeight / yRange;

        int right = margin + plotWidth;
        int bottom = margin + plotHeight;

        for (int i = 0; i <= xRange; i++) {
            int x = (int) (margin + i * pixelPerX);
            t.moveTo(x, margin);
            t.lineTo(x, bottom);
        }

        for (int i = 0; i <= yRange; i++) {
            int y = (int) (margin + i * pixelPerY);
            t.moveTo(margin, y);
            t.lineTo(right, y);
        }
    }

    /*
        Zeichnet die Funktion in das Koordinatensystem
     */
    void drawFunction(Turtle t, Function function) {
        if (function == null || function.function == null) return;

        t.color(function.color.getRed(), function.color.getGreen(), function.color.getBlue());

        double scaleX = (double) plotWidth / (bounds.get(2) - bounds.get(0));
        double scaleY = (double) plotHeight / (bounds.get(3) - bounds.get(1));

        for (double x = bounds.get(0); x <= bounds.get(2); x += step) {
            double y = function.function.applyAsDouble(x);
            if (y < bounds.get(1) || y > bounds.get(3)) continue;

            double screenX = (halfWidth + x * scaleX);
            double screenY = (halfHeight - y * scaleY);

            double[] adjustedCoordinates = adjustCoordinates(y, scaleY, bounds.get(1), bounds.get(3));
            screenY = adjustedCoordinates[0];
            y = adjustedCoordinates[1];

            if (y < bounds.get(1) || y > bounds.get(3)) continue;


            double nextX = x + step;
            double nextY = function.function.applyAsDouble(nextX);

            double minStep = step/16;
            while ((nextY > bounds.get(3) || nextY < bounds.get(1)) && step > minStep) {
                System.out.println("Step: "+step);
                System.out.println("NextY: "+nextY);
                step /= 2;
                nextX = x + step;
                nextY = function.function.applyAsDouble(nextX);
            }

            double nextScreenX = (halfWidth + nextX * scaleX);
            double nextScreenY = (halfHeight - nextY * scaleY);

            adjustedCoordinates = adjustCoordinates(nextY, scaleY, bounds.get(1), bounds.get(3));
            nextScreenY = adjustedCoordinates[0];
            nextY = adjustedCoordinates[1];

            if (!(Double.isNaN(y) || Double.isInfinite(y)) && !(Double.isNaN(nextY) || Double.isInfinite(nextY))) {
                t.moveTo(screenX, screenY);
                t.lineTo(nextScreenX, nextScreenY);
            }
        }
    }

    /*
        Berechnet die Bildschirmkoordinaten für den y-Wert und begrenzt ihn innerhalb der angegebenen Grenzen.
     */
    private double[] adjustCoordinates(double y, double scaleY, double lowerBound, double upperBound) {
        double screenY = halfHeight - y * scaleY;
        if (y < lowerBound) {
            y = lowerBound;
            screenY = (halfHeight - y * scaleY);
        } else if (y > upperBound) {
            y = upperBound;
            screenY = (halfHeight - y * scaleY);
        }
        return new double[] { screenY, y };
    }


    /*
    Zeichnet alle Parameterfunktionen.
    */
    void drawParameterFunctions(Turtle t, Function function) {

        if(function1 == function) {
            for (Function functionI : parameterFunctions1) {
                drawFunction(t, functionI);
            }
        }
        if(function2 == function) {
            for (Function functionI : parameterFunctions2) {
                drawFunction(t, functionI);
            }
        }
        if(function3 == function) {
            for (Function functionI : parameterFunctions3) {
                drawFunction(t, functionI);
            }
        }

    }

    /*
        Beschriftet die Achsen
    */
    void labelAxis(Turtle t) {
        t.color(0);
        t.left(90);

        int xMin = bounds.get(0);
        int xMax = bounds.get(2);

        for (int i = xMin; i <= xMax; i++) {
            int xPos = halfWidth + i * tileSizeX;
            t.moveTo(xPos, margin + plotHeight + 15);
            t.text(String.valueOf(i));
        }

        int yMin = bounds.get(1);
        int yMax = bounds.get(3);

        for (int i = yMin; i <= yMax; i++) {
            int yPos = halfHeight - i * tileSizeY;
            t.moveTo(margin - 15, yPos);
            t.text(String.valueOf(i));
        }

        t.moveTo(margin - 15, halfHeight);
        t.text("0");
        t.moveTo(halfWidth, margin + plotHeight + 15);
        t.text("0");
    }

    /*
        Setzt den Plotter zurück, initialisiert die Grenzen und Parameter neu und setzt die Funktionen zurück.
        Danach wird der Plotter neu gezeichnet.
     */
    void resetPlotter(Turtle t) {
        t.reset();
        useParameter = false;
        initializeBounds();
        initializeParameters();
        initialBounds = bounds.stream().mapToInt(Integer::intValue).boxed().collect(Collectors.toList());
        function1 = null;
        function2 = null;
        function3 = null;
        drawPlotter(t);
    }


    //-----------------------------setup-methods------------------------------//

    /*
      Methode zum berechnen der Kästchengröße
   */
    void generateTileSizes() {
        List<Integer> coordinates = bounds.stream().mapToInt(Integer::intValue).boxed().collect(Collectors.toList());

        // Anzahl der Schritte berechnen
        int xSteps = coordinates.get(2) - coordinates.get(0);
        int ySteps = coordinates.get(3) - coordinates.get(1);

        // Schrittgrößen berechnen
        tileSizeX = plotWidth / xSteps;
        tileSizeY = plotHeight / ySteps;
    }

    /*
        Initialisiert das Eingabefeld und den Button für die Funktionseingabe,
        um eine mathematische Funktion zu definieren und anzuzeigen.
     */
    private void setupFunctions() {
        createFunctionInput("f", Color.BLUE, delegate -> function1 = parseAndCreateFunction(function1, delegate, Color.BLUE));
        createFunctionInput("g", Color.RED, delegate -> function2 = parseAndCreateFunction(function2, delegate, Color.RED));
        createFunctionInput("h", Color.GREEN, delegate -> function3 = parseAndCreateFunction(function3, delegate, Color.GREEN));

        Button functionButton = new Button(Clerk.view(), "Generate");
        functionButton.attachTo(() -> {
            drawIfNotNull(function1);
            drawIfNotNull(function2);
            drawIfNotNull(function3);
            drawPlotter(t);
        });
    }

    /*
        Erstellt die Funktions-Input-Felder
     */
    void createFunctionInput(String label, Color color, Consumer<String> action) {
        TextInput functionInput = new TextInput(Clerk.view(), label + "(x) = ", color.toString().toLowerCase(), label + "(x)" );
        functionInput.attachTo(delegate -> {
            if(delegate.isEmpty()) {
                action.accept(null);
            }
            action.accept(delegate);
        });
    }

    /*
        Parst die Eingabe, um eine Funktion zu erstellen, und gibt sie zusammen mit der Farbe zurück.
        Unterstützt sowohl Funktionen mit Parametern als auch einfache Funktionen ohne Parameter.
     */
    Function parseAndCreateFunction(Function function, String input, Color color) {
        String key = function == function1 ? "f" : function == function2 ? "g" : function == function3 ? "h" : null;
        if (key != null) functionMap.put(key, input);

        if (input == null) return null;
        DoubleUnaryOperator newFunction = null;

        List<Function> parameterFunctions = null;
        if (function == function1) parameterFunctions = parameterFunctions1;
        else if (function == function2) parameterFunctions = parameterFunctions2;
        else if (function == function3) parameterFunctions = parameterFunctions3;

        if (parameterFunctions != null) {
            parameterFunctions.clear();
            for (Integer parameter : aInterval) {
                newFunction = mathbib.parseParameter(input, parameter);
                if (newFunction != null) {
                    parameterFunctions.add(new Function(newFunction, color));
                }
            }
        }

        newFunction = mathbib.parseFunction(input);

        return newFunction != null ? new Function(newFunction, color) : null;
    }


    /*
        Zeichnet die Funktion, wenn sie nicht null ist und eine gültige Funktionsreferenz enthält.
     */
    private void drawIfNotNull(Function function) {
        if (function != null && function.getFunction() != null) {
            drawFunction(t, function);
        }
    }


    /*
        Initialisiert die Eingabe- und Steuerungselemente,
        um die Begrenzungen (xMin, xMax, yMin, yMax) des Koordinatensystems
        festzulegen und diese auf das Koordinatensystem anzuwenden.
     */
    private void setupBoundsInput() {
        Clerk.write(Clerk.view(), "Grenzen setzen");

        createBoundsInput("xMin", bounds.get(0), value -> bounds.set(0, Integer.parseInt(value)));
        createBoundsInput("xMax", bounds.get(2), value -> bounds.set(2, Integer.parseInt(value)));
        createBoundsInput("yMin", bounds.get(1), value -> bounds.set(1, Integer.parseInt(value)));
        createBoundsInput("yMax", bounds.get(3), value -> bounds.set(3, Integer.parseInt(value)));

        Button setBoundsButton = new Button(Clerk.view(), "Set bounds");
        setBoundsButton.attachTo(() -> {
            setBounds(bounds.get(0), bounds.get(1), bounds.get(2), bounds.get(3));
            initialBounds = bounds.stream().mapToInt(Integer::intValue).boxed().collect(Collectors.toList());
            t.reset();
            drawPlotter(t);
        });
    }

    /*
        Erstellt ein Eingabefeld für eine Ganzzahl mit einem Standardwert und einer Änderungsaktion.
     */
    private void createBoundsInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, label,  defaultValue);
        input.attachTo(onChange);
    }

    /*
    Setzt die Grenzen (Bounds) mit den gegebenen Werten
    */
    public void setBounds(int xMin, int yMin, int xMax, int yMax) {
        if(xMin >= xMax) {
            System.out.println("xMin muss kleiner als xMax sein. (xMin = " + xMin + ", xMax = " + xMax + ")");
        }
        if(yMin >= yMax) {
            System.out.println("yMin muss kleiner als yMax sein. (yMin = " + yMin + ", yMax = " + yMax + ")");
        }
        else {
            bounds.set(0, xMin);
            bounds.set(1, yMin);
            bounds.set(2, xMax);
            bounds.set(3, yMax);
        }
    }

    /*
    Erstellt eine Checkbox und Eingabefelder für die Definition eines Parameter-Intervalls.
    Wenn die Checkbox aktiviert wird, wird der Parameterbereich für die Funktion f(x;a) gesetzt.
    */
    private void setupParameterInput() {
        Checkbox parameterCheckbox = new Checkbox(Clerk.view(), "Solve function as f(x;a) with");
        createParameterInput("from", 1, value -> from = Integer.parseInt(value));
        createParameterInput("to", 5, value -> to = Integer.parseInt(value));
        Button parameterButton = new Button(Clerk.view(), "Set parameters");

        parameterButton.attachTo(() -> {
            if( from < to) {
                updateInterval(from, to);
            } else {
                System.err.println("Invalid parameter range: 'from' must be less than 'to'");
            }
        });
        parameterCheckbox.attachTo(value -> {
            useParameter = !useParameter;
        });
    }

    /*
        Erzeugt ein Eingabefeld für eine Funktion mit Parameter
     */
    void createParameterInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, label,  defaultValue);
        input.attachTo(onChange);
    }

    /*
        Aktualisiert die Liste der Parameter basierend auf den Grenzen 'from' und 'to'.
     */
    void updateInterval(int from, int to) {
        aInterval.clear();
        for (int i = from; i <= to; i++) {
            aInterval.add(i);
        }
    }

    /*
       Erstellt einen Slider zur Einstellung des Zooms und ruft die zoom() Methode auf.
    */
    private void setupZoomSlider() {
        SliderStufen zoomSlider = new SliderStufen(Clerk.view(), 0, 5, "Zoom",0, zoom.zoomValue);
        new Thread(() -> {
            zoomSlider.attachTo( response -> {
                zoom(t, Integer.parseInt(response), zoom);
            });
        }).start();
    }

    /*
        Initialisiert die Buttons "Save" und "Load" mit ihren jeweiligen Funktionen.
        Der "Save"-Button speichert die Daten, während der "Load"-Button die Daten lädt und die Darstellung aktualisiert.
     */
    private void setupSaveAndLoadButton(){
        Button saveButton = new Button(Clerk.view(),"Save");
        saveButton.attachTo(() -> {
            save();
            Clerk.write(Clerk.view(), "Saved plot to savedPlot.csv");
        });

        Button loadButton = new Button(Clerk.view(),"Load");
        loadButton.attachTo( () -> {
            load(t);
            Clerk.write(Clerk.view(), "Loaded plot from savedPlot.csv");

        });
    }

    //-----------------------------------------Funktionen---------------------------------------------//


    /*
        Speichert die Funktion und die Grenzen in die Datei "savedPlot.csv"
     */
    void save() {
        try (FileOutputStream fos = new FileOutputStream("savedPlot.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter bw = new BufferedWriter(osw)) {

            // Funktionen speichern
            saveFunction(function1, "f", bw);
            saveFunction(function2, "g", bw);
            saveFunction(function3, "h", bw);

            // Bounds speichern
            if (bounds == null || bounds.isEmpty()) {
                bw.write("Bounds list is empty.");
            } else {
                String boundsString = bounds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                bw.write(boundsString);
            }

            bw.newLine();
            System.out.println("File saved successfully.");

        } catch (IOException e) {
            System.err.println("An error occurred while saving the file: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Invalid state: " + e.getMessage());
        }
    }

    /*
        Speichert die Funktion in eine Datei.
     */
    private void saveFunction(Function function, String functionKey, BufferedWriter bw) throws IOException {
        if (function == null || function.getFunction() == null) {
            bw.write("null");
        } else {
            bw.write(functionMap.get(functionKey));
        }
        bw.newLine();
    }



    /*
        Lädt die Funktion und Grenzen aus der Datei "savedPlot.csv"
     */
    void load(Turtle t) {
        try (FileInputStream fis = new FileInputStream("savedPlot.csv");
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            function1 = loadFunction(br, "function1", Color.BLUE);
            function2 = loadFunction(br, "function2", Color.RED);
            function3 = loadFunction(br, "function3", Color.GREEN);

            String boundsInput = br.readLine();

            if (boundsInput == null || boundsInput.isEmpty()) {
                System.err.println("No bounds found in file.");
                bounds.clear();
            } else {
                String[] boundsArray = boundsInput.split(",");
                bounds.clear();
                for (String bound : boundsArray) {
                    try {
                        bounds.add(Integer.parseInt(bound.trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number in bounds: " + bound);
                    }
                }
                setBounds(bounds.get(0), bounds.get(1), bounds.get(2), bounds.get(3));
            }


            initialBounds = new ArrayList<>(bounds);

            drawPlotter(t);
            drawFunction(t, function1);
            drawFunction(t, function2);
            drawFunction(t, function3);

        } catch (IOException e) {
            System.err.println("An error occurred while loading the file: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Invalid state: " + e.getMessage());
        }
    }

    /*
        Lädt eine Funktion aus einer Datei.
     */
    private Function loadFunction(BufferedReader br, String functionName, Color color) throws IOException {
        String functionInput = br.readLine();

        if (functionInput == null || functionInput.isEmpty()) {
            return null;
        } else if ("null".equals(functionInput)) {
            return null;
        } else {
            return parseAndCreateFunction(null, functionInput, color);
        }
    }


    /*
        Der Slider passt die Grenzen der Achsen an und aktualisiert die Darstellung des Diagramms.
     */
    void zoom(Turtle t, int response, Zoom zoom) {
        zoom.xMin = initialBounds.get(0);
        zoom.yMin = initialBounds.get(1);
        zoom.xMax = initialBounds.get(2);
        zoom.yMax = initialBounds.get(3);

        if(response < 0 || response > 5) {
            System.err.println("Invalid zoom value: " + response + ". Should be between 0 and 5.");
        } else {
            setBounds(zoom.xMin + response, zoom.yMin + response, zoom.xMax - response, zoom.yMax -response);
            zoom.xMin = bounds.get(0);
            zoom.yMin = bounds.get(1);
            zoom.xMax = bounds.get(2);
            zoom.yMax = bounds.get(3);
            drawPlotter(t);
        }
    }
}


//-------------------------------------Klassen---------------------------------------//


class Zoom {
    int xMin, yMin, xMax, yMax;
    int zoomValue;

    Zoom(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zoomValue = 0;
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

/*
    Die Klasse MathBib enthält Methoden zum Parsen und Berechnen von mathematischen Funktionen.
 */
class MathBib {

    /*
        Parst eine mathematische Funktion aus einem String und gibt die entsprechende
        DoubleUnaryOperator-Implementierung zurück.
     */
    public DoubleUnaryOperator parseFunction(String functionInput) {
        if (functionInput == null || functionInput.isBlank()) {
            throw new IllegalArgumentException("Eingabe darf nicht leer sein");
        }
        functionInput = functionInput.replaceAll("\\s+", "");

        if (functionInput.matches("^[+-]?(\\d+)?x([+-]\\d+)?$")) {
            return parseLinear(functionInput);
        }

        if (functionInput.matches("^[+-]?(\\d+)?[xX]\\^[-+]?\\d+$")) {
            return parsePowerFunction(functionInput);
        }

        if (functionInput.matches("^[+-]?(sin|cos|tan|sqrt)\\(x\\)$")) {
            return parseTrigonometricFunction(functionInput);
        }

        throw new IllegalArgumentException("Unbekanntes Format: " + functionInput);
    }
    /*
        Parst eine Lineare Funktion (x+2) mit optionalem Vorzeichen (+ oder -).
     */
    private DoubleUnaryOperator parseLinear(String functionInput) {
        Pattern pattern = Pattern.compile("([+-]?(\\d+)?)x([+-]\\d+)?");
        Matcher matcher = pattern.matcher(functionInput);

        if (matcher.matches()) {
            final int coefficient = matcher.group(2) == null ? 1 : Integer.parseInt(matcher.group(2));
            final int adjustedCoefficient = matcher.group(1) != null && matcher.group(1).startsWith("-") ? -coefficient : coefficient;
            final int constant = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));

            return (x) -> adjustedCoefficient * x + constant;
        }

        throw new IllegalArgumentException("Ungültiges lineares Format: " + functionInput);
    }

    /*
        Parst eine Potenzfunktion (x^2) mit optionalem Vorzeichen (+ oder -).
     */
    DoubleUnaryOperator parsePowerFunction(String functionInput) {
        String[] parts = functionInput.split("[xX]\\^");

        final int coefficient = parts[0].isEmpty() || parts[0].equals("+") ? 1 :
                parts[0].equals("-") ? -1 : Integer.parseInt(parts[0]);
        final int power = Integer.parseInt(parts[1]);

        return (x) -> {
            double result = 1;
            for (int i = 0; i < power; i++) {
                result *= x;
            }
            return coefficient * result;
        };
    }


    /*
        Parst eine trigonometrische Funktion (sin(x), cos(x), tan(x)) mit optionalem Vorzeichen (+ oder -).
     */
    DoubleUnaryOperator parseTrigonometricFunction(String functionInput) {
        final boolean isNegative = functionInput.startsWith("-");
        final String function = functionInput.replaceFirst("^[+-]", "");

        switch (function) {
            case "sin(x)":
                return isNegative ? (x) -> -sin(x) : this::sin;
            case "cos(x)":
                return isNegative ? (x) -> -cos(x) : this::cos;
            case "tan(x)":
                return isNegative ? (x) -> -tan(x) : this::tan;
            default:
                throw new IllegalArgumentException("Unbekannte trigonometrische Funktion: " + functionInput);
        }
    }

    /*
        Berechnet sin(x)
     */
    double sin(double x) {
        double result = 0;
        double term = x;
        int sign = 1;

        for (int i = 1; i <= 50; i++) {
            result += sign * term;
            sign = -sign;
            term *= x * x / (2 * i * (2 * i + 1));
        }

        return result;
    }

    /*
        Berechnet cos(x)
     */
    double cos(double x) {
        double result = 1;
        double term = 1;
        int sign = -1;

        for (int i = 0; i < 50; i+=2) {
            term *= x * x/((i+1)*(i+2));
            result += sign * term;
            sign = -sign;
        }
        return result;
    }

    /*
        Berechnet tan(x)
     */
    double tan(double x) {
        double sinValue = sin(x);
        double cosValue = cos(x);

        return sinValue / cosValue;
    }

    /*
        Funktionen mit Parameter, z.B. x + a, x - a, x^2 + a
     */
    DoubleUnaryOperator parseParameter(String functionInput, int a) {
        functionInput = functionInput.replaceAll("\\s", "");
        String cleanedInput = functionInput.toLowerCase();

        if (functionInput.matches("^[+-]?[xX][+-]?a$")) {
            char operator = cleanedInput.charAt(1);
            return (x) -> operator == '+' ? x + a : x - a;
        }

        if (functionInput.matches("^[+-]?[xX]\\^\\d+[+-]?a$")) {
            int powerStartIndex = cleanedInput.indexOf("^") + 1;
            int powerEndIndex = cleanedInput.indexOf("+") > -1 ? cleanedInput.indexOf("+") : cleanedInput.indexOf("-");

            if (powerEndIndex == -1) {
                powerEndIndex = cleanedInput.length();
            }

            int power = Integer.parseInt(cleanedInput.substring(powerStartIndex, powerEndIndex));
            char operator = cleanedInput.charAt(powerEndIndex);

            return (x) -> {
                double result = 1;
                for (int i = 0; i < power; i++) {
                    result *= x;
                }
                return operator == '+' ? result + a : result - a;
            };
        }

        if (functionInput.matches("^[+-]?(sin|cos|tan|sqrt)\\(x\\)[+-]?a$")) {
            String functionName = cleanedInput.replaceAll("\\(x\\)[+-]?a$", "");
            boolean isNegative = cleanedInput.endsWith("-a");
            return getTrigFunction(functionName, isNegative, a);
        }

        return null;
    }

    /*
        Gibt eine Funktion zurück, die den trigonometrischen Wert (sin, cos, tan) von x berechnet
        und den Wert a entweder addiert oder subtrahiert, je nachdem, ob isNegative wahr oder falsch ist.
     */
    private DoubleUnaryOperator getTrigFunction(String functionName, boolean isNegative, int a) {
        return (x) -> {
            double result;
            switch (functionName) {
                case "sin": result = sin(x); break;
                case "cos": result = cos(x); break;
                case "tan": result = tan(x); break;
                default: return 0;
            }
            return isNegative ? result - a : result + a;
        };
    }
}
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
    Funktionsplotter v = new Funktionsplotter();

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

        // JavaScript: Event-Listener für den Slider mit Debounce (1 Sekunde Verzögerung)
        Clerk.script(view, Text.fillOut(
                """
                let slider${0}Timeout;
                slider${0}.addEventListener('input', (event) => {
                    const value = event.target.value;
                    console.log("slider${0}: value = " + value);
                    valueDisplay${0}.textContent = value; // Aktualisiere Wertanzeige
                    
                    // Falls bereits ein Timer läuft, diesen zurücksetzen
                    clearTimeout(slider${0}Timeout);
                    
                    // Neuen Timer starten (1 Sekunde Verzögerung)
                    slider${0}Timeout = setTimeout(() => {
                        fetch('slider${0}', {
                            method: 'post',
                            body: value.toString()
                        }).catch(console.error);
                    }, 200);
                });
                """, Map.of("0", ID)));

        return this;
    }

}

//---------------------------------------Dokumentation-----------------------------------------//

class Documentation {
    Documentation() {

    }
}

