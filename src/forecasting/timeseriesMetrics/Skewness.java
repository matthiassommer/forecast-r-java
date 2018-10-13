package forecasting.timeseriesMetrics;

/**
 * Skewness is a measure of the asymmetry (around mean value).
 * <p>
 * negative: skewed left
 * positive: skewed right
 * <p>
 * normal distribtion is zero, symmetric time series should tend to zero.
 * <p>
 * https://en.wikipedia.org/wiki/Skewness
 */
public class Skewness extends TimeseriesMetric {
    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);
            //remove NA values, otherwise we might get no result
            return rConnection.eval("skewness(input, na.rm=TRUE)").asDouble();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
