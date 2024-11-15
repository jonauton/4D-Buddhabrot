package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that represents a 2D plane embedded in 4D space.
 */
public class SamplePlane4D implements SampleSpace {
    private final SamplingMethod samplingMethod;
    private final Function function;
    private final Plane4D samplingPlane;
    private final int sampleWidth;
    private final int sampleHeight;
    private final int sampleIter;
    private final double sampleBailout;
    private boolean[][] fractalInterior;

    /**
     * Constructor for the 4D sampling plane.
     * @param samplingMethod The sampling method to use.
     * @param function The function to sample from.
     * @param samplingPlane The plane to sample.
     * @param sampleWidth The width of the sample.
     * @param sampleHeight The height of the sample.
     * @param sampleIter The number of iterations to use to create the sampling fractal interior.
     * @param sampleBailout The bailout value for the sampling.
     */
    public SamplePlane4D(SamplingMethod samplingMethod, Function function, Plane4D samplingPlane, int sampleWidth, int sampleHeight, int sampleIter, double sampleBailout) {
        this.samplingMethod = samplingMethod;
        this.function = function;
        this.samplingPlane = samplingPlane;
        this.sampleWidth = sampleWidth;
        this.sampleHeight = sampleHeight;
        this.sampleIter = sampleIter;
        this.sampleBailout = sampleBailout;

        // Generate the fractal interior for the appropriate sampling method.
        switch (samplingMethod) {
            case EXTERIOR:
                this.fractalInterior = convolveInterior(
                        FractalImageGen.generateCrossSectionBooleanArray(
                                function,
                                samplingPlane,
                                sampleWidth,
                                sampleHeight,
                                sampleIter,
                                sampleBailout
                        )
                );
                break;
            case INTERIOR:
                this.fractalInterior = convolveExterior(
                        FractalImageGen.generateCrossSectionBooleanArray(
                                function,
                                samplingPlane,
                                sampleWidth,
                                sampleHeight,
                                sampleIter,
                                sampleBailout
                        )
                );
                break;
        }
    }

    /**
     * Runs a convolution to estimate the interior of the fractal.
     * ( AND for all neighbors of a pixel )
     * @param fractal The fractal to convolve.
     * @return The convolved fractal.
     */
    static boolean[][] convolveInterior(boolean[][] fractal) {
        boolean[][] newFractal = new boolean[fractal.length][fractal[0].length];
        for (int x = 1; x < fractal.length - 1; x++) {
            for (int y = 1; y < fractal[0].length - 1; y++) {
                newFractal[x][y] = fractal[x][y] &&
                        fractal[x][y - 1] &&
                        fractal[x][y + 1] &&
                        fractal[x - 1][y] &&
                        fractal[x - 1][y - 1] &&
                        fractal[x - 1][y + 1] &&
                        fractal[x + 1][y - 1] &&
                        fractal[x + 1][y] &&
                        fractal[x + 1][y + 1];
            }
        }
        return newFractal;
    }

    /**
     * Runs a convolution to estimate the exterior of the fractal.
     * ( OR for all neighbors of a pixel )
     * @param fractal The fractal to convolve.
     * @return The convolved fractal.
     */
    static boolean[][] convolveExterior(boolean[][] fractal) {
        boolean[][] newFractal = new boolean[fractal.length][fractal[0].length];
        for (int x = 1; x < fractal.length - 1; x++) {
            for (int y = 1; y < fractal[0].length - 1; y++) {
                newFractal[x][y] = fractal[x][y] ||
                        fractal[x][y - 1] ||
                        fractal[x][y + 1] ||
                        fractal[x - 1][y] ||
                        fractal[x - 1][y - 1] ||
                        fractal[x - 1][y + 1] ||
                        fractal[x + 1][y - 1] ||
                        fractal[x + 1][y] ||
                        fractal[x + 1][y + 1];
            }
        }
        return newFractal;
    }

    public SamplingMethod getSamplingMethod() {
        return samplingMethod;
    }

    @Override
    public Quaternion getRandomSample() {
        double xRandom = ThreadLocalRandom.current().nextDouble(0, sampleWidth);
        double yRandom = ThreadLocalRandom.current().nextDouble(0, sampleHeight);

        if (
                (samplingMethod == SamplingMethod.EXTERIOR && fractalInterior[(int) xRandom][(int) yRandom]) ||
                (samplingMethod == SamplingMethod.INTERIOR && !fractalInterior[(int) xRandom][(int) yRandom])
        ) return null;

        return samplingPlane.getPointAtPixel(xRandom, yRandom, sampleWidth, sampleHeight);
    }
}
