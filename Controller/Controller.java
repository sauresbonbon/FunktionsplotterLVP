package Controller;

import Model.*;
import View.IView;

import java.util.List;

public class Controller implements IController{

    private IView view;
    private IModel model;

    public void setView(IView view) {
        this.view = view;
    }

    public void setModel(IModel model) {
        this.model = model;
    }

    public void setXY(int xMin, int yMin, int xMax, int yMax) {
        model.setXY(xMin, yMin, xMax, yMax);
    }

    @Override
    public List<Integer> getXY() {
        return model.getXY();
    }

    @Override
    public void initializePlotter() {
        model.initializePlotter();
    }

}
