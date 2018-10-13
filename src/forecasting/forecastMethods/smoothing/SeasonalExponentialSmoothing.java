/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods.smoothing;

import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.jetbrains.annotations.Nullable;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * The method calculates a trend line for the data as well as seasonal indices
 * that weight the values in the trend line based on where that time point falls
 * in the cycle.
 * <p>
 * To initialize the seasonal indices there must be at least one
 * complete cycle in the data.
 * <p>
 * https://en.wikipedia.org/wiki/Exponential_smoothing#Triple_exponential_smoothing
 *
 * @author Matthias Sommer
 */
public class SeasonalExponentialSmoothing extends AbstractForecastMethod {
    /**
     * if false inital state values are optimzed along with the smoothing parameters, otherwise initials values are obtained
     * by simple calcs on first few observations
     */
    private final boolean simpleInitalStateValues = false;
    /**
     * data smoothing factor, 0 < alpha < 1. If null, its estimated.
     */
    @Nullable
    private Float alpha = null;
    /**
     * trend smoothing factor, 0 < beta < 1, beta < alpha. If null, its estimated.
     */
    @Nullable
    private Float beta = null;
    /**
     * seasonal smoothing factor. If null, its estimated.
     */
    @Nullable
    private Float gamma = null;
    /**
     * boolean indicating whether to use a damped trend
     */
    private boolean damped = false;
    private boolean additiveSeasonality = false;
    private boolean multiplicativeSeasonality = true;
    /**
     * fit an exponential trend, otherwise trend is linear
     */
    private boolean fitExponentialTrend = false;

    public SeasonalExponentialSmoothing(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= this.minObservations) {
            super.injectTimeSeries();

            //hw is a wrapper function for forecast(ets(<hw params>))
            StringBuilder sb = new StringBuilder();
            sb.append("hw(input");
            sb.append(", ").append("h=").append(timeStep);

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

            if (gamma != null) {
                sb.append(", gamma=").append(gamma);
            }

            if (multiplicativeSeasonality) {
                sb.append(", seasonal=\"multiplicative\"");
            }

            sb.append(")");

            return runForecast(sb.toString(), timeStep);
        }

        return Double.NaN;
    }
}