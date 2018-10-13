/*
 * Copyright (c) 2015. Matthias Sommer, All rights reserved.
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

public class DoubleSeasonalHoltWinters extends AbstractForecastMethod {
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
     * seasonal smoothing factor for first seasonal period. If null, its estimated.
     */
    @Nullable
    private Float gamma = null;

    /**
     * seasonal smoothing factor for second seasonal period. If null, its estimated.
     */
    @Nullable
    private Float omega = null;

    /**
     * period of shorter seasonsal period (eg. 48 for half hourly data for the daily period)
     */
    private int period1 = 96;

    /**
     * period of longer seasonsal period (eg. 336 for half hourly data for the weekly period)
     */
    private int period2 = 672;

    /**
     * If TRUE, the forecasts are adjusted using an AR(1) model for the errors. Default is true.
     */
    @Nullable
    private Boolean armethod = true;

    public DoubleSeasonalHoltWinters(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= this.minObservations) {
            super.injectTimeSeries();

            //hw is a wrapper function for forecast(ets(<hw params>))
            StringBuilder sb = new StringBuilder();
            sb.append("dshw(input");
            sb.append(", period1=").append(period1).append(", period2=").append(period2);
            sb.append(", ").append("h=").append(timeStep);

            if (alpha != null) {
                sb.append(", alpha=").append(alpha);
            }

            if (beta != null) {
                sb.append(", beta=").append(beta);
            }

            if (gamma != null) {
                sb.append(", gamma=").append(gamma);
            }

            if (omega != null) {
                sb.append(", omega=").append(omega);
            }

            if (armethod != null && !armethod) {
                sb.append(", armethod=FALSE");
            }

            sb.append(")");

            return runForecast(sb.toString(), timeStep);
        }

        return Double.NaN;
    }
}
