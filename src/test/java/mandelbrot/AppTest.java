package mandelbrot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Quaternion;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Unit test test
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    /**
     * Test the runtime of IntegerImage against DoubleImage, with a simple operation.
     * Both accessing and setting values are tested.
     * Final results are validated against each other.
     */
    @Test
    public void testRuntime() {
        // NOTE: I made a mistake initially which caused integer overflows to occur, which didn't happen in the more
        //  precise double-world! It's always important to watch out for things like overflows when manipulating large numbers.
        int width = 10_000;
        int height = 1_000;
        int iterations = 10_000_000;

        // IntegerImage
        long startTime = System.nanoTime();
        IntegerImage integerImage = new IntegerImage(width, height);
        integerImage.set(0, 0, 0);
        for (int i = 1; i < iterations; i++) {
            integerImage.set(i % width, i / width, (1 + (i % 256) * (integerImage.get((i-1) % width, (i-1) / width))  % 256) % 256);
        }
        long endTime = System.nanoTime();
        System.out.println("IntegerImage runtime: " + (endTime - startTime) / 1_000_000 + "ms");

        // DoubleImage
        startTime = System.nanoTime();
        DoubleImage doubleImage = new DoubleImage(width, height);
        doubleImage.set(0, 0, 0);
        for (int i = 1; i < iterations; i++) {
            doubleImage.set(i % width, i / width, (1 + (i % 256) * ((int)doubleImage.get((i-1) % width, (i-1) / width)) % 256) % 256);
        }
        endTime = System.nanoTime();
        System.out.println("DoubleImage runtime: " + (endTime - startTime) / 1_000_000 + "ms");

        // Ordinary integer array
        startTime = System.nanoTime();
        int[][] integerImageArray = new int[width][height];
        integerImageArray[0][0] = 0;
        for (int i = 1; i < iterations; i++) {
            integerImageArray[i % width][i / width] = (1 + (i % 256) * (integerImageArray[(i-1) % width][(i-1) / width] % 256)) % 256;
        }
        endTime = System.nanoTime();
        System.out.println("Ordinary integer array runtime: " + (endTime - startTime) / 1_000_000 + "ms");

        // Ordinary double array
        startTime = System.nanoTime();
        double[][] doubleImageArray = new double[width][height];
        doubleImageArray[0][0] = 0;
        for (int i = 1; i < iterations; i++) {
            doubleImageArray[i % width][i / width] = (1 + (i % 256) * ((int)doubleImageArray[(i-1) % width][(i-1) / width] % 256)) % 256;
        }
        endTime = System.nanoTime();
        System.out.println("Ordinary double array runtime: " + (endTime - startTime) / 1_000_000 + "ms");

        // Validate results
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                assertEquals(integerImage.get(x, y), doubleImage.get(x, y), 0.0);
                assertEquals(integerImage.get(x, y), integerImageArray[x][y]);
                assertEquals(integerImage.get(x, y), doubleImageArray[x][y], 0.0);
            }
        }
    }

    /**
     * Test the power function of the Plane4D.
     */
    @Test
    public void testPow() {
        double delta = 1e-12;
        Quaternion one = new Quaternion(1, 0, 0, 0);
        Quaternion i = new Quaternion(0, 1, 0, 0);
        Quaternion j = new Quaternion(0, 0, 1, 0);
        Quaternion k = new Quaternion(0, 0, 0, 1);
        Quaternion zero = new Quaternion(0, 0, 0, 0);
        Quaternion ones = new Quaternion(1, 1, 1, 1);
        Quaternion twos = new Quaternion(2, 2, 2, 2);
        Quaternion minusOne = new Quaternion(-1, 0, 0, 0);
        Quaternion two = new Quaternion(2, 0, 0, 0);


        // Test 1^1 = 1
        assertEquals(quatDistance(one, Plane4D.pow(one, 1)), 0, delta);
        // Test 1^0 = 1
        assertEquals(quatDistance(one, Plane4D.pow(one, 0)), 0, delta);
        // Test 1^2 = 1
        assertEquals(quatDistance(one, Plane4D.pow(one, 2)), 0, delta);

        // Test i^1 = i
        assertEquals(quatDistance(i, Plane4D.pow(i, 1)), 0, delta);
        // Test i^0 = 1
        assertEquals(quatDistance(one, Plane4D.pow(i, 0)), 0, delta);
        // Test i^2 = -1
        assertEquals(quatDistance(minusOne, Plane4D.pow(i, 2)), 0, delta);

        // Test j^1 = j
        assertEquals(quatDistance(j, Plane4D.pow(j, 1)), 0, delta);
        // Test j^0 = 1
        assertEquals(quatDistance(one, Plane4D.pow(j, 0)), 0, delta);
        // Test j^2 = -1
        assertEquals(quatDistance(minusOne, Plane4D.pow(j, 2)), 0, delta);

        // Test k^1 = k
        assertEquals(quatDistance(k, Plane4D.pow(k, 1)), 0, delta);
        // Test k^0 = 1
        assertEquals(quatDistance(one, Plane4D.pow(k, 0)), 0, delta);
        // Test k^2 = -1
        assertEquals(quatDistance(minusOne, Plane4D.pow(k, 2)), 0, delta);

        // Test ones^1 = ones
        assertEquals(quatDistance(ones, Plane4D.pow(ones, 1)), 0, delta);
        // Test ones^0 = 1
        assertEquals(quatDistance(one, Plane4D.pow(ones, 0)), 0, delta);
        // Test ones^2 = -2 + 2i + 2j + 2k
        assertEquals(quatDistance(new Quaternion(-2, 2, 2, 2), Plane4D.pow(ones, 2)), 0, delta);
        // Test ones^-1 = 0.25 - 0.25i - 0.25j - 0.25k
        assertEquals(quatDistance(new Quaternion(0.25, -0.25, -0.25, -0.25), Plane4D.pow(ones, -1)), 0, delta);
    }

    private double quatDistance(Quaternion a, Quaternion b) {
        return Math.sqrt(Math.pow(a.getQ0() - b.getQ0(), 2) + Math.pow(a.getQ1() - b.getQ1(), 2) + Math.pow(a.getQ2() - b.getQ2(), 2) + Math.pow(a.getQ3() - b.getQ3(), 2));
    }
}
