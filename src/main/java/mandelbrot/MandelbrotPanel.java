package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.PI;
import static java.lang.Math.max;


/**
 * This class is the main GUI component of the program.
 * It is a JPanel that displays the Mandelbrot set or Buddhabrot.
 * It also handles user input and updates the displayed image accordingly.
 */
public class MandelbrotPanel extends JPanel {
    private Point dragStart;
    private Point mousePos;
    private BufferedImage image;
    private int maxIter;
    private long numSamples;
    private Plane4D plane;
    private final double rotationSpeed = PI / 72;
    private final double movementSpeed = 0.01;
    private static final int FPS = 60;
    private IntegerImage imageRed;
    private IntegerImage imageGreen;
    private IntegerImage imageBlue;
    private ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private Plane4D savedPlane;
    private final SamplingMethod samplingMethod;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static SamplePlane4D samplePlane;
    private final SampleCube4D sampleCube;
    private final Function function;
    private final double bailout;

    private State state = State.CROSS_SECTION;
    private enum State {
        CROSS_SECTION,
        BUDDHABROT,
        FULL_BUDDHABROT
    }

    /**
     * This method is called by the constructor to initialize the MandelbrotPanel.
     * It initializes the panel and sets up the mouse and keyboard listeners.
     */
    public MandelbrotPanel(int width, int height, Function f, double bailout, SamplingMethod samplingMethod) {
        // Create the panel and initialize the variables
        super();
        this.function = f;
        this.bailout = bailout;
        this.samplingMethod = samplingMethod;
        this.sampleCube = new SampleCube4D(samplingMethod, new Quaternion(-2.0d, -2.0d, -2.0d, -2.0d), new Quaternion(2.0d, 2.0d, 2.0d, 2.0d));
        resetView(width, height);

        // Track mouse clicks, and the initial click position of a drag
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateRegion(dragStart, e.getPoint());
                dragStart = null;
                restartRender();
            }
        });

        // Track mouse movement, and update the view if the user is dragging
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePos = e.getPoint();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
            }
        });

        // Track mouse wheel movement, and zoom in/out accordingly
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = e.getWheelRotation() > 0 ? 0.95 : 1.05;  // e.getWheelRotation()) is -1 if the wheel was rotated up, 1 if the wheel was rotated down
                plane.scaleScaleX(zoomFactor);
                plane.scaleScaleY(zoomFactor);
                restartRender();
            }
        });

        Action resetViewAction = new AbstractAction("Reset View") {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
            }
        };

        Action increaseMaxIterAction = new AbstractAction("Increase Max Iterations") {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxIter = (int) Math.max(maxIter + 25, 25);
                restartRender();
            }
        };

        Action decreaseMaxIterAction = new AbstractAction("Decrease Max Iterations") {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxIter = Math.max(maxIter - 25, 25);
                restartRender();
            }
        };

        Action littleIncreaseMaxIterAction = new AbstractAction("Little Increase Max Iterations") {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxIter = (int) Math.max(maxIter + 1, 1);
                restartRender();
            }
        };

        Action littleDecreaseMaxIterAction = new AbstractAction("Little Decrease Max Iterations") {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxIter = Math.max(maxIter - 1, 1);
                restartRender();
            }
        };

        Action swapBuddha = new AbstractAction("Swap Buddha") {
            @Override
            public void actionPerformed(ActionEvent e) {
                state = state == State.BUDDHABROT ? State.CROSS_SECTION : State.BUDDHABROT;
                System.out.println("Buddhabrot active: " + state);
                samplePlane = new SamplePlane4D(
                        samplingMethod,
                        function,
                        new Plane4D(getWidth() / 2.0d, getHeight() / 2.0d),
                        getWidth(),
                        getHeight(),
                        200,
                        bailout
                );
                restartRender();
            }
        };

        Action swapFullBuddha = new AbstractAction("Swap Full Buddha") {
            @Override
            public void actionPerformed(ActionEvent e) {
                state = state == State.FULL_BUDDHABROT ? State.CROSS_SECTION : State.FULL_BUDDHABROT;
                System.out.println("Buddhabrot active: " + state);
                restartRender();
            }
        };

        Action saveImage = new AbstractAction("Save Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (state == State.BUDDHABROT)
                    saveImageToFile(ColorMap.colorNebula(imageRed, imageGreen, imageBlue, 1.0d));
                else
                    saveImageToFile(image);
            }
        };

        Action increaseSamples = new AbstractAction("Increase Samples") {
            @Override
            public void actionPerformed(ActionEvent e) {
                numSamples = Math.max(numSamples * 10L, 1);
                System.out.println("Samples: " + numSamples);
                restartRender();
            }
        };

        Action decreaseSamples = new AbstractAction("Decrease Samples") {
            @Override
            public void actionPerformed(ActionEvent e) {
                numSamples = Math.max(numSamples / 10L, 1);
                System.out.println("Samples: " + numSamples);
                restartRender();
            }
        };

        Action rotXY = new AbstractAction("Rotate XY") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXY(rotationSpeed);
                restartRender();
            }
        };

        Action negRotXY = new AbstractAction("Rotate -XY") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXY(-rotationSpeed);
                restartRender();
            }
        };

        Action rotXZ = new AbstractAction("Rotate XZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXZ(rotationSpeed);
                restartRender();
            }
        };

        Action negRotXZ = new AbstractAction("Rotate -XZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXZ(-rotationSpeed);
                restartRender();
            }
        };

        Action rotXW = new AbstractAction("Rotate XW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXW(rotationSpeed);
                restartRender();
            }
        };

        Action negRotXW = new AbstractAction("Rotate -XW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateXW(-rotationSpeed);
                restartRender();
            }
        };

        Action rotYZ = new AbstractAction("Rotate YZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateYZ(rotationSpeed);
                restartRender();
            }
        };

        Action negRotYZ = new AbstractAction("Rotate -YZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateYZ(-rotationSpeed);
                restartRender();
            }
        };

        Action rotYW = new AbstractAction("Rotate YW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateYW(rotationSpeed);
                restartRender();
            }
        };

        Action negRotYW = new AbstractAction("Rotate -YW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateYW(-rotationSpeed);
                restartRender();
            }
        };

        Action rotZW = new AbstractAction("Rotate ZW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateZW(rotationSpeed);
                restartRender();
            }
        };

        Action negRotZW = new AbstractAction("Rotate -ZW") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.rotateZW(-rotationSpeed);
                restartRender();
            }
        };

        Action moveX = new AbstractAction("Move X") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveX(movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action negMoveX = new AbstractAction("Move -X") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveX(-movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action moveY = new AbstractAction("Move Y") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveY(movementSpeed * getHeight() / plane.getScaleY());
                restartRender();
            }
        };

        Action negMoveY = new AbstractAction("Move -Y") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveY(-movementSpeed * getHeight() / plane.getScaleY());
                restartRender();
            }
        };

        Action moveZ = new AbstractAction("Move Z") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveZ(movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action negMoveZ = new AbstractAction("Move -Z") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveZ(-movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action moveW = new AbstractAction("Move W") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveW(movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action negMoveW = new AbstractAction("Move -W") {
            @Override
            public void actionPerformed(ActionEvent e) {
                plane.relativeMoveW(-movementSpeed * getWidth() / plane.getScaleX());
                restartRender();
            }
        };

        Action saveView = new AbstractAction("Save View") {
            @Override
            public void actionPerformed(ActionEvent e) {
                savedPlane = plane.copy();
            }
        };

        Action lerpSaved = new AbstractAction("LERP to Saved View") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (savedPlane != null) {
                    plane.slerp(savedPlane, 0.5);
                    restartRender();
                }
            }
        };

        Action saveViewCSV = new AbstractAction("Save View CSV") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCameraPosition("anim", 0.01);
            }
        };

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "saveView");
        actionMap.put("saveView", saveView);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "lerpSaved");
        actionMap.put("lerpSaved", lerpSaved);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "saveViewCSV");
        actionMap.put("saveViewCSV", saveViewCSV);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "resetView");
        actionMap.put("resetView", resetViewAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "increaseMaxIter");
        actionMap.put("increaseMaxIter", increaseMaxIterAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "decreaseMaxIter");
        actionMap.put("decreaseMaxIter", decreaseMaxIterAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "littleIncreaseMaxIter");
        actionMap.put("littleIncreaseMaxIter", littleIncreaseMaxIterAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "littleDecreaseMaxIter");
        actionMap.put("littleDecreaseMaxIter", littleDecreaseMaxIterAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "swapBuddha");
        actionMap.put("swapBuddha", swapBuddha);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.SHIFT_DOWN_MASK), "swapFullBuddha");
        actionMap.put("swapFullBuddha", swapFullBuddha);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveImage");
        actionMap.put("saveImage", saveImage);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "increaseSamples");
        actionMap.put("increaseSamples", increaseSamples);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "decreaseSamples");
        actionMap.put("decreaseSamples", decreaseSamples);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "rotXY");
        actionMap.put("rotXY", rotXY);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK), "negRotXY");
        actionMap.put("negRotXY", negRotXY);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "rotXZ");
        actionMap.put("rotXZ", rotXZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK), "negRotXZ");
        actionMap.put("negRotXZ", negRotXZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "rotYZ");
        actionMap.put("rotYZ", rotYZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK), "negRotYZ");
        actionMap.put("negRotYZ", negRotYZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "rotXW");
        actionMap.put("rotXW", rotXW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK), "negRotXW");
        actionMap.put("negRotXW", negRotXW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0), "rotYW");
        actionMap.put("rotYW", rotYW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK), "negRotYW");
        actionMap.put("negRotYW", negRotYW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0), "rotZW");
        actionMap.put("rotZW", rotZW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK), "negRotZW");
        actionMap.put("negRotZW", negRotZW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "moveZ");
        actionMap.put("moveZ", moveZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "negMoveZ");
        actionMap.put("negMoveZ", negMoveZ);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "negMoveX");
        actionMap.put("negMoveX", negMoveX);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "moveX");
        actionMap.put("moveX", moveX);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "moveY");
        actionMap.put("moveY", moveY);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "negMoveY");
        actionMap.put("negMoveY", negMoveY);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "moveW");
        actionMap.put("moveW", moveW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "negMoveW");
        actionMap.put("negMoveW", negMoveW);


        // Update the panel at a constant frame rate
        Timer timer = new Timer(1000 / FPS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Repaint the panel
                repaint();
            }
        });
        timer.start();
    }

    /**
     * Drags the rendered image along with the mouse.
     * @param dragStart The point where the drag started (the previous mouse position)
     * @param dragEnd The point where the drag ended (the current mouse position)
     */
    private void updateRegion(Point dragStart, Point dragEnd) {
        if (dragStart == null || dragEnd == null) return;

        // Move the region along the direction of the drag
        double dx = dragStart.x - dragEnd.x;
        double dy = dragStart.y - dragEnd.y;

        // Calculate the pan
        double realPan = dx / plane.getScaleX();
        double imagPan = dy / plane.getScaleY();

        // Translate the region
        plane.relativeMoveX(realPan);
        plane.relativeMoveY(imagPan);

        // Repaint the panel
        repaint();
    }


    /**
     * This method is called whenever the panel needs to be repainted.
     * For example, when the window is resized, this method is called.
     * This should not be called directly, but can be called by calling repaint().
     * @param g The graphics object to draw with
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // If the user is dragging the image, simply translate the image by the change in position
        if (dragStart != null) {
            int dx = mousePos.x - dragStart.x;
            int dy = mousePos.y - dragStart.y;
            g.drawImage(image, dx, dy, this);

            return;
        }

        // Pick the correct image generation method, store the image in the image variable
        if (state == State.CROSS_SECTION) {
            g.drawImage(image, 0, 0, this);
        }
        else {
            image = ColorMap.colorNebula(imageRed.copy(), imageGreen.copy(), imageBlue.copy(), 1.0d);
            g.drawImage(image, 0, 0, this);
        }
    }

    /**
     * Restarts the rendering process, by creating a new thread to render the image.
     */
    protected void restartRender() {
        restartRender(getWidth(), getHeight());
    }

    /**
     * Restarts the rendering process, by creating a new thread to render the image.
     */
    protected void restartRender(int width, int height) {
        // Pick the correct image generation method, store the image in the image variable
        if (state == State.CROSS_SECTION) {
            image = FractalImageGen.generateCrossSection(function, plane, width, height, maxIter, bailout);
        }
        else if (state == State.BUDDHABROT) {
            imageExecutor.shutdownNow();
            try {
                imageExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            imageExecutor = Executors.newFixedThreadPool(THREADS);

            imageRed = new IntegerImage(width, height);
            imageGreen = new IntegerImage(width, height);
            imageBlue = new IntegerImage(width, height);

            FractalImageGen.generateNebulaProjection(
                    function,
                    //new SampleCube4D(samplingMethod, new Quaternion(-2.0d, -2.0d, -2.0d, -2.0d), new Quaternion(2.0d, 2.0d, 2.0d, 2.0d)),
                    samplePlane,
                    plane,
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

            imageExecutor.shutdown();
        }
        else if (state == State.FULL_BUDDHABROT) {
            imageExecutor.shutdownNow();
            try {
                imageExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            imageExecutor = Executors.newFixedThreadPool(THREADS);

            imageRed = new IntegerImage(width, height);
            imageGreen = new IntegerImage(width, height);
            imageBlue = new IntegerImage(width, height);

            FractalImageGen.generateNebulaProjection(
                    function,
                    sampleCube,
                    plane,
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

            imageExecutor.shutdown();
        }
    }

    /**
     * Resets the variables to their default values, and repaints the panel.
     */
    public void resetView() {
        resetView(getWidth(), getHeight());
    }

    /**
     * Resets the variables to their default values, and repaints the panel.
     * @param width The width of the panel (-1 to use panel width)
     * @param height The height of the panel (-1 to use panel height)
     */
    public void resetView(int width, int height) {
        System.out.println("Resetting view");
        // Reset the variables
        maxIter = 100;
        numSamples = 3_000_000L;

        // Reset the camera
        plane = new Plane4D(width, height);
        savedPlane = new Plane4D(width, height);
        resetSamplePlane(width, height);
        // Restart the rendering process
        restartRender(width, height);

        // Repaint the panel
        repaint();
    }

    /**
     * Resets the sample plane to the default values.
     * @param width The width of the sampling plane in pixels.
     * @param height The height of the sampling plane in pixels.
     */
    private void resetSamplePlane(int width, int height) {
        Plane4D samplingPlane = new Plane4D(width / 2.0d, height / 2.0d);
//        samplingPlane.setScaleX(plane.getScaleX());
//        samplingPlane.setScaleY(plane.getScaleY());
        samplePlane = new SamplePlane4D(
                samplingMethod,
                function,
                samplingPlane,
                width,
                height,
                200,
                bailout
        );
    }

    /**
     * Saves the currently rendered image to a file.
     * @param image The image to save
     */
    public static void saveImageToFile(BufferedImage image) {
        try {
            File folder = new File("images/");
            if (!folder.exists()) folder.mkdir();
            File file = new File(folder, String.valueOf(System.currentTimeMillis()) + ".png");
            ImageIO.write(image, "png", file);
            System.out.println("Image saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save image to file.");
            e.printStackTrace();
        }
    }

    /**
     * Adds the current camera position to a csv file, alongside the interpolation factor.
     * @param name The name of the file
     * @param factor The interpolation factor
     */
    public void saveCameraPosition(String name, double factor) {
        try {
            File folder = new File("animation_files/");
            if (!folder.exists()) folder.mkdir();
            File file = new File(folder, name + ".csv");
            FileWriter writer = new FileWriter(file, true);
            writer.append(plane.toCSV()).append(",").append(String.valueOf(factor)).append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
