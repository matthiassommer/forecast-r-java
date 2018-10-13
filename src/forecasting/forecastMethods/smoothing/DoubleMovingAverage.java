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
 * The double moving average model is an extension of the simple moving average
 * model. It is used for curves that follow a trend pattern. The trend
 * adjustment is received by adjusting the values for the values for n steps in
 * future.
 * <p>
 * DMA has the advantage of quick low cost updates and accurate short-term
 * prediction. Its disadvantage is not coping well with trend or seasonality.
 * <p>
 * Theory and Application of Advanced Traffic Forecast Methods
 *
 * @author Matthias Sommer
 */
public class DoubleMovingAverage extends AbstractForecastMethod {
    public DoubleMovingAverage(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public final double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (timeSeries.getSize() >= this.minObservations) {
            super.injectTimeSeries();
            return runForecast("forecast(Arima(input, c(0,0,2)), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}
