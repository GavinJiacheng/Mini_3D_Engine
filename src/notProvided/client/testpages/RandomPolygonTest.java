package notProvided.client.testpages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.Random;


public class RandomPolygonTest {

    private final PolygonRenderer renderer;
    private final Drawable panel;
    Vertex3D points[][] = new Vertex3D[10][10];
    Polygon triangle;
    private Color randomcolor;
    public static int NO_PERTURBATION = 0;
    public static int USE_PERTURBATION = 1;

    public RandomPolygonTest(Drawable panel, PolygonRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;
        render();
    }

    private void render() {
        for (int i=0;i<20;i++){
            Randomtriangle();
            renderer.drawPolygon(triangle, panel);
        }
    }

    private Vertex3D setPoint(double X, double Y, Color color) {
        return new Vertex3D(X, Y, 0, color);
    }


    private void Randomcolor() {
        Random rd = new Random();
        double R = rd.nextInt(256);
        double G = rd.nextInt(256);
        double B = rd.nextInt(256);
        R=R/255;
        G=G/255;
        B=B/255;
        randomcolor = new Color(R, G, B);
    }

    private void Randomtriangle() {
        Random rd = new Random();
        double X1 = rd.nextInt(300);
        double X2,X3;
        do {
            X2 = rd.nextInt(300);
        }while ( X2 == X1);
        do {
            X3 = rd.nextInt(300);
        }while(X3 == X2 || X3 == X1);
        rd = new Random();
        double Y1 = rd.nextInt(300);
        double Y2,Y3;
        do {
            Y2 = rd.nextInt(300);
        }while ( Y2 == Y1);
        do {
            Y3 = rd.nextInt(300);
        }while(Y3 == Y2 || Y3 == Y1);
        Randomcolor();
        Vertex3D P1,P2,P3;
        P1=setPoint(X1,Y1,randomcolor);
        P2=setPoint(X2,Y2,randomcolor);
        P3=setPoint(X3,Y3,randomcolor);
        triangle = Polygon.make(P1, P2, P3);
    }
}

