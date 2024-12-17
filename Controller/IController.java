package Controller;

import java.util.List;

public interface IController {
    void setXY(int xMin, int yMin, int xMax, int yMax);
    List<Integer> getXY();
    void initializePlotter();
}
