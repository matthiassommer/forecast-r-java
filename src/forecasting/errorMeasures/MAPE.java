/*
 * Copyright (c) 2015. Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

/**
 * Mean Average Percentage error.
 * <p>
 * Return the MAPE in percent (%).
 * https://en.wikipedia.org/wiki/Mean_absolute_percentage_error
 */
public class MAPE extends AbstractErrorMeasure {
    @Override
    public double run(double[] observations, double[] forecasts) {
        double errorSum = 0;
        int count = 0;

        for (int i = 0; i < forecasts.length - 1; i++) {
            double actual = observations[i];
            double forecast = forecasts[i];

            if (!Double.isNaN(actual) && !Double.isNaN(forecast)) {
                if (actual != 0) {
                    errorSum += Math.abs((actual - forecast) / actual);
                    count++;
                }
            }
        }
        return (errorSum / count) * 100;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
