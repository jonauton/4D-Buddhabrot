package mandelbrot;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.cli.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A class that generates an animation of the Nebulabrot.
 */
public class GenerateAnimation {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    /** Renders a premade animation from a CSV file.
     * @param filename The filename of the CSV file.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param numSamples The number of samples to take.
     * @param bailout The bailout value.
     * @param function The function to use.
     * @param samplePlane The plane to sample from.
     */
    public static void genAnimationFile(String filename, String imageDirectory, int width, int height, long numSamples, double bailout, Function function, Plane4D samplePlane, SamplingMethod samplingMethod) throws IOException {
        // ArrayList to store the generated planes.
        ArrayList<Plane4D> planes = new ArrayList<>();
        ArrayList<Double> factors = new ArrayList<>();

        // Start by reading the CSV file.
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                double[] values = new double[15];
                int i = 0;
                for (String value : nextLine) {
                    double numericValue = Double.parseDouble(value);
                    values[i] = numericValue;
                    i += 1;
                }

                // Create the plane.
                Quaternion p1 = new Quaternion(values[0], values[1], values[2], values[3]);
                Quaternion p2 = new Quaternion(values[4], values[5], values[6], values[7]);
                Quaternion p3 = new Quaternion(values[8], values[9], values[10], values[11]);
                double scaleX = values[12];
                double scaleY = values[13];
                double factor = values[14];

                // Add the plane to the list.
                planes.add(new Plane4D(p1, p2, p3, scaleX, scaleY));
                factors.add(factor);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Get the fractal interior.
        SamplePlane4D samplePlane4D = new SamplePlane4D(
                samplingMethod,
                function,
                samplePlane,
                width,
                height,
                400,
                bailout
        );

        // Create the folder for the animation.
        File folder = new File(imageDirectory);
        if (!folder.exists()) folder.mkdir();

        // Generate the animation.
        int imageNum = 0;
        for (int i = 1; i < planes.size(); i++) {
            double erpProgress = 0.0d;
            while (erpProgress < 1.0d) {
                // Generate the nebulabrot.
                ExecutorService imageExecutor = Executors.newFixedThreadPool(THREADS);

                IntegerImage imageRed = new IntegerImage(width, height);
                IntegerImage imageGreen = new IntegerImage(width, height);
                IntegerImage imageBlue = new IntegerImage(width, height);

                FractalImageGen.generateNebulaProjection(
                        function,
                        samplePlane4D,
                        Plane4D.slerp(planes.get(i - 1), planes.get(i), erpProgress),
                        imageRed,
                        imageGreen,
                        imageBlue,
                        50,
                        500,
                        5000,
                        bailout,
                        numSamples,
                        imageExecutor
                );

                // Wait for the executor to finish.
                imageExecutor.shutdown();
                try {
                    imageExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                // Save the generated image to a file.
                BufferedImage image = ColorMap.colorNebula(imageRed, imageGreen, imageBlue, 1.0d);
                ImageIO.write(image, "png", new File(folder, imageNum++ + ".png"));
                System.out.println("Frame " + imageNum + " generated.");

                // Update the progress.
                erpProgress += factors.get(i);
            }
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("w", "width", true, "Width (px) of the output images (default: 1000)");
        options.addOption("v", "height", true, "Height (px) of the output images (default: 1000)");
        options.addOption("s", "samples", true, "Number of samples taken per image (default: 50000000)");
        options.addOption("b", "bailout", true, "Bailout value (default: 128.0)");
        options.addOption("f", "function", true, "The choice of function, choices must be one of: [mandelbrot, multibrot3, tricorn, burningship, buffalo]");
        options.addOption("i", "interior", false, "Use interior sampling");
        options.addOption("e", "exterior", false, "Use exterior sampling (default)");
        options.addOption("a", "all", false, "Use all samples");

        // Create a description for the command
        String header = "Generates a fractal animation given a CSV viewpoint input file and output directory.";
        String footer = "";
        String lineUsage = "java -jar GenerateAnimation.jar [INPUT FILE] [OUTPUT DIRECTORY]";

        // Create the formatter and parsers
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();

        try {
            // Parse the command line arguments.
            CommandLine cmd = parser.parse(options, args);

            // If the help flag is set, print the help message.
            if (cmd.hasOption("h")) {
                formatter.printHelp(lineUsage, header, options, footer, false);
                System.exit(0);
            }

            // Get the width, height, and number of samples.
            int width = 1000;
            if (cmd.hasOption("width")) width = Integer.parseInt(cmd.getOptionValue("width"));

            int height = 1000;
            if (cmd.hasOption("height")) height = Integer.parseInt(cmd.getOptionValue("height"));

            long numSamples = 50000000;
            if (cmd.hasOption("samples")) numSamples = Long.parseLong(cmd.getOptionValue("samples"));

            double bailout = 128.0d;
            if (cmd.hasOption("bailout")) bailout = Double.parseDouble(cmd.getOptionValue("bailout"));

            // Get the function.
            Function function = new FunctionLibrary.Mandelbrot();
            if (cmd.hasOption("function")) {
                switch (cmd.getOptionValue("function")) {
                    case "mandelbrot":
                        function = new FunctionLibrary.Mandelbrot();
                        break;
                    case "multibrot3":
                        function = new FunctionLibrary.Multibrot3();
                        break;
                    case "tricorn":
                        function = new FunctionLibrary.Tricorn();
                        break;
                    case "burningship":
                        function = new FunctionLibrary.BurningShip();
                        break;
                    case "buffalo":
                        function = new FunctionLibrary.Buffalo();
                        break;
                    default:
                        System.err.println("Invalid function choice " + cmd.getOptionValue("function") + ".");
                        System.exit(1);
                }
            }

            // Get the sampling method.
            SamplingMethod samplingMethod = SamplingMethod.EXTERIOR;
            if (cmd.hasOption("interior")) samplingMethod = SamplingMethod.INTERIOR;
            else if (cmd.hasOption("exterior")) samplingMethod = SamplingMethod.EXTERIOR;
            else if (cmd.hasOption("all")) samplingMethod = SamplingMethod.ALL;

            // Get the input and output files.
            String[] arguments = cmd.getArgs();
            if (arguments.length != 2) {
                System.err.println("Invalid number of arguments.");
                formatter.printHelp(lineUsage, header, options, footer, false);
                System.exit(1);
            }
            String inputFile = arguments[0];
            String outputDir = arguments[1];

            // Generate the animation.
            try {
                genAnimationFile(inputFile, outputDir, width, height, numSamples, bailout, function, new Plane4D(width, height), samplingMethod);
            } catch (IOException e) {
                System.err.println("Error when attempting to access files: " + e.getMessage());
                System.exit(1);
            }
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(1);
        }
    }
}
