package forecasting.timeseriesMetrics;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * check residuals for correlation.
 * >0.05 -> residuals are independent
 * more exact: no evidence that they are dependent
 * "serial correlation" in cluster paper -> higher p-value, noiser data set
 *
 * How to set the lag:
 * For non-seasonal time series, use h=min(10,T/5). For seasonal time series, use h=min(2m,T/5).
 * Another one is the forecasting horizon, if you use the model for forecasting.
 *
 * http://robjhyndman.com/hyndsight/ljung-box-test/
 */
public class LjungBoxTest extends TimeseriesMetric {
    private int lag = 10;

    @Override
    public double run(double[] input) {
        this.lag = Math.min(10, input.length / 5);
        try {
            rConnection.assign("input", input);
            return rConnection.eval("Box.test(input, lag=" + lag + ", type=\"Ljung\")$p.value").asDouble();
        } catch (REngineException | REXPMismatchException e) {
            return Double.NaN;
        }
    }
}
