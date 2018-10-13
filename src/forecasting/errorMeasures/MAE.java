/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

/**
 * https://en.wikipedia.org/wiki/Mean_absolute_error
 *
 * @author Matthias Sommer.
 */
public class MAE extends AbstractErrorMeasure {
    @Override
    public double run(double[] observations, double[] forecasts) {
        double errorSum = 0;
        int count = 0;

        for (int i = 0; i < forecasts.length; i++) {
            double actual = observations[i];
            double forecast = forecasts[i];

            if (!Double.isNaN(actual) && !Double.isNaN(forecast)) {
                errorSum += Math.abs(actual - forecast);
                count++;
            }
        }
        return errorSum / count;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
