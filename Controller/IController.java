package Controller;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public interface IController {
    void setBounds(int xMin, int yMin, int xMax, int yMax);
    List<Integer> getXY();
    void initializePlotter();

    DoubleUnaryOperator getFunction();
    void setFunction(DoubleUnaryOperator f);
}
