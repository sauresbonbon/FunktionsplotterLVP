package Controller;

import Model.*;
import View.IView;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

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
        model.setBounds(xMin, yMin, xMax, yMax);
    }

    @Override
    public List<Integer> getXY() {
        return model.getBounds();
    }

    @Override
    public void initializePlotter() {
        model.initializePlotter();
    }

    public void setFunction(DoubleUnaryOperator f) {
        model.setFunction(f);
    }

    public DoubleUnaryOperator getFunction() {
        return model.getFunction();
    }

}
