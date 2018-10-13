package forecasting.timeseriesMetrics;

/**
 * Test a time series for non-linearity.
 * package: fNonLinear
 *
 * Teraesvirta Neural Network Test:
 * The null is the hypotheses of linearity in mean.
 * This test uses a Taylor series expansion of the activation function to arrive at a suitable test statistic.
 * If type equals "F", then the F-statistic instead of the Chi-Squared statistic is used in analogy to the classical linear regression.
 * Missing values are not allowed.
 * <p>
 * small p-values indicate non-linearity.
 */
public class NonLinearity extends TimeseriesMetric {
    private int lag = 2;

    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);
            return rConnection.eval("tnnTest(input, lag=" + lag + ")@test$p.value").asDouble();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
