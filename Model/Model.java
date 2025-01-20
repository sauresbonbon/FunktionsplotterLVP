package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public class Model implements IModel{
    List<Integer> bounds = new ArrayList<>();
    private DoubleUnaryOperator f;

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


    @Override
    public List<Integer> getBounds() {
        return bounds;
    }

    public void setFunction(DoubleUnaryOperator f) {
        this.f = f;
    }
    public DoubleUnaryOperator getFunction() {
        return f;
    }


}
