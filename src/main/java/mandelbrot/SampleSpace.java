package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;

/**
 * A simple interface for a sample space.
 * Call the method getSample to get a Quaternion sample.
 */
public interface SampleSpace {
    /**
     * Get a sample from the sample space.
     * @return A Quaternion sample, or null for a failed sample.
     */
    Quaternion getRandomSample();

    /**
     * Get the sampling method for the sample space.
     * @return The sampling method.
     */
    SamplingMethod getSamplingMethod();
}