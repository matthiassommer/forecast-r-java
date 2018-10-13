package forecasting.timeseriesMetrics;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;

/**
 * Augmented Dickey-Fuller test for stationarity.
 * <p>
 * Stationarity: the series does not change behavior in time.
 * No trends, seasonal components or changes in variance with time.
 * Stationary series can have significant autocorrelation.
 * <p>
 * p<0.05 -> stationary
 * <p>
 * The null-hypothesis for an ADF test is that the data are non-stationary.
 * Large p-values are indicative of non-stationarity, and small p-values suggest stationarity.
 * Using the usual 5% threshold, differencing is required if the p-value is greater than 0.05.
 */
public class Stationarity extends TimeseriesMetric {
    private double dickeyFullerValue;
    private double lag;
    private double p;
    private String hypothesis;

    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);

            RList result = rConnection.eval("adf.test(input, alternative='stationary')").asList();

            dickeyFullerValue = result.at(0).asDouble();
            lag = result.at(1).asDouble();
            hypothesis = result.at(2).asString();
            p = result.at(3).asDouble();
            return p;
        } catch (REngineException | REXPMismatchException e) {
            System.err.println(e.getMessage());
            return Double.NaN;
        }
    }

    public String toString() {
        return "Dickey-Fuller\t" + dickeyFullerValue + ", Lag order = " + lag + ", p-value = " + p + ", alternative hypothesis: " + hypothesis;
    }
}
