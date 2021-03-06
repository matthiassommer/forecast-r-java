/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods.smoothing;

import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * The implementation of the double exponential smoothing algorithm. It uses the
 * previous traffic flows and forecasts the next one based on the constants
 * alpha and beta. Referred to as Holt-Winters double exponential smoothing.
 * <p>
 * It has the advantage of quick low cost updates and accurate short-term
 * prediction. Its disadvantage is not coping well with trend or seasonality.
 * <p>
 * Throws an exception if input is too short (<10).
 * ARIMA(0,2,2)
 *
 * @author Matthias Sommer
 */
public class DoubleExponentialSmoothing extends AbstractForecastMethod {
    /**
     * false: inital state values are optimzed along with the smoothing parameters, otherwise initials values are obtained.
     */
    private final boolean simpleInitalStateValues = false;
    /**
     * Data smoothing factor, 0 < alpha < 1. If null, its estimated.
     */
    @Nullable
    private Float alpha = null;
    /**
     * Trend smoothing factor, 0 < beta < 1, beta < alpha. If null, its estimated.
     */
    @NotNull
    private Float beta = 0.4f;
    /**
     * Indicating whether to use a damped trend.
     */
    private boolean damped = true;
    /**
     * Fit an exponential trend, otherwise trend is linear.
     */
    private boolean fitExponentialTrend = false;

    public DoubleExponentialSmoothing(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= this.minObservations) {
            super.injectTimeSeries();

            //holt is a wrapper function for forecast(ets(<holt params>))
            StringBuilder sb = new StringBuilder();
            sb.append("holt(input");
            sb.append(", ").append("h=" + timeStep);

            if (damped) {
                sb.append(", damped=TRUE");
            }

            String level = "level=c(" + predictionConfidenceLowerLevel + "," + predictionConfidenceUpperLevel + ")";
            sb.append(", ").append(level);

            if (simpleInitalStateValues) {
                sb.append(", initial=\"simple\"");
            }

            if (fitExponentialTrend) {
                sb.append(", exponential=TRUE");
            }

            if (alpha != null) {
                sb.append(", alpha=").append(alpha);
            }

            if (beta != null) {
                sb.append(", beta=").append(beta);
            }

            sb.append(")");

            return runForecast(sb.toString(), timeStep);
        }

        return Double.NaN;
    }
}
