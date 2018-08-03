package polygon;

import geometry.Vertex3D;
import line.ColorInterpolationDDARender;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class OLDPolygonRenderer implements PolygonRenderer {

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
            if (rlen > llen) {
                Y = rightchain.get(1).getY();
                TOP = rightchain.get(0);
                MID = rightchain.get(1);
                BOT = rightchain.get(2);
                double divide = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                double offset = divide * (BOT.getX() - TOP.getX());
                double offsetZ = divide * (BOT.getZ() - TOP.getZ());
                Color distance = ColorInterpolationDDARender.colorDistance(TOP.getColor(), BOT.getColor());
                Color offsetColor = ColorInterpolationDDARender.colorMultiply(distance,divide);
                Midcolor = ColorInterpolationDDARender.colorPlus(TOP.getColor(),offsetColor);
                X = TOP.getX() + offset;
                Z = TOP.getZ() + offsetZ;
            } else {
                Y = leftchain.get(1).getY();
                TOP = leftchain.get(0);
                MID = leftchain.get(1);
                BOT = leftchain.get(2);
                double divide = (MID.getY() - TOP.getY()) / (BOT.getY() - TOP.getY());
                double offset = divide * (BOT.getX() - TOP.getX());
                double offsetZ = divide * (BOT.getZ() - TOP.getZ());
                Color distance = ColorInterpolationDDARender.colorDistance(TOP.getColor(), BOT.getColor());
                Color offsetColor = ColorInterpolationDDARender.colorMultiply(distance,divide);
                Midcolor = ColorInterpolationDDARender.colorPlus(TOP.getColor(),offsetColor);
                X = TOP.getX() + offset;
                Z = TOP.getZ() + offsetZ;
            }
            Vertex3D newmid = new Vertex3D(X, Y, Z, Midcolor);
                //if (TOP.getZ() != 0){
                //    System.out.println(TOP.getZ());
               // }
            fillTopTriangle(TOP, MID, newmid, drawable);
            fillBottomTriangle(MID, newmid, BOT, drawable);
        }
    }

        private void fillTopTriangle(Vertex3D x1,Vertex3D x2, Vertex3D x3, Drawable drawable) {
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
            Color deltaCR = ColorInterpolationDDARender.colorDistance(RIGHT.getColor(),TOP.getColor());
            Color deltaCL = ColorInterpolationDDARender.colorDistance(LEFT.getColor(),TOP.getColor());
            double ml = deltal / deltaY;
            double mr = deltar / deltaY;
            Color mCL = ColorInterpolationDDARender.colorDivideBy(deltaCL,deltaY);
            Color mCR = ColorInterpolationDDARender.colorDivideBy(deltaCR,deltaY);
            double mlZ = deltaLZ / deltaY;
            double mrZ = deltaRZ / deltaY;
            double lx = TOP.getX();
            double rx = TOP.getX();
            Color CL = TOP.getColor();
            Color CR = TOP.getColor();
            double lz = TOP.getZ();
            double rz = TOP.getZ();
            int end = RIGHT.getIntY();


            for (int i = TOP.getIntY()-1; i >= end; i--) {
                lx = lx - ml;
                rx = rx - mr;
                lz = lz - mlZ;
                rz = rz - mrZ;
                CL = ColorInterpolationDDARender.colorDistance(mCL, CL);
                CR = ColorInterpolationDDARender.colorDistance(mCR, CR);
                double irx = Math.floor(rx);
                double ilx = Math.ceil(lx);
                if (lx == ilx){
                    ilx ++;
                }
                LEFT = new Vertex3D(ilx, i, lz, CL);
                RIGHT = new Vertex3D(irx, i, rz, CR);
//                if (ilx < 0){
//                    System.out.println(" L: " + lx + "y : "+ i);
//                    System.out.println("FF: "+mid2.getX()+ " fL:" + mid1.getX() + " END: " + end);
//                }
                renderer.drawLine(LEFT, RIGHT, drawable);
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
        double deltaY = BOT.getY() - LEFT.getY();
        double deltal = BOT.getX() - LEFT.getX();
        double deltar = BOT.getX() - RIGHT.getX();
        double deltaRZ = BOT.getZ() - RIGHT.getZ();
        double deltaLZ = BOT.getZ() - LEFT.getZ();
        double ml = deltal / deltaY;
        double mr = deltar / deltaY;
        double lx = BOT.getX();
        double rx = BOT.getX();

        Color deltaCR = ColorInterpolationDDARender.colorDistance(RIGHT.getColor(),BOT.getColor());
        Color deltaCL = ColorInterpolationDDARender.colorDistance(LEFT.getColor(),BOT.getColor());
        Color mCL = ColorInterpolationDDARender.colorDivideBy(deltaCL,deltaY);
        Color mCR = ColorInterpolationDDARender.colorDivideBy(deltaCR,deltaY);
        double mlZ = deltaLZ / deltaY;
        double mrZ = deltaRZ / deltaY;
        Color CL = BOT.getColor();
        Color CR = BOT.getColor();
        double lz = BOT.getZ();
        double rz = BOT.getZ();


        int end = RIGHT.getIntY();

        for (int i = BOT.getIntY()+1; i < end; i++) {

            lx += ml;
            rx += mr;
            lz = lz + mlZ;
            rz = rz + mrZ;
            CL = ColorInterpolationDDARender.colorPlus(mCL, CL);
            CR = ColorInterpolationDDARender.colorPlus(mCR, CR);
            double irx = Math.floor(rx);
            double ilx = Math.ceil(lx);
            if (lx == ilx){
                ilx ++;
            }
            LEFT = new Vertex3D(ilx, i, lz, CL);
            RIGHT = new Vertex3D(irx, i, rz, CR);
//            if (ilx < 0){
//                System.out.println(" L: " + lx + "y : "+ i);
//                System.out.println("FF: "+mid2.getX()+ " fL:" + mid1.getX() + " END: " + end);
//            }
            renderer.drawLine(LEFT, RIGHT, drawable);
        }
    }






    public static OLDPolygonRenderer make(){
        return  new OLDPolygonRenderer();
    }
}
