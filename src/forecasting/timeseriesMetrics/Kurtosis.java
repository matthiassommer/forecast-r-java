package forecasting.timeseriesMetrics;

/**
 * 3 is the result for a standard normal distribution.
 * higher: sharper peak near mean
 * lower: flatter near mean (extreme: uniform distribution)
 * https://en.wikipedia.org/wiki/Kurtosis
 */
public class Kurtosis extends TimeseriesMetric {
    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);
            //remove NA values, otherwise we might get no result
            return rConnection.eval("kurtosis(input, na.rm=TRUE)").asDouble();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
