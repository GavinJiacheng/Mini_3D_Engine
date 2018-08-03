package line;

import client.Client;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;


public class ColorInterpolationDDARender implements LineRenderer {


    private ColorInterpolationDDARender(){}
    //private Client C;


    public static Color colorDivideBy(Color color, double distance){
        double newR = color.getR()/distance;
        double newG = color.getG()/distance;
        double newB = color.getB()/distance;
        return  (new Color(newR, newG, newB));
    }
    public static Color colorPlus(Color color, Color addColor){
        double newR = color.getR()+addColor.getR();
        double newG = color.getG()+addColor.getG();
        double newB = color.getB()+addColor.getB();
        return  (new Color(newR, newG, newB));
    }
    public static Color colorDistance(Color color, Color finalColor){
        double newR = finalColor.getR()-color.getR();
        double newG = finalColor.getG()-color.getG();
        double newB = finalColor.getB()-color.getB();
        return  (new Color(newR, newG, newB));
    }

    public static Color colorMultiply(Color color, double distance){
        double newR = color.getR()*distance;
        double newG = color.getG()*distance;
        double newB = color.getB()*distance;
        return  (new Color(newR, newG, newB));
    }

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
        int x0 = p1.getIntX();
        int x1 = p2.getIntX();
        int y0 = p1.getIntY();
        int y1 = p2.getIntY();
        int z0 = p1.getIntZ();
        int z1 = p2.getIntZ();
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double m = dy/dx;
        double mz = dz/dx;
        double b = y0 - m* x0;
        double y = (double) y0;
        double z = (double) z0;
        Color P1bColor = p1.getColor();
        Color P2bColor = p2.getColor();
        Color distance = colorDistance(P1bColor,P2bColor);
        Color unitDistance = colorDivideBy(distance, dx);
        Color CurrentColor = P1bColor;
        //if (z != 0){
        //   System.out.println(z);
        //}
        //System.out.println("From: "+x0+" TO: "+x1);
        for (int x = x0; x <= x1; x++){
            int argbColor = CurrentColor.asARGB();
            int IntY = (int)Math.round(y);
            //if (z>C.Zbuff[x+624][IntY+624]) { ;
           //     C.Zbuff[x+624][IntY+624] = z;
            panel.setPixel(x, IntY, z, argbColor);
          //  }
            CurrentColor = colorPlus(CurrentColor, unitDistance);
            y = y + m;
            z = z + mz;
        }
    }


    public static LineRenderer make() {
        return new AnyOctantLineRenderer(new ColorInterpolationDDARender());
    }
}
