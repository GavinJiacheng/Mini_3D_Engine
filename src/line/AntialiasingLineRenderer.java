package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class AntialiasingLineRenderer   implements LineRenderer {

    double b, m, denominator;
    private static final double r = 0.5;
    int direction;

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
        int x0 = p1.getIntX();
        int x1 = p2.getIntX();
        int y0 = p1.getIntY();
        int y1 = p2.getIntY();
        double dx = x1 - x0;
        double dy = y1 - y0;
        if (dy >= 0){
            direction = 1;
        }else{
            direction = -1;
        }
        int color = p1.getColor().asARGB();
        m = dy/dx;
        b = y0 - m* x0;
        denominator  = Math.sqrt(m*m+1);
        fillthepixel(x0,y0,x1,color,panel,true);

    }

    public int fillthepixel(int x, int y,int xfinal, int argbColor, Drawable panel, boolean twodirection){
        if (x>xfinal){
            return 0;
        }
        boolean needfilled = true;
        double numerator = m*x-y+b;
        double weight;
        double d;
        if (numerator <0){
            numerator = -numerator;
        }
        double distance = numerator/denominator;
        if (distance >= 1){
            return 0;
        }
        else if (distance == 0){
            weight = 1;
        }
        else{
            d = distance - 0.5;
            double theta = Math.acos(d/r);
            double areawedge = (1-theta/Math.PI)*(Math.PI)*r*r;
            double areatotal = areawedge + d*Math.sqrt(r*r-d*d);
            weight = 1 - areatotal/((Math.PI)*r*r);
        }



        if (needfilled && twodirection){
            panel.setPixelWithCoverage(x,y,0,argbColor,weight);
            int ver = fillthepixel(x,y+direction, xfinal, argbColor,panel,true);
            if (ver == 0){
                int right = fillthepixel(x+1,y, xfinal, argbColor,panel,true);
                return 2;
            }else{
                int right = fillthepixel(x+1,y,xfinal,argbColor,panel,false);
                return 2;
            }
        }else if (needfilled){
            panel.setPixelWithCoverage(x,y,0,argbColor,weight);
            int right = fillthepixel(x+1,y,xfinal,argbColor,panel,false);
            return 3;
        }else{
            return 0;
        }
    }

    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
    }


}
