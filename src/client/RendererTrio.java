package client;
import line.ColorInterpolationDDARender;
import polygon.PolygonRenderer;
import line.BresenhamLineRenderer;
import polygon.*;
import line.LineRenderer;

public class RendererTrio {

    private LineRenderer lineRenderer;
    private PolygonRenderer filledRenderer;
    private PolygonRenderer wireFrameRenderer;


    public RendererTrio() {
        this.lineRenderer = ColorInterpolationDDARender.make();
        this.filledRenderer = FilledPolygonRenderer.make();
        this.wireFrameRenderer = WireframePolygonRenderer.make();
    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public PolygonRenderer getFilledRenderer() {
        return filledRenderer;
    }

    public PolygonRenderer getWireframeRenderer() {
        return wireFrameRenderer;
    }
}