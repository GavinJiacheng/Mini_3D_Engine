package notProvided.client.testpages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.Random;

public class RandomLineTest {
    private static final int NUM = 30;

    //private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;

    private final LineRenderer renderer;
    private final Drawable panel;
    private static Vertex3D P1[] = new Vertex3D [30];
    private static Vertex3D P2[] = new Vertex3D [30];
    private static Color randomcolor[] = new Color [30];
    private static int check;


    public RandomLineTest(Drawable panel, LineRenderer renderer) {
        Random();
        this.panel = panel;
        this.renderer = renderer;
        render();
    }

    private void render() {
        for(int p = 0; p < NUM; p++) {
            renderer.drawLine(P1[p], P2[p], panel);
        }
    }

    private void Random() {
        if (check == 0) {
            for (int p = 0; p < NUM; p++) {
                Random rd = new Random();
                double R = rd.nextInt(256);
                double G = rd.nextInt(256);
                double B = rd.nextInt(256);
                R=R/255;
                G=G/255;
                B=B/255;
                randomcolor[p] = new Color(R, G, B);
                int X1 = rd.nextInt(300);
                int X2 = rd.nextInt(300);
                int Y1 = rd.nextInt(300);
                int Y2 = rd.nextInt(300);
                P1[p] = new Vertex3D(X1, Y1, 0, randomcolor[p]);
                P2[p] = new Vertex3D(X2, Y2, 0, randomcolor[p]);
            }
            check = 1;
        }
    }

}
