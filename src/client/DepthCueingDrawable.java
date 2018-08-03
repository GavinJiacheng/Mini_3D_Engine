


package client;


import geometry.Point2D;
import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;


/*
public class DepthCueingDrawable extends DrawableDecorator {
    private static final Color DEFAULT_AMBIENT_COLOR = Color.BLACK;

    private int frontClip;
    private int backClip;

    private Color drawColor;
    private Color ambientColor;

    public DepthCueingDrawable(Drawable delegate, int frontClip, int backClip, Color color) {
        super(delegate);
        this.backClip = backClip;
        this.frontClip = frontClip;
        drawColor = color;
        ambientColor = DEFAULT_AMBIENT_COLOR;
    }

    @Override
    public void setPixel(int x, int y, double z, int argbColor) {
        double ambientCoverage = getAmbientCoverage(z);
        Color color = ambientColor.blendInto(ambientCoverage, drawColor);
        super.setPixel(x, y, z, color.asARGB());
    }

    private double getAmbientCoverage(double z) {
        double z_range = frontClip - backClip;
        assert z_range > 0;
        if(z_range <= 0) {return 0;}

        double zDistance = frontClip - z;
        double result = zDistance / z_range;
        return result;
    }
}
*/



public class DepthCueingDrawable extends DrawableDecorator {

    public double StartZ, EndZ, Distance;
    public Color color,DisColor;

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

    public DepthCueingDrawable(Drawable delegate, double startZ, double endZ, Color color) {
        super(delegate);
        StartZ = startZ;
        EndZ = endZ;
        this.color = color;
        DisColor = colorDistance(Color.BLACK, color);
        Distance = EndZ - StartZ;
    }

    public final double[][] Zbuff = new double[751][751];
    {
        for (int i = 0; i < 751;i++) {
            for (int j = 0; j < 751; j++) {
                Zbuff[i][j] = -200;
            }
        }
    }


    //public double[][] Zbuff = new double[super.getWidth()+1][super.getHeight()+1];

    //private class Zbuff{
    //	public final double[][] Zbuff = new double[751][751];
    //}

    public DepthCueingDrawable(Drawable delegate, Point2D origin, Dimensions size) {
        super(delegate);
    }


    @Override
    public void setPixel(int x, int y, double z, int argbColor) {

         //System.out.println("x,y: "+ (x) +":" + (y));
        //System.out.println("z: "+ z);
        //System.out.println("Zbuff: "+Zbuff[x+300][y+300]);

        if(z >0){
        }
        else if (z >Zbuff[x][y] ) {
//            System.out.println("z: "+ z);
            double dz = EndZ - z;
            double factor = dz/Distance;
            Color NewColor = colorMultiply (DisColor, factor);
            delegate.setPixel(x, y, z, NewColor.asARGB());
        }
    }

    @Override
    public int getPixel(int x, int y) {
        return delegate.getPixel(x, y);
    }
    @Override
    public double getZValue(int x, int y) {
        return delegate.getZValue(x, y);
    }
    @Override
    public int getWidth() {
        return delegate.getWidth();
    }
    @Override
    public int getHeight() {
        return delegate.getHeight();
    }
    @Override
    public void setPixelWithCoverage(int x, int y, double z, int argbColor, double coverage) {
        delegate.setPixelWithCoverage(x, y, z, argbColor, coverage);
    }

    @Override
    public void clear(){
        super.clear();
        for (int i = 0; i < 751; i++) {
            for (int j = 0; j < 751; j++) {
                Zbuff[i][j] = -200;
            }
        }
    }
}
