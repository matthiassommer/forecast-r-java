/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

import forecasting.forecastMethods.smoothing.DoubleExponentialSmoothing;

/**
 * A scaled error is less than one if it arises
 * from a better forecast than the average one-step benchmark forecast computed in-sample. Conversely,
 * it is greater than one if the forecast is worse than the average one-step benchmark
 * forecast computed in-sample.
 * <p>
 * When MASE < 1, the proposed method gives, on average, smaller errors than the one-step
 * errors from the benchmark method.
 * <p>
 * If multi-step forecasts are being computed, it is possible to
 * scale by the in-sample MAE computed from multi-step benchmark forecasts.
 * <p>
 * Ref: Another Look at Measures of Forecast Accuracy
 * Rob J. Hyndman and Anne B. Koehler
 * <p>
 * https://en.wikipedia.org/wiki/Mean_absolute_scaled_error
 *
 * @author Matthias Sommer.
 */
public final class MASE extends AbstractErrorMeasure {
    @Override
    public double run(double[] timeseries, double[] forecasts) {
        double sum_et = getErrors(timeseries, forecasts);
        double denominator = getInSampleMAE(timeseries);

        if (denominator == 0) {
            denominator = 0.01;
        } else if (Double.isNaN(denominator)) {
            return Double.NaN;
        }

        return sum_et / denominator;
    }

    private double getErrors(double[] timeseries, double[] forecasts) {
        double sumErrors = 0;
        for (int i = 0; i < forecasts.length; i++) {
            if (!Double.isNaN(forecasts[i]) && !Double.isNaN(timeseries[i])) {
                sumErrors += Math.abs(timeseries[i] - forecasts[i]);
            }
        }
        return sumErrors;
    }

    /**
     * Reference method: naive forecast
     */
    private double getInSampleMAE(double[] timeseries) {
        double sum = 0;
        int count = 1;
        for (int i = 1; i < timeseries.length; i++) {
            double y_t = timeseries[i];
            double y_t_1 = timeseries[i - 1];

            if (!Double.isNaN(y_t) && !Double.isNaN(y_t_1)) {
                sum += Math.abs(y_t - y_t_1);
                count++;
            }
        }

        if (count == 1) {
            return Double.NaN;
        }
        return sum * count / (count - 1);
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
