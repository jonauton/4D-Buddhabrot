package mandelbrot;

/**
 * Represents a complex number.
 */
public class Complex {
    private double real;
    private double imag;

    /**
     * Creates a new Complex number.
     *
     * @param real The real part.
     * @param imag The imaginary part.
     */
    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    /**
     * Gets the real part.
     *
     * @return The real part.
     */
    public double getReal() {
        return real;
    }

    /**
     * Gets the imaginary part.
     *
     * @return The imaginary part.
     */
    public double getImag() {
        return imag;
    }

    /**
     * Gets the magnitude of the complex number.
     *
     * @return The magnitude of the complex number.
     */
    public double magnitude() {
        return Math.sqrt(real * real + imag * imag);
    }

    /**
     * Adds two complex numbers.
     *
     * @param c1 The first complex number.
     * @param c2 The second complex number.
     * @return The sum of the two complex numbers.
     */
    public static Complex add(Complex c1, Complex c2) {
        return new Complex(c1.getReal() + c2.getReal(), c1.getImag() + c2.getImag());
    }

    /**
     * Adds a complex number to this complex number.
     *
     * @param c The complex number to add.
     * @return The sum of the two complex numbers.
     */
    public Complex add(Complex c) {
        return add(this, c);
    }

    /**
     * Multiplies two complex numbers.
     *
     * @param c1 The first complex number.
     * @param c2 The second complex number.
     * @return The product of the two complex numbers.
     */
    public static Complex multiply(Complex c1, Complex c2) {
        // (a + bi)(c + di) = (ac - bd) + (ad + bc)i
        return new Complex(c1.getReal() * c2.getReal() - c1.getImag() * c2.getImag(),
                c1.getReal() * c2.getImag() + c1.getImag() * c2.getReal());
    }

    /**
     * Multiplies this complex number by another complex number.
     *
     * @param c The complex number to multiply by.
     * @return The product of the two complex numbers.
     */
    public Complex multiply(Complex c) {
        return multiply(this, c);
    }

    /**
     * Squares this complex number.
     *
     * @return The square of this complex number.
     */
    public Complex square() {
        return multiply(this);
    }

    /**
     * Divides two complex numbers.
     *
     * @param c1 The first complex number.
     * @param c2 The second complex number.
     * @return The quotient of the two complex numbers.
     */
    public static Complex divide(Complex c1, Complex c2) {
        // (a + bi)/(c + di) = (a + bi)(c - di)/(c^2 + d^2)
        double denominator = c2.getReal() * c2.getReal() + c2.getImag() * c2.getImag();
        return new Complex((c1.getReal() * c2.getReal() + c1.getImag() * c2.getImag()) / denominator,
                (c1.getImag() * c2.getReal() - c1.getReal() * c2.getImag()) / denominator);
    }

    /**
     * Divides this complex number by another complex number.
     *
     * @param c The complex number to divide by.
     * @return The quotient of the two complex numbers.
     */
    public Complex divide(Complex c) {
        return divide(this, c);
    }
}
