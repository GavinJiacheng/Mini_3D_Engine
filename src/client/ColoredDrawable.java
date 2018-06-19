package client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;

public class ColoredDrawable  extends DrawableDecorator {

    int argbcolor;

    public ColoredDrawable(Drawable delegate, int argb) {
        super(delegate);
        argbcolor = argb;
    }

    @Override
    public void clear() {
        fill(argbcolor, 0);
    }



}
