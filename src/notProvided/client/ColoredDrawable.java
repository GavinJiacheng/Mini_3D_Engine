package notProvided.client;

import geometry.Point2D;
import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;

public class ColoredDrawable extends DrawableDecorator {
    public ColoredDrawable (Drawable delegate, int argbcolor) {
        super(delegate);
        this.fill(argbcolor,0);
        delegate.fill(argbcolor,0);
    }
    @Override
    public void fill(int argbColor, double z) {
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                delegate.setPixel(x, y, z, argbColor);
            }
        }
    }

    @Override
    public void setPixel(int x, int y, double z, int argbColor) {
        delegate.setPixel(x, y, z, argbColor);
    }
    @Override
    public int getWidth() {
        return delegate.getWidth();
    }
    @Override
    public int getHeight() {
        return delegate.getHeight();
    }
}
