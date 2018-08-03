package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.Random;

public class centeredTriangleTest {

    public centeredTriangleTest(Drawable panel, PolygonRenderer renderer){
        PolygonRenderer renderer1 = renderer;
        double[] v = {1,0.85,0.7,0.55,0.4,0.25};
        double[] x = {100,500,300};
        double[] y = {184.53, 184.53, 530.94};
        double[] X = {0,0,0};
        double[] Y = {0,0,0};
        Random rd = new Random();
        for (int i =0;i<6; i++){
            double z = rd.nextInt(199);
            z += 1;
            z = -z;
            Color color = new Color (v[i],v[i],v[i]);
            int angle = rd.nextInt(121);
            double PInumber = Math.PI *angle/180;
            for ( int j = 0; j<3;j++) {
                X[j] = (x[j]-300) * Math.cos(PInumber) - (y[j]-300) * Math.sin(PInumber) +300;
                Y[j] = (x[j]-300) * Math.sin(PInumber) + (y[j]-300) * Math.cos(PInumber) + 300;
            }
            Vertex3D p1 = new Vertex3D(X[0], Y[0], z, color);
            Vertex3D p2 = new Vertex3D(X[1], Y[1], z, color);
            Vertex3D p3 = new Vertex3D(X[2], Y[2], z, color);
            Polygon triangle = Polygon.make(p1, p2, p3);
            renderer1.drawPolygon(triangle,panel);
        }
    }
}
