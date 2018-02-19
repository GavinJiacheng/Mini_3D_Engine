package notProvided.line;

import geometry.Vertex3D;
import line.AnyOctantLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer implements LineRenderer{
    private BresenhamLineRenderer() {}

    /*
     * (non-Javadoc)
     * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
     *
     * @pre: p2.x >= p1.x && p2.y >= p1.y
     */
    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {

        int x1 = p1.getIntX();
        int x2 = p2.getIntX();
        int y1 = p1.getIntY();
        int y2 = p2.getIntY();
        int deltaX = Math.abs(p2.getIntX() - p1.getIntX());
        int deltaY = Math.abs(p2.getIntY() - p1.getIntY());
        int argbColor = p1.getColor().asARGB();

        if ((x1 == x2) && (y1 == y2)) {
            drawable.setPixel(x1, y1, 0.0, argbColor);
        } else {
            int dfiiXY = deltaX - deltaY;
            int posun_x, posun_y;
            if (x1 < x2) posun_x = 1;
            else posun_x = -1;
            if (y1 < y2) posun_y = 1;
            else posun_y = -1;
            while ((x1 != x2) || (y1 != y2)) {
                int p = 2 * dfiiXY;
                if (p > -deltaY) {
                    dfiiXY = dfiiXY - deltaY;
                    x1 = x1 + posun_x;
                }
                if (p < deltaX) {
                    dfiiXY = dfiiXY + deltaX;
                    y1 = y1 + posun_y;
                }
                drawable.setPixel(x1, y1, 0.0, argbColor);
            }
        }
    }


    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new BresenhamLineRenderer());
    }

}
