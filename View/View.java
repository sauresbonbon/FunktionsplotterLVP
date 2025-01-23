package View;

import skills.Text.*;
import views.Turtle.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class View {
    private final int height = 600;
    private final int width = 600;
    int margin = 50;
    private DoubleUnaryOperator f;
    Function function1, function2, function3;
    MathBib mathbib = new MathBib();

    int halfWidth = width / 2;
    int halfHeight = height / 2;
    int plotWidth = width - 2 * margin;
    int plotHeight = plotWidth;
    int tileSizeX, tileSizeY;
    boolean useParameter = false;
    int zoomValue;

    Map<String, String> functionMap = Map.of("f(x)", "", "g(x)", "", "h(x)", "");
    List<Integer> bounds = new ArrayList<>();
    List<Integer> ogCoordinates;
    List<Integer> parameters = new ArrayList<>();
    List<Function> parameterFunctions1 = new ArrayList<>();

    int xMin, xMax, yMin, yMax;
    int from, to;

    private Turtle t1,t2;

    //-------------------------------Initialize--------------------------//

    void initializeBounds() {
        //xMin
        bounds.add(-10);
        //yMin
        bounds.add(-10);

        //xMax
        bounds.add(10);
        //yMax
        bounds.add(10);
    }
    void initializeParameters() {
        for (int i = 1; i <= 5; i++) {
            parameters.add(i);
        }
    }
    //----------------------------draw-methods--------------------------//

    public void draw() {
        Clerk.view();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        initializeBounds();
        initializeParameters();
        ogCoordinates = getBounds();
        t1 = new Turtle(width, height);
//        t2 = new Turtle(width, height);

        drawPlotter();
        drawUI();
    }

    /*
        Zeichnet das gesamte Koordinatensystem mit der generierten tileSize
     */
    void drawPlotter() {
        generateTileSizes();
        drawGrid(plotWidth, plotHeight);
        drawPlotterArea();
        labelAxis();

        if (function1 != null) {
            if(useParameter) {
                drawParameterFunctions();
            } else {
                drawFunction(function1);
            }
        }
        if (function2 != null) drawFunction(function2);
        if (function3 != null) drawFunction(function3);

    }

    /*
        Fügt Button, Slider und Textfelder der UI hinzu
     */
    void drawUI() {
        setupFunctions();
        setupBoundsInput();
        setupParameterInput();
        setupZoomSlider();
        setupSaveAndLoadButton();
    }

    void drawParameterFunctions() {
        for (Function function : parameterFunctions1) {
            try {
                drawFunction(function);
            } catch (Exception e) {
                System.err.println("An error occurred in drawFunction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



    /*
        Zeichnet den Rahmen und die Achsen
     */
    public void drawPlotterArea() {
        t1.color(0);
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
        t1.lineWidth(2);
        t1.moveTo(halfWidth, margin); // y-Achse
        t1.lineTo(halfWidth, margin + plotHeight);
        t1.moveTo(margin, halfHeight); // x-Achse
        t1.lineTo(margin + plotWidth, halfHeight);
        t1.lineWidth(1);

        // Rahmen zeichnen
        t1.moveTo(margin, margin); // oben
        t1.lineTo(margin + plotWidth, margin); // rechts
        t1.lineTo(margin + plotWidth, margin + plotHeight); // unten
        t1.lineTo(margin, margin + plotHeight); // links
        t1.lineTo(margin, margin); // zurück zum Startpunkt
    }


    /*
        Zeichnet die Kästchen des Koordinatensystems
    */
    void drawGrid(int plotWidth, int plotHeight) {
        t1.color(211, 211, 211);

        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        double pixelPerX = plotWidth / xRange;
        double pixelPerY = plotHeight / yRange;

        int right = margin + plotWidth;
        int bottom = margin + plotHeight;

        // Vertikale Linien zeichnen (X-Achse)
        for (int i = 0; i <= xRange; i++) {
            int x = (int) (margin + i * pixelPerX);
            t1.moveTo(x, margin);
            t1.lineTo(x, bottom);
        }

        // Horizontale Linien zeichnen (Y-Achse)
        for (int i = 0; i <= yRange; i++) {
            int y = (int) (margin + i * pixelPerY);
            t1.moveTo(margin, y);
            t1.lineTo(right, y);
        }
    }


    /*
        Zeichnet die Funktion in das Koordinatensystem
     */
    void drawFunction(Function function) {
        try {
            if (function == null || function.function == null) {
                System.err.println("Skipping drawing due to uninitialized function.");
                return;
            }

            t1.color(function.color.getRed(), function.color.getGreen(), function.color.getBlue());
            double step = 0.01;
            double scaleX = (double) plotWidth / (getBounds().get(2) - getBounds().get(0));
            double scaleY = (double) plotHeight / (getBounds().get(3) - getBounds().get(1));

            for (double x = xMin; x <= xMax; x += step) {
                if(x > xMax) break;
                double y = function.function.applyAsDouble(x);
                if (Double.isNaN(y) || Double.isInfinite(y) || y < yMin || y > yMax) continue;

                double screenX = (halfWidth + x * scaleX);
                double screenY = (halfHeight - y * scaleY);

                double nextY = function.function.applyAsDouble(x + step);
                if (Double.isNaN(nextY) || Double.isInfinite(nextY) || nextY < yMin || nextY > yMax) continue;

                double nextScreenX = (halfWidth + (x + step) * scaleX);
                double nextScreenY = (halfHeight - nextY * scaleY);

                t1.moveTo(screenX, screenY);
                t1.lineTo(nextScreenX, nextScreenY);
            }
        } catch (Exception e) {
            System.err.println("Error drawing function: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /*
        Beschriftet die Achsen
    */
    void labelAxis() {
        t1.color(0);
        t1.left(90);

        // X-Achse
        int xMin = getBounds().get(0);
        int xMax = getBounds().get(2);

        // X-Beschriftung
        for (int i = xMin; i <= xMax; i++) {
            int xPos = halfWidth + i * tileSizeX;
            t1.moveTo(xPos, margin + plotHeight + 15);
            t1.text(String.valueOf(i));
        }

        // Y-Achse
        int yMin = getBounds().get(1);
        int yMax = getBounds().get(3);

        // Y-Beschriftung
        for (int i = yMin; i <= yMax; i++) {
            int yPos = halfHeight - i * tileSizeY;
            t1.moveTo(margin - 15, yPos);
            t1.text(String.valueOf(i));
        }

        // Ursprung (0,0)
        t1.moveTo(margin - 15, halfHeight);
        t1.text("0");
        t1.moveTo(halfWidth, margin + plotHeight + 15);
        t1.text("0");
    }


    //-----------------------------setup-methods------------------------------//

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
        Initialisiert das Eingabefeld und den Button für die Funktionseingabe,
        um eine mathematische Funktion zu definieren und anzuzeigen.
     */
    private void setupFunctions() {

        createFunctionInput("f(x) = ", Color.BLUE, delegate -> function1 = parseAndCreateFunction(delegate, Color.BLUE));
        createFunctionInput("g(x) = ", Color.RED, delegate -> function2 = parseAndCreateFunction(delegate, Color.RED));
        createFunctionInput("h(x) = ", Color.GREEN, delegate -> function3 = parseAndCreateFunction(delegate, Color.GREEN));

        Button functionButton = new Button(Clerk.view(), "Generate");
        functionButton.attachTo(() -> {
            drawIfNotNull(function1);
            drawIfNotNull(function2);
            drawIfNotNull(function3);
            t1.reset();
            drawPlotter();
        });
    }

    /*
        Erstellt die Funktions-Input-Felder
     */
    void createFunctionInput(String label, Color color, Consumer<String> t) {
        TextInput functionInput = new TextInput(Clerk.view(), label, color.toString().toLowerCase(), label.replace(" = ", "") );
        functionInput.attachTo(delegate -> {
            if(delegate.isEmpty()) {
                t.accept(null);
            }
            t.accept(delegate);
        });

    }

    /*

     */
    Function parseAndCreateFunction(String input, Color color) {
        if(input == null)return null;
        DoubleUnaryOperator newFunction = null;

        if(useParameter) {
            parameterFunctions1.clear();
            for (Integer parameter : parameters) {
                newFunction = mathbib.parseParameter(input, parameter);
                if (newFunction != null) {
                    parameterFunctions1.add(new Function(newFunction, color));
                    System.out.println("Parsed parameter function: " + newFunction);
                } else {
                    System.err.println("Failed to parse parameter function for input: " + input + " with parameter: " + parameter);
                }
            }

        } else {
            newFunction = mathbib.parseFunction(input);
            if(newFunction != null) {
                System.err.println("Failed to parse function for input: " + input);
            }
        }
        return newFunction != null ? new Function(newFunction, color) : null;
    }

    private void drawIfNotNull(Function function) {
        if (function != null && function.getFunction() != null) {
            drawFunction(function);
        }
    }


    /*
        Initialisiert die Eingabe- und Steuerungselemente,
        um die Begrenzungen (xMin, xMax, yMin, yMax) des Koordinatensystems
        festzulegen und diese auf das Koordinatensystem anzuwenden.
     */
    private void setupBoundsInput() {
        Clerk.write(Clerk.view(), "Grenzen setzen");

        createBoundsInput("xMin", getBounds().get(0), value -> xMin = Integer.parseInt(value));
        createBoundsInput("xMax", getBounds().get(2), value -> xMax = Integer.parseInt(value));
        createBoundsInput("yMin", getBounds().get(1), value -> yMin = Integer.parseInt(value));
        createBoundsInput("yMax", getBounds().get(3), value -> yMax = Integer.parseInt(value));

        Button setBoundsButton = new Button(Clerk.view(), "Set bounds");
        setBoundsButton.attachTo(() -> {
            setBounds(xMin, yMin, xMax, yMax);
            ogCoordinates = getBounds();
            t1.reset();
            drawPlotter();
        });
    }
    private void createBoundsInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, label,  defaultValue);
        input.attachTo(onChange);
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
                updateParameterInput();
            } else {
                System.err.println("Invalid parameter range: 'from' must be less than 'to'");
            }
        });
        parameterCheckbox.attachTo(value -> {
            useParameter = !useParameter;
        });
    }

    void createParameterInput(String label, int defaultValue, Consumer<String> onChange) {
        IntegerInput input = new IntegerInput(Clerk.view(), label, label,  defaultValue);
        input.attachTo(onChange);
    }
    void updateParameterInput() {
        parameters.clear();
        for (int i = from; i <= to; i++) {
            parameters.add(i);
        }
        System.out.println("Updated parameters: " + parameters);
    }

    /*
       Erstellt einen Slider zur Einstellung des Zooms und ruft die zoom() Methode auf.
    */
    private void setupZoomSlider() {
        SliderStufen zoomSlider = new SliderStufen(Clerk.view(), 0, 5, "Zoom",0, zoomValue);
        new Thread(() -> {
            zoom(zoomSlider);
        }).start();
    }

    private void setupSaveAndLoadButton(){
        Button saveButton = new Button(Clerk.view(),"Save");
        saveButton.attachTo(() -> {
            save();
            Clerk.write(Clerk.view(), "Saved plot to savedPlot.csv");
        });

        Button loadButton = new Button(Clerk.view(),"Load");
        loadButton.attachTo( () -> {
            load();
            Clerk.write(Clerk.view(), "Loaded plot from savedPlot.csv");
            t1.reset();
            drawFunction(function1);
            drawPlotter();
        });
    }

    void save() {
        try (FileOutputStream fos = new FileOutputStream("savedPlot.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter bw = new BufferedWriter(osw)) {

            if (function1 == null || function1.getFunction() == null) {
                System.out.println("Function is null.");
            }
            bw.write(function1.getFunction().toString());
            bw.newLine();

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

    void load() {
        try (FileInputStream fis = new FileInputStream("savedPlot.csv");
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String functionInput = br.readLine();
            System.out.println(functionInput + "functionInput");
            if (functionInput == null || functionInput.isEmpty()) {
                System.err.println("No function found in file.");
                return;
            } else {
                function1 = parseAndCreateFunction(functionInput, Color.BLUE);
            }

            String boundsInput = br.readLine();
            System.out.println(boundsInput + "boundsInput");
            if (boundsInput == null || boundsInput.isEmpty()) {
                System.err.println("No bounds found in file.");
                bounds.clear();
            } else {
                String[] boundsArray = boundsInput.split(",");
                bounds.clear(); // Clear existing data
                for (String bound : boundsArray) {
                    try {
                        bounds.add(Integer.parseInt(bound.trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number in bounds: " + bound);
                    }
                }
            }

            System.out.println("File loaded successfully.");

        } catch (IOException e) {
            System.err.println("Error while reading the file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }



    //-----------------------------------------Funktionen---------------------------------------------//

    /*
        Der Slider passt die Grenzen der Achsen an und aktualisiert die Darstellung des Diagramms.
     */
    void zoom(SliderStufen s) {
        s.attachTo(response -> {
            // temp gibt an, ob der Zoomwert steigt oder sinkt, also rein oder rauszoomen
            int temp = (Integer.parseInt(response) - zoomValue);
            zoomValue = Integer.parseInt(response);
            if(temp > 0) {
                zoomOut(temp);
            } else if (temp < 0) {
                zoomIn(temp);
            }
            setBounds(xMin, yMin, xMax, yMax);

            new Thread(() -> {
                t1.reset();
                drawPlotter();
            }).start();
        });
    }

    // Wenn der Slider-Wert steigt (Rauszoomen),
    // "vergrößern" wir xMin, yMin und verkleinern xMax, yMax
    void zoomOut(int temp) {
        List<Runnable> updates = List.of(
                () -> { if (ogCoordinates.getFirst() <= -1) xMin += 1; },
                () -> { if (ogCoordinates.get(1) <= -1) yMin += 1; },
                () -> { if (ogCoordinates.get(2) >= 1) xMax -= 1; },
                () -> { if (ogCoordinates.get(3) >= 1) yMax -= 1; }
        );
        updates.forEach(Runnable::run);
    }
    // Wenn der Slider-Wert sinkt (Reinzoomen),
    // verkleinern wir xMin, yMin und "vergrößern" xMax, yMax
    void zoomIn(int temp) {
        xMin = (xMin <= -1) ? xMin - 1 : xMin;
        yMin = (yMin <= -1) ? yMin - 1 : yMin;
        xMax = (xMax >= 1) ? xMax + 1 : xMax;
        yMax = (yMax >= 1) ? yMax + 1 : yMax;
    }


    //-----------------------------------Getter-und-Setter-------------------------//

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
}


//-------------------------------------Klassen---------------------------------------//

class MathBib {

    public DoubleUnaryOperator parseFunction(String functionInput) {
        functionInput = functionInput.replaceAll("\\s+", ""); // Entfernt alle Leerzeichen

        //Potenzfunktionen 2x^2
        if (functionInput.matches("^[+-]?(\\d+)?[xX]\\^[-+]?\\d+$")) {
            String[] parts = functionInput.split("[xX]\\^");
            String coefficientPart = parts[0];
            String exponentPart = parts[1];

            int coefficient = coefficientPart.isEmpty() || coefficientPart.equals("+") ? 1 :
                    coefficientPart.equals("-") ? -1 : Integer.parseInt(coefficientPart);
            int power = Integer.parseInt(exponentPart);

            return (x) -> {
                double result = 1.0;

                if (power >= 0) {
                    for (int i = 0; i < power; i++) {
                        result *= x;
                    }
                } else {
                    for (int i = 0; i < -power; i++) {
                        result *= x;
                    }
                    result = 1.0 / result;
                }

                return coefficient * result;
            };
        }

        // Lineare Funktionen 2x + 3
        if (functionInput.matches("^[+-]?(\\d+)?x([+-]?\\d+)?$")) {
            Pattern pattern = Pattern.compile("([+-]?(\\d+)?x)([+-]?\\d+)?");
            Matcher matcher = pattern.matcher(functionInput);
            if (matcher.matches()) {
                final int coefficient = matcher.group(2) == null || matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                final int adjustedCoefficient = matcher.group(1).startsWith("-") ? -coefficient : coefficient;
                final int constant = matcher.group(3) == null || matcher.group(3).isEmpty() ? 0 : Integer.parseInt(matcher.group(3));
                return (x) -> adjustedCoefficient * x + constant;
            }
        }


        // sin(), cos(), tan() und sqrt()
        if (functionInput.matches("^[+-]?(sin|cos|tan|sqrt)\\(x\\)$")) {
            boolean isNegative = functionInput.startsWith("-");

            if (functionInput.contains("sin")) {
                return isNegative ? (x) -> -sin(x) : this::sin;
            } else if (functionInput.contains("cos")) {
                return isNegative ? (x) -> -cos(x) : this::cos;
            } else if (functionInput.contains("tan")) {
                return isNegative ? (x) -> -tan(x) : this::tan;
            }
        }

        throw new IllegalArgumentException("Unbekanntes Format: " + functionInput);
    }


    double sin(double x) {
        double result = 0;
        double term = x;
        int sign = 1;

        for (int i = 1; i <= 30; i++) {
            result += sign * term;
            sign = -sign;
            term *= x * x / (2 * i * (2 * i + 1));
        }

        return result;
    }


    double cos(double x) {
        double result = 1;
        double term = 1;
        int sign = -1;

        for (int i = 0; i < 30; i+=2) { // i < 30 WARUM?
            term *= x * x/((i+1)*(i+2));
            result += sign * term;
            sign = -sign;
        }
        return result;
    }

    double tan(double x) {
        double sinValue = sin(x);
        double cosValue = cos(x);

        return sinValue / cosValue;
    }


    // Funktionen mit Parameter, z.B. x + a, x - a, x^2 + a
    DoubleUnaryOperator parseParameter(String functionInput, int a) {
        functionInput = functionInput.replaceAll("\\s", "");

        // Erkenne den Fall x + a oder x - a
        if (functionInput.matches("^[+-]?[xX][+-]?a$")) {
            String cleanedInput = functionInput.toLowerCase();
            if (cleanedInput.matches("x[+-]a")) {
                char operator = cleanedInput.charAt(1);  // '+' oder '-'
                return (x) -> operator == '+' ? x + a : x - a;
            } else {
                System.err.println("Invalid parameterized function format: " + functionInput);
            }
        }
        // Erkenne den Fall x^2 + a oder x^2 - a
        else if (functionInput.matches("^[+-]?[xX]\\^2[+-]?a$")) {
            String cleanedInput = functionInput.toLowerCase();
            char operator = cleanedInput.charAt(3);  // '+' oder '-'

            return (x) -> {
                double result = Math.pow(x, 2);  // Berechne x^2
                return operator == '+' ? result + a : result - a;
            };
        }

        else if (functionInput.matches("^[+-]?(sin|cos|tan|sqrt)\\(x\\)[+-]a$")) {
            String functionName = functionInput.replaceAll("\\(x\\)[+-]?a$", "");
            boolean isNegative = functionInput.endsWith("-a");

            // Rückgabe der jeweiligen Funktion mit der Konstante a
            switch (functionName) {
                case "sin" -> {
                    return (x) -> {
                        double result = sin(x); // Berechne sin(x)
                        return isNegative ? result - a : result + a;
                    };
                }
                case "cos" -> {
                    return (x) -> {
                        double result = cos(x); // Berechne cos(x)
                        return isNegative ? result - a : result + a;
                    };
                }
                case "tan" -> {
                    return (x) -> {
                        double result = tan(x); // Berechne tan(x)
                        return isNegative ? result - a : result + a;
                    };
                }
            }
        }
        return null;
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

