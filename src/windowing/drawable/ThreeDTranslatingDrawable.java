package windowing.drawable;

import geometry.Point2D;
import windowing.graphics.Dimensions;

public class ThreeDTranslatingDrawable extends DrawableDecorator {
	public double[][] Zbuff = new double[751][751];


	//public double[][] Zbuff = new double[super.getWidth()+1][super.getHeight()+1];

	//private class Zbuff{
	//	public final double[][] Zbuff = new double[751][751];
	//}

	private final Point2D origin;
	private final Dimensions size;

	public ThreeDTranslatingDrawable(Drawable delegate, Point2D origin, Dimensions size) {
		super(delegate);
		for (int i = 0; i < 751; i++) {
			for (int j = 0; j < 751; j++) {
				Zbuff[i][j] = -200;
			}
		}
		this.origin = origin;
		this.size = size;
	}

	
	public int transformedX(int x) {
		return x + origin.getIntX();
	}	
	public int transformedY(int y) {
		return y + origin.getIntY();
	}

	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		//System.out.println("FUCKING x and y: " +x + " , "+y);
		if(x < 0 || x> 750){
			System.out.println("Wrong x: " +x);
		}
		else if(y < 0 || y> 750){
			System.out.println("Wrong y: " +y);
		}
		else if(z >=0){
			delegate.setPixel(transformedX(x), transformedY(y), z, argbColor);
		}
		else if (z >Zbuff[x][y] ) {
			Zbuff[x][y] = z;
			delegate.setPixel(transformedX(x), transformedY(y), z, argbColor);
		}
	}
	@Override
	public int getPixel(int x, int y) {
		return delegate.getPixel(transformedX(x), transformedY(y));
	}
	@Override
	public double getZValue(int x, int y) {
		return delegate.getZValue(transformedX(x), transformedY(y));
	}
	@Override
	public int getWidth() {
		return size.getWidth();
	}
	@Override
	public int getHeight() {
		return size.getHeight();
	}
	@Override
	public void setPixelWithCoverage(int x, int y, double z, int argbColor, double coverage) {
		delegate.setPixelWithCoverage(transformedX(x), transformedY(y), z, argbColor, coverage);
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
