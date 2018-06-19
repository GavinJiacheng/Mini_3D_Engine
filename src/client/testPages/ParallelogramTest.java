package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ParallelogramTest {
    private static final int MAX_P = 50;
    //private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;

    private final LineRenderer renderer;
    private final Drawable panel;
    Vertex3D P1,P2,P3,P4;

    public ParallelogramTest(Drawable panel, LineRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;
        render();
    }

    private void render() {
        int X1 = 20;
        int X2 = 150;
        int X3 = 160;
        int X4 = 240;
        int Y1 = 220;
        int Y2 = 150;
        int Y3 = 30;
        int Y4 = 260;
        P1 = new Vertex3D(X1, Y1, 0, Color.WHITE);
        P2 = new Vertex3D(X2, Y2, 0, Color.WHITE);
        P3 = new Vertex3D(X3, Y3, 0, Color.WHITE);
        P4 = new Vertex3D(X4, Y4, 0, Color.WHITE);
        for(int p = 0; p < MAX_P; p++) {
            renderer.drawLine(P1, P2, panel);
            renderer.drawLine(P3, P4, panel);
            p_plus();
        }
    }


    private void p_plus(){
        int y1 = P1.getIntY() -1;
        int y2 = P2.getIntY() -1;
        int x3 = P3.getIntX() +1;
        int x4 = P4.getIntX() +1;
        P1 = new Vertex3D(P1.getIntX(), y1, 0, Color.WHITE);
        P2 = new Vertex3D(P2.getIntX(), y2, 0, Color.WHITE);
        P3 = new Vertex3D(x3, P3.getIntY(), 0, Color.WHITE);
        P4 = new Vertex3D(x4, P4.getIntY(), 0, Color.WHITE);
    }

}
