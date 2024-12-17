package Model;

import java.util.ArrayList;
import java.util.List;

public class Model implements IModel{
    List<Integer> xy = new ArrayList<>();

    public void initializePlotter() {
        //xMin
        xy.add(-10);
        //yMin
        xy.add(-10);
        //xMax
        xy.add(10);
        //yMax
        xy.add(10);
    }

    public void setXY(int xMin, int yMin, int xMax, int yMax) {
        if(xMin >= xMax || yMin >= yMax) {
            System.out.println("xMin/yMin muss kleiner als xMax/yMax sein.");
            System.exit(1);
        }
        else {
            xy.set(0, xMin);
            xy.set(1, yMin);
            xy.set(2, xMax);
            xy.set(3, yMax);
        }
    }

    @Override
    public List<Integer> getXY() {
        return xy;
    }
}
