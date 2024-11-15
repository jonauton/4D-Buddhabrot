To run the GUI, simply open this folder in a terminal and run:
java -jar FractalViewer4D.jar

For additional arguments, read:
usage: java -jar GenerateAnimation.jar
Start a GUI window to interact with the generalised M-set, and 4D
Buddhabrot.
 -a,--all              Use all samples
 -b,--bailout <arg>    Bailout value (default: 128.0)
 -e,--exterior         Use exterior sampling (default)
 -f,--function <arg>   The choice of function, choices must be one of:
                       [mandelbrot, multibrot3, tricorn, burningship,
                       buffalo]
 -h,--help             Print this message.
 -i,--interior         Use interior sampling

The contols include:
- CTRL+R to reset the space to the initial position.
- WASD to locally translate the viewing plane up and down, left and right.
- Z, X, Space, Backspace to locally translate forward, backwards, w-into, w-out of.
- 123456 to locally rotate in the xy, xz, yz, xw, zw planes where such planes are
defined with x being horizontal and y being vertical with respect to the current plane.
- CTRL+(123456) to locally rotate in the opposite directions as above.
- ARROW_UP, ARROW_DOWN to increase, decrease iterations by 25.
- SHIFT+(ARROW_UP, ARROW_DOWN) to increase, decrease iterations by 1.
- ARROW_LEFT, ARROW_RIGHT to decrease, increase samples by a factor of 10.
- CTRL+S to save the current image to a file.
- CTRL+P to add the current viewpoint to a CSV.
- B to change between cross-sectional and projectional view.
- SHIFT+B to change between a cross-sectional and projectional view on a 4D sample space.
- The scroll-wheel is used to zoom into and out of the image.
- The mouse can be dragged across the screen to move the viewing plane instead of WASD.

To create a Buddhabrot, start the GUI then press B. Press "3" 18 times then "4" 18 times, this
will rotate you 90 degrees in the two planes and create the Buddhabrot. To improve the quality of the image,
press ARROW_RIGHT. Use the scrollwheel and mouse to zoom and pan, and save any interesting images with CTRL+S.

If you move in the space, then rotation will be with respect to that new position.
If you get lost, press CTRL+R to reset to the initial position.

To generate an animation, first use CRTL+P to save viewpoints in an animation file with the GUI
(this will automatically create a folder "animation_files/") then run:
java -jar GenerateAnimation.jar animation_files/anim.csv animation_files/anim/

NOTE: the settings during the GUI mode are not saved in the viewpoint file. Select the appropriate parameters
to correctly reconstruct the experience with the GUI. This method cannot generate cross-section animations, 
only projection animations (seen when B or SHIFT+B is pressed when using the GUI).

For additional arguments, read:
usage: java -jar GenerateAnimation.jar [INPUT FILE] [OUTPUT DIRECTORY]
Generates a fractal animation given a CSV viewpoint input file and output
directory.
 -a,--all              Use all samples
 -b,--bailout <arg>    Bailout value (default: 128.0)
 -e,--exterior         Use exterior sampling (default)
 -f,--function <arg>   The choice of function, choices must be one of:
                       [mandelbrot, multibrot3, tricorn, burningship,
                       buffalo]
 -h,--help             Print this message.
 -i,--interior         Use interior sampling
 -s,--samples <arg>    Number of samples taken per image (default:
                       50000000)
 -v,--height <arg>     Height (px) of the output images (default: 1000)
 -w,--width <arg>      Width (px) of the output images (default: 1000)