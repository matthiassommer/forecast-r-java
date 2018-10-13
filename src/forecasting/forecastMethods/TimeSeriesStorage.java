/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods;

import org.jetbrains.annotations.NotNull;
import tools.LimitedQueue;

import java.util.List;

/**
 * A simple LIFO queue which stores the last n time series values.
 *
 * @author Matthias Sommer
 */
public class TimeSeriesStorage {
    /**
     * Stored values over a certain time span.
     */
    @NotNull
    private LimitedQueue<Double> values;

    /**
     * Specifies the maximum number of entries in timeSeries.
     */
    public void setCapacity(final int capacity) {
        this.values = new LimitedQueue<>(capacity);
    }

    /**
     * Add a value to the time series and return the moving average.
     *
     * @param value to store
     * @return average or {@code NaN}
     */
    public final void addValue(final double value) {
        this.values.add(value);
    }

    /**
     * @return the time series
     */
    @NotNull
    public final List<Double> getValues() {
        return this.values;
    }

    /**
     * Returns last value of the time series or {@code NaN} if the time series is empty.
     *
     * @return value or {@code NaN}
     */
    public final double getLastValue() {
        if (this.values.isEmpty()) {
            return Double.NaN;
        }
        return this.values.getLast();
    }

    /**
     * Returns number of stored values.
     *
     * @return length of time series
     */
    public final int getSize() {
        return this.values.size();
    }
}
