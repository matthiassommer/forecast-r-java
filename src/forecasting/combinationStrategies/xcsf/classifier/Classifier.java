package forecasting.combinationStrategies.xcsf.classifier;

import forecasting.combinationStrategies.xcsf.StateDescriptor;
import forecasting.combinationStrategies.xcsf.XCSFConstants;

import java.io.Serializable;

/**
 * This class encapsulates methods for matching, prediction and classifier
 * updates.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class Classifier implements Cloneable, Serializable {
    private ConditionHyperellipsoid condition;
    private RLSPrediction rlsPrediction;
    private double fitness;
    private int numerosity;
    private int experience;
    private double setSizeEstimate;
    private double predictionError;
    private int timestamp;
    // temporary
    private static double[] tmpCenterDifference;

    /**
     * Default constructor used for covering.
     *
     * @param state     the state to cover.
     * @param timestamp the timestamp of this new classifier.
     */
    public Classifier(StateDescriptor state, int timestamp) {
        this.fitness = XCSFConstants.fitnessIni;
        this.numerosity = 1;
        this.experience = 0;
        this.setSizeEstimate = 1;
        this.predictionError = XCSFConstants.predictionErrorIni;
        this.timestamp = timestamp;

        // ---[ condition ]---
        this.condition = new ConditionHyperellipsoid(state.getConditionInput());

        // ---[ prediction ]---
        this.rlsPrediction = new RLSPrediction(
                state.getPredictionInput().length, state.getOutput());

        // temporary array
        if (tmpCenterDifference == null) {
            tmpCenterDifference = new double[state.getConditionInput().length];
        }
    }

    private Classifier() {
        // empty
    }

    /**
     * Returns the activity of the <code>ConditionHyperEllipsoid</code>.
     *
     * @param state the state to match.
     * @return the activity for the <code>state</code>.
     */
    public double getActivity(StateDescriptor state) {
        return this.condition.getActivity(state.getConditionInput());
    }

    /**
     * Returns if the <code>ConditionHyperEllispoid</code> matches the given
     * <code>state</code>.
     *
     * @param state the state to match.
     * @return true, if the condition matches the <code>state</code>.
     */
    public boolean doesMatch(StateDescriptor state) {
        return this.condition.doesMatch(state.getConditionInput());
    }

    /**
     * Generates the prediction for the given <code>state</code>.
     *
     * @param state the state to use for prediction.
     * @return the prediction for the given <code>state</code>.
     */
    public double[] predict(StateDescriptor state) {
        initTmpCenterDifference(state);
        if (state.isSameInput()) {
            this.condition.getOffsetVector(state.getConditionInput(),
                    tmpCenterDifference);
            return this.rlsPrediction.predict(tmpCenterDifference);
        } else {
            return this.rlsPrediction.predict(state.getPredictionInput());
        }
    }

    /**
     * Updates the experience, prediction and predictionError of this
     * classifier.
     * <p>
     * The update methods are seperated for performance reasons.
     *
     * @param state The current state, which this classifier matches.
     */
    public void update1(StateDescriptor state) {
        initTmpCenterDifference(state);

        // ---[ experience ]---
        this.experience++;
        // ---[ prediction ]---
        if (state.isSameInput()) {
            this.condition.getOffsetVector(state.getConditionInput(),
                    tmpCenterDifference);
            this.rlsPrediction.updatePrediction(tmpCenterDifference, state
                    .getOutput());
        } else {
            this.rlsPrediction.updatePrediction(state.getPredictionInput(),
                    state.getOutput());
        }

        // ---[ predictionError ]---
        // calculate absolute error
        double absError = 0;
        double[] currentPrediction = this.predict(state);
        double[] actualValue = state.getOutput();
        for (int i = 0; i < currentPrediction.length; i++) {
            absError += Math.abs(currentPrediction[i] - actualValue[i]);
        }
        // update
        if (experience < 1. / XCSFConstants.beta) {
            predictionError += (1. / experience) * (absError - predictionError);
        } else {
            // normal error update
            predictionError += XCSFConstants.beta * (absError - predictionError);
        }
    }

    /**
     * Updates the setSizeEstimate and the fitness.
     * <p>
     * Precondition: {@link #update1(StateDescriptor)} was called before.
     * <p>
     * The update methods are seperated for performance reasons.
     *
     * @param accuracy      the accuracy of this classifier.
     * @param accuracySum   the sum of accuracies in the <code>MatchSet</code> this
     *                      classifier is in.
     * @param numerositySum the sum of numerosities in the <code>MatchSet</code> this
     *                      classifier is in.
     */
    public void update2(double accuracy, double accuracySum, int numerositySum) {
        // ---[ setSizeEstimate ]---
        if (this.experience < 1.0 / XCSFConstants.beta) {
            this.setSizeEstimate += (numerositySum - this.setSizeEstimate)
                    / this.experience;
        } else {
            this.setSizeEstimate += (numerositySum - this.setSizeEstimate)
                    * XCSFConstants.beta;
        }
        // ---[ fitness ]---
        this.fitness += XCSFConstants.beta * ((accuracy * numerosity) / accuracySum - fitness);
    }

    /**
     * Creates a clone of this classifier. Note, that the numerosity is reduced
     * to one and the fitness is proportinal to the new numerosity. Additionally
     * the gainmatrix of the <code>RLSPrediction</code> is reset.
     *
     * @return a clone of this classifier with numerosity one, fitness
     * proportinal to the numerosity and a new prediction gainmatrix.
     * @see java.lang.Object#clone()
     */
    public Classifier clone() {
        Classifier clone = new Classifier();
        // cloned fields
        clone.condition = this.condition.clone();
        clone.rlsPrediction = this.rlsPrediction.clone();
        clone.setSizeEstimate = this.setSizeEstimate;
        clone.predictionError = this.predictionError;
        clone.timestamp = this.timestamp;
        // modified fields
        clone.fitness = this.fitness / this.numerosity;
        clone.numerosity = 1;
        clone.experience = 0;
        return clone;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "cl{fit="
                + this.fitness //
                + ",num="
                + this.numerosity //
                + ",exp="
                + this.experience //
                + ",setSizeEst="
                + this.setSizeEstimate//
                + " " + this.condition.toString() + " "
                + this.rlsPrediction.toString() + "}";
    }

    /**
     * Returns the "vote" for deletion of this classifier.
     *
     * @param meanFitness The mean fitness in the population considered for deletion.
     * @return The "vote" for deletion of this classifier.
     */
    public double getDeletionVote(double meanFitness) {
        if (fitness / numerosity >= XCSFConstants.delta * meanFitness
                || experience < XCSFConstants.theta_del) {
            return setSizeEstimate * numerosity;
        } else {
            return setSizeEstimate * numerosity * meanFitness
                    / (fitness / numerosity);
        }
    }

    /**
     * Determines the current accuracy of the classifier based on its current
     * predictionError estimate.
     */
    public double getAccuracy() {
        if (predictionError <= XCSFConstants.epsilon_0) {
            return 1.0;
        } else {
            return XCSFConstants.alpha
                    * Math.pow(XCSFConstants.epsilon_0 / predictionError,
                    XCSFConstants.nu);
        }
    }

    /**
     * Returns true, if this classifier is a possible subsumer.
     */
    public boolean canSubsume() {
        return this.experience > XCSFConstants.theta_sub
                && this.predictionError < XCSFConstants.epsilon_0;
    }

    /**
     * Determines if this classifier is more general than the other.
     *
     * @param other the possibly less general classifier.
     * @return true, if this classifier is more general than <code>other</code>.
     */
    public boolean isMoreGeneral(Classifier other) {
        return this.condition.isMoreGeneral(other.condition);
    }

    /**
     * Returns the condition of this classifier.
     */
    public ConditionHyperellipsoid getCondition() {
        return this.condition;
    }

    /**
     * Returns the linear recursive least squares prediction of this classifier.
     */
    public RLSPrediction getRlsPrediction() {
        return this.rlsPrediction;
    }

    /**
     * Returns the fitness of this classifier.
     */
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Sets the fitness of this classifier.
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Scales the fitness of this classifier.
     */
    public void scaleFitness(double factor) {
        this.fitness *= factor;
    }

    /**
     * Returns the prediction error of this classifier.
     */
    public double getPredictionError() {
        return this.predictionError;
    }

    /**
     * Sets the prediction error of this classifier.
     */
    public void setPredictionError(double predictionError) {
        this.predictionError = predictionError;
    }

    /**
     * Scales the prediction error of this classifier.
     */
    public void scalePredictionError(double factor) {
        this.predictionError *= factor;
    }

    /**
     * Determines the generality of this classifiers condition.
     */
    public double getGenerality() {
        return this.condition.calculateVolume();
    }

    /**
     * Returns the numerosity of this classifier.
     */
    public int getNumerosity() {
        return this.numerosity;
    }

    /**
     * Adds <code>val</code> to the numerosity of this classifier.
     */
    public void addNumerosity(int val) {
        this.numerosity += val;
    }

    /**
     * Returns the experience of this classifier.
     */
    public int getExperience() {
        return this.experience;
    }

    /**
     * Returns the setSizeEstimate of this classifier.
     */
    public double getSetSizeEstimate() {
        return this.setSizeEstimate;
    }

    /**
     * Returns the timestamp of this classifier.
     */
    public int getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the timestamp of this classifier.
     */
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    private void initTmpCenterDifference(StateDescriptor state) {
        // temporary array
        if (tmpCenterDifference == null) {
            tmpCenterDifference = new double[state.getConditionInput().length];
        }
    }
}
