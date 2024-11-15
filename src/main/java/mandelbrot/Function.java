package mandelbrot;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

@FunctionalInterface
public interface Function {
    public Vector2D apply(double cx, double cy, double zx, double zy);
}