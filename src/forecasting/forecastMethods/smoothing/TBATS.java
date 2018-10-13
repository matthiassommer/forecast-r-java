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
 * Creates a TBATS model (Trigonometric Box-Cox transform, ARMA error, TrendStationary, Seasonal component).
 * Created by alexandermartel on 13.08.15.
 */
public class TBATS extends AbstractForecastMethod {
    public TBATS(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();

            return runForecast("forecast(tbats(input), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}
