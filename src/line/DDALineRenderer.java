package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class DDALineRenderer   implements LineRenderer {

    private DDALineRenderer(){}
    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
        int x0 = p1.getIntX();
        int x1 = p2.getIntX();
        int y0 = p1.getIntY();
        int y1 = p2.getIntY();
        double dx = x1 - x0;
        double dy = y1 - y0;
        double m = dy/dx;
        double b = y0 - m* x0;
        double y = (double) y0;
        int argbColor = p1.getColor().asARGB();
        for (int x = x0; x <= x1; x++){
            panel.setPixel(x, (int)Math.round(y), 0, argbColor);
            y = y + m;
        }
    }

    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new DDALineRenderer());
    }

}
