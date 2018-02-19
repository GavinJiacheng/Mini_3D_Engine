package notProvided.line;

import geometry.Vertex;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import line.LineRenderer;
import line.AnyOctantLineRenderer;

public class DDALineRenderer implements LineRenderer{
    private DDALineRenderer() {}


    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {

        int x1 = p1.getIntX();
        int x2 = p2.getIntX();
        int y1 = p1.getIntY();
        int y2 = p2.getIntY();
        double deltaX = p2.getIntX() - p1.getIntX();
        double deltaY = p2.getIntY() - p1.getIntY();
        double slope = deltaY / deltaX;
        //double intercept = p2.getIntY() - slope * p2.getIntX();
        int argbColor = p1.getColor().asARGB();
        double m = Math.abs(slope);
        double absx = Math.abs(deltaX);
        double absy = Math.abs(deltaY);

        if (absy <= absx) {
            if ((x1 == x2) && (y1 == y2)) {
                drawable.setPixel(x1, y1, 0.0, argbColor);
            } else {
                if (x2 < x1) {
                    int tmp = x2;
                    x2 = x1;
                    x1 = tmp;
                    tmp = y2;
                    y2 = y1;
                    y1 = tmp;
                }
                int cele_y;
                double y = (double) y1;
                for (int x = x1; x <= x2; x++) {
                    cele_y = (int) Math.round(y);
                    drawable.setPixel(x, cele_y, 0.0, argbColor);
                    y += slope;
                }
            }
        } else {
            if (y2 < y1) {
                int tmp = x2;
                x2 = x1;
                x1 = tmp;
                tmp = y2;
                y2 = y1;
                y1 = tmp;
            }
            int cele_x;
            double x = (double) x1;
            for (int y = y1; y <= y2; y++) {
                cele_x = (int) Math.round(x);
                drawable.setPixel(cele_x, y, 0.0, argbColor);
                x += slope;
            }
        }
    }
    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new DDALineRenderer());
    }

}
