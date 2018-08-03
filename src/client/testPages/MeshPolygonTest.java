package client.testPages;

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
    private Color randomcolor_bleow,randomcolor_up,randomcolor;
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
                P1=points[i][j];
                P2=points[i][j+1];
                P3=points[i+1][j+1];
                triangle = Polygon.make(P1, P2, P3);
                renderer.drawPolygon(triangle, panel);
            }
        }
        for (int i=1;i<10;i++){
            for (int j =0;j<9;j++){
                P1=points[i][j];
                P2=points[i][j+1];
                P3=points[i-1][j];
                triangle = Polygon.make(P1, P2, P3);
                renderer.drawPolygon(triangle, panel);
            }
        }

        // Assignment 1:
        /*
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
        } */
    }

    private Vertex3D setPoint(double X, double Y, Color color) {
        return new Vertex3D(X, Y, 0, color);
    }

    public void no_PERTURBATION(){
        double x=32.5,y=32.5;
        for(int i=0;i<10;i++) {
            for (int j = 0; j < 10; j++) {
                Randomcolor(i,j);
                points[i][j] = setPoint(x, y, randomcolor);
                x += 65;
            }
            y += 65;
            x = 32.5;
        }
    }

    public void use_PERTURBATION(){
        double x=16.25,y=16.25;
        for(int i=0;i<10;i++) {
            for (int j = 0; j < 10; j++) {
                Random rd = new Random();
                double addx = rd.nextInt(24);
                double addy = rd.nextInt(24);
                Randomcolor(i,j);
                points[i][j] = setPoint(x+addx, y+addy, randomcolor);
                x += 65;
            }
            y += 65;
            x = 16.25;
        }
    }

    private void Randomcolor(int i, int j) {
        Random rd = new Random();
        double R = rd.nextInt(256);
        double G = rd.nextInt(128);
        double B = rd.nextInt(128);
        if (i % 2 ==0) {
            G += 128;
        }if (j % 2 == 0) {
            B += 128;
        }
        R = R/255;
        G = G/255;
        B = B/255;
        randomcolor = new Color(R, G, B);
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
