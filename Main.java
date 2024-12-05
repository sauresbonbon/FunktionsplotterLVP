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

        String bName = "button";
        String sName = "slider";
        String sStName = "slider mit Stufen";
        Button b = new Button(Clerk.view(), bName);
        Slider s = new Slider(Clerk.view(), 0,10, sName);
        SliderStufen sSt = new SliderStufen(Clerk.view(), 1, 5, sStName);
    }

    class Button implements Clerk {
        final String ID;
        LiveView view;

        Button(LiveView view, String label) {
            this.view = view;
            ID = Clerk.getHashID(this);
            Clerk.write(view, "<div><button id='button" + ID + "'>" + label + "</button></div>");
            Clerk.script(view, "const button" + ID + " = document.getElementById('button" + ID + "');");
        }

        Button attachTo(Runnable delegate) {
            this.view.createResponseContext("/button" + ID, (body) -> delegate.run(), ID);
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

}