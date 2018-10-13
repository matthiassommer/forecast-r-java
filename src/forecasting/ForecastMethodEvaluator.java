/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting;

import forecasting.errorMeasures.AbstractErrorMeasure;
import forecasting.errorMeasures.MASE;
import org.jetbrains.annotations.NotNull;
import tools.LimitedQueue;

import java.util.Iterator;
import java.util.List;

/**
 * Each instance of this class belongs exactly to one forecast method.
 * <p>
 * It stores its forecasts and the actual traffic values.
 * Evaluates a method based on its forecast errors.
 *
 * @author Matthias Sommer
 */
public class ForecastMethodEvaluator {
    /**
     * Stores the last mappings of forecasts and actual values.
     */
    @NotNull
    private final LimitedQueue<ForecastRealValueEntry> forecastActualPairs = new LimitedQueue<>(10);
    @NotNull
    private final AbstractErrorMeasure mase = new MASE();

    @NotNull
    public LimitedQueue<ForecastRealValueEntry> getForecastActualPairs() {
        return forecastActualPairs;
    }

    /**
     * Method for usage in {@see ForecastModule}.
     * FIFO.
     * add forecast to timeSeries for calculation of forecast error etc.
     * Only stores one-step forecasts.
     *
     * @param forecastTime time step the forecast was made for
     * @param forecast
     * @param step         multi- or singlestep forecast
     * @param timeseries   the  timeseries the forecast is based on
     */
    final void addForecast(float forecastTime, double forecast, int step, List<Double> timeseries) {
        if (Double.isNaN(forecast)) {
            return;
        }

        // entry already exists for this time step and this forecast method
        for (ForecastRealValueEntry storage : this.forecastActualPairs) {
            if (storage.getForecastTime() == forecastTime) {
                return;
            }
        }

        ForecastRealValueEntry entry = new ForecastRealValueEntry(forecast, forecastTime, step, timeseries);
        this.forecastActualPairs.add(entry);
    }

    /**
     * Add the actual value to a forecast-observation pair.
     *
     * @param time  the time step of the observation
     * @param value actual observation
     */
    public final void addActualValueToPair(float time, double value) {
        Iterator<ForecastRealValueEntry> it = this.forecastActualPairs.descendingIterator();
        while (it.hasNext()) {
            ForecastRealValueEntry pair = it.next();

            // next pairs are only further away in time
            if (pair.getForecastTime() < time) {
                break;
            }

            if (pair.getForecastTime() == time) {
                pair.setActualValue(value);
                break;
            }
        }
    }

    double lastAbsoluteError() {
        if (this.forecastActualPairs.size() < 2) {
            return Double.NaN;
        }
        int lastValidEntry = this.forecastActualPairs.size() - 2;

        double actual = this.forecastActualPairs.get(lastValidEntry).getActualValue();
        double forecast = this.forecastActualPairs.get(lastValidEntry).getForecast();

        return Math.abs(actual - forecast);
    }

    double getMASE() {
        if (!this.forecastActualPairs.isFull()) {
            return Double.NaN;
        }

        // -1 because newest entry has no actual value
        int size = this.forecastActualPairs.size() - 1;

        double[] actual = new double[size];
        double[] forecasts = new double[size];
        for (int i = 0; i < size; i++) {
            ForecastRealValueEntry entry = this.forecastActualPairs.get(i);
            actual[i] = entry.getActualValue();
            forecasts[i] = entry.getForecast();
        }

        return mase.run(actual, forecasts);
    }
}
