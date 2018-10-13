package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.xcsf.classifier.Classifier;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The Population class extends <code>ClassifierSet</code> and offers deletion
 * and greedy compaction methods.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class Population extends ClassifierSet implements Serializable {
    /**
     * Comparator to allow sorting of classifiers by predictionError.
     * <p>
     * Note: inexperienced classifiers are assumed to have high error.
     */
    private final static Comparator<Classifier> COMPACTION_COMPARATOR = new Comparator<Classifier>() {
        private final static double INEXPERIENCED = 1000;

        public int compare(Classifier o1, Classifier o2) {
            if (o1.getExperience() < XCSFConstants.theta_sub
                    && o2.getExperience() < XCSFConstants.theta_sub) {
                // both inexperienced => equality for sorter
                return 0;
            } else if (o1.getExperience() < XCSFConstants.theta_sub) {
                // o1 low experience => assume high error
                return Double.compare(INEXPERIENCED, o2.getPredictionError());
            } else if (o2.getExperience() < XCSFConstants.theta_sub) {
                // o2 low experience => assume high error
                return Double.compare(o1.getPredictionError(), INEXPERIENCED);
            } else {
                // two experienced classifiers: compare prediction error
                return Double.compare(o1.getPredictionError(), o2
                        .getPredictionError());
            }
        }
    };

    Population() {
        super();
    }

    /**
     * Delete <code>number</code> classifiers by roulette wheel selection.
     *
     * @param number the number of classifiers to delete.
     */
    void deleteWorstClassifiers(int number) {
        // ---[ init roulette wheel ]---
        double meanFitness = 0;
        int numerositySum = 0;
        for (Classifier cl : this) {
            meanFitness += cl.getFitness();
            numerositySum += cl.getNumerosity();
        }
        meanFitness /= numerositySum;
        int n = super.size();
        double[] rouletteWheel = new double[n];
        rouletteWheel[0] = super.get(0).getDeletionVote(meanFitness);
        for (int i = 1; i < n; i++) {
            rouletteWheel[i] = rouletteWheel[i - 1]
                    + super.get(i).getDeletionVote(meanFitness);
        }

        // ---[ delete number classifiers with given roulettewheel ]---
        int[] deletedIndices = new int[number];
        for (int i = 0; i < number; i++) {
            deletedIndices[i] = -1;
        }
        int deleted = 0; // increased, if numerosity is reduced
        int reallyDeleted = 0; // increased if numerosity is reduced to 0
        while (deleted < number) {
            double choicePoint = rouletteWheel[n - 1]
                    * XCSFUtils.Random.uniRand();
            int index = binaryRWSearch(rouletteWheel, choicePoint);
            // choicepoint found. classifier at index stil exists?
            boolean alreadyDeleted = false;
            for (int j = 0; j < deleted; j++) {
                if (deletedIndices[j] == index) {
                    alreadyDeleted = true;
                    break;
                }
            }
            // if classifier is fine, reduce numerosity
            // else re-roll random index
            if (!alreadyDeleted) {
                Classifier cl = super.get(index);
                cl.addNumerosity(-1);
                if (cl.getNumerosity() == 0) {
                    // really delete this classifier after while-loop
                    deletedIndices[deleted] = index;
                    reallyDeleted++;
                }
                deleted++;
            }
        }
        // delete classifiers with zero-numerosity
        if (reallyDeleted > 0) {
            int[] indices = new int[reallyDeleted];
            int i = 0;
            for (int index : deletedIndices) {
                if (index != -1) {
                    indices[i++] = index;
                }
            }
            super.remove(indices);
        }
    }

    /**
     * Applies the greedy compaction mechanism specified in the IEEE TEC paper
     * (Butz, Lanzi, Wilson, in press). Greedily considers all experienced,
     * low-error classifiers and subsumes all that overlap with the center of
     * the candidate classifier.
     */
    void applyGreedyCompaction() {
        if (super.size() < 2) {
            return;
        }
        // sorting the population based on experience & predictionError
        super.sort(COMPACTION_COMPARATOR);
        // now the first element has lowest error
        // least elements are inexperienced

        for (int i = 0; i < super.size(); i++) {
            Classifier clLow = super.get(i);
            double[] reference = clLow.getCondition().getCenter();
            for (int j = i + 1; j < super.size(); j++) {
                Classifier clHigh = super.get(j);
                // if clHigh matches clLow.center, delete clHigh
                // remember: high index => high error and vice versa
                if (clHigh.getCondition().doesMatch(reference)) {
                    // clLow subsumes clHigh
                    clLow.addNumerosity(clHigh.getNumerosity());
                    super.remove(j);
                    j--; // don't miss element after deletion index
                }
            }
        }
    }

    /**
     * Implementation of a binary search for the rouletteWheel. Returns the
     * index with
     * <p>
     * <pre>
     * rw[index-1] &lt;= choicePoint
     * rw[index]   &gt;  choicePoint
     * </pre>
     * <p>
     * Precondition: 0 <= choicePoint < rw[rw.length-1]
     *
     * @param rw          the ascending sorted roulettewheel.
     * @param choicePoint the chosen point in the given roulettewheel.
     * @return the first index with rw[index] > choicePoint
     */
    private static int binaryRWSearch(double[] rw, double choicePoint) {
        int low = 0;
        int high = rw.length - 1;
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (choicePoint < rw[mid]) {
                high = mid;
            } else if (choicePoint > rw[mid]) {
                low = mid + 1;
            } else { // rare case: exactly hit the key, quick return
                return mid + 1;
            }
        }
        return low;
    }
}
