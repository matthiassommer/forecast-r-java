package forecasting.combinationStrategies.xcsf.classifier;

import forecasting.combinationStrategies.xcsf.XCSFConstants;
import forecasting.combinationStrategies.xcsf.XCSFUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Implementation of a rotating hyperellipsoid.
 * <p>
 * Forward transformation, i.e. (transform = translation * rotation * stretch)
 * maps the unit sphere surface to the rotated & translated hyperellipsoid
 * surface:
 * <p>
 * <pre>
 * ellipsoidPoint = transform * unitSpherePoint
 * </pre>
 * <p>
 * Inverse transformation maps a point on the rotated & translated
 * hyperellipsoid surface to the unit sphere surface:
 * <p>
 * <pre>
 * unitSpherePoint = inverseTransform * ellipsoidPoint
 * </pre>
 * <p>
 * <p>
 * The condition matches a given point, if the inverse transfored point lies
 * within radius 1.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class ConditionHyperellipsoid implements Cloneable, Serializable {
    // temporary arrays to avoid mem alloc.
    private static double[] tmpCenterDiff;
    private static double[] tmpArray;
    private static double[][] tmpSingleRotation;
    private static double[][] tmpTransformation;
    private static double[][] tmpMatrix;

    // center, stretch & engles define location & shape of this hyperellipsoid
    private int dimension;
    private double[] center;
    private double[] stretch;
    private double[] angle;
    // derived transformation matrices
    private double[][] transform;
    private double[][] inverseTransform;

    // to avoid multiple calculations for one state
    private double[] conditionInput;
    private double squareDistance;

    /**
     * Default constructor creates a condition, that matches the given
     * <code>conditionInput</code>.
     *
     * @param conditionInput the input for this condition.
     */
    ConditionHyperellipsoid(double[] conditionInput) {
        this.dimension = conditionInput.length;
        // center matches input
        this.center = new double[conditionInput.length];
        System.arraycopy(conditionInput, 0, center, 0, this.dimension);
        // random stretch between min and min+range
        this.stretch = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            stretch[i] = XCSFConstants.minConditionStretch
                    + XCSFUtils.Random.uniRand()
                    * XCSFConstants.coverConditionRange;
        }
        // random angle
        this.angle = new double[dimension * (dimension - 1) / 2];
        for (int i = 0; i < this.angle.length; i++) {
            angle[i] = XCSFUtils.Random.uniRand() * 2.0 * Math.PI;
        }

        // first constructor call
        if (tmpCenterDiff == null) {
            tmpCenterDiff = new double[dimension];
            tmpArray = new double[dimension];
            tmpMatrix = new double[dimension][dimension];
            tmpTransformation = new double[dimension + 1][dimension + 1];
            tmpSingleRotation = new double[dimension][dimension];
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    tmpSingleRotation[i][j] = (i != j) ? 0 : 1;
                }
            }
        }

        // derived: the transformation matrices of this hyperellipsoid
        this.transform = new double[dimension + 1][dimension + 1];
        this.inverseTransform = new double[dimension + 1][dimension + 1];
        recalculateTransformationMatrix();
    }

    /**
     * Private constructor for efficient cloning only.
     */
    private ConditionHyperellipsoid() {
        // for cloning purpose
    }

    /**
     * Returns true, if this condition matches the given <code>conInput</code>,
     * i.e. if the ellipsoid contains the given point.
     * <p>
     * Note that multiple calls of this method don't produce much overhead,
     * since the distance for a given input is stored, if calculated once.
     *
     * @param conInput the input to match.
     * @return true, if this condition matches the <code>conInput</code>.
     */
    public boolean doesMatch(double[] conInput) {
        if (!XCSFUtils.arrayEquals(this.conditionInput, conInput)) {
            this.squareDistance = calculateRelativeSquaredDistance(conInput);
            this.conditionInput = conInput;
        }
        return this.squareDistance < 1;
    }

    /**
     * Calculates the activity of this condition concerning the given
     * <code>conInput</code>. If the input equals the center of this condition,
     * the activity is 1 (maximum). The higher the distance to this condition,
     * the lower the activity.
     * <p>
     * Note that multiple calls of this method don't produce much overhead,
     * since the distance for a given input is stored, if calculated once.
     *
     * @param conInput the input for this condition.
     * @return the activity of this condition for the given
     * <code>conInput</code>.
     */
    double getActivity(double[] conInput) {
        if (!XCSFUtils.arrayEquals(this.conditionInput, conInput)) {
            this.squareDistance = calculateRelativeSquaredDistance(conInput);
            this.conditionInput = conInput;
        }
        return Math.exp(-this.squareDistance);
    }

    /**
     * Compares this condition with <code>other</code> for generality.
     * <p>
     * Returns true, if <code>this</code> ellipsoid contains the
     * <code>other</code> ellipsoid.
     *
     * @param other the possibly less general condition.
     * @return true, if this condition contains the elongenated difference
     * vector.
     */
    boolean isMoreGeneral(ConditionHyperellipsoid other) {
        initializeTempVariables(this.dimension);
        // magic matrix multiplication: multiply this inverse transformation
        // with other transformation (not inverse).
        // resulting transformation maps the unit sphere to the
        // other ellipsoid and from the coordinate system of this
        // ellipsoid back to the unit sphere.
        XCSFUtils.multiply(this.inverseTransform, other.transform,
                tmpTransformation, dimension + 1);

        // check, if resulting transformation stays in unit sphere,
        // i.e. length of unit vector for each dim < 1
        for (int dim = 0; dim < dimension; dim++) {
            double length1 = 0, length2 = 0;
            for (int row = 0; row < dimension; row++) {
                // positive unit vector
                double v = tmpTransformation[row][dim]
                        + tmpTransformation[row][dimension];
                length1 += v * v;
                // negative unit vector
                v = -tmpTransformation[row][dim]
                        + tmpTransformation[row][dimension];
                length2 += v * v;
            }
            if (length1 > 1 || length2 > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method calculates the distance from the center to <code>point</code>
     * , where the distance is...
     * <ul>
     * <li>0, iff center == point <li>0 > x > 1, iff this hyperellipsoid
     * contains point <li>1, iff point lies on the hyperellipsoid surface <li>x
     * > 1 else
     * </ul>
     *
     * @param point the condition input.
     * @return the relative distance.
     */
    public double calculateRelativeSquaredDistance(double[] point) {
        initializeTempVariables(this.dimension);
        // use inverse transformation:
        // ellipsoidal coodrinate system -> default coordinate system
        XCSFUtils
                .multiplyExtended(inverseTransform, point, tmpArray, dimension);
        // return sqared distance to the unit-sphere center
        double dist = 0;
        for (int i = 0; i < dimension; i++) {
            dist += tmpArray[i] * tmpArray[i];
        }
        return dist;
    }

    /**
     * Determines the volume of this condition. This includes the covered space
     * outside the problem-input-space.
     * <p>
     * volume = 2^(n-1)/n * PI * stretch[0] * stretch[1] * ...
     *
     * @return the volume of the condition.
     */
    double calculateVolume() {
        int n = this.center.length;
        double volume = Math.pow(2.0, n - 1) / n * Math.PI;
        for (int i = 0; i < n; i++) {
            volume *= stretch[i];
        }
        return volume;
    }

    /**
     * Computes the inverse transformation for this ellipsoid, i.e. any point on
     * the ellipsoid is mapped to a radius-1-sphere.
     * <p>
     * <pre>
     * transformation&circ;-1 = stretch&circ;-1 * rotation&circ;-1
     * </pre>
     */
    public void recalculateTransformationMatrix() {
        initializeTempVariables(this.dimension);
        setInverseTransform(this.inverseTransform, center, stretch, angle,
                dimension);
        setTransform(this.transform, center, stretch, angle, dimension);
        this.conditionInput = null; // reset activity calculation
    }

    /**
     * Compares this condition with <code>other</code> and returns true, if the
     * conditions have equal center, stretch and angles. Fail fast behavior.
     *
     * @param other the condition to compare with.
     * @return true, if this condition equals <code>other</code>.
     */
    public boolean equals(ConditionHyperellipsoid other) {
        for (int i = 0; i < dimension; i++) {
            if (this.center[i] != other.center[i]
                    || this.stretch[i] != other.stretch[i]) {
                return false;
            }
        }
        for (int i = 0; i < this.angle.length; i++) {
            if (this.angle[i] != other.angle[i]) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "condition{center" + Arrays.toString(this.center) + " stretch"
                + Arrays.toString(this.stretch) + " angles"
                + Arrays.toString(this.angle) + "}";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    public ConditionHyperellipsoid clone() {
        ConditionHyperellipsoid clone = new ConditionHyperellipsoid();
        clone.dimension = this.dimension;
        clone.center = new double[dimension];
        clone.stretch = new double[dimension];
        clone.angle = new double[this.angle.length];
        clone.inverseTransform = new double[dimension + 1][dimension + 1];
        clone.transform = new double[dimension + 1][dimension + 1];
        // copy values
        clone.inverseTransform[dimension][dimension] = clone.transform[dimension][dimension] = 1;
        for (int i = 0; i < dimension; i++) {
            System.arraycopy(transform[i], 0, clone.transform[i], 0,
                    dimension + 1);
            System.arraycopy(inverseTransform[i], 0, clone.inverseTransform[i],
                    0, dimension + 1);
        }
        System.arraycopy(center, 0, clone.center, 0, dimension);
        System.arraycopy(stretch, 0, clone.stretch, 0, dimension);
        System.arraycopy(angle, 0, clone.angle, 0, angle.length);
        return clone;
    }

    /**
     * Returns the center of this condition.
     *
     * @return the center.
     */
    public double[] getCenter() {
        return this.center;
    }

    /**
     * Returns the stretch of this condition.
     *
     * @return the stretch.
     */
    public double[] getStretch() {
        return this.stretch;
    }

    /**
     * Returns the rotation angles of this condition.
     *
     * @return the angles.
     */
    public double[] getAngles() {
        return this.angle;
    }

    /**
     * Returns the transformation matrix of this condition.
     *
     * @return the transformation matrix.
     */
    public double[][] getTransform() {
        return this.transform;
    }

    /**
     * Returns the inverse transformation matrix of this condition.
     *
     * @return the inverse transformation matrix.
     * @see #recalculateTransformationMatrix()
     */
    public double[][] getInverseTransform() {
        return this.inverseTransform;
    }

    /**
     * Calculates the difference to this center, i.e. the vector from
     * <code>this.center</code> to <code>conInput</code> and stores the result
     * in <code>destination</code>.
     *
     * @param conInput    the condition input.
     * @param destination the destination vector.
     */
    void getOffsetVector(double[] conInput, double[] destination) {
        for (int i = 0; i < dimension; i++) {
            destination[i] = conInput[i] - this.center[i];
        }
    }

    /**
     * Sets <code>matrix</code> to the ellipsoidal transformation matrix, i.e. a
     * matrix of size (<code>dim+1</code> x <code>dim+1</code>) with
     * <p>
     * <pre>
     * matrix = translationMatrix * rotationMatrix * stretchMatrix
     * </pre>
     * <p>
     * <p>
     * The matrix can be used to transform any point from the default coordinate
     * system into the ellipsoidal coordinate system (origin =
     * <code>center</code>, scaled with <code>stretch</code> and rotated by
     * <code>angle</code>).
     *
     * @param matrix  the destination matrix of size (<code>dim+1</code> x
     *                <code>dim+1</code>), which will hold the transformation.
     * @param center  the center of the ellipsoid.
     * @param stretch the stretch of the ellipsoid.
     * @param angle   the rotation angles of the ellipsoid.
     * @param dim     the dimension of the coordinate system.
     */
    private static void setTransform(double[][] matrix, double[] center,
                                     double[] stretch, double[] angle, int dim) {
        // matrix = translation * rotation * stretch
        // 1. set identity & translation
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                matrix[i][j] = (i != j) ? 0 : 1;
            }
            // last column: translation
            matrix[i][dim] = center[i];
            // last row: zero
            matrix[dim][i] = 0;
        }
        // last row&column: one
        matrix[dim][dim] = 1;

        // 2. rotation
        int a = angle.length - 1; // angle index
        for (int i = dim - 1; i >= 0; i--) {
            for (int j = dim - 1; j > i; j--) {
                // singleRotation == identity at this point.
                tmpSingleRotation[i][i] = tmpSingleRotation[j][j] = Math
                        .cos(angle[a]);
                tmpSingleRotation[i][j] = -Math.sin(angle[a]);
                tmpSingleRotation[j][i] = -tmpSingleRotation[i][j];

                // multiply inverseTransformation with singlerotation
                XCSFUtils.multiply(matrix, tmpSingleRotation, tmpMatrix, dim);
                XCSFUtils.copyMatrix(tmpMatrix, matrix, dim);
                a--;

                // reset singleRotation to identity
                tmpSingleRotation[i][i] = 1.0;
                tmpSingleRotation[i][j] = 0.0;
                tmpSingleRotation[j][i] = 0.0;
                tmpSingleRotation[j][j] = 1.0;
            }
        }

        // 3. stretch
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                matrix[i][j] *= stretch[j];
            }
        }
    }

    /**
     * Sets <code>matrix</code> to the inverse ellipsoidal transformation
     * matrix, i.e. a matrix of size (<code>dim+1</code> x <code>dim+1</code>)
     * with
     * <p>
     * <pre>
     * matrix = inverse stretchMatrix * inverse rotationMatrix * inverse translationMatrix
     * </pre>
     * <p>
     * <p>
     * The matrix can be used to transform any point from the ellipsoidal
     * coordinate system (origin = <code>center</code>, scaled with
     * <code>stretch</code> and rotated by <code>angle</code>) into the default
     * coordinate system (zero origin, identity stretch and no rotation).
     *
     * @param matrix  the destination matrix of size (<code>dim+1</code> x
     *                <code>dim+1</code>), which will hold the inverse
     *                transformation.
     * @param center  the center of the ellipsoid.
     * @param stretch the stretch of the ellipsoid.
     * @param angle   the rotation angles of the ellipsoid.
     * @param dim     the dimension of the coordinate system.
     */
    private static void setInverseTransform(double[][] matrix, double[] center,
                                            double[] stretch, double[] angle, int dim) {
        // matrix = stretch^-1 * rotation^-1 * translation^-1
        // 1. set identity & inverse stretch
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                matrix[i][j] = (i != j) ? 0 : 1.0 / stretch[i];
            }
            // last column & row: zero
            matrix[i][dim] = matrix[dim][i] = 0;
        }
        // last row&column: one
        matrix[dim][dim] = 1;

        // 2. inverse rotation
        int a = 0; // angle index
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                // singleRotation == identity at this point.
                tmpSingleRotation[i][i] = tmpSingleRotation[j][j] = Math
                        .cos(angle[a]);
                tmpSingleRotation[i][j] = Math.sin(angle[a]);
                tmpSingleRotation[j][i] = -tmpSingleRotation[i][j];

                // multiply inverseTransformation with singlerotation
                XCSFUtils.multiply(matrix, tmpSingleRotation, tmpMatrix, dim);
                XCSFUtils.copyMatrix(tmpMatrix, matrix, dim);
                a++;

                // reset singleRotation to identity
                tmpSingleRotation[i][i] = 1.0;
                tmpSingleRotation[i][j] = 0.0;
                tmpSingleRotation[j][i] = 0.0;
                tmpSingleRotation[j][j] = 1.0;
            }
        }

        // 3. inverse translation: only last column changes
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                matrix[i][dim] -= matrix[i][j] * center[j];
            }
        }
    }

    private void initializeTempVariables(int dimension) {
        if (tmpCenterDiff == null) {
            tmpCenterDiff = new double[dimension];
            tmpArray = new double[dimension];
            tmpMatrix = new double[dimension][dimension];
            tmpTransformation = new double[dimension + 1][dimension + 1];
            tmpSingleRotation = new double[dimension][dimension];
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    tmpSingleRotation[i][j] = (i != j) ? 0 : 1;
                }
            }
        }
    }
}
