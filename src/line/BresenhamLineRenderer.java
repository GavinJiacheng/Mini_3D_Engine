package line;


import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer  implements LineRenderer {
    private BresenhamLineRenderer(){}

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
        int x1 = p1.getIntX();
        int x2 = p2.getIntX();
        int dx = x2 - x1;
        int y1 = p1.getIntY();
        int y2 = p2.getIntY();
        int dy = y2 - y1;
        int m = 2 *dy;
        int q = m -2*dx;
        int argbColor = p1.getColor().asARGB();
        panel.setPixel(x1,y1,0.0,argbColor);
        int y = y1;
        int err = m -dx;
        for (int x = x1 +1; x <= x2; x++){
            if (err >= 0) {
                err += q;
                y++;
            }else {
                err += m;
            }
            panel.setPixel(x,y,0.0,argbColor);
        }
    }

    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new BresenhamLineRenderer());
    }
}
