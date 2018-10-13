/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

/**
 * 2nd version of SMAPE, which allows to measure the direction of the bias (in %).
 * <p>
 * Symmetric mean absolute percentage error is an accuracy measure based on
 * percentage (or relative) errors.
 * <p>
 * Worst value is 100%, best 0%.
 *
 * @author Matthias Sommer.
 * @see <a href="http://en.wikipedia.org/wiki/Symmetric_mean_absolute_percentage_error"></a>
 */
public class SMAPE extends AbstractErrorMeasure {
    @Override
    public double run(double[] observations, double[] forecasts) {
        double sum = 0;
        int count = 0;

        for (int i = 0; i < forecasts.length; i++) {
            double actual = observations[i];
            double forecast = forecasts[i];

            if (!Double.isNaN(forecast) && !Double.isNaN(actual)) {
                double demoninator = Math.abs(forecast) + Math.abs(actual);

                if (demoninator != 0) {
                    sum += (Math.abs(forecast - actual)) / demoninator;
                    count++;
                }
            }
        }

        return (sum / count) * 100;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
