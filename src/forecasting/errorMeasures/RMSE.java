/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

import forecasting.ForecastRealValueEntry;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The root-mean-square deviation (RMSD) or root-mean-square error (RMSE) is
 * a frequently used measure of the differences between values predicted by
 * a model or an estimator and the values actually observed.
 *
 * @author Matthias Sommer.
 * @see <a href="http://en.wikipedia.org/wiki/Root_mean_square_deviation"></a>
 */
public class RMSE extends AbstractErrorMeasure {
    @Override
    public double run(double[] observations, double[] forecasts) {
        double squaredSumDifferences = 0;
        double n = 0;

        for (int i = 0; i < forecasts.length - 1; i++) {
            double realValue = observations[i + 1];
            double forecast = forecasts[i];

            if (!Double.isNaN(realValue) && !Double.isNaN(forecasts[i])) {
                squaredSumDifferences += FastMath.pow(realValue - forecast, 2);
                n++;
            }
        }

        return Math.sqrt(squaredSumDifferences / n);
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
