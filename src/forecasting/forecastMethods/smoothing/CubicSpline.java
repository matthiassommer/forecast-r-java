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
 * Returns local linear forecasts and prediction intervals using cubic smoothing splines.
 * <p>
 * Cubic spline's smoothing model is equivalent to an ARIMA(0,2,2) model but with a restricted parameter space.
 * The advantage of cubic spline over the full ARIMA model is that it provides
 * a smooth historical trend as well as a linear forecast function.
 */
public class CubicSpline extends AbstractForecastMethod {
    public CubicSpline(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();
            return runForecast("splinef(input, h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}
