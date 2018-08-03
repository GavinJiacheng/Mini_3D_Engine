package shading;

import geometry.Vertex;
import geometry.Vertex3D;
import line.ColorInterpolationDDARender;
import line.LineRenderer;
import polygon.Chain;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.ArrayList;

public class PhongRender implements PolygonRenderer {

    private boolean hasLights = false;
    private PhongShading shader;
    private Polygon thisPolygon;


    public void addLights(Polygon tri, ArrayList<Light> lightList, double SpecularCoefficient, double SpecularExp, Color ObjectColor) {
        shader = new PhongShading();
        shader.addLights(lightList, SpecularCoefficient, SpecularExp, ObjectColor);
        hasLights = true;
        thisPolygon = tri;
    }


    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader) {
        Chain leftchain = polygon.leftChain();
        Chain rightchain = polygon.rightChain();
        int len = polygon.length();
        int llen = leftchain.length();
        int rlen = rightchain.length();
        assert len == 3;
        if (rlen == llen) {
            if (polygon.get(0).getY() > leftchain.get(0).getY() && polygon.get(1).getY() > leftchain.get(0).getY() && polygon.get(2).getY() > leftchain.get(0).getY()) {
                fillTopTriangle(polygon.get(0), polygon.get(1), polygon.get(1), drawable);
                return;
            } else {
                fillBottomTriangle(polygon.get(0), polygon.get(0), polygon.get(1), drawable);
                return;
            }
        } else {
            double Y, X, Z;
            Vertex3D TOP, MID, BOT;
            Color Midcolor;
            // for perspective:
            double newZ;
            Color newcolor;

            double CX, CY, CZ;
            double N0 =-1, N1=-1, N2=-1;
            if (rlen > llen) {
                Y = rightchain.get(1).getY();
                TOP = rightchain.get(0);
                MID = rightchain.get(1);
                BOT = rightchain.get(2);
                double divide = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                double offset = divide * (BOT.getX() - TOP.getX());
                double offsetZ = divide * (BOT.getZ() - TOP.getZ());
                Color distance = colorDistance(TOP.getColor(), BOT.getColor());
                Color offsetColor = colorMultiply(distance,divide);
                Midcolor = colorPlus(TOP.getColor(),offsetColor);
                X = TOP.getX() + offset;
                Z = TOP.getZ() + offsetZ;
                // for perspective:
                double t = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                newZ =   1 / (   (1/TOP.getZ()) + (1/BOT.getZ() - 1/TOP.getZ()) *t     );

                double newr =  newZ * ((TOP.getColor().getR()/TOP.getZ())*(1-t) + (BOT.getColor().getR()/BOT.getZ())*t);
                double newg =  newZ * ((TOP.getColor().getG()/TOP.getZ())*(1-t) + (BOT.getColor().getG()/BOT.getZ())*t);
                double newb =  newZ * ((TOP.getColor().getB()/TOP.getZ())*(1-t) + (BOT.getColor().getB()/BOT.getZ())*t);
                newcolor = new Color(newr, newg, newb);

                CX = t * (BOT.getCameraPoint().getX() - TOP.getCameraPoint().getX()) + TOP.getCameraPoint().getX();
                CY = t * (BOT.getCameraPoint().getY() - TOP.getCameraPoint().getY()) + TOP.getCameraPoint().getY();
                CZ = t * (BOT.getCameraPoint().getZ() - TOP.getCameraPoint().getZ()) + TOP.getCameraPoint().getZ();

                if (BOT.hasNormal && TOP.hasNormal){
                    N0 = t * (BOT.Normal[0] - TOP.Normal[0]) + TOP.Normal[0];
                    N1 = t * (BOT.Normal[1] - TOP.Normal[1]) + TOP.Normal[1];
                    N2 = t * (BOT.Normal[2] - TOP.Normal[2]) + TOP.Normal[2];
                }
            } else {
                Y = leftchain.get(1).getY();
                TOP = leftchain.get(0);
                MID = leftchain.get(1);
                BOT = leftchain.get(2);
                double divide = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                double offset = divide * (BOT.getX() - TOP.getX());
                double offsetZ = divide * (BOT.getZ() - TOP.getZ());
                Color distance = colorDistance(TOP.getColor(), BOT.getColor());
                Color offsetColor = colorMultiply(distance,divide);
                Midcolor = colorPlus(TOP.getColor(),offsetColor);
                X = TOP.getX() + offset;
                Z = TOP.getZ() + offsetZ;
                // for perspective:
                double t = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                newZ =   1 / (   (1/TOP.getZ()) + (1/BOT.getZ() - 1/TOP.getZ()) *t     );

                double newr =  newZ * ((TOP.getColor().getR()/TOP.getZ())*(1-t) + (BOT.getColor().getR()/BOT.getZ())*t);
                double newg =  newZ * ((TOP.getColor().getG()/TOP.getZ())*(1-t) + (BOT.getColor().getG()/BOT.getZ())*t);
                double newb =  newZ * ((TOP.getColor().getB()/TOP.getZ())*(1-t) + (BOT.getColor().getB()/BOT.getZ())*t);
                newcolor = new Color(newr, newg, newb);

                CX = t * (BOT.getCameraPoint().getX() - TOP.getCameraPoint().getX()) + TOP.getCameraPoint().getX();
                CY = t * (BOT.getCameraPoint().getY() - TOP.getCameraPoint().getY()) + TOP.getCameraPoint().getY();
                CZ = t * (BOT.getCameraPoint().getZ() - TOP.getCameraPoint().getZ()) + TOP.getCameraPoint().getZ();

                if (BOT.hasNormal && TOP.hasNormal){
                    N0 = t * (BOT.Normal[0] - TOP.Normal[0]) + TOP.Normal[0];
                    N1 = t * (BOT.Normal[1] - TOP.Normal[1]) + TOP.Normal[1];
                    N2 = t * (BOT.Normal[2] - TOP.Normal[2]) + TOP.Normal[2];
                }
            }
            //Vertex3D newmid = new Vertex3D(X, Y, Z, Midcolor);
            // for perspective:
            Vertex3D newmid = new Vertex3D(X, Y, newZ, newcolor);
            newmid.setCmaeraPoint(CX,CY,CZ);
            if (N0 >=0 && N1 >= 0 && N2>=0) {
                newmid.setNormal(N0, N1, N2);
            }

            fillTopTriangle(TOP, MID, newmid, drawable);
            fillBottomTriangle(MID, newmid, BOT, drawable);
        }
    }

        private void fillTopTriangle(Vertex3D x1,Vertex3D x2, Vertex3D x3, Drawable drawable) {
//            System.out.println("x1: "+x1.getCameraPoint().getX());
//            System.out.println("x2: "+x2.getCameraPoint().getX());
//            System.out.println("x3: "+x3.getCameraPoint().getX());
            Vertex3D mid1, mid2, TOP;
            if (x1.getY() > x2.getY() && x1.getY() > x3.getY()){
                TOP = x1;
                mid1 = x2;
                mid2 = x3;
            }else if(x2.getY() > x1.getY() && x2.getY() > x3.getY()){
                TOP = x2;
                mid1 = x1;
                mid2 = x3;
            }else{
                TOP = x3;
                mid1 = x1;
                mid2 = x2;
            }
            LineRenderer renderer = ColorInterpolationDDARender.make();
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
            double deltaRZ = TOP.getZ() - RIGHT.getZ();
            double deltaLZ = TOP.getZ() - LEFT.getZ();
            Color deltaCR = colorDistance(RIGHT.getColor(),TOP.getColor());
            Color deltaCL = colorDistance(LEFT.getColor(),TOP.getColor());
            double ml = deltal / deltaY;
            double mr = deltar / deltaY;
            Color mCL = colorDivideBy(deltaCL,deltaY);
            Color mCR = colorDivideBy(deltaCR,deltaY);
            double mlZ = deltaLZ / deltaY;
            double mrZ = deltaRZ / deltaY;
            double lx = LEFT.getX();
            double rx = RIGHT.getX();
            Color CL = LEFT.getColor();
            Color CR = RIGHT.getColor();
            double lz = LEFT.getZ();
            double rz = RIGHT.getZ();
            int end = TOP.getIntY();

            //forget all about z:
            // using perspective-correct:
            double realTOPZ = TOP.getZ();
            double realLEFTZ = LEFT.getZ();
            double realRIGHTZ = RIGHT.getZ();

            drawLine(LEFT, RIGHT, drawable);
            for (int i = LEFT.getIntY()+1; i < end; i++) {
                lx = lx + ml;
                rx = rx + mr;
                lz = lz + mlZ;
                rz = rz + mrZ;

                double rt = (i - RIGHT.getY()) / (TOP.getY() - RIGHT.getY());
                double lt = (i - LEFT.getY()) / (TOP.getY() - LEFT.getY());
                double newRIGHTZ =   1 / (   (1/RIGHT.getZ()) + (1/TOP.getZ() - 1/RIGHT.getZ()) *rt     );
                double newLEFTZ =   1 / (   (1/LEFT.getZ()) + (1/TOP.getZ() - 1/LEFT.getZ()) *lt     );

                if (newLEFTZ >= 0)
                {System.out.println("left larger than 0: " + newLEFTZ);}
                if (newRIGHTZ >= 0)
                {System.out.println("right larger than 0: " + newRIGHTZ);}


                double newRIGHTr =  newRIGHTZ * ((RIGHT.getColor().getR()/RIGHT.getZ())*(1-rt) + (TOP.getColor().getR()/TOP.getZ())*rt);
                double newRIGHTg =  newRIGHTZ * ((RIGHT.getColor().getG()/RIGHT.getZ())*(1-rt) + (TOP.getColor().getG()/TOP.getZ())*rt);
                double newRIGHTb =  newRIGHTZ * ((RIGHT.getColor().getB()/RIGHT.getZ())*(1-rt) + (TOP.getColor().getB()/TOP.getZ())*rt);

                double newLEFTr =  newLEFTZ * ((LEFT.getColor().getR()/LEFT.getZ())*(1-lt) + (TOP.getColor().getR()/TOP.getZ())*lt);
                double newLEFTg =  newLEFTZ * ((LEFT.getColor().getG()/LEFT.getZ())*(1-lt) + (TOP.getColor().getG()/TOP.getZ())*lt);
                double newLEFTb =  newLEFTZ * ((LEFT.getColor().getB()/LEFT.getZ())*(1-lt) + (TOP.getColor().getB()/TOP.getZ())*lt);


                Color newRcolor = new Color(newRIGHTr, newRIGHTg, newRIGHTb);
                Color newLcolor = new Color(newLEFTr, newLEFTg, newLEFTb);

                CL = colorDistance(mCL, CL);
                CR = colorDistance(mCR, CR);
                int irx = (int)Math.round(rx);
                int ilx = (int)Math.round(lx);

                double RCX = rt * (TOP.getCameraPoint().getX() - RIGHT.getCameraPoint().getX()) + RIGHT.getCameraPoint().getX();
                double RCY = rt * (TOP.getCameraPoint().getY() - RIGHT.getCameraPoint().getY()) + RIGHT.getCameraPoint().getY();
                double RCZ = rt * (TOP.getCameraPoint().getZ() - RIGHT.getCameraPoint().getZ()) + RIGHT.getCameraPoint().getZ();
                Vertex3D oldR = RIGHT;
                RIGHT = new Vertex3D(irx, i, newRIGHTZ, newRcolor); // not rz
                RIGHT.setCmaeraPoint(RCX,RCY,RCZ);
                if (TOP.hasNormal && oldR.hasNormal){
                    double N0 = rt * (TOP.Normal[0] - oldR.Normal[0]) + oldR.Normal[0];
                    double N1 = rt * (TOP.Normal[1] - oldR.Normal[1]) + oldR.Normal[1];
                    double N2 = rt * (TOP.Normal[2] - oldR.Normal[2]) + oldR.Normal[2];
                    RIGHT.setNormal(N0, N1, N2);
                }

                double LCX = lt * (TOP.getCameraPoint().getX() - LEFT.getCameraPoint().getX()) + LEFT.getCameraPoint().getX();
                double LCY = lt * (TOP.getCameraPoint().getY() - LEFT.getCameraPoint().getY()) + LEFT.getCameraPoint().getY();
                double LCZ = lt * (TOP.getCameraPoint().getZ() - LEFT.getCameraPoint().getZ()) + LEFT.getCameraPoint().getZ();
                Vertex3D oldL = LEFT;
                LEFT = new Vertex3D(ilx, i, newLEFTZ, newLcolor); // not lz
                LEFT.setCmaeraPoint(LCX,LCY,LCZ);
                if (TOP.hasNormal && oldL.hasNormal){
                    double N0 = lt * (TOP.Normal[0] - oldL.Normal[0]) + oldL.Normal[0];
                    double N1 = lt * (TOP.Normal[1] - oldL.Normal[1]) + oldL.Normal[1];
                    double N2 = lt * (TOP.Normal[2] - oldL.Normal[2]) + oldL.Normal[2];
                    LEFT.setNormal(N0, N1, N2);
                }
                drawLine(LEFT, RIGHT, drawable);
            }

        }


    private void fillBottomTriangle(Vertex3D x1,Vertex3D x2, Vertex3D x3, Drawable drawable) {
        Vertex3D mid1, mid2, BOT;
        if (x1.getY() < x2.getY() && x1.getY() < x3.getY()){
            BOT = x1;
            mid1 = x2;
            mid2 = x3;
        }else if(x2.getY() < x1.getY() && x2.getY() < x3.getY()){
            BOT = x2;
            mid1 = x1;
            mid2 = x3;
        }else{
            BOT = x3;
            mid1 = x1;
            mid2 = x2;
        }

        LineRenderer renderer = ColorInterpolationDDARender.make();
        Vertex3D LEFT, RIGHT;
        if (mid1.getX() < mid2.getX()) {
            LEFT = mid1;
            RIGHT = mid2;
        } else {
            LEFT = mid2;
            RIGHT = mid1;
        }
        double deltaY = LEFT.getY() - BOT.getY();
        double deltal = LEFT.getX() - BOT.getX();
        double deltar = RIGHT.getX() - BOT.getX();
        double deltaRZ = RIGHT.getZ() - BOT.getZ();
        double deltaLZ = LEFT.getZ() - BOT.getZ();
        double ml = deltal / deltaY;
        double mr = deltar / deltaY;
        double lx = BOT.getX();
        double rx = BOT.getX();



        Color deltaCR = colorDistance(RIGHT.getColor(),BOT.getColor());
        Color deltaCL = colorDistance(LEFT.getColor(),BOT.getColor());
        Color mCL = colorDivideBy(deltaCL,deltaY);
        Color mCR = colorDivideBy(deltaCR,deltaY);
        double mlZ = deltaLZ / deltaY;
        double mrZ = deltaRZ / deltaY;
        Color CL = BOT.getColor();
        Color CR = BOT.getColor();
        double lz = BOT.getZ();
        double rz = BOT.getZ();


        int end = RIGHT.getIntY();

        drawLine(BOT, BOT, drawable);
        for (int i = BOT.getIntY()+1; i < end; i++) {

            lx += ml;
            rx += mr;
            lz = lz + mlZ;
            rz = rz + mrZ;

            double rt = (i - RIGHT.getY()) / (BOT.getY() - RIGHT.getY());
            double lt = (i - LEFT.getY()) / (BOT.getY() - LEFT.getY());
            double newRIGHTZ =   1 / (   (1/RIGHT.getZ()) + (1/BOT.getZ() - 1/RIGHT.getZ()) *rt     );
            double newLEFTZ =   1 / (   (1/LEFT.getZ()) + (1/BOT.getZ() - 1/LEFT.getZ()) *lt     );

            if (newLEFTZ >= 0)
            {System.out.println("left larger than 0: " + newLEFTZ);}
            if (newRIGHTZ >= 0)
            {System.out.println("right larger than 0: " + newRIGHTZ);}


            double newRIGHTr =  newRIGHTZ * ((RIGHT.getColor().getR()/RIGHT.getZ())*(1-rt) + (BOT.getColor().getR()/BOT.getZ())*rt);
            double newRIGHTg =  newRIGHTZ * ((RIGHT.getColor().getG()/RIGHT.getZ())*(1-rt) + (BOT.getColor().getG()/BOT.getZ())*rt);
            double newRIGHTb =  newRIGHTZ * ((RIGHT.getColor().getB()/RIGHT.getZ())*(1-rt) + (BOT.getColor().getB()/BOT.getZ())*rt);

            double newLEFTr =  newLEFTZ * ((LEFT.getColor().getR()/LEFT.getZ())*(1-lt) + (BOT.getColor().getR()/BOT.getZ())*lt);
            double newLEFTg =  newLEFTZ * ((LEFT.getColor().getG()/LEFT.getZ())*(1-lt) + (BOT.getColor().getG()/BOT.getZ())*lt);
            double newLEFTb =  newLEFTZ * ((LEFT.getColor().getB()/LEFT.getZ())*(1-lt) + (BOT.getColor().getB()/BOT.getZ())*lt);

            Color newRcolor = new Color(newRIGHTr, newRIGHTg, newRIGHTb);
            Color newLcolor = new Color(newLEFTr, newLEFTg, newLEFTb);


            CL = colorPlus(mCL, CL);
            CR = colorPlus(mCR, CR);
            int irx = (int)Math.round(rx);
            int ilx = (int)Math.round(lx);
            double RCX = rt * (BOT.getCameraPoint().getX() - RIGHT.getCameraPoint().getX()) + RIGHT.getCameraPoint().getX();
            double RCY = rt * (BOT.getCameraPoint().getY() - RIGHT.getCameraPoint().getY()) + RIGHT.getCameraPoint().getY();
            double RCZ = rt * (BOT.getCameraPoint().getZ() - RIGHT.getCameraPoint().getZ()) + RIGHT.getCameraPoint().getZ();
            Vertex3D oldR = RIGHT;
            RIGHT = new Vertex3D(irx, i, newRIGHTZ, newRcolor);
            RIGHT.setCmaeraPoint(RCX,RCY,RCZ);
            if (BOT.hasNormal && oldR.hasNormal){
                double N0 = rt * (BOT.Normal[0] - oldR.Normal[0]) + oldR.Normal[0];
                double N1 = rt * (BOT.Normal[1] - oldR.Normal[1]) + oldR.Normal[1];
                double N2 = rt * (BOT.Normal[2] - oldR.Normal[2]) + oldR.Normal[2];
                RIGHT.setNormal(N0, N1, N2);
            }

            double LCX = lt * (BOT.getCameraPoint().getX() - LEFT.getCameraPoint().getX()) + LEFT.getCameraPoint().getX();
            double LCY = lt * (BOT.getCameraPoint().getY() - LEFT.getCameraPoint().getY()) + LEFT.getCameraPoint().getY();
            double LCZ = lt * (BOT.getCameraPoint().getZ() - LEFT.getCameraPoint().getZ()) + LEFT.getCameraPoint().getZ();
            Vertex3D oldL = LEFT;
            LEFT = new Vertex3D(ilx, i, newLEFTZ, newLcolor);
            LEFT.setCmaeraPoint(LCX,LCY,LCZ);
            if (BOT.hasNormal && oldL.hasNormal){
                double N0 = lt * (BOT.Normal[0] - oldL.Normal[0]) + oldL.Normal[0];
                double N1 = lt * (BOT.Normal[1] - oldL.Normal[1]) + oldL.Normal[1];
                double N2 = lt * (BOT.Normal[2] - oldL.Normal[2]) + oldL.Normal[2];
                LEFT.setNormal(N0, N1, N2);
            }
//            if (ilx < 0){
//                System.out.println(" L: " + lx + "y : "+ i);
//                System.out.println("FF: "+mid2.getX()+ " fL:" + mid1.getX() + " END: " + end);
//            }
            drawLine(LEFT, RIGHT, drawable);
        }
    }

    private void drawLine (Vertex3D p1, Vertex3D p2, Drawable drawable){
        if (p1.getIntX() == p2.getIntX()){
            double z;
            Color color;
            double x, y;
            if (p1.getZ() > p2.getZ()){
                z = p1.getZ();
                x = p1.getX();
                y = p1.getY();
                color = p1.getColor();
            }else {
                z = p2.getZ();
                x = p2.getX();
                y = p2.getY();
                color = p2.getColor();
            }
            if (hasLights){
                Vertex3D newVertex = new Vertex3D(x, y, z, color);
                color = shader.shade(thisPolygon, newVertex);
            }
            drawable.setPixel(p1.getIntX(), p1.getIntY(), z, color.asARGB());
            return;
        }
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

            double t = (x - p1.getX()) / (p2.getX() - p1.getX());
            double newZ =   1 / (   (1/p1.getZ()) + (1/p2.getZ() - 1/p1.getZ()) *t     );

            double newr =  newZ * ((p1.getColor().getR()/p1.getZ())*(1-t) + (p2.getColor().getR()/p2.getZ())*t);
            double newg =  newZ * ((p1.getColor().getG()/p1.getZ())*(1-t) + (p2.getColor().getG()/p2.getZ())*t);
            double newb =  newZ * ((p1.getColor().getB()/p1.getZ())*(1-t) + (p2.getColor().getB()/p2.getZ())*t);
            Color newcolor = new Color(newr, newg, newb);

//            if(hasLights){
//                double oriX = (-x * newZ);
//                double oriY = (-y * newZ);
//                //Vertex3D newVertex = new Vertex3D(oriX, oriY, newZ, newcolor);
//                Vertex3D newVertex = new Vertex3D(oriX, oriY, newZ, newcolor);
//                newcolor = shader.shade(thisPolygon, newVertex);
//            }

            if(hasLights){

                double CX = t * (p2.getCameraPoint().getX() - p1.getCameraPoint().getX()) + p1.getCameraPoint().getX();
                double CY = t * (p2.getCameraPoint().getY() - p1.getCameraPoint().getY()) + p1.getCameraPoint().getY();
                double CZ = t * (p2.getCameraPoint().getZ() - p1.getCameraPoint().getZ()) + p1.getCameraPoint().getZ();
                Vertex3D newVertex = new Vertex3D(CX, CY, CZ, newcolor);
                if (p2.hasNormal && p1.hasNormal){
                    double N0 = t * (p2.Normal[0] - p1.Normal[0]) + p1.Normal[0];
                    double N1 = t * (p2.Normal[1] - p1.Normal[1]) + p1.Normal[1];
                    double N2 = t * (p2.Normal[2] - p1.Normal[2]) + p1.Normal[2];
                    newVertex.setNormal(N0, N1, N2);
                }
                newcolor = shader.shade(thisPolygon, newVertex);
            }
            drawable.setPixel(x, IntY, newZ, newcolor.asARGB());
            CurrentColor = colorPlus(CurrentColor, unitDistance);
            y = y + m;
        }
    }



    // for color calcuate:
    //------------------------------------------
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






    public static PhongRender make(){
        return  new PhongRender();
    }
}
