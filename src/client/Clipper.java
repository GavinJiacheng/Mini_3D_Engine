
package client;

import geometry.Vertex;
import geometry.Vertex3D;
import line.ColorInterpolationDDARender;
import polygon.Polygon;
import windowing.graphics.Color;

import java.util.ArrayList;

public class Clipper {

    private double z_max = 0;
    private double z_min = 0;
    private double x_min = 0;
    private double y_min = 0;
    private double x_max = 0;
    private double y_max = 0;

    private Vertex3D v1,v2,v3;




    public ArrayList<Vertex3D> NewPolygonPoints = new ArrayList<Vertex3D>();
    public ArrayList<Vertex3D> storearray = new ArrayList<Vertex3D>();


    public Clipper(double x_min, double y_min, double z_min, double x_max, double y_max, double z_max, ArrayList<Vertex3D> Vertexs){
        this.x_max = x_max;
        this.x_min = x_min;
        this.y_max = y_max;
        this.y_min = y_min;
        this.z_min = z_min;
        this.z_max = z_max;
        storearray = Vertexs;
        CutNewPoints(storearray);
        //System.out.println("y:"+v3.getX()+","+v3.getY());
        //System.out.println("storearray lenght "+storearray.size());
        //System.out.println("x1 "+v1.getX()+"y: "+v1.getY());
        //System.out.println("x2 "+v2.getX()+"y: "+v2.getY());
        //System.out.println("x3 "+v3.getX()+"y: "+v3.getY());
//        CutNewPoints(storearray);
        //Check_if_Piercing_camera();
        //System.out.println("array lenght "+NewPolygonPoints.size());
    }
//
//    private void Check_if_Piercing_camera(){
//        ArrayList<Vertex3D> points_in_camera_screen = new ArrayList<Vertex3D>();
//        for (Vertex3D visitor : NewPolygonPoints){
//            if (visitor.getZ() == z_max){
//                points_in_camera_screen.add(visitor);
//            }
//        }
//        if (points_in_camera_screen.size() >= 1){
//            for (Vertex3D visitor : points_in_camera_screen){
//                if (visitor.getX() > x_min && visitor.getX() < x_max && visitor.getY() > y_min && visitor.getY() <y_max ){
//                    NewPolygonPoints.clear();
//                }
//            }
//        }
//    }

    private Vertex3D CutHorizontal (double y, Vertex3D V1, Vertex3D V2){
        Vertex3D bottom, top;
        if (V1.getY() < V2.getY()){
            bottom = V1;
            top = V2;
        }else{
            bottom = V2;
            top = V1;
        }

        double dy = top.getY()-bottom.getY();
        double dx = top.getX()-bottom.getX();
        double dz = top.getZ()-bottom.getZ();


        Color dcolor = ColorInterpolationDDARender.colorDistance(bottom.getColor(),top.getColor());
        Color dcolorY = ColorInterpolationDDARender.colorDivideBy(dcolor, dy);

        double dxy = dx /dy;
        double dzy = dz /dy;
        double t = (y - V1.getY()) / (V2.getY() - V1.getY());
        double newZ =   1 / (   (1/V1.getZ()) + (1/V2.getZ() - 1/V1.getZ()) *t     );

        double newr =  newZ * ((V1.getColor().getR()/V1.getZ())*(1-t) + (V2.getColor().getR()/V2.getZ())*t);
        double newg =  newZ * ((V1.getColor().getG()/V1.getZ())*(1-t) + (V2.getColor().getG()/V2.getZ())*t);
        double newb =  newZ * ((V1.getColor().getB()/V1.getZ())*(1-t) + (V2.getColor().getB()/V2.getZ())*t);
        Color newcolor = new Color(newr, newg, newb);

        //double newZ =  y/ (bottom.getY() + (top.getY() - bottom.getY()) * t);

        double newX = bottom.getX() + dxy * (y-bottom.getY());
        //double newZ = bottom.getZ() + dzy * (y-bottom.getY());
        Color newC = ColorInterpolationDDARender.colorPlus( bottom.getColor() , ColorInterpolationDDARender.colorMultiply(dcolorY,(y-bottom.getY())));

        Vertex3D ret = new Vertex3D(newX,y,newZ, newcolor);

        if (V2.hasNormal && V1.hasNormal){
            double N0 = t * (V2.Normal[0] - V1.Normal[0]) + V1.Normal[0];
            double N1 = t * (V2.Normal[1] - V1.Normal[1]) + V1.Normal[1];
            double N2 = t * (V2.Normal[2] - V1.Normal[2]) + V1.Normal[2];
            ret.setNormal(N0,N1,N2);
        }
        double CX = t * (V2.getCameraPoint().getX() - V1.getCameraPoint().getX()) + V1.getCameraPoint().getX();
        double CY = t * (V2.getCameraPoint().getY() - V1.getCameraPoint().getY()) + V1.getCameraPoint().getY();
        double CZ = t * (V2.getCameraPoint().getZ() - V1.getCameraPoint().getZ()) + V1.getCameraPoint().getZ();
        ret.setCmaeraPoint(CX,CY,CZ);

        return ret;

    }

    private Vertex3D CutVertical (double x, Vertex3D V1, Vertex3D V2){
        Vertex3D left, right;
        if (V1.getX() < V2.getX()){
            left = V1;
            right = V2;
        }else{
            left = V2;
            right = V1;
        }

        double dy = right.getY()-left.getY();
        double dx = right.getX()-left.getX();
        double dz = right.getZ()-left.getZ();

        Color dcolor = ColorInterpolationDDARender.colorDistance(left.getColor(),right.getColor());
        Color dcolorX = ColorInterpolationDDARender.colorDivideBy(dcolor, dx);
        Color newC = ColorInterpolationDDARender.colorPlus( left.getColor() , ColorInterpolationDDARender.colorMultiply(dcolorX,(x-left.getX())));

        double t = (x - V1.getX()) / (V2.getX() - V1.getX());
        double newZ =   1 / (   (1/V1.getZ()) + (1/V2.getZ() - 1/V1.getZ()) *t     );
        //double newZ =   x / (left.getX() + (right.getX() - left.getX()) * t);


        double newr =  newZ * ((V1.getColor().getR()/V1.getZ())*(1-t) + (V2.getColor().getR()/V2.getZ())*t);
        double newg =  newZ * ((V1.getColor().getG()/V1.getZ())*(1-t) + (V2.getColor().getG()/V2.getZ())*t);
        double newb =  newZ * ((V1.getColor().getB()/V1.getZ())*(1-t) + (V2.getColor().getB()/V2.getZ())*t);
        Color newcolor = new Color(newr, newg, newb);


        double dyx = dy /dx;
        double dzx = dz /dx;
        double newY = left.getY() + dyx * (x-left.getX());
        //double newZ = left.getZ() + dzx * (x-left.getX());

        Vertex3D ret = new Vertex3D(x,newY,newZ, newcolor);
        if (V2.hasNormal && V1.hasNormal){
            double N0 = t * (V2.Normal[0] - V1.Normal[0]) + V1.Normal[0];
            double N1 = t * (V2.Normal[1] - V1.Normal[1]) + V1.Normal[1];
            double N2 = t * (V2.Normal[2] - V1.Normal[2]) + V1.Normal[2];
            ret.setNormal(N0,N1,N2);
        }
        double CX = t * (V2.getCameraPoint().getX() - V1.getCameraPoint().getX()) + V1.getCameraPoint().getX();
        double CY = t * (V2.getCameraPoint().getY() - V1.getCameraPoint().getY()) + V1.getCameraPoint().getY();
        double CZ = t * (V2.getCameraPoint().getZ() - V1.getCameraPoint().getZ()) + V1.getCameraPoint().getZ();
        ret.setCmaeraPoint(CX,CY,CZ);
        return ret;

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
        for (int i =0;i<arraylist.size();i++){
            Vertex3D V1 = arraylist.get(i);
            Vertex3D V2;
            if (i != arraylist.size()-1) {
                V2 = arraylist.get(i + 1);
            }else{
                V2 = arraylist.get(0);
            }
            if (V1.getY() <= y_max) {
                if (V2.getY() <= y_max) {
                    PolygonPoints.add(V2);
                }else {
                    PolygonPoints.add(CutHorizontal(y_max,V2,V1));
                }
            }else{
                if (V2.getY() <= y_max) {
                    PolygonPoints.add(CutHorizontal(y_max,V2,V1));
                    PolygonPoints.add(V2);
                }
            }
        }
        //System.out.println("PolygonPoints1 lenght "+PolygonPoints.size());

        // for left
        NewPolygonPoints.clear();
        for (int i =0;i<PolygonPoints.size();i++){
            Vertex3D V1 = PolygonPoints.get(i);
            Vertex3D V2;
            if (i != PolygonPoints.size()-1) {
                V2 = PolygonPoints.get(i + 1);
            }else{
                V2 = PolygonPoints.get(0);
            }
            if (V1.getX() >= x_min) {
                if (V2.getX() >= x_min) {
                    NewPolygonPoints.add(V2);
                }else{
                    NewPolygonPoints.add(CutVertical(x_min,V2,V1));
                }
            }else{
                if (V2.getX() >= x_min) {
                    NewPolygonPoints.add(CutVertical(x_min,V2,V1));
                    NewPolygonPoints.add(V2);
                }
            }
        }
        //System.out.println("NewPolygonPoints1 lenght "+NewPolygonPoints.size());

        // for bottom
        PolygonPoints.clear();
        for (int i =0;i<NewPolygonPoints.size();i++){
            Vertex3D V1 = NewPolygonPoints.get(i);
            Vertex3D V2;
            if (i != NewPolygonPoints.size()-1) {
                V2 = NewPolygonPoints.get(i + 1);
            }else{
                V2 = NewPolygonPoints.get(0);
            }
            if (V1.getY() >= y_min) {
                if (V2.getY() >= y_min) {
                    PolygonPoints.add(V2);
                }else{
                    PolygonPoints.add(CutHorizontal(y_min,V2,V1));
                }
            }else{
                if (V2.getY() >= y_min) {
                    PolygonPoints.add(CutHorizontal(y_min,V2,V1));
                    PolygonPoints.add(V2);
                }
            }
        }

       // System.out.println("PolygonPoints2 lenght "+PolygonPoints.size());
        // for right
        NewPolygonPoints.clear();
        for (int i =0;i<PolygonPoints.size();i++){
            Vertex3D V1 = PolygonPoints.get(i);
            Vertex3D V2;
            if (i != PolygonPoints.size()-1) {
                V2 = PolygonPoints.get(i + 1);
            }else{
                V2 = PolygonPoints.get(0);
            }
            if (V1.getX() <= x_max) {
                if (V2.getX() <= x_max) {
                    NewPolygonPoints.add(V2);
                }else{
                    NewPolygonPoints.add(CutVertical(x_max,V2,V1));
                }
            }else{
                if (V2.getX() <= x_max) {
                    NewPolygonPoints.add(CutVertical(x_max,V2,V1));
                    NewPolygonPoints.add(V2);
                }
            }
        }

        //System.out.println("size: "+ NewPolygonPoints.size());

//        PolygonPoints.clear();
//        for (int i =0;i<NewPolygonPoints.size();i++){
//            Vertex3D V1 = NewPolygonPoints.get(i);
//            Vertex3D V2;
//            if (i != NewPolygonPoints.size()-1) {
//                V2 = NewPolygonPoints.get(i + 1);
//            }else{
//                V2 = NewPolygonPoints.get(0);
//            }
//            if (V1.getZ() >= z_min) {
//                if (V2.getZ() >= z_min) {
//                    PolygonPoints.add(V2);
//                }else{
//                    PolygonPoints.add(CutZ(z_min,V2,V1));
//                }
//            }else{
//                if (V2.getZ() >= z_min) {
//                    PolygonPoints.add(CutZ(z_min,V2,V1));
//                    PolygonPoints.add(V2);
//                }
//            }
//        }
//
//        NewPolygonPoints.clear();
//        for (int i =0;i<PolygonPoints.size();i++){
//            Vertex3D V1 = PolygonPoints.get(i);
//            Vertex3D V2;
//            if (i != PolygonPoints.size()-1) {
//                V2 = PolygonPoints.get(i + 1);
//            }else{
//                V2 = PolygonPoints.get(0);
//            }
//            if (V1.getZ() <= z_max) {
//                if (V2.getZ() <= z_max) {
//                    NewPolygonPoints.add(V2);
//                }else{
//                    NewPolygonPoints.add(CutZ(z_max,V2,V1));
//                }
//            }else{
//                if (V2.getZ() <= z_max) {
//                    NewPolygonPoints.add(CutZ(z_max,V2,V1));
//                    NewPolygonPoints.add(V2);
//                }
//            }
//        }



        //System.out.println("NewPolygonPoints2 lenght "+NewPolygonPoints.size());
    }



}




