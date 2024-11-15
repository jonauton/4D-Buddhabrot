package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

/**
 * A class that generates a fractal image, given a Function.
 * Has support for both cross-section and projection rendering.
 */
public class FractalImageGen {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * Generates a cross-sectional fractal image, given a Function.
     * @param f The function to use.
     * @param camera The 4D camera to use.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param maxIter The maximum number of iterations to use.
     * @param bailout The bailout value to use.
     * @return The generated image.
     */
    public static BufferedImage generateCrossSection(Function f, Plane4D camera, int width, int height, int maxIter, double bailout) {
        // Initialize the image.
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double sqrBailout = bailout*bailout;

        // IntStream automatically parallelizes the for loop.
        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                int i = runIterations(f, camera.getPointAtPixel(x, y, width, height), maxIter, sqrBailout);
                int color = ColorMap.colorFunction(maxIter, i);
                image.setRGB(x, y, color);
            }
        });

        return image;
    }

    /**
     * Generates a cross-sectional fractal image, given a Function.
     * @param f The function to use.
     * @param camera The 4D camera to use.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param maxIter The maximum number of iterations to use.
     * @param bailout The bailout value to use.
     * @return The generated image.
     */
    public static boolean[][] generateCrossSectionBooleanArray(Function f, Plane4D camera, int width, int height, int maxIter, double bailout) {
        // Initialize the image.
        boolean[][] image = new boolean[width][height];
        double sqrBailout = bailout*bailout;

        // IntStream automatically parallelizes the for loop.
        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                int i = runIterations(f, camera.getPointAtPixel(x, y, width, height), maxIter, sqrBailout);
                image[x][y] = (i == maxIter);
            }
        });

        return image;
    }

    /**
     * Runs the iterations of the fractal function.
     * @param f The function to use.
     * @param point The point to run the iterations on.
     * @param maxIter The maximum number of iterations to use.
     * @param sqrBailout The square of the bailout value.
     * @return The number of iterations before either the bailout value or the maximum number of iterations was reached.
     */
    public static int runIterations(Function f, Quaternion point, int maxIter, double sqrBailout) {
        int i = 0;
        double cx = point.getQ0();
        double cy = point.getQ1();
        double zx = point.getQ2();
        double zy = point.getQ3();

        while (zx*zx + zy*zy < sqrBailout && i < maxIter) {
            Vector2D vec = f.apply(cx, cy, zx, zy);
            zx = vec.getX();
            zy = vec.getY();
            i++;
        }
        return i;
    }

    /**
     * Generates a projection fractal image of the exterior of the fractal, given a Function, over three different
     * maximum iterations.
     * @param f The function to use.
     * @param sampleSpace The plane sample space to use.
     * @param camera The 4D camera to use.
     * @param imageBox1 The first image-box to project onto. (MUST BE THE SAME SIZE AS imageBox2 AND imageBox3)
     * @param imageBox2 The second image-box to project onto. (MUST BE THE SAME SIZE AS imageBox1 AND imageBox3)
     * @param imageBox3 The third image-box to project onto. (MUST BE THE SAME SIZE AS imageBox1 AND imageBox2)
     * @param maxIter1 The maximum number of iterations to use for the first image-box.
     * @param maxIter2 The maximum number of iterations to use for the second image-box. (maxIter2 > maxIter1)
     * @param maxIter3 The maximum number of iterations to use for the third image-box. (maxIter3 > maxIter2)
     * @param bailout The bailout value to use.
     * @param samples The number of samples to use.
     * @param executor The executor to use (this will use ALL available threads to render).
     */
    public static void generateNebulaProjection(Function f, SampleSpace sampleSpace, Plane4D camera, IntegerImage imageBox1, IntegerImage imageBox2, IntegerImage imageBox3, int maxIter1, int maxIter2, int maxIter3, double bailout, long samples, ExecutorService executor) {
        // Use a thread pool to parallelize the loop.
        long samplesPerThread = samples / THREADS / 3;

        // Cache the projection.
        camera.cacheProjection();

        // Start executing the threads.
        for (int i = 0; i < THREADS; i++) {
            executor.execute(new ProjectionThreadNebula(
                    f,
                    sampleSpace,
                    camera,
                    imageBox1,
                    imageBox2,
                    imageBox3,
                    maxIter1,
                    maxIter2,
                    maxIter3,
                    bailout,
                    samplesPerThread
            ));
        }
    }

    /**
     * Generates a projection fractal image of the exterior of the fractal, given a Function.
     * @param f The function to use.
     * @param sampleSpace The sample space to use.
     * @param camera The 4D camera to use.
     * @param imageBox The image-box to project onto.
     * @param maxIter The maximum number of iterations to use for the first image-box.
     * @param bailout The bailout value to use.
     * @param samples The number of samples to use.
     * @param executor The executor to use (this will use ALL available threads to render).
     */
    public static void generateProjection(Function f, SampleSpace sampleSpace, Plane4D camera, IntegerImage imageBox, int maxIter, double bailout, long samples, ExecutorService executor) {
        // Use a thread pool to parallelize the loop.
        long samplesPerThread = samples / THREADS;

        // Cache the projection.
        camera.cacheProjection();

        // Start executing the threads.
        for (int i = 0; i < THREADS; i++) {
            executor.execute(new ProjectionThread(
                    f,
                    sampleSpace,
                    camera,
                    imageBox,
                    maxIter,
                    bailout,
                    samplesPerThread
            ));
        }
    }

    /**
     * Generates a projection fractal image of the fractal, given a Function, over three different
     * maximum iterations.
     * @param f The function to use.
     * @param sampleCube The 4D sample space to use.
     * @param camera The 4D camera to use.
     * @param imageBox1 The first image-box to project onto. (MUST BE THE SAME SIZE AS imageBox2 AND imageBox3)
     * @param imageBox2 The second image-box to project onto. (MUST BE THE SAME SIZE AS imageBox1 AND imageBox3)
     * @param imageBox3 The third image-box to project onto. (MUST BE THE SAME SIZE AS imageBox1 AND imageBox2)
     * @param maxIter1 The maximum number of iterations to use for the first image-box.
     * @param maxIter2 The maximum number of iterations to use for the second image-box. (maxIter2 > maxIter1)
     * @param maxIter3 The maximum number of iterations to use for the third image-box. (maxIter3 > maxIter2)
     * @param bailout The bailout value to use.
     * @param samples The number of samples to use.
     * @param executor The executor to use (this will use ALL available threads to render).
     */
    public static void generateHoloInteriorSamplesProjection(Function f, SampleCube4D sampleCube, Plane4D camera, IntegerImage imageBox1, IntegerImage imageBox2, IntegerImage imageBox3, int maxIter1, int maxIter2, int maxIter3, double bailout, long samples, ExecutorService executor) {
        // Use a thread pool to parallelize the loop.
        long samplesPerThread = samples / THREADS / 3;

        // Start executing the threads.
        for (int i = 0; i < THREADS; i++) {
            executor.execute(new ProjectionThreadHoloInteriorSamples(f, imageBox1, imageBox2, imageBox3, sampleCube, camera, maxIter1, maxIter2, maxIter3, bailout, samplesPerThread));
        }
    }

    private static class ProjectionThreadNebula extends Thread {
        private final SamplingMethod samplingMethod;
        private final Function f;
        private final IntegerImage imageBox1;
        private final IntegerImage imageBox2;
        private final IntegerImage imageBox3;
        private final SampleSpace sampleSpace;
        private final Plane4D camera;
        private final int width;
        private final int height;
        private final int maxIter1;
        private final int maxIter2;
        private final int maxIter3;
        private final double bailout;
        private final long numSamples;

        /**
         * Constructs a new ProjectionThreadNebula.
         * @param f The function to use.
         * @param sampleSpace The plane sample space to use.
         * @param camera The 4D camera to project onto.
         * @param imageBox1 The 1st shared number-box of the fractal projection.
         * @param imageBox2 The 2nd shared number-box of the fractal projection.
         * @param imageBox3 The 3rd shared number-box of the fractal projection.
         * @param maxIter1 The maximum number of iterations to use for imageBox1.
         * @param maxIter2 The maximum number of iterations to use for imageBox2.
         * @param maxIter3 The maximum number of iterations to use for imageBox3.
         * @param bailout The bailout value to use.
         * @param numSamples The number of samples to use per thread.
         */
        public ProjectionThreadNebula(Function f, SampleSpace sampleSpace, Plane4D camera, IntegerImage imageBox1, IntegerImage imageBox2, IntegerImage imageBox3, int maxIter1, int maxIter2, int maxIter3, double bailout, long numSamples) {
            this.samplingMethod = sampleSpace.getSamplingMethod();
            this.f = f;
            this.sampleSpace = sampleSpace;
            this.camera = camera;
            this.imageBox1 = imageBox1;
            this.imageBox2 = imageBox2;
            this.imageBox3 = imageBox3;
            this.width = imageBox1.getWidth();
            this.height = imageBox1.getHeight();
            this.maxIter1 = maxIter1;
            this.maxIter2 = maxIter2;
            this.maxIter3 = maxIter3;
            this.bailout = bailout;
            this.numSamples = numSamples;
        }

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            switch (samplingMethod) {
                case ALL:
                    runAll();
                    break;
                case INTERIOR:
                    runInterior();
                    break;
                case EXTERIOR:
                    runExterior();
                    break;
            }
        }

        private void runAll() {
            final double sqrBailout = bailout * bailout;

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter3) {
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) {
                        if (i < maxIter1) imageBox1.increment(p.x, p.y);
                        if (i < maxIter2) imageBox2.increment(p.x, p.y);
                        imageBox3.increment(p.x, p.y);
                    }

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }
            }
        }

        private void runInterior() {
            final double sqrBailout = bailout * bailout;
            final boolean[] useTrace = new boolean[maxIter3];
            final int[] traceX = new int[maxIter3];
            final int[] traceY = new int[maxIter3];

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter3) {
                    useTrace[i] = false;
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) {
                        if (i < maxIter1) imageBox1.increment(p.x, p.y);
                        if (i < maxIter2) imageBox2.increment(p.x, p.y);
                        imageBox3.increment(p.x, p.y);

                        traceX[i] = p.x;
                        traceY[i] = p.y;
                        useTrace[i] = true;
                    }

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }

                if (i < maxIter1) {
                    for (int k = 0; k < i; k++) {
                        if (useTrace[k]) {
                            imageBox1.decrement(traceX[k], traceY[k]);
                            imageBox2.decrement(traceX[k], traceY[k]);
                            imageBox3.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
                else if (i < maxIter2) {
                    for (int k = 0; k < i; k++) {
                        if (useTrace[k]) {
                            imageBox2.decrement(traceX[k], traceY[k]);
                            imageBox3.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
                else if (i != maxIter3) {
                    for (int k = 0; k < i; k++) {
                        if (useTrace[k]) {
                            imageBox3.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
            }
        }

        private void runExterior() {
            final double sqrBailout = bailout * bailout;
            final boolean[] useTrace = new boolean[maxIter3];
            final int[] traceX = new int[maxIter3];
            final int[] traceY = new int[maxIter3];

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter3) {
                    useTrace[i] = false;
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) {
                        if (i < maxIter1) imageBox1.increment(p.x, p.y);
                        if (i < maxIter2) imageBox2.increment(p.x, p.y);
                        imageBox3.increment(p.x, p.y);

                        traceX[i] = p.x;
                        traceY[i] = p.y;
                        useTrace[i] = true;
                    }

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }

                if (i == maxIter3) {
                    for (int k = 0; k < maxIter3; k++) {
                        if (useTrace[k]) {
                            if (k < maxIter1) imageBox1.decrement(traceX[k], traceY[k]);
                            if (k < maxIter2) imageBox2.decrement(traceX[k], traceY[k]);
                            imageBox3.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
                else if (i >= maxIter2) {
                    for (int k = 0; k < maxIter2; k++) {
                        if (useTrace[k]) {
                            if (k < maxIter1) imageBox1.decrement(traceX[k], traceY[k]);
                            imageBox2.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
                else if (i >= maxIter1) {
                    for (int k = 0; k < maxIter1; k++) {
                        if (useTrace[k]) {
                            imageBox1.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
            }
        }
    }

    /**
     * A projection thread that uses a full 4-dimensional sample space over three different number-boxes.
     */
    private static class ProjectionThreadHoloInteriorSamples extends Thread {
        private final Function f;
        private final IntegerImage imageBox1;
        private final IntegerImage imageBox2;
        private final IntegerImage imageBox3;
        private final SampleCube4D sampleCube;
        private final Plane4D camera;
        private final int width;
        private final int height;
        private final int maxIter1;
        private final int maxIter2;
        private final int maxIter3;
        private final double bailout;
        private final long numSamples;

        /**
         * Constructs a new ProjectionThreadNebulaHolo.
         * @param f The function to use.
         * @param imageBox1 The 1st shared number-box of the fractal projection.
         * @param imageBox2 The 2nd shared number-box of the fractal projection.
         * @param imageBox3 The 3rd shared number-box of the fractal projection.
         * @param sampleCube The plane sample space to use.
         * @param camera The 4D camera to project onto.
         * @param maxIter1 The maximum number of iterations to use for imageBox1.
         * @param maxIter2 The maximum number of iterations to use for imageBox2.
         * @param maxIter3 The maximum number of iterations to use for imageBox3.
         * @param bailout The bailout value to use.
         * @param numSamples The number of samples to use per thread.
         */
        public ProjectionThreadHoloInteriorSamples(Function f, IntegerImage imageBox1, IntegerImage imageBox2, IntegerImage imageBox3, SampleCube4D sampleCube, Plane4D camera, int maxIter1, int maxIter2, int maxIter3, double bailout, long numSamples) {
            this.f = f;
            this.imageBox1 = imageBox1;
            this.imageBox2 = imageBox2;
            this.imageBox3 = imageBox3;
            this.sampleCube = sampleCube;
            this.camera = camera;
            this.width = imageBox1.getWidth();
            this.height = imageBox1.getHeight();
            this.maxIter1 = maxIter1;
            this.maxIter2 = maxIter2;
            this.maxIter3 = maxIter3;
            this.bailout = bailout;
            this.numSamples = numSamples;
        }

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            final double sqrBailout = bailout * bailout;

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleCube.getRandomSample();

                Point p = camera.projectToPixel(point, width, height);
                if (p == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter3) {
                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }

                if (i == maxIter3) {
                    imageBox1.increment(p.x, p.y);
                    imageBox2.increment(p.x, p.y);
                    imageBox3.increment(p.x, p.y);
                }
                else if (i >= maxIter2) {
                    imageBox1.increment(p.x, p.y);
                    imageBox2.increment(p.x, p.y);
                }
                else if (i >= maxIter1) {
                    imageBox1.increment(p.x, p.y);
                }
            }
        }
    }

    private static class ProjectionThread extends Thread {
        private final SamplingMethod samplingMethod;
        private final Function f;
        private final IntegerImage imageBox;
        private final SampleSpace sampleSpace;
        private final Plane4D camera;
        private final int width;
        private final int height;
        private final int maxIter;
        private final double bailout;
        private final long numSamples;

        /**
         * Constructs a new ProjectionThread.
         * @param f The function to use.
         * @param sampleSpace The plane sample space to use.
         * @param camera The 4D camera to project onto.
         * @param imageBox The shared number-box of the fractal projection.
         * @param maxIter The maximum number of iterations to use.
         * @param bailout The bailout value to use.
         * @param numSamples The number of samples to use per thread.
         */
        public ProjectionThread(Function f, SampleSpace sampleSpace, Plane4D camera, IntegerImage imageBox, int maxIter, double bailout, long numSamples) {
            this.samplingMethod = sampleSpace.getSamplingMethod();
            this.f = f;
            this.sampleSpace = sampleSpace;
            this.camera = camera;
            this.imageBox = imageBox;
            this.width = imageBox.getWidth();
            this.height = imageBox.getHeight();
            this.maxIter = maxIter;
            this.bailout = bailout;
            this.numSamples = numSamples;
        }

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            switch (samplingMethod) {
                case ALL:
                    runAll();
                    break;
                case INTERIOR:
                    runInterior();
                    break;
                case EXTERIOR:
                    runExterior();
                    break;
            }
        }

        private void runAll() {
            final double sqrBailout = bailout * bailout;

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter) {
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) imageBox.increment(p.x, p.y);

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }
            }
        }

        private void runInterior() {
            final double sqrBailout = bailout * bailout;
            final boolean[] useTrace = new boolean[maxIter];
            final int[] traceX = new int[maxIter];
            final int[] traceY = new int[maxIter];

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter) {
                    useTrace[i] = false;
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) {
                        imageBox.increment(p.x, p.y);

                        traceX[i] = p.x;
                        traceY[i] = p.y;
                        useTrace[i] = true;
                    }

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }

                if (i < maxIter) {
                    for (int k = 0; k < i; k++) {
                        if (useTrace[k]) {
                            imageBox.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
            }
        }

        private void runExterior() {
            final double sqrBailout = bailout * bailout;
            final boolean[] useTrace = new boolean[maxIter];
            final int[] traceX = new int[maxIter];
            final int[] traceY = new int[maxIter];

            for (int j = 0; j < numSamples; j++) {
                // Add thread interruption check.
                if (Thread.interrupted()) return;

                Quaternion point = sampleSpace.getRandomSample();
                if (point == null) continue;

                double cx = point.getQ0();
                double cy = point.getQ1();
                double zx = point.getQ2();
                double zy = point.getQ3();

                int i = 0;
                while (zx*zx + zy*zy < sqrBailout && i < maxIter) {
                    useTrace[i] = false;
                    Point p = camera.cachedProjectToPixel(new Quaternion(cx, cy, zx, zy), width, height);
                    if (p != null) {
                        imageBox.increment(p.x, p.y);

                        traceX[i] = p.x;
                        traceY[i] = p.y;
                        useTrace[i] = true;
                    }

                    Vector2D vec = f.apply(cx, cy, zx, zy);
                    zx = vec.getX();
                    zy = vec.getY();
                    i++;
                }

                if (i == maxIter) {
                    for (int k = 0; k < maxIter; k++) {
                        if (useTrace[k]) {
                            imageBox.decrement(traceX[k], traceY[k]);
                        }
                    }
                }
            }
        }
    }
}