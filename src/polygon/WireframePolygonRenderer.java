package polygon;

import line.ColorInterpolationDDARender;
import line.LineRenderer;
import windowing.drawable.Drawable;

public class WireframePolygonRenderer implements PolygonRenderer {

    LineRenderer renderer = ColorInterpolationDDARender.make();
    //renderer.drawLine(LEFT, RIGHT, drawable);
    @Override
    public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader){
        Chain leftchain = polygon.leftChain();
        Chain rightchain = polygon.rightChain();
        int len = polygon.length();
        int llen = leftchain.length();
        int rlen = rightchain.length();
        for (int i = 0; i< llen-1; i++){
            renderer.drawLine(leftchain.get(i), leftchain.get(i+1), drawable);
        }
        for (int i = 0; i< rlen-1; i++){
            renderer.drawLine(rightchain.get(i), rightchain.get(i+1), drawable);
        }
    }
    public static WireframePolygonRenderer make(){
        return  new WireframePolygonRenderer();
    }
}
