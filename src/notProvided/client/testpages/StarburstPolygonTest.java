package notProvided.client.testpages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;


public class StarburstPolygonTest {
    private static final int NUM_RAYS = 90;
    private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;

    private final PolygonRenderer renderer;
    private final Drawable panel;
    Vertex3D center;
    Polygon triangle;
    private Color randomcolor = Color.WHITE;

    public StarburstPolygonTest(Drawable panel, PolygonRenderer renderer) {
        this.panel = panel;
        this.renderer = renderer;
        makeCenter();
        render();
    }

    private void render() {
        double radius = computeRadius();
        double angleDifference = (2.0 * Math.PI) / NUM_RAYS;
        double angle = 0.0;

        Randomcolor();
        Vertex3D lastpoint_store = radialPoint(radius, angle);
        for(int ray = 0; ray < NUM_RAYS; ray++) {
            angle = angle + angleDifference;
            makeCenter();
            Vertex3D radialPoint = radialPoint(radius, angle);
            triangle = Polygon.make(lastpoint_store, center, radialPoint);
            renderer.drawPolygon(triangle, panel);
            Randomcolor();
            lastpoint_store = radialPoint;
            lastpoint_store = lastpoint_store.replaceColor(randomcolor);

       //     Randomcolor();
        //    makeCenter();
       //     Vertex3D radialPoint = radialPoint(radius, angle);
        //    renderer.drawLine(center, radialPoint, panel);

         //   angle = angle + angleDifference;
        }
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

    private void makeCenter() {
        int centerX = panel.getWidth() / 2;
        int centerY = panel.getHeight() / 2;
        center = new Vertex3D(centerX, centerY, 0, randomcolor);
    }

    private Vertex3D radialPoint(double radius, double angle) {
        double x = center.getX() + radius * Math.cos(angle);
        double y = center.getY() + radius * Math.sin(angle);
        return new Vertex3D(x, y, 0, randomcolor);
    }


    private double computeRadius() {
        int width = panel.getWidth();
        int height = panel.getHeight();

        int minDimension = width < height ? width : height;

        return (minDimension / 2.0) * FRACTION_OF_PANEL_FOR_DRAWING;
    }
}