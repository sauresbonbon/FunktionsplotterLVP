import Controller.Controller;
import View.View;
import Model.Model;
import View.Clerk;

public class Main {

    public static void main(String[] args) {
        var view = new View();
        var model = new Model();
        var controller = new Controller();

        controller.setView(view);
        controller.setModel(model);
        view.setController(controller);

        view.draw();
    }
}