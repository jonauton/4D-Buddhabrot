package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class FunctionLibrary {
    // Mandelbrot: z -> z^2 + c
    public static class Mandelbrot implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(zx*zx - zy*zy + cx, 2.0d*zx*zy + cy);
        }
    }

    // Logistic: z -> c*z*(1-z)
    public static class Logistic implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            double a = zx - zx*zx + zy*zy;
            double b = zy - 2*zx*zy;
            return new Vector2D(cx*a - cy*b, cx*b + cy*a);
        }
    }

    // MandelbrotInv: z -> z^2 + 1/c
    public static class MandelbrotInv implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(zx*zx - zy*zy + cx/(cx*cx + cy*cy), 2.0d*zx*zy - cy/(cx*cx + cy*cy));
        }
    }

    // Tricorn: z -> conj(z)^2 + c
    public static class Tricorn implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(zx*zx - zy*zy + cx, -2.0d*zx*zy + cy);
        }
    }

    // Burning Ship: z -> (|Re(z)| + i*|Im(z)|)^2 + c
    public static class BurningShip implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(zx*zx - zy*zy + cx, 2.0d*Math.abs(zx*zy) + cy);
        }
    }

    // Multibrot3: z -> z^3 + c
    public static class Multibrot3 implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(zx*zx*zx - 3.0d*zx*zy*zy + cx, 3.0d*zx*zx*zy - zy*zy*zy + cy);
        }
    }

    // Multibrot-1: z -> z^-1 + c
    public static class MultibrotNeg1 implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            double d = zx*zx + zy*zy;
            return d != 0 ? new Vector2D((zx/d) + cx, (-zy/d) + cy) : new Vector2D(1000000.0d, 1000000.0d);
        }
    }

    // Multibrot-2: z -> z^-2 + c
    public static class MultibrotNeg2 implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            double xSqr = zx*zx;
            double ySqr = zy*zy;
            double d = xSqr*xSqr + 2.0d*xSqr*ySqr + ySqr*ySqr;
            return d != 0 ? new Vector2D(((xSqr - ySqr)/d) + cx, ((-2.0d*zx*zy)/d) + cy) : new Vector2D(1000000.0d, 1000000.0d);
        }
    }

    // Buffalo: z -> |Re(z^2)| + i*|Im(z^2)| + c
    public static class Buffalo implements Function {
        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(Math.abs(zx*zx - zy*zy) + cx, Math.abs(2.0d*zx*zy) + cy);
        }
    }

    // Multibrot: z -> z^d + c
    public static class Multibrot implements Function {
        private final double d;

        public Multibrot(double d) {
            this.d = d;
        }

        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            double r = Math.hypot(zx, zy);
            double newTheta = d*Math.atan2(zy, zx);
            double rPowD = Math.pow(r, d);
            return new Vector2D(rPowD*Math.cos(newTheta) + cx, rPowD*Math.sin(newTheta) + cy);
        }
    }

    // Aquarius: z -> rz^2 + 2rz + c
    public static class Aquarius implements Function {
        private final double r;

        public Aquarius(double r) {
            this.r = r;
        }

        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(r*(zx*zx - zy*zy + 2*zx) + cx, r*(2.0d*zx*zy + 2*zy) + cy);
        }
    }

    // Vajra: z -> rz^3 + 0.5z + c
    public static class Vajra implements Function {
        private final double r;

        public Vajra(double r) {
            this.r = r;
        }

        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(r*(zx*zx*zx - 3.0d*zx*zy*zy) + 0.5*zx + cx, r*(3.0d*zx*zx*zy - zy*zy*zy) + 0.5*zy + cy);
        }
    }

    // Keyura: z -> rz^3 + 0.97rz + c
    public static class Keyura implements Function {
        private final double r;

        public Keyura(double r) {
            this.r = r;
        }

        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(r*(zx*zx*zx - 3.0d*zx*zy*zy + 0.97*zx) + cx, r*(3.0d*zx*zx*zy - zy*zy*zy + 0.97*zy) + cy);
        }
    }

    // Shrivatsa: z -> conj(z)^3 + rz + c
    public static class Shrivatsa implements Function {
        private final double r;

        public Shrivatsa(double r) {
            this.r = r;
        }

        @Override
        public Vector2D apply(double cx, double cy, double zx, double zy) {
            return new Vector2D(r*(zx*zx*zx - 3.0d*zx*zy*zy + zx) + cx, r*(-3.0d*zx*zx*zy + zy*zy*zy + zy) + cy);
        }
    }
}
