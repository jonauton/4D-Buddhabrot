package mandelbrot;

import org.apache.commons.math3.complex.Quaternion;

import java.awt.*;

/**
 * A class that represents an oriented plane sitting in a 4D space.
 * The class provides methods for projecting points onto the plane, and for moving and orienting that plane.
 */
public class Plane4D {
    private Quaternion position;
    private Quaternion leftIso;
    private Quaternion rightIso;
    private double scaleX;
    private double scaleY;

    private double[][] projectionMatrix;

    /**
     * Constructor for the Plane4D.
     * @param position The centered position of the plane.
     * @param leftIso The direction the plane is facing, as a rotation. (Orienting the 3d-cross-section so xw, yw, or zw)
     * @param rightIso The orientation of the plane. (Within the 3d-cross-section of the 4d-space so xy, xz, or yz)
     * @param scaleX The scale of the plane in the x-direction.
     * @param scaleY The scale of the plane in the y-direction.
     */
    public Plane4D(Quaternion position, Quaternion leftIso, Quaternion rightIso, double scaleX, double scaleY) {
        this.position = position;
        this.leftIso = leftIso;
        this.rightIso = rightIso;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Simple constructor for the Plane4D.
     * @param width The width (in pixels) of the output image.
     * @param height The height (in pixels) of the output image.
     */
    public Plane4D(int width, int height) {
        this(
                new Quaternion(0, 0, 0, 0),
                new Quaternion(1, 0, 0, 0),
                new Quaternion(1, 0, 0, 0),
                width / 4.0d,
                height / 4.0d
        );
    }

    /**
     * Simple constructor for the Plane4D.
     * @param width The width (in pixels) of the output image.
     * @param height The height (in pixels) of the output image.
     */
    public Plane4D(double width, double height) {
        this(
                new Quaternion(0, 0, 0, 0),
                new Quaternion(1, 0, 0, 0),
                new Quaternion(1, 0, 0, 0),
                width / 4.0d,
                height / 4.0d
        );
    }

    /**
     * Gets the position of the plane.
     * @return The position of the plane.
     */
    public Quaternion getPosition() {
        return position;
    }

    /**
     * Gets the direction the plane is facing, as a rotation. (Normal to the 3d-cross-section of the 4d-space)
     * @return The direction the plane is facing, as a rotation.
     */
    public Quaternion getLeftIso() {
        return leftIso;
    }

    /**
     * Gets the orientation of the plane.
     * @return The orientation of the plane.
     */
    public Quaternion getRightIso() {
        return rightIso;
    }

    /**
     * Gets the scale of the plane in the x-direction.
     * @return The scale of the plane in the x-direction.
     */
    public double getScaleX() {
        return scaleX;
    }

    /**
     * Gets the scale of the plane in the y-direction.
     * @return The scale of the plane in the y-direction.
     */
    public double getScaleY() {
        return scaleY;
    }

    /**
     * Gets the CSV representation of the plane.
     * @return The CSV representation of the plane.
     */
    public String toCSV() {
        return position.getQ0() + "," + position.getQ1() + "," + position.getQ2() + "," + position.getQ3() + "," +
                leftIso.getQ0() + "," + leftIso.getQ1() + "," + leftIso.getQ2() + "," + leftIso.getQ3() + "," +
                rightIso.getQ0() + "," + rightIso.getQ1() + "," + rightIso.getQ2() + "," + rightIso.getQ3() + "," +
                scaleX + "," + scaleY;
    }

    /**
     * Sets the position of the plane.
     * @param position The position of the plane.
     */
    public void setPosition(Quaternion position) {
        this.position = position;
    }

    /**
     * Sets the direction the plane is facing, as a rotation. (Normal to the 3d-cross-section of the 4d-space)
     * @param leftIso The direction the plane is facing, as a rotation.
     */
    public void setLeftIso(Quaternion leftIso) {
        this.leftIso = leftIso;
    }

    /**
     * Sets the orientation of the plane.
     * @param rightIso The orientation of the plane.
     */
    public void setRightIso(Quaternion rightIso) {
        this.rightIso = rightIso;
    }

    /**
     * Sets the scale of the plane in the x-direction.
     * @param scaleX The scale of the plane in the x-direction.
     */
    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    /**
     * Sets the scale of the plane in the y-direction.
     * @param scaleY The scale of the plane in the y-direction.
     */
    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    /**
     * Scales the scale of the plane in the x-direction.
     * @param scaleX The scale of the plane in the x-direction.
     */
    public void scaleScaleX(double scaleX) {
        this.scaleX *= scaleX;
    }

    /**
     * Scales the scale of the plane in the y-direction.
     * @param scaleY The scale of the plane in the y-direction.
     */
    public void scaleScaleY(double scaleY) {
        this.scaleY *= scaleY;
    }

    /**
     * Saves the cached projection sequence as a matrix.
     */
    public void cacheProjection() {
        Quaternion leftIsoInverse = leftIso.getInverse().normalize();
        Quaternion rightIsoInverse = rightIso.getInverse().normalize();
        double a = leftIsoInverse.getQ0();
        double b = leftIsoInverse.getQ1();
        double c = leftIsoInverse.getQ2();
        double d = leftIsoInverse.getQ3();
        double p = rightIsoInverse.getQ0();
        double q = rightIsoInverse.getQ1();
        double r = rightIsoInverse.getQ2();
        double s = rightIsoInverse.getQ3();

        projectionMatrix = new double[][] {
            {a*p-b*q-c*r-d*s, -a*q-b*p+c*s-d*r, -a*r-b*s-c*p+d*q, -a*s+b*r-c*q-d*p},
            {b*p+a*q-d*r+c*s, -b*q+a*p+d*s+c*r, -b*r+a*s-d*p-c*q, -b*s-a*r-d*q+c*p},
            {c*p+d*q+a*r-b*s, -c*q+d*p-a*s-b*r, -c*r+d*s+a*p+b*q, -c*s-d*r+a*q-b*p},
            {d*p-c*q+b*r+a*s, -d*q-c*p-b*s+a*r, -d*r-c*s+b*p-a*q, -d*s+c*r+b*q+a*p}
        };
    }

    /**
     * Projects a point onto the plane, returning the point as a 4D vector.
     * @param point The point to project.
     * @return The projected point.
     */
    public Quaternion projectQuaternion(Quaternion point) {
        // We need to apply the inverse of the plane's orientation and position to the point.
        // Imagine transforming the entire space, then we need the plane to be at the origin and facing the x-axis.
        // Then we can project the point onto the plane by simply ignoring the z and w coordinates.

        // Step 1: Correct translation.
        point = point.subtract(position);

        // Step 2: Correct orientation. It can be shown that we represent the rotation with a
        // left-isoclinic rotation and a right-isoclinic rotation, combined with Q_L * p * Q_R.
        // Therefore, the inverse is given by Q_L^-1 * p * Q_R^-1.
        point = leftIso.getInverse().multiply(point).multiply(rightIso.getInverse());

        // Step 3: The projection of the point onto the plane is in (x, y)
        return point;
    }

    /**
     * Projects a point onto the plane, returning the point as a 4D vector. Uses the cached projection matrix.
     * @param point The point to project.
     * @return The projected point.
     */
    public Quaternion cachedProjectQuaternion(Quaternion point) {
        // Step 1: Correct translation.
        double a = point.getQ0() - position.getQ0();
        double b = point.getQ1() - position.getQ1();
        double c = point.getQ2() - position.getQ2();
        double d = point.getQ3() - position.getQ3();

        // Step 2: Correct orientation.
        point = new Quaternion(
                projectionMatrix[0][0] * a + projectionMatrix[0][1] * b + projectionMatrix[0][2] * c + projectionMatrix[0][3] * d,
                projectionMatrix[1][0] * a + projectionMatrix[1][1] * b + projectionMatrix[1][2] * c + projectionMatrix[1][3] * d,
                projectionMatrix[2][0] * a + projectionMatrix[2][1] * b + projectionMatrix[2][2] * c + projectionMatrix[2][3] * d,
                projectionMatrix[3][0] * a + projectionMatrix[3][1] * b + projectionMatrix[3][2] * c + projectionMatrix[3][3] * d
        );

        // Step 3: The projection of the point onto the plane is in (x, y)
        return point;
    }

    /**
     * Projects a point onto a pixel position using the cached projection matrix.
     * @param point The point to project.
     * @param width The width of the screen.
     * @param height The height of the screen.
     * @return The pixel position if the point is on the plane, null otherwise.
     */
    public Point cachedProjectToPixel(Quaternion point, int width, int height) {
        // Project the point onto the plane.
        Quaternion projected = cachedProjectQuaternion(point);

        // Scale the point to the screen and translate the point to the center of the screen.
        Point projectedPoint = new Point(
                (int)(projected.getQ0() * scaleX + width  / 2.0d),
                (int)(projected.getQ1() * scaleY + height / 2.0d)
        );

        // Return the pixel position.
        if (projectedPoint.getX() >= 0 && projectedPoint.getX() < width &&
            projectedPoint.getY() >= 0 && projectedPoint.getY() < height)
            return projectedPoint;
        else return null;
    }

    /**
     * Projects a point onto a pixel position using the cached projection matrix. Adds Perspective.
     * @param point The point to project.
     * @param width The width of the screen.
     * @param height The height of the screen.
     * @param maxScalingFactor The maximum amount of scaling to apply.
     * @param focalLength The focal length of the camera.
     * @param allowBehindCamera Whether to allow points behind the camera.
     * @return The pixel position if the point is on the plane, null otherwise.
     */
    public Point cachedProjectToPixelPerspective(Quaternion point, int width, int height, double maxScalingFactor, double focalLength, boolean allowBehindCamera) {
        // Project the point onto the plane.
        Quaternion projected = cachedProjectQuaternion(point);

        // Get the distance of the point from the camera, this is the norm of the vector (z, w).
        if (!allowBehindCamera && (projected.getQ2() < 0 || projected.getQ3() < 0)) return null; // Behind the camera (z < 0 or w < 0)
        double distance = Math.sqrt(projected.getQ2() * projected.getQ2() + projected.getQ3() * projected.getQ3());
        if (distance < 1e-5) return null;

        // Apply perspective projection.
        double projX, projY;
        if (maxScalingFactor > 0) {
            projX = (projected.getQ0() * focalLength) / Math.max(distance, focalLength / maxScalingFactor);
            projY = (projected.getQ1() * focalLength) / Math.max(distance, focalLength / maxScalingFactor);
        }
        else {
            projX = projected.getQ0() * focalLength / distance;
            projY = projected.getQ1() * focalLength / distance;
        }

        // Scale the point to the screen and translate the point to the center of the screen.
        Point projectedPoint = new Point(
                (int)(projX * scaleX + width  / 2.0d),
                (int)(projY * scaleY + height / 2.0d)
        );

        // Return the pixel position.
        if (projectedPoint.getX() >= 0 && projectedPoint.getX() < width &&
            projectedPoint.getY() >= 0 && projectedPoint.getY() < height)
            return projectedPoint;
        else return null;
    }

    /**
     * Projects a point onto a pixel position.
     * @param point The point to project.
     * @param width The width of the screen.
     * @param height The height of the screen.
     * @return The pixel position if the point is on the plane, null otherwise.
     */
    public Point projectToPixel(Quaternion point, int width, int height) {
        // Project the point onto the plane.
        Quaternion projected = projectQuaternion(point);

        // Scale the point to the screen and translate the point to the center of the screen.
        Point projectedPoint = new Point(
                (int)(projected.getQ0() * scaleX + width  / 2.0d),
                (int)(projected.getQ1() * scaleY + height / 2.0d)
        );

        // Return the pixel position.
        if (projectedPoint.getX() >= 0 && projectedPoint.getX() < width &&
            projectedPoint.getY() >= 0 && projectedPoint.getY() < height)
            return projectedPoint;
        else return null;
    }

    /**
     * Finds the point on the plane that corresponds to a pixel position.
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param width The width of the screen.
     * @param height The height of the screen.
     */
    public Quaternion getPointAtPixel(int x, int y, int width, int height) {
        return getPointAtPixel((double)x, (double)y, width, height);
    }

    /**
     * Finds the point on the plane that corresponds to a non-integer pixel position.
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param width The width of the screen.
     * @param height The height of the screen.
     */
    public Quaternion getPointAtPixel(double x, double y, int width, int height) {
        // Find the position on the unoriented plane.
        Quaternion position = new Quaternion((x - width / 2.0d) / scaleX, (y - height / 2.0d) / scaleY, 0.0d, 0.0d);

        // Apply the orientation.
        position = leftIso.multiply(position).multiply(rightIso);

        // Apply the translation.
        position = position.add(this.position);

        // Return the point.
        return position;
    }

    /**
     * Rotates about the xy-plane
     * @param angle The angle to rotate by.
     */
    public void rotateXY(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), Math.sin(angle / 2.0d), 0.0d, 0.0d));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), Math.sin(angle / 2.0d), 0.0d, 0.0d)).multiply(rightIso);
    }

    /**
     * Rotates about the zw-plane
     * @param angle The angle to rotate by.
     */
    public void rotateZW(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), Math.sin(angle / 2.0d), 0.0d, 0.0d));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), -Math.sin(angle / 2.0d), 0.0d, 0.0d)).multiply(rightIso);
    }

    /**
     * Rotates about the xz-plane
     * @param angle The angle to rotate by.
     */
    public void rotateXZ(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), 0.0d, Math.sin(angle / 2.0d), 0.0d));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), 0.0d, Math.sin(angle / 2.0d), 0.0d)).multiply(rightIso);
    }

    /**
     * Rotates about the yw-plane
     * @param angle The angle to rotate by.
     */
    public void rotateYW(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), 0.0d, Math.sin(angle / 2.0d), 0.0d));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), 0.0d, -Math.sin(angle / 2.0d), 0.0d)).multiply(rightIso);
    }

    /**
     * Rotates about the xw-plane
     * @param angle The angle to rotate by.
     */
    public void rotateXW(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), 0.0d, 0.0d, Math.sin(angle / 2.0d)));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), 0.0d, 0.0d, Math.sin(angle / 2.0d))).multiply(rightIso);
    }

    /**
     * Rotates about the yz-plane
     * @param angle The angle to rotate by.
     */
    public void rotateYZ(double angle) {
        leftIso = leftIso.multiply(new Quaternion(Math.cos(angle / 2.0d), 0.0d, 0.0d, Math.sin(angle / 2.0d)));
        rightIso = (new Quaternion(Math.cos(angle / 2.0d), 0.0d, 0.0d, -Math.sin(angle / 2.0d))).multiply(rightIso);
    }

    /**
     * Moves the plane in the x direction.
     * @param amount The amount to move by.
     */
    public void absoluteMoveX(double amount) {
        position = position.add(new Quaternion(amount, 0.0d, 0.0d, 0.0d));
    }

    /**
     * Moves the plane in the y direction.
     * @param amount The amount to move by.
     */
    public void absoluteMoveY(double amount) {
        position = position.add(new Quaternion(0.0d, amount, 0.0d, 0.0d));
    }

    /**
     * Moves the plane in the z direction.
     * @param amount The amount to move by.
     */
    public void absoluteMoveZ(double amount) {
        position = position.add(new Quaternion(0.0d, 0.0d, amount, 0.0d));
    }

    /**
     * Moves the plane in the w direction.
     * @param amount The amount to move by.
     */
    public void absoluteMoveW(double amount) {
        position = position.add(new Quaternion(0.0d, 0.0d, 0.0d, amount));
    }

    /**
     * Moves the plane in the x direction relative to the plane.
     * @param amount The amount to move by.
     */
    public void relativeMoveX(double amount) {
        // First we need to find the direction of the x component after rotation.
        Quaternion xDir = new Quaternion(1.0d, 0.0d, 0.0d, 0.0d);
        xDir = leftIso.multiply(xDir).multiply(rightIso);

        // Now we can move in that direction.
        position = position.add(xDir.multiply(amount));
    }

    /**
     * Moves the plane in the y direction relative to the plane.
     * @param amount The amount to move by.
     */
    public void relativeMoveY(double amount) {
        // First we need to find the direction of the y component after rotation.
        Quaternion yDir = new Quaternion(0.0d, 1.0d, 0.0d, 0.0d);
        yDir = leftIso.multiply(yDir).multiply(rightIso);

        // Now we can move in that direction.
        position = position.add(yDir.multiply(amount));
    }

    /**
     * Moves the plane in the z direction relative to the plane.
     * @param amount The amount to move by.
     */
    public void relativeMoveZ(double amount) {
        // First we need to find the direction of the z component after rotation.
        Quaternion zDir = new Quaternion(0.0d, 0.0d, 1.0d, 0.0d);
        zDir = leftIso.multiply(zDir).multiply(rightIso);

        // Now we can move in that direction.
        position = position.add(zDir.multiply(amount));
    }

    /**
     * Moves the plane in the w direction relative to the plane.
     * @param amount The amount to move by.
     */
    public void relativeMoveW(double amount) {
        // First we need to find the direction of the w component after rotation.
        Quaternion wDir = new Quaternion(0.0d, 0.0d, 0.0d, 1.0d);
        wDir = leftIso.multiply(wDir).multiply(rightIso);

        // Now we can move in that direction.
        position = position.add(wDir.multiply(amount));
    }

    /**
     * Copies the plane, for managing varying viewpoints.
     */
    public Plane4D copy() {
        return new Plane4D(position, leftIso, rightIso, scaleX, scaleY);
    }

    /**
     * Linearly interpolates between two planes. (fast but incorrect)
     * @param plane The plane to interpolate from.
     * @param other The other plane to interpolate to.
     * @param t The interpolation factor, between 0 and 1.
     */
    public static Plane4D lerp(Plane4D plane, Plane4D other, double t) {
        Quaternion position = plane.position.multiply(1 - t).add(other.position.multiply(t));
        Quaternion leftIso = plane.leftIso.multiply(1 - t).add(other.leftIso.multiply(t)).normalize();
        Quaternion rightIso = plane.rightIso.multiply(1 - t).add(other.rightIso.multiply(t)).normalize();
        double scaleX = plane.scaleX * (1 - t) + other.scaleX * t;
        double scaleY = plane.scaleY * (1 - t) + other.scaleY * t;

        return new Plane4D(position, leftIso, rightIso, scaleX, scaleY);
    }

    /**
     * Linearly interpolates between two planes. (fast but incorrect)
     * @param other The other plane to interpolate to.
     * @param t The interpolation factor, between 0 and 1.
     */
    public void lerp(Plane4D other, double t) {
        Plane4D res = lerp(this, other, t);
        position = res.position;
        leftIso = res.leftIso;
        rightIso = res.rightIso;
        scaleX = res.scaleX;
        scaleY = res.scaleY;
    }

    /**
     * SLERP for quaternions.
     * @param q1 The first quaternion.
     * @param q2 The second quaternion.
     * @param t The interpolation factor, between 0 and 1.
     * @return The interpolated quaternion.
     */
    private static Quaternion slerpQuat(Quaternion q1, Quaternion q2, double t) {
        double dot = q1.dotProduct(q2);

        // If the dot product is negative, the quaternions
        // are more than 90 degrees apart, so we invert one.
        if (dot < 0) {
            q2 = q2.multiply(-1);
            dot = -dot;
        }

        // If the quaternions are close enough, we can
        // just use linear interpolation.
        if (dot > 0.9995) {
            return q1.multiply(1 - t).add(q2.multiply(t)).normalize();
        }

        // We want to interpolate the rotation of the plane,
        // with respect to the global rotation of both planes.
        double omega = Math.acos(dot);
        double sinOmega = Math.sin(omega);
        return q1.multiply(Math.sin((1 - t) * omega) / sinOmega).add(q2.multiply(Math.sin(t * omega) / sinOmega)).normalize();
    }

    /**
     * (Hyper)Spherically interpolates between two planes. (slow but correct)
     * @param plane The plane to interpolate from.
     * @param other The other plane to interpolate to.
     * @param t The interpolation factor, between 0 and 1.
     */
    public static Plane4D slerp(Plane4D plane, Plane4D other, double t) {
        // We want to interpolate the rotation of the plane, with respect to the global rotation of both planes.
        Quaternion ql1 = plane.leftIso;
        Quaternion qr1 = plane.rightIso;
        Quaternion ql2 = other.leftIso;
        Quaternion qr2 = other.rightIso;

        double dot1 = ql1.dotProduct(ql2);
        double dot2 = qr1.dotProduct(qr2);

        // If both dot products are negative then both quaternions are more than 90 degrees apart, so we invert both.
        if (dot1 < 0 && dot2 < 0) {
            ql2 = ql2.multiply(-1);
            qr2 = qr2.multiply(-1);
            dot1 = -dot1;
            dot2 = -dot2;
        }

        // Interpolate the left-isoclinic rotation.
        Quaternion leftIso;
        if (dot1 > 0.999) {
            // If the quaternions are close enough, we can just use linear interpolation.
            leftIso = ql1.multiply(1 - t).add(ql2.multiply(t)).normalize();
        }
        else {
            // We want to interpolate the rotation of the plane,
            // with respect to the global rotation of both planes.
            double omega = Math.acos(dot1);
            double sinOmega = Math.sin(omega);
            leftIso = ql1.multiply(Math.sin((1 - t) * omega) / sinOmega).add(ql2.multiply(Math.sin(t * omega) / sinOmega)).normalize();
        }

        // Interpolate the right-isoclinic rotation.
        Quaternion rightIso;
        if (dot2 > 0.9995) {
            rightIso = qr1.multiply(1 - t).add(qr2.multiply(t)).normalize();
        }
        else {
            double omega = Math.acos(dot2);
            double sinOmega = Math.sin(omega);
            rightIso = qr1.multiply(Math.sin((1 - t) * omega) / sinOmega).add(qr2.multiply(Math.sin(t * omega) / sinOmega)).normalize();
        }

        // Linearly interpolate the scale.
        double scaleX = plane.scaleX * (1 - t) + other.scaleX * t;
        double scaleY = plane.scaleY * (1 - t) + other.scaleY * t;

        // Linearly interpolate the position.
        Quaternion position = plane.position.multiply(1 - t).add(other.position.multiply(t));

        // Return the interpolated plane.
        return new Plane4D(position, leftIso, rightIso, scaleX, scaleY);
    }

    /**
     * (Hyper)Spherically interpolates between two planes. (slow but correct)
     * @param other The other plane to interpolate to.
     * @param t The interpolation factor, between 0 and 1.
     */
    public void slerp(Plane4D other, double t) {
        Plane4D res = slerp(this, other, t);
        position = res.position;
        leftIso = res.leftIso;
        rightIso = res.rightIso;
        scaleX = res.scaleX;
        scaleY = res.scaleY;
    }

    /**
     * Calculates the power of a quaternion.
     * @param q The quaternion to raise to a power.
     * @param power The power to raise the quaternion to.
     */
    public static Quaternion pow(Quaternion q, double power) {
        // First we need to find the polar form of the quaternion.
        // q = r * e ^ (n̂ * φ) = r * (cos(φ) + n̂ * sin(φ))
        // r = |q|, n̂ = v / |v|, φ = acos(q_0 / |q|)
        double r = q.getNorm();
        if (r == 0.0d) return new Quaternion(0.0d, 0.0d, 0.0d, 0.0d); // 0 ^ p = 0
        Quaternion v = new Quaternion(0.0d, q.getQ1(), q.getQ2(), q.getQ3());
        if (v.getNorm() == 0.0d) return new Quaternion(Math.pow(r, power), 0.0d, 0.0d, 0.0d); // r ^ p = r ^ p
        Quaternion nHat = v.normalize();
        double phi = Math.acos(q.getQ0() / r);

        // Now we can use the polar form to raise the quaternion to a power.
        // q ^ p = r ^ p * e ^ (n̂ * p * φ) = r ^ p * (cos(p * φ) + n̂ * sin(p * φ))
        double rPowP = Math.pow(r, power);
        double pPhi = power * phi;
        double cosPPhi = Math.cos(pPhi);
        double sinPPhi = Math.sin(pPhi);
        return new Quaternion(
                rPowP * cosPPhi,
                rPowP * nHat.getQ1() * sinPPhi,
                rPowP * nHat.getQ2() * sinPPhi,
                rPowP * nHat.getQ3() * sinPPhi
        );
    }
}
