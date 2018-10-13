/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
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
 * An implementation of the Autoregression Integrated Moving Average Model for
 * time series forecasting (ARIMA).
 */
public class ARIMA extends AbstractForecastMethod {
    /**
     * Order of autoregression.
     */
    private int p = 1;
    /**
     * Order of differenciation.
     */
    private int d = 1;
    /**
     * Number of lagged forecast errors.
     */
    private int q = 1;

    public ARIMA(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    public void setP(int p) {
        this.p = p;
    }

    public void setD(int d) {
        this.d = d;
    }

    public void setQ(int q) {
        this.q = q;
    }

    @Override
    public double runForecast(final int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();
            return runForecast("forecast(Arima(input, c(" + p + "," + d + "," + q + "), include.drift=T), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }

    public String toString() {
        return "ARIMA(" + p + "," + d + "," + q + ")";
    }
}
