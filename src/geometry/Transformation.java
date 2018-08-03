package geometry;

public class Transformation {

    private double Transform[][];


    public Transformation() {
        Transform = new double[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i == j) {
                    Transform[i][j] = 1;
                }
                else {
                    Transform[i][j] = 0;
                }
            }
        }
    }

    public static Transformation identity() {
        Transformation T = new Transformation();
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i == j) {
                    T.Transform[i][j] = 1;
                }
                else {
                    T.Transform[i][j] = 0;
                }
            }
        }
        return T;
    }

    public void init() {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i == j) {
                    Transform[i][j] = 1;
                }
                else {
                    Transform[i][j] = 0;
                }
            }
        }
    }

    public static double[][] reset() {
        double[][] T = new double[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                T[i][j] = 0;
            }
        }
        return T;
    }


    public void transformMatrix(int i, int j, double transform) {
        Transform[i][j] = transform;
    }
    public double[][] getMatrix() {
        double[][] newarray = Transform;
        return newarray;
    }

    public void setValue(int i, int j, double value){
        //if(value == 10* Transform[i][j]){
            //System.out.println("bug");
        //}
        Transform[i][j] = value;
    }

    public void setValue(double[][] value){
        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                Transform[x][y] = value[x][y];
            }
        }
    }
}
