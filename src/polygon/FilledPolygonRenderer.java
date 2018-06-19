package polygon;

import geometry.Vertex3D;
import line.LineRenderer;
import line.DDALineRenderer;
import windowing.drawable.Drawable;

public class FilledPolygonRenderer implements PolygonRenderer {

    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader) {
        Chain leftchain = polygon.leftChain();
        Chain rightchain = polygon.rightChain();
        int len = polygon.length();
        int llen = leftchain.length();
        int rlen = rightchain.length();
        if (len == 3) {

            if (rlen == llen) {
                if (rightchain.get(0).getX() != leftchain.get(0).getX()) {
                    fillBottomTriangle(leftchain.get(0), rightchain.get(0), leftchain.get(1), drawable);
                    return;
                } else {
                    fillTopTriangle(rightchain.get(0), rightchain.get(1), leftchain.get(1), drawable);
                    return;
                }
            } else {
                double Y, X;
                Vertex3D TOP, MID, BOT;
                if (rlen > llen) {
                    Y = rightchain.get(1).getY();
                    double divide = (rightchain.get(1).getY() - rightchain.get(0).getY()) / (rightchain.get(2).getY() - rightchain.get(0).getY());
                    double offset = divide * (rightchain.get(2).getX() - rightchain.get(0).getX());
                    X = rightchain.get(0).getX() + offset;
                    TOP = rightchain.get(0);
                    MID = rightchain.get(1);
                    BOT = rightchain.get(2);
                } else {
                    Y = leftchain.get(1).getY();
                    double divide = (leftchain.get(1).getY() - leftchain.get(0).getY()) / (leftchain.get(2).getY() - leftchain.get(0).getY());
                    double offset = divide * (leftchain.get(2).getX() - leftchain.get(0).getX());
                    X = leftchain.get(0).getX() + offset;
                    TOP = leftchain.get(0);
                    MID = leftchain.get(1);
                    BOT = leftchain.get(2);
                }
                Vertex3D newmid = new Vertex3D(X, Y, 0, TOP.getColor());
                fillTopTriangle(TOP, MID, newmid, drawable);
                fillBottomTriangle(MID, newmid, BOT, drawable);
            }
        }
    }

        private void fillTopTriangle(Vertex3D TOP,Vertex3D mid1, Vertex3D mid2, Drawable drawable) {
            LineRenderer renderer = DDALineRenderer.make();
            Vertex3D LEFT, RIGHT;
            if (mid1.getX() < mid2.getX()) {
                LEFT = mid1;
                RIGHT = mid2;
            } else {
                LEFT = mid2;
                RIGHT = mid1;
            }
            double deltaY = TOP.getY() - LEFT.getY();
            double deltal = TOP.getX() - LEFT.getX();
            double deltar = TOP.getX() - RIGHT.getX();
            double ml = deltal / deltaY;
            double mr = deltar / deltaY;
            double lx = TOP.getX();
            double rx = TOP.getX();
            int end = RIGHT.getIntY();
            for (int i = TOP.getIntY()-1; i >= end; i--) {
                lx = lx - ml;
                rx = rx - mr;
                double irx = Math.floor(rx);
                double ilx = Math.ceil(lx);
                if (lx == ilx){
                    ilx ++;
                }
                LEFT = new Vertex3D(ilx, i, 0, LEFT.getColor());
                RIGHT = new Vertex3D(irx, i, 0, RIGHT.getColor());
                renderer.drawLine(LEFT, RIGHT, drawable);
            }
        }


    private void fillBottomTriangle(Vertex3D mid1,Vertex3D mid2, Vertex3D BOT, Drawable drawable) {
        LineRenderer renderer = DDALineRenderer.make();
        Vertex3D LEFT, RIGHT;
        if (mid1.getX() < mid2.getX()) {
            LEFT = mid1;
            RIGHT = mid2;
        } else {
            LEFT = mid2;
            RIGHT = mid1;
        }
        double deltaY = BOT.getY() - LEFT.getY();
        double deltal = BOT.getX() - LEFT.getX();
        double deltar = BOT.getX() - RIGHT.getX();
        double ml = deltal / deltaY;
        double mr = deltar / deltaY;
        double lx = BOT.getX();
        double rx = BOT.getX();

        int end = RIGHT.getIntY();
        for (int i = BOT.getIntY()+1; i < end; i++) {
            lx += ml;
            rx += mr;
            double irx = Math.floor(rx);
            double ilx = Math.ceil(lx);
            if (lx == ilx){
                ilx ++;
            }
            LEFT = new Vertex3D(ilx, i, 0, LEFT.getColor());
            RIGHT = new Vertex3D(irx, i, 0, RIGHT.getColor());
            renderer.drawLine(LEFT, RIGHT, drawable);
        }
    }






    public static FilledPolygonRenderer make(){
        return  new FilledPolygonRenderer();
    }
}
