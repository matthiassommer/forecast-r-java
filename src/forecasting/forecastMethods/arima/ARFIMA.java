/*
 * Copyright (c) 2015. Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods.arima;

import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * This function combines fracdiff and auto.arima to automatically select and estimate an ARFIMA
 * model.
 * <p>
 * An ARFIMA(p,d,q) model is selected and estimated automatically using the Hyndman-Khandakar
 * (2008) algorithm to select p and q, and the Haslett and Raftery (1989) algorithm to estimate the
 * parameters including d.
 */
public class ARFIMA extends AbstractForecastMethod {
    public ARFIMA(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();
            return runForecast("forecast(arfima(input), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}
