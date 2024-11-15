package mandelbrot;

import com.google.common.util.concurrent.AtomicDoubleArray;

/**
 * A class that represents an image of doubles, similar to the IntegerImage.
 * This is commonly used to represent Buddhabrot images generated from an importance sampling algorithm.
 * It implements a thread-safe 2d array of doubles.
 */
public class DoubleImage {
    private final int width;
    private final int height;
    private final AtomicDoubleArray image;

    /**
     * Constructor for the DoubleImage.
     * @param width The width of the image.
     * @param height The height of the image.
     */
    public DoubleImage(int width, int height) {
        this.image = new AtomicDoubleArray(width * height);
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the width of the image.
     * @return The width of the image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the image.
     * @return The height of the image.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the value at the specified index.
     * @param x The x-coordinate of the index.
     * @param y The y-coordinate of the index.
     * @return The value at the specified index.
     */
    public double get(int x, int y) {
        return image.get(x + y * width);
    }

    /**
     * Sets the value at the specified index.
     * @param x The x-coordinate of the index.
     * @param y The y-coordinate of the index.
     * @param value The value to set.
     */
    public void set(int x, int y, double value) {
        image.set(x + y * width, value);
    }

    /**
     * Adds a value to the specified index.
     * @param x The x-coordinate of the index.
     * @param y The y-coordinate of the index.
     * @param value The value to add.
     */
    public void add(int x, int y, double value) {
        image.addAndGet(x + y * width, value);
    }
}
