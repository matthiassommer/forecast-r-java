package forecasting.timeseriesMetrics;

import org.rosuda.REngine.RList;

/**
 * KPSS Test for TrendStationary Stationarity.
 * <p>
 * https://cran.r-project.org/web/packages/tseries/tseries.pdf
 * <p>
 * Computes the Kwiatkowski-Phillips-Schmidt-Shin (KPSS) test for the null hypothesis that x is level
 * or trend stationary
 * <p>
 * kpss.test(x, null = c("Level", "TrendStationary"), lshort = TRUE)
 * <p>
 * x: a numeric vector or univariate time series.
 * null: indicates the null hypothesis and must be one of "Level" (default) or "TrendStationary".
 * lshort: a logical indicating whether the short or long version of the truncation lag parameter is used
 * <p>
 * TrendStationary stationary means "stationary around the trend", i.e. the trend needn't be stationary, but the de-trended data is.
 * Level stationary means that the data is like white noise.
 * <p>
 * p < 0.05: the process is not trend stationary
 * p > 0.05: no evidence that it is not trend stationary
 */
public class TrendStationary extends TimeseriesMetric {
    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);

            RList rList = rConnection.eval("kpss.test(input, null = 'TrendStationary', lshort = TRUE)").asList();
            double trendValue = rList.at(2).asDouble();
            double p = rList.at(2).asDouble();
            return p;
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
