package tools;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.List;

/**
 * Offers mathematical and statistical helper functions.
 *
 * @author Matthias Sommer.
 */
public abstract class MathFunctions {
    /**
     * Computes the mean of the given values, that is the average value.
     *
     * @param values the values
     * @return the mean of {@code values}
     */
    public static float mean(List<Float> values) {
        float mean = 0;
        for (float d : values) {
            mean += d;
        }
        mean /= values.size();
        return mean;
    }

    public static double mean(double[] values) {
        double mean = 0;
        for (double d : values) {
            mean += d;
        }
        mean /= values.length;
        return mean;
    }

    /**
     * Computes the sample variance of the given values, that is the quadratic
     * deviation from mean.
     *
     * @param values the values
     * @param mean   the mean of {@code values}
     * @return he variance of {@code values}
     */
    public static float variance(List<Float> values, float mean) {
        float var = 0;
        for (float d : values) {
            var += (d - mean) * (d - mean);
        }
        var /= values.size() - 1;
        return var;
    }

    /**
     * Computes the standard deviation of the given values, that is the square
     * root of the sample variance.
     *
     * @param values the values
     * @param mean   the mean of {@code values}
     * @return the standard deviation of {@code values}
     */
    public static float standardDeviation(List<Float> values, float mean) {
        return (float) Math.sqrt(variance(values, mean));
    }

    /**
     * Calculates the confidence interval based on the T-distribution.
     *
     * @param significance typically 0.01 or 0.05
     * @param n            number of values
     * @param stdDev       standard deviation
     * @param mean
     * @return the lower and upper bound of the confidence interval
     */
    public static double[] getConfidenceInterval(double significance, int n, double stdDev, double mean) {
        RealDistribution tdist = new TDistribution(n);
        double t = tdist.inverseCumulativeProbability(1.0 - significance / 2);

        double factor = t * stdDev / Math.sqrt(n);
        double lower = mean - factor;
        double upper = mean + factor;

        return new double[]{lower, upper};
    }
}
