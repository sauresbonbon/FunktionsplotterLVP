package Model;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public interface IModel {
    void setBounds(int xMin, int yMin, int xMax, int yMax);
    List<Integer> getBounds();
    void initializePlotter();
    void setFunction(DoubleUnaryOperator f);
    DoubleUnaryOperator getFunction();

}
