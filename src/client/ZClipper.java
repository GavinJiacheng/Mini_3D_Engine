
package client;

import geometry.Vertex3D;
import line.ColorInterpolationDDARender;
import windowing.graphics.Color;

import java.util.ArrayList;

public class ZClipper {

    private double z_max = 0;
    private double z_min = 0;
    private double x_min = 0;
    private double y_min = 0;
    private double x_max = 0;
    private double y_max = 0;

    private Vertex3D v1,v2,v3;

    public ArrayList<Vertex3D> NewPolygonPoints = new ArrayList<Vertex3D>();
    public ArrayList<Vertex3D> storearray = new ArrayList<Vertex3D>();


    public ZClipper(double z_min, double z_max, Vertex3D v1, Vertex3D v2, Vertex3D v3){
        this.z_min = z_min;
        this.z_max = z_max;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        storearray.add(v1);
        storearray.add(v2);
        storearray.add(v3);
        CutNewPoints(storearray);

        //System.out.println("z max : "+z_max);
        //System.out.println("z z_min : "+z_min);
        //System.out.println("storearray lenght "+storearray.size());
        //System.out.println("x1 "+v1.getX()+"y: "+v1.getY());
        //System.out.println("x2 "+v2.getX()+"y: "+v2.getY());
        //System.out.println("x3 "+v3.getX()+"y: "+v3.getY());
        //Check_if_Piercing_camera();
        //System.out.println("array lenght "+NewPolygonPoints.size());
    }




    private Vertex3D CutZ (double z, Vertex3D V1, Vertex3D V2){
        Vertex3D down, up;
        if (V1.getZ() < V2.getZ()){
            down = V1;
            up = V2;
        }else{
            down = V2;
            up = V1;
        }

        double dy = up.getY()-down.getY();
        double dx = up.getX()-down.getX();
        double dz = up.getZ()-down.getZ();

        Color dcolor = ColorInterpolationDDARender.colorDistance(down.getColor(),up.getColor());
        Color dcolorZ = ColorInterpolationDDARender.colorDivideBy(dcolor, dz);

        double dxz = dx /dz;
        double dyz = dy /dz;
        double newX = down.getX() + dxz * (z-down.getZ());
        double newY = down.getY() + dyz * (z-down.getZ());
        Color newC = ColorInterpolationDDARender.colorPlus( down.getColor() , ColorInterpolationDDARender.colorMultiply(dcolorZ,(z-down.getZ())));
        return new Vertex3D(newX,newY,z, newC);


    }

    private void CutNewPoints(ArrayList<Vertex3D> arraylist){
        // for top side
        ArrayList<Vertex3D> PolygonPoints = new ArrayList<Vertex3D>();

        PolygonPoints.clear();
        for (int i =0;i<arraylist.size();i++){
            Vertex3D V1 = arraylist.get(i);
            Vertex3D V2;
            if (i != arraylist.size()-1) {
                V2 = arraylist.get(i + 1);
            }else{
                V2 = arraylist.get(0);
            }
            if (V1.getZ() >= z_min) {
                if (V2.getZ() >= z_min) {
                    PolygonPoints.add(V2);
                }else{
                    PolygonPoints.add(CutZ(z_min,V2,V1));
                }
            }else{
                if (V2.getZ() >= z_min) {
                    PolygonPoints.add(CutZ(z_min,V2,V1));
                    PolygonPoints.add(V2);
                }
            }
        }

        NewPolygonPoints.clear();
        for (int i =0;i<PolygonPoints.size();i++){
            Vertex3D V1 = PolygonPoints.get(i);
            Vertex3D V2;
            if (i != PolygonPoints.size()-1) {
                V2 = PolygonPoints.get(i + 1);
            }else{
                V2 = PolygonPoints.get(0);
            }
            if (V1.getZ() <= z_max) {
                if (V2.getZ() <= z_max) {
                    NewPolygonPoints.add(V2);
                }else{
                    NewPolygonPoints.add(CutZ(z_max,V2,V1));
                }
            }else{
                if (V2.getZ() <= z_max) {
                    NewPolygonPoints.add(CutZ(z_max,V2,V1));
                    NewPolygonPoints.add(V2);
                }
            }
        }



        //System.out.println("NewPolygonPoints2 lenght "+NewPolygonPoints.size());
    }



}




