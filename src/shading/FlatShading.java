package shading;

import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

import java.util.ArrayList;

public class FlatShading implements FaceShader{
    private Polygon retPolygon;
    private double AttenuationA[];
    private double AttenuationB[];
    private double SpecularExp;
    private double SpecularCoefficient;
    private double distance[];

    private int lightListSize;

    private Vertex3D Vertex1;
    private Vertex3D Vertex2;
    private Vertex3D Vertex3;
    private double faceNormal[];
    private double ViewVector[];

    private Color ambient;
    private Color diffuse;

    private ArrayList<Vertex3D> LightLocation;
    private ArrayList<Color> LightIntensity;

    private ArrayList<double[]> reflection;
    private ArrayList<double[]> Lvector;
    private ArrayList<Light> lights;

    public FlatShading(Polygon polygon){
        this.retPolygon = polygon;
    }

    public void addLights(ArrayList<Light> lightList, double SpecularCoefficient, double SpecularExp, Color ObjectColor){
        this.ambient = retPolygon.get(0).getColor();
        this.SpecularCoefficient = SpecularCoefficient;
        this.SpecularExp = SpecularExp;
        this.diffuse = ObjectColor;
        this.lightListSize = lightList.size();
        this.lights = lightList;
        LightIntensity = new ArrayList<Color>();
        AttenuationA = new double[lights.size()];
        AttenuationB = new double[lights.size()];
    }

    @Override
    public Polygon shade(Polygon polygon){
        Polygon temp_polygon = polygon;
        Vertex1 = polygon.get(1);
        Vertex2 = polygon.get(0);
        Vertex3 = polygon.get(2);

        Vertex3D Midpoint = CalcuateMiddlePoint(Vertex1, Vertex2,Vertex3);
        faceNormal = getNormalVector(Vertex1, Vertex2,Vertex3);
        ViewVector = getViewVector(Midpoint);


        Lvector = getLvector(Midpoint);
        reflection = getRvector(Midpoint);
        distance = getDistance(Midpoint);
        retPolygon = getNewPolygon(Vertex1, Vertex2, Vertex3);
        return retPolygon;
    }

    private Polygon getNewPolygon(Vertex3D v1, Vertex3D v2, Vertex3D v3){
        int size = lights.size();
        for(int i = 0; i < size; i++) {
            LightIntensity.add(lights.get(i).LightIntensity);
        }

        double IntensityR[] = new double[size];
        double IntensityG[] = new double[size];
        double IntensityB[] = new double[size];

        for(int i = 0; i < size; i++) {
            IntensityR[i] = LightIntensity.get(i).getR();
            IntensityG[i] = LightIntensity.get(i).getG();
            IntensityB[i] = LightIntensity.get(i).getB();
        }

        for(int i = 0; i < size; i++) {
            AttenuationA[i] = lights.get(i).AttenuationA;
            AttenuationB[i] = lights.get(i).AttenuationB;
        }

        Polygon ret;
        double r = ambient.getR();
        double g = ambient.getG();
        double b = ambient.getB();

        double diffuseR = diffuse.getR();
        double diffuseG = diffuse.getG();
        double diffuseB = diffuse.getB();

        double finalR = calcuateLight(r, IntensityR, diffuseR);
        double finalG = calcuateLight(g, IntensityG, diffuseG);
        double finalB = calcuateLight(b, IntensityB, diffuseB);

        Color newColor = new Color(finalR, finalG, finalB);
        //System.out.println("newR: "+finalR + " newG: " + finalG + " newB: "+finalB);
        v1 = new Vertex3D(v1.getX(), v1.getY(), v1.getZ(), newColor);
        v2 = new Vertex3D(v2.getX(), v2.getY(), v2.getZ(), newColor);
        v3 = new Vertex3D(v3.getX(), v3.getY(), v3.getZ(), newColor);
        ret = Polygon.make(v3, v1, v2);
        return ret;

    }

    private double calcuateLight(double ambient, double[] intensity, double diffuse){
        double ret = 0;
        double AmbientDiffuse = ambient * diffuse;
        int size = lights.size();

        for(int i = 0; i < size; i++) {

            double l_vector[] = Lvector.get(i);
            double ref[] = reflection.get(i);
            double NL = (-1) * faceNormal[0] * l_vector[0] - faceNormal[1] * l_vector[1] - faceNormal[2] * l_vector[2];
            double VR = ViewVector[0] * ref[0] + ViewVector[1] * ref[1] + ViewVector[2] * ref[2];
            double fatti = 1/(AttenuationA[i] + AttenuationB[i] * distance[i]);
            ret += intensity[i] * fatti * (diffuse * NL + SpecularCoefficient * Math.pow(VR, SpecularExp));
        }
        ret += AmbientDiffuse;
        return ret;
    }

    private double[] getDistance(Vertex3D Midpoint){
        double ret[] = new double[lights.size()];
        int size = lights.size();
        for(int i = 0; i < size; i++) {
            double dX = lights.get(i).LightLocation.getX() - Midpoint.getX();
            double dY = lights.get(i).LightLocation.getY() - Midpoint.getY();
            double dZ = lights.get(i).LightLocation.getZ() - Midpoint.getZ();
            double distance = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
            ret[i] = distance;
        }
        return ret;
    }

    private Vertex3D CalcuateMiddlePoint(Vertex3D v1, Vertex3D v2, Vertex3D v3) {
        double X = (v1.getX() + v2.getX() + v3.getX())/3;
        double Y = (v1.getY() + v2.getY() + v3.getY())/3;
        double Z = (v1.getZ() + v2.getZ() + v3.getZ())/3;
        double R = (v1.getColor().getR() + v2.getColor().getR() + v3.getColor().getR())/3;
        double G = (v1.getColor().getG() + v2.getColor().getG() + v3.getColor().getG())/3;
        double B = (v1.getColor().getB() + v2.getColor().getB() + v3.getColor().getB())/3;
        Vertex3D ret = new Vertex3D(X, Y, Z, new Color(R,G,B));
        return ret;
    }

    private double[] getNormalVector(Vertex3D v1, Vertex3D v2, Vertex3D v3) {

        double dx12 = v2.getX() - v1.getX();
        double dy12 = v2.getY() - v1.getY();
        double dz12 = v2.getZ() - v1.getZ();
        double dx13 = v3.getX() - v1.getX();
        double dy13 = v3.getY() - v1.getY();
        double dz13 = v3.getZ() - v1.getZ();

        double X = dy12 * dz13 - dz12 * dy13;
        double Y = dz12 * dx13 - dx12 * dz13;
        double Z = dx12 * dy13 - dy12 * dx13;
        double length = Math.sqrt(X*X + Y*Y + Z*Z);
        double unitX = X/length;
        double unitY = Y/length;
        double unitZ = Z/length;
        double[] ret = new double[3];
        ret[0] = unitX;
        ret[1] = unitY;
        ret[2] = unitZ;
        return ret;
    }

    private double[] getViewVector(Vertex3D Midpoint) {
        double X = Midpoint.getX();
        double Y = Midpoint.getY();
        double Z = Midpoint.getZ();
        double length = Math.sqrt(X*X + Y*Y + Z*Z);
        double unitX = X/length;
        double unitY = Y/length;
        double unitZ = Z/length;
        double[] ret = new double[3];
        ret[0] = unitX;
        ret[1] = unitY;
        ret[2] = unitZ;
        return ret;
    }

    private ArrayList<double[]> getLvector(Vertex3D Midpoint) {
        ArrayList<double[]> ListL = new ArrayList<double[]>();
        int size = lights.size();
        double L[] = new double[3];
        for(int i = 0; i < size; i++) {
            L[0] = lights.get(i).LightLocation.getX() - Midpoint.getX();
            L[1] = lights.get(i).LightLocation.getY() - Midpoint.getY();
            L[2] = lights.get(i).LightLocation.getZ() - Midpoint.getZ();
            double length = Math.sqrt(L[0]*L[0] + L[1]*L[1] + L[2]*L[2]);
            L[0] = L[0]/length;
            L[1] = L[1]/length;
            L[2] = L[2]/length;
            ListL.add(L);
        }
        return ListL;
    }

    private ArrayList<double[]> getRvector(Vertex3D Midpoint) {
        ArrayList<double[]> ListR = new ArrayList<double[]>();
        int size = lights.size();
        double R[] = new double[3];
        for(int i = 0; i < size; i++) {
            double Lv[] = Lvector.get(i);
            double dotProduct = Lv[0] * faceNormal[0] + Lv[1] * faceNormal[1] + Lv[2] * faceNormal[2];
            R[0] = Lv[0] - 2 * dotProduct * faceNormal[0];
            R[1] = Lv[1] - 2 * dotProduct * faceNormal[1];
            R[2] = Lv[2] - 2 * dotProduct * faceNormal[2];
            double R_length = Math.sqrt(R[0]*R[0] + R[1]*R[1] + R[2]*R[2]);
            R[0] = R[0]/R_length;
            R[1] = R[1]/R_length;
            R[2] = R[2]/R_length;
            ListR.add(R);
        }
        return ListR;
    }

}
