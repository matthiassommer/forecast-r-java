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
 * Single smoothing model, also called exponential smoothing. Equal to ARIMA(0,1,1).
 *
 * @author Matthias Sommer.
 */
public class ExponentialSmoothing extends AbstractForecastMethod {
    /**
     * if false: inital state values are optimzed along with the smoothing parameters,
     * otherwise initials values are obtained by simple estimation on first few observations.
     */
    private final boolean simpleInitalStateValues = false;
    /**
     * data smoothing parameter, 0 < alpha < 1. If null, its estimated.
     */
    @Nullable
    private final Float alpha = null;

    public ExponentialSmoothing(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= this.minObservations) {
            super.injectTimeSeries();

            //holt is a wrapper function for forecast(ets(<holt params>))
            StringBuilder sb = new StringBuilder();
            sb.append("ses(input");
            sb.append(", ").append("h=").append(timeStep);

            String level = "level=c(" + predictionConfidenceLowerLevel + "," + predictionConfidenceUpperLevel + ")";
            sb.append(", ").append(level);

            if (simpleInitalStateValues) {
                sb.append(", initial=\"simple\"");
            }
            if (alpha != null) {
                sb.append(", alpha=").append(alpha);
            }
            sb.append(")");

            return runForecast(sb.toString(), timeStep);
        }

        return Double.NaN;
    }
}
