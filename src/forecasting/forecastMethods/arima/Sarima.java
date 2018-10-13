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
 * An implementation of the Sesonal Autoregression Integrated Moving Average Model (SARIMA) for time series forecasting.
 */
public class Sarima extends AbstractForecastMethod {
    /**
     * AR(p) seasonal Autoregression order.
     */
    private int seasonal_P = 1;
    /**
     * Seasonal difference order.
     */
    private int seasonal_D = 0;
    /**
     * MA(q) seasonal Moving average order.
     */
    private int seasonal_Q = 0;
    /**
     * Number of autoregression terms / order of autoregression.
     */
    private int p = 1;
    /**
     * Order of integration / differencing.
     */
    private int d = 1;
    /**
     * Number of lagged forecast errors in predictionformula.
     */
    private int q = 1;
    /**
     * number of periods in season: 12 for months, 4 for quarters, 365 for daily data.
     */
    private int seasonPeriod = 12;

    public Sarima( TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();

            String order = p + "," + d + "," + q;
            String seasonalOrder = seasonal_P + "," + seasonal_D + "," + seasonal_Q;

            return runForecast("forecast(Arima(input, c(" + order + ")" +
                    ", seasonal=list(order=c(" + seasonalOrder
                    + "), period=" + seasonPeriod + ")), h = " + timeStep + ")", timeStep);
        }
        return Double.NaN;
    }
}