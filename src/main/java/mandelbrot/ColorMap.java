package mandelbrot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A collection of color functions.
 */
public class ColorMap {
    /**
     * A color function that maps the number of iterations to an RGB color.
     * @param max_iter The maximum number of iterations.
     * @param i The number of iterations.
     * @return The color.
     */
    public static int colorFunction(int max_iter, int i) {
        return i < max_iter ? Color.HSBtoRGB(i / 36.0f, 1.0f, 1.0f) : 0x00000000;
    }

    /**
     * A color function for the Buddhabrot that maps the number of iterations to an RGB color, interpolating based on a maximum value.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param max The maximum value.
     * @param image_box The pixels hit during iteration.
     * @return The color.
     */
    public static int colorFunctionHist(int x, int y, int max, IntegerImage image_box) {
        return image_box.get(x, y) > 0 ? Color.HSBtoRGB((float)image_box.get(x, y) / max, 1.0f, Math.min(4*(float)image_box.get(x, y) / max, 1.0f)): 0x000000;
    }

    /**
     * A color function for the Buddhabrot that maps the number of iterations to an RGB color, interpolating based on a maximum value.
     * @param image_box The pixels hit during iteration.
     * @return The color.
     */
    public static BufferedImage colorFunctionHist(IntegerImage image_box) {
        // Find the 99th percentile
        int[] values = new int[image_box.getWidth() * image_box.getHeight()];
        int index = 0;
        for (int i = 0; i < image_box.getWidth(); i++) {
            for (int j = 0; j < image_box.getHeight(); j++) {
                values[index] = image_box.get(i, j);
                index++;
            }
        }
        Arrays.sort(values);
        int max = values[(int)(0.99 * values.length)];

        BufferedImage img = new BufferedImage(image_box.getWidth(), image_box.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < image_box.getWidth(); i++) {
            for (int j = 0; j < image_box.getHeight(); j++) {
                img.setRGB(i, j, colorFunctionHist(i, j, max, image_box));
            }
        }

        return img;
    }

    /**
     * A color function for the Buddhabrot that maps the number of iterations to a fully saturated RGB color, interpolating based on a maximum value.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param max The maximum value.
     * @param image_box The pixels hit during iteration.
     * @return The color.
     */
    public static int colorFunctionAnyHist(int x, int y, int max, int[][] image_box) {
        return image_box[y][x] > 0 ? Color.HSBtoRGB((float)image_box[y][x] / max, 1.0f, 1.0f): 0x000000;
    }

    /**
     * A color function for the Buddhabrot that maps the number of iterations to a grayscale color, interpolating based on a maximum value.
     * This uses a gamma of 2.2 to make the image look more natural.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param max The maximum value.
     * @param image_box The pixels hit during iteration.
     * @return The color.
     */
    public static int colorGrayscaleHist(int x, int y, int max, int[][] image_box) {
        return image_box[y][x] > 0 ? Color.HSBtoRGB(0.0f, 0.0f, (float) (Math.pow((double) image_box[y][x] / max, 2.2d))): 0x000000;
    }

    /**
     * A color function that is black if the pixel is in the Mandelbrot set, and white otherwise.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param image_box The pixels hit during iteration.
     * @return The color.
     */
    public static int colorBWHist(int x, int y, int[][] image_box) {
        return image_box[y][x] > 0 ? 0xFFFFFF : 0x000000;
    }

    /**
     * A color function that is black if the pixel is in the Mandelbrot set, and white otherwise.
     * @param max_iter The maximum number of iterations.
     * @param i The number of iterations.
     * @return The color.
     */
    public static int colorBW(int max_iter, int i) {
        return i > 0 ? 0xFFFFFF : 0x000000;
    }

    /**
     * A color function that creates a Nebulabrot coloring.
     * @param imageBox1 The blue image box.
     * @param imageBox2 The green image box.
     * @param imageBox3 The red image box.
     * @return The generated image.
     */
    public static BufferedImage colorNebula(IntegerImage imageBox1, IntegerImage imageBox2, IntegerImage imageBox3, double brightness) {
        int width = imageBox1.getWidth();
        int height = imageBox1.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Find the 95th percentile in each box
        int[] imageBox1Sorted = new int[width * height];
        int[] imageBox2Sorted = new int[width * height];
        int[] imageBox3Sorted = new int[width * height];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageBox1Sorted[index] = imageBox1.get(x, y);
                imageBox2Sorted[index] = imageBox2.get(x, y);
                imageBox3Sorted[index] = imageBox3.get(x, y);
                index++;
            }
        }
        Arrays.sort(imageBox1Sorted);
        Arrays.sort(imageBox2Sorted);
        Arrays.sort(imageBox3Sorted);

        double max1 = imageBox1Sorted[(int)(0.99 * width * height)];
        double max2 = imageBox2Sorted[(int)(0.99 * width * height)];
        double max3 = imageBox3Sorted[(int)(0.99 * width * height)];

        // Generate the image grayscale histogram
        int[] imageHistogram = new int[0xFF + 1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int redCh = (int)(Math.min((float)imageBox3.get(x, y) / max3, 1.0f) * 0xFF);
                int greenCh = (int)(Math.min((float)imageBox2.get(x, y) / max2, 1.0f) * 0xFF);
                int blueCh = (int)(Math.min((float)imageBox1.get(x, y) / max1, 1.0f) * 0xFF);
                int grayCh = (int)(redCh * 0.299 + greenCh * 0.587 + blueCh * 0.114);
                imageHistogram[grayCh]++;
            }
        }

        // Cumulative histogram
        for (int i = 1; i < imageHistogram.length; i++) {
            imageHistogram[i] += imageHistogram[i - 1];
        }

        // Generate the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double redCh = (Math.min((float)imageBox3.get(x, y) / max3, 1.0f));
                double greenCh = (Math.min((float)imageBox2.get(x, y) / max2, 1.0f));
                double blueCh = (Math.min((float)imageBox1.get(x, y) / max1, 1.0f));
                int grayCh = (int)((redCh * 0.299 + greenCh * 0.587 + blueCh * 0.114)*0xFF);

                // This has the effect of darkening the dark areas, and keeping the bright areas unchanged
                // Proportional to what proportion of the image is at least that dark
                double factor = brightness * imageHistogram[grayCh] / (width * height);
                redCh = Math.min((int)(redCh * factor * 0xFF), 0xFF);
                greenCh = Math.min((int)(greenCh * factor * 0xFF), 0xFF);
                blueCh = Math.min((int)(blueCh * factor * 0xFF), 0xFF);
                image.setRGB(x, y, ((int)redCh << 16) | ((int)greenCh << 8) | (int)blueCh);
            }
        }

        return image;
    }


}
