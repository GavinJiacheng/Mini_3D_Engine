package notProvided.client.testpages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.Random;

public class MeshPolygonTest {

    private final PolygonRenderer renderer;
    private final Drawable panel;
    Vertex3D points[][] = new Vertex3D[10][10];
    Polygon triangle;
    private Color randomcolor_bleow,randomcolor_up;
    public static int NO_PERTURBATION = 0;
    public static int USE_PERTURBATION = 1;

    public MeshPolygonTest(Drawable panel, PolygonRenderer renderer, int check) {
        this.panel = panel;
        this.renderer = renderer;
        if (check == 0)
            no_PERTURBATION();
        else
            use_PERTURBATION();
        render();
    }

    private void render() {

        Vertex3D P1,P2,P3;
        for (int i=0;i<9;i++){
            for (int j =0;j<9;j++){
                Randomcolor_bleow();
                P1=points[i][j].replaceColor(randomcolor_bleow);
                P2=points[i][j+1].replaceColor(randomcolor_bleow);
                P3=points[i+1][j+1].replaceColor(randomcolor_bleow);
                triangle = Polygon.make(P1, P2, P3);
                renderer.drawPolygon(triangle, panel);
            }
        }
        for (int i=1;i<10;i++){
            for (int j =0;j<9;j++){
                Randomcolor_up();
                P1=points[i][j].replaceColor(randomcolor_up);
                P2=points[i][j+1].replaceColor(randomcolor_up);
                P3=points[i-1][j].replaceColor(randomcolor_up);
                triangle = Polygon.make(P1, P2, P3);
                renderer.drawPolygon(triangle, panel);
            }
        }
    }

    private Vertex3D setPoint(double X, double Y, Color color) {
        return new Vertex3D(X, Y, 0, color);
    }

    public void no_PERTURBATION(){
        double x=15.0,y=15.0;
        for(int i=0;i<10;i++) {
            for (int j = 0; j < 10; j++) {
                points[i][j] = setPoint(x, y, Color.WHITE);
                x += 30;
            }
            y += 30;
            x = 15;
        }
    }

    public void use_PERTURBATION(){
        double x=3.0,y=3.0;
        for(int i=0;i<10;i++) {
            for (int j = 0; j < 10; j++) {
                Random rd = new Random();
                double addx = rd.nextInt(24);
                double addy = rd.nextInt(24);
                points[i][j] = setPoint(x+addx, y+addy, Color.WHITE);
                x += 30;
            }
            y += 30;
            x = 3;
        }
    }

    private void Randomcolor_bleow() {
        Random rd = new Random();
        double R = rd.nextInt(256);
        double G = rd.nextInt(256);
        double B = rd.nextInt(128);
        R = R/255;
        G = G/255;
        B = B/255;
        randomcolor_bleow = new Color(R, G, B);
    }
    private void Randomcolor_up() {
        Random rd = new Random();
        double R = rd.nextInt(256);
        double G = rd.nextInt(256);
        double B = rd.nextInt(128);
        B += 128;
        R = R/255;
        G = G/255;
        B = B/255;
        randomcolor_up = new Color(R, G, B);
    }
}
