/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting;

import forecasting.forecastMethods.AbstractForecastMethod;

import java.util.List;

/**
 * Stores a forecast with his associated time step and the actual value for later evaluation of the {@link AbstractForecastMethod}.
 *
 * @author Matthias Sommer
 */
public class ForecastRealValueEntry {
    /**
     * The time series on which the forecast was calculated.
     */
    private final List<Double> timeseries;
    /**
     * Not the time step when forecast was generated, but the time step for which the forecast was made.
     */
    private final float forecastTime;
    /**
     * Value of 1 equals a single step forecast, bigger values equal a multi-step forecast.
     */
    private final int step;
    /**
     * The actualValue represents the forecast based on the timeseries.
     */
    private double forecast = Double.NaN;
    /**
     * The according real actualValue for this forecast.
     */
    private double actualValue = Double.NaN;

    public ForecastRealValueEntry(double forecast, float forecastTime, int step, List<Double> timeseries) {
        this.forecast = forecast;
        this.forecastTime = forecastTime;
        this.timeseries = timeseries;
        this.step = step;
    }

    public List<Double> getTimeseries() {
        return this.timeseries;
    }

    public final double getActualValue() {
        return this.actualValue;
    }

    public final void setActualValue(double value) {
        this.actualValue = value;
    }

    final float getForecastTime() {
        return this.forecastTime;
    }

    public final double getForecast() {
        return this.forecast;
    }

    public double getAbsoluteError() {
        return Math.abs(this.forecast - this.actualValue);
    }
}
