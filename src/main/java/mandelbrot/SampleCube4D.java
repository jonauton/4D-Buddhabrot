package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that represents a 4D cube.
 */
public class SampleCube4D implements SampleSpace {
    private SamplingMethod samplingMethod;
    private Quaternion vertex1;
    private Quaternion vertex2;

    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private double zmin;
    private double zmax;
    private double wmin;
    private double wmax;

    /**
     * Constructor for the 4D cube.
     * @param samplingMethod The sampling method to use.
     * @param vertex1 The first vertex of the cube.
     * @param vertex2 The second vertex of the cube.
     */
    public SampleCube4D(SamplingMethod samplingMethod, Quaternion vertex1, Quaternion vertex2) {
        this.samplingMethod = samplingMethod;
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;

        updateBounds();
    }

    private void updateBounds() {
        xmin = Math.min(vertex1.getQ0(), vertex2.getQ0());
        xmax = Math.max(vertex1.getQ0(), vertex2.getQ0());
        ymin = Math.min(vertex1.getQ1(), vertex2.getQ1());
        ymax = Math.max(vertex1.getQ1(), vertex2.getQ1());
        zmin = Math.min(vertex1.getQ2(), vertex2.getQ2());
        zmax = Math.max(vertex1.getQ2(), vertex2.getQ2());
        wmin = Math.min(vertex1.getQ3(), vertex2.getQ3());
        wmax = Math.max(vertex1.getQ3(), vertex2.getQ3());
    }

    /**
     * Gets the first vertex of the cube.
     * @return The first vertex of the cube.
     */
    public Quaternion getVertex1() {
        return vertex1;
    }

    /**
     * Gets the second vertex of the cube.
     * @return The second vertex of the cube.
     */
    public Quaternion getVertex2() {
        return vertex2;
    }

    /**
     * Sets the first vertex of the cube.
     */
    public void setVertex1(Quaternion vertex1) {
        this.vertex1 = vertex1;
        updateBounds();
    }

    /**
     * Sets the second vertex of the cube.
     */
    public void setVertex2(Quaternion vertex2) {
        this.vertex2 = vertex2;
        updateBounds();
    }

    /**
     * Gets the center of the cube.
     * @return The center of the cube.
     */
    public Quaternion getCenter() {
        return vertex1.add(vertex2).multiply(0.5d);
    }

    /**
     * Takes a random sample from inside the cube.
     * @return A random sample from inside the cube.
     */
    @Override
    public Quaternion getRandomSample() {
        double x = xmin < xmax ? ThreadLocalRandom.current().nextDouble(xmin, xmax) : xmin;
        double y = ymin < ymax ? ThreadLocalRandom.current().nextDouble(ymin, ymax) : ymin;
        double z = zmin < zmax ? ThreadLocalRandom.current().nextDouble(zmin, zmax) : zmin;
        double w = wmin < wmax ? ThreadLocalRandom.current().nextDouble(wmin, wmax) : wmin;
        return new Quaternion(x, y, z, w);
    }

    public SamplingMethod getSamplingMethod() {
        return samplingMethod;
    }
}
