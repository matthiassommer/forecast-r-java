/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods.smoothing;

import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * Simple cumulative moving average; equal to ARIMA(0,0,1).
 * <p>
 * MA has the advantage of quick low cost updates and accurate short-term
 * prediction. Its disadvantage is not coping well with trend or seasonality.
 *
 * @author Matthias Sommer
 */
public class MovingAverage extends AbstractForecastMethod {
    private final int order = 3;

    public MovingAverage(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();
            return runForecast("forecast(ma(input, order=" + order + "), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}
