package model;

import java.util.Random;

public class CityMap {
    private int MAX = 30;
    private int MIN = 15;
    private Field map[][];
    private int row, column;
    private int dimension;

    public CityMap() {
        dimension = 18;
        //dimension = new Random().nextInt(MAX - MIN) + MIN;
        generateMap(dimension);
    }

    private void generateMap(int dimension) {
        this.row = dimension;
        this.column = dimension;
        System.out.println("row = " + row + " column = " + column);
        map = new Field[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                map[i][j] = new Field();
            }
        }
    }

    public Field[][] getMap() {
        return map;
    }

    public void setMap(Field[][] map) {
        this.map = map;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
