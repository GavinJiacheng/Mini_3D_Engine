package shading;

import geometry.Vertex3D;
import windowing.graphics.Color;

public class Light {
    public static Color LightIntensity;
    public static double AttenuationA;
    public static double AttenuationB;
    public static Vertex3D LightLocation;

    public Light(Color light, double a, double b, Vertex3D light_location) {
        this.LightIntensity = light;
        this.AttenuationA = a;
        this.AttenuationB = b;
        this.LightLocation = light_location;
    }
}
