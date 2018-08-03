package shading;

import geometry.Vertex;
import geometry.Vertex3D;
import javafx.scene.layout.Background;
import polygon.Polygon;
import windowing.graphics.Color;

import java.util.ArrayList;

public class GouraudShading implements VertexShader{

    private Polygon retPolygon;
    private Vertex3D retVertex;
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

    public GouraudShading(){
        //this.retPolygon = polygon;
    }

    public void addLights(ArrayList<Light> lightList, double SpecularCoefficient, double SpecularExp, Color ObjectColor){
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
    public Vertex3D shade(Polygon polygon, Vertex3D vertex) {
        retVertex = vertex;
        this.ambient = vertex.getColor();
        Vertex3D v1  = polygon.get(1);
        Vertex3D v2  = polygon.get(0);
        Vertex3D v3  = polygon.get(2);
        distance = getDistance(vertex);
        faceNormal = getNormalVector(v1, v2, v3, vertex);
        ViewVector = getViewVector(vertex);
        Lvector = getLvector(vertex);
        reflection = getRvector(vertex);
        retVertex = getNewVertex(vertex);
        retVertex.setNormal(faceNormal[0], faceNormal[1], faceNormal[2]);
        return retVertex;
    }

    private Vertex3D getNewVertex(Vertex3D vertex) {
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
        Vertex3D ret = new Vertex3D(vertex.getX(), vertex.getY(), vertex.getZ(), newColor);
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

    private double[] getDistance(Vertex3D vertex) {
        int size = lights.size();
        double distance[] = new double[size];
        for(int i = 0; i < size; i++) {
            double dx = lights.get(i).LightLocation.getX() - vertex.getX();
            double dy = lights.get(i).LightLocation.getY() - vertex.getY();
            double dz = lights.get(i).LightLocation.getZ() - vertex.getZ();
            distance[i] = Math.sqrt(dx*dx + dy*dy + dz*dz);;
        }
        return distance;
    }

    private double[] getNormalVector(Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D vertex) {
        if (vertex.hasNormal){
            double[] ret = vertex.Normal;
            double length = Math.sqrt(ret[0]*ret[0] + ret[1]*ret[1] + ret[2]*ret[2]);
            ret[0] = ret[0]/length;
            ret[1] = ret[1]/length;
            ret[2] = ret[2]/length;
            return ret;
        }
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
