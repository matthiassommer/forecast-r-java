/*
 * Copyright (c) 2015. Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.timeseriesMetrics;

import org.rosuda.REngine.RList;

import java.util.Arrays;

/**
 * If the predictive model cannot be improved upon, there should be no correlations between forecast errors for successive predictions.
 * <p>
 * 1) forecast errors are calculated as the observed values minus predicted values, for each time point.
 * 2) obtain a correlogram of the in-sample forecast errors for lags 1-20.
 * 3) using the acf() function in R, specify the maximum lag, e.g. lag.max=20
 * 4) To test whether there is significant evidence for non-zero correlations at lags 1-20, we carry out a Ljung-Box test.
 * p->0 evidence for non-zero autocorrelations in the forecast errors at lags 1-20
 * p->1 very little evidence for non-zero autocorrelations in the forecast errors at lags 1-20
 * <p>
 * https://a-little-book-of-r-for-time-series.readthedocs.org/en/latest/src/timeseries.html#holt-s-exponential-smoothing
 * <p>
 * http://coolstatsblog.com/2013/08/07/how-to-use-the-autocorreation-function-acf/
 * <p>
 * If  p>0.05 then the residuals are independent which we want for the model to be correct.
 * <p>
 * http://www.inside-r.org/packages/cran/forecast/docs/Acf
 */
public class ACF1 extends TimeseriesMetric {
    private double[] forecasts;
    /**
     * TRUE->window with plot is created by R
     */
    private String plot = "FALSE";
    // Result of the box-ljung text
    private double p;
    private float xsquared;
    private float df;

    public double[] getLags() {
        return lags;
    }

    // ACF lags
    private double[] lags;

    public ACF1(double[] forecasts) {
        this.forecasts = forecasts;
    }

    public double getP() {
        return p;
    }

    public float getXsquared() {
        return xsquared;
    }

    public void activatePlot() {
        this.plot = "TRUE";
    }

    public double run(double[] timeSeries) {
        double forecastErrors[] = new double[forecasts.length];
        for (int i = 0; i < forecasts.length; i++) {
            double error = timeSeries[i] - forecasts[i];
            if (!Double.isNaN(error)) {
                forecastErrors[i] = error;
            }
        }

        try {
            rConnection.assign("input", forecastErrors);
            RList acfResults = rConnection.eval("acf(input, lag.max=20, plot=" + this.plot + ", type='correlation')").asList();
            lags = acfResults.at(0).asDoubles();

            RList boxTestResult = rConnection.eval("Box.test(input, lag=20, type='Ljung-Box')").asList();
            xsquared = (float) boxTestResult.at(0).asDouble();
            df = (float) boxTestResult.at(1).asDouble();
            p = boxTestResult.at(2).asDouble();
            return p;
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    public String toString() {
        return "ACF1\t" + Arrays.toString(lags) + "\nLjung-Box test:\tX-squared: " + xsquared + "\tdf: " + df + "\tp-value: " + p;
    }
}
