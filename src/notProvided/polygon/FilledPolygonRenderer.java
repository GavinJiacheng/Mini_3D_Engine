package notProvided.polygon;

import geometry.Vertex3D;
import line.AnyOctantLineRenderer;
import line.LineRenderer;
import notProvided.line.BresenhamLineRenderer;
import polygon.Chain;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.ArrayList;
import java.util.List;


public class FilledPolygonRenderer implements PolygonRenderer {

    private int t = 0;
    protected List<Vertex3D> Lpoints = new ArrayList<Vertex3D>();
    protected List<Vertex3D> Rpoints = new ArrayList<Vertex3D>();
    int hl=0;
    int hr=0;
    private FilledPolygonRenderer() {}


    /*
     * (non-Javadoc)
     * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
     *
     * @pre: p2.x >= p1.x && p2.y >= p1.y
     */
    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader){
        Vertex3D vertices[] = new Vertex3D[3];
        int l = polygon.length();
        for(int i =0;i<l;i++)
            vertices[i] = polygon.get(i);
        int argbcolor = vertices[0].getColor().asARGB();
        Vertex3D highone, midone, lowone;
       // Vertex3D []leftvertices = new Vertex3D[leftlength];
        //Vertex3D []rightvertices = new Vertex3D[rightlength];

        if (vertices[0].getIntY()<vertices[1].getIntY()) {
            if (vertices[0].getIntY() < vertices[2].getIntY()){
                lowone = vertices[0];
                if (vertices[1].getIntY()<vertices[2].getIntY()){
                    midone=vertices[1];
                    highone=vertices[2];
                }
                else {
                    midone=vertices[2];
                    highone=vertices[1];
                }
            }
            else{
                midone = vertices[0];
                highone = vertices[1];
                lowone = vertices[2];
            }
        }
        else {
            if (vertices[0].getIntY() > vertices[2].getIntY()) {
                highone = vertices[0];
                if (vertices[2].getIntY() < vertices[1].getIntY()) {
                    lowone = vertices[2];
                    midone = vertices[1];
                } else {
                    lowone = vertices[1];
                    midone = vertices[2];
                }
            } else {
                midone = vertices[0];
                lowone = vertices[1];
                highone = vertices[2];
            }
        }

        if (midone.getIntY() == highone.getIntY())
        {
            fillBottomFlatTriangle(lowone, midone, highone, drawable);
        }
        else if (lowone.getIntY() == midone.getIntY())
        {
            fillTopFlatTriangle(lowone, midone, highone, drawable);
        }
        else
        {
            double newx= (lowone.getX() + ((float)(midone.getY() - lowone.getY()) / (float)(highone.getY() - lowone.getY())) * (highone.getX() - lowone.getX()));
            Vertex3D newone = new Vertex3D(newx, midone.getY(),0,vertices[0].getColor());
            fillBottomFlatTriangle(lowone, midone, newone, drawable);
            fillTopFlatTriangle(midone, newone, highone, drawable);
        }

        for(int i = 0; i<hl-1; i++){
            int y = Lpoints.get(i).getIntY();
            for (int j = Lpoints.get(i).getIntX();j<Rpoints.get(i).getIntX();j++){
                drawable.setPixel(j,y,0,argbcolor);
            }
        }
    }



    private void fillTopFlatTriangle(Vertex3D p1, Vertex3D p2, Vertex3D p3, Drawable drawable)
    {


        double slope1 = (p3.getX() - p1.getX()) / (p3.getY() - p1.getY());
        double slope2 = (p3.getX() - p2.getX()) / (p3.getY() - p2.getY());
        Color color = p1.getColor();

        double x1 = p3.getX();
        double x2 = p3.getX();

        Vertex3D newpoint1, newpoint2;
        for (int h = p3.getIntY(); h > p1.getIntY(); h--)
        {
            newpoint1 = new Vertex3D(x1,h,0,color);
            newpoint2 = new Vertex3D(x2,h,0,color);
            Bresenham (newpoint1, newpoint2,drawable);
            x1 -= slope1;
            x2 -= slope2;
        }
    }


    private void fillBottomFlatTriangle(Vertex3D p1, Vertex3D p2, Vertex3D p3, Drawable drawable)
    {
        double slope1 = (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
        double slope2 = (p3.getX() - p1.getX()) / (p3.getY() - p1.getY());
        Color color = p1.getColor();

        double x1 = p1.getX();
        double x2 = p1.getX();

        Vertex3D newpoint1, newpoint2;
        for (int h = p1.getIntY(); h <= p2.getIntY(); h++)
        {
            newpoint1 = new Vertex3D(x1,h,0,color);
            newpoint2 = new Vertex3D(x2,h,0,color);
            Bresenham (newpoint1, newpoint2,drawable);
            x1 += slope1;
            x2 += slope2;
        }
    }



    public void Bresenham(Vertex3D p1, Vertex3D p2, Drawable drawable) {

        int x1 = p1.getIntX();
        int x2 = p2.getIntX();
        int y1 = p1.getIntY();
        int y2 = p2.getIntY();
        int deltaX = Math.abs(p2.getIntX() - p1.getIntX());
        int deltaY = Math.abs(p2.getIntY() - p1.getIntY());
        int argbColor = p1.getColor().asARGB();

        if ((x1 == x2) && (y1 == y2)) {
            drawable.setPixel(x1, y1, 0.0, argbColor);
        } else {
            int dfiiXY = deltaX - deltaY;
            int posun_x, posun_y;
            if (x1 < x2) posun_x = 1;
            else posun_x = -1;
            if (y1 < y2) posun_y = 1;
            else posun_y = -1;
            while ((x1 != x2) || (y1 != y2)) {
                int p = 2 * dfiiXY;
                if (p > -deltaY) {
                    dfiiXY = dfiiXY - deltaY;
                    x1 = x1 + posun_x;
                }
                if (p < deltaX) {
                    dfiiXY = dfiiXY + deltaX;
                    y1 = y1 + posun_y;
                }
                drawable.setPixel(x1, y1, 0.0, argbColor);
            }
        }
    }

    /*
    public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader){
        Chain leftchain = polygon.leftChain();
        Chain rightchain = polygon.rightChain();
        int leftlength = leftchain.length();
        int rightlength = rightchain.length();
        Vertex3D leftvertices[] = new Vertex3D[leftlength];
        Vertex3D rightvertices[] = new Vertex3D[rightlength];
        for (int i=0; i<leftlength;i++) {
            leftvertices[i] =  leftchain.get(i);
        }
        for (int i=0; i<rightlength;i++) {
            rightvertices[i] =  rightchain.get(i);
        }
        int height;
        int y1 = leftvertices[0].getIntY();
        int y2 = leftvertices[leftlength-1].getIntY();
        if (y1<y2){
            height = y2-y1;
        }
        else{
            height = y1-y2;
        }
        Vertex3D leftpoints[] = new Vertex3D[height];
        Vertex3D rightpoints[] = new Vertex3D[height];
        for (int i =0; i<leftlength-1; i++){
            Bresenham(leftvertices[i],leftvertices[i+1],  leftpoints);}
        t = 0;
        for (int i =0; i<rightlength-1; i++)
            Bresenham(rightvertices[i],rightvertices[i+1],  rightpoints);
        int argbColor = leftpoints[0].getColor().asARGB();
        for (int i =0; i<t; i++){
            int left=leftpoints[i].getIntX();
            int right=rightpoints[i].getIntX();
            int h=leftpoints[i].getIntY();
            for (int j = left;j<right;j++){
                drawable.setPixel(j, h, 0.0, argbColor);
            }
        }
    }

    private void Bresenham(Vertex3D p1, Vertex3D p2, Vertex3D[] points) {

        int x1 = p1.getIntX();
        int x2 = p2.getIntX();
        int y1 = p1.getIntY();
        int y2 = p2.getIntY();
        int deltaX = Math.abs(p2.getIntX() - p1.getIntX());
        int deltaY = Math.abs(p2.getIntY() - p1.getIntY());
        Color color =p1.getColor();

        if ((x1 == x2) && (y1 == y2)) {
            points[t]= returnvertex(x1,y1,color);//drawable.setPixel(x1, y1, 0.0, argbColor);
        } else {
            int dfiiXY = deltaX - deltaY;
            int posun_x, posun_y;
            if (x1 < x2) posun_x = 1;
            else posun_x = -1;
            if (y1 < y2) posun_y = 1;
            else posun_y = -1;
            while ((x1 != x2) || (y1 != y2)) {
                int p = 2 * dfiiXY;
                if (p > -deltaY) {
                    dfiiXY = dfiiXY - deltaY;
                    x1 = x1 + posun_x;
                }
                if (p < deltaX) {
                    dfiiXY = dfiiXY + deltaX;
                    y1 = y1 + posun_y;
                }
                if (y1 != y1 - posun_y) {
                    points[t] = returnvertex(x1, y1, color);
                    if (t > 0) {
                        if (points[t].getIntY() != points[t - 1].getIntY()) {
                            t++;
                        }
                    }
                }
            }
        }
    }
*/

    private Vertex3D returnvertex(int x, int y, Color color){
        double X = (double)x;
        double Y = (double)y;
        return new Vertex3D(X, Y, 0, color);
    }

    public static PolygonRenderer make() {
        return new FilledPolygonRenderer();
    }

}
