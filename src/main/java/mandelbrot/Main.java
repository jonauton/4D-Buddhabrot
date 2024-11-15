package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.cli.*;

import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * The main class.
 */
public class Main {

    // Resolution of the window
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;
    public static Function f;
    public static double bailout;
    public static SamplingMethod samplingMethod;

    /**
     * The main method. Creates the interactive GUI window and starts the program.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args ) {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("f", "function", true, "The choice of function, choices must be one of: [mandelbrot, multibrot3, tricorn, burningship, buffalo]");
        options.addOption("b", "bailout", true, "Bailout value (default: 128.0)");
        options.addOption("i", "interior", false, "Use interior sampling");
        options.addOption("e", "exterior", false, "Use exterior sampling (default)");
        options.addOption("a", "all", false, "Use all samples");

        // Create a description for the command
        String header = "Start a GUI window to interact with the generalised M-set, and 4D Buddhabrot.";
        String footer = "";
        String lineUsage = "java -jar GenerateAnimation.jar";

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

            bailout = 128.0d;
            if (cmd.hasOption("bailout")) bailout = Double.parseDouble(cmd.getOptionValue("bailout"));
            // Get the function.
            f = new FunctionLibrary.Mandelbrot();
            if (cmd.hasOption("function")) {
                switch (cmd.getOptionValue("function")) {
                    case "mandelbrot":
                        f = new FunctionLibrary.Mandelbrot();
                        break;
                    case "multibrot3":
                        f = new FunctionLibrary.Multibrot3();
                        break;
                    case "tricorn":
                        f = new FunctionLibrary.Tricorn();
                        break;
                    case "burningship":
                        f = new FunctionLibrary.BurningShip();
                        break;
                    case "buffalo":
                        f = new FunctionLibrary.Buffalo();
                        break;
                    default:
                        System.err.println("Invalid function choice " + cmd.getOptionValue("function") + ".");
                        System.exit(1);
                }
            }

            // Get the sampling method.
            samplingMethod = SamplingMethod.EXTERIOR;
            if (cmd.hasOption("interior")) samplingMethod = SamplingMethod.INTERIOR;
            else if (cmd.hasOption("exterior")) samplingMethod = SamplingMethod.EXTERIOR;
            else if (cmd.hasOption("all")) samplingMethod = SamplingMethod.ALL;

            // Get the input and output files.
            String[] arguments = cmd.getArgs();
            if (arguments.length != 0) {
                System.err.println("Invalid number of arguments.");
                formatter.printHelp(lineUsage, header, options, footer, false);
                System.exit(1);
            }

        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(1);
        }

        JFrame frame = new JFrame("FractalViewer4D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setPreferredSize(frame.getSize());
        frame.getContentPane().add(new MandelbrotPanel(WIDTH, HEIGHT, f, bailout, samplingMethod));
        frame.pack();
        frame.setVisible(true);
    }
}
