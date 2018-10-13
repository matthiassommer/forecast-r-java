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
 * Croston’s method involves using simple exponential smoothing (SES) on the non-zero elements of the time series
 * and a separate application of SES to the times between non- zero elements of the time series.
 * <p>
 * IMPORTANT: Not suitable for time series with negative values!
 */
public class Croston extends AbstractForecastMethod {
    /**
     * Smoothing parameter, the higher it gets, the more weight is given to recent observations.
     * same for both time series (non-zeroes and intervals between non-zeroes). Default: 0.1
     */
    private float alpha = 0.2f;

    public Croston(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int step) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();
            return runForecast("croston(input, h=" + step + ", alpha=" + alpha + ")", step);
        }

        return Double.NaN;
    }
}
