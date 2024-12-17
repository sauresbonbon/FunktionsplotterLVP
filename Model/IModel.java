package Model;

import java.util.List;

public interface IModel {
    void setXY(int xMin, int yMin, int xMax, int yMax);
    List<Integer> getXY();
    void initializePlotter();
}
