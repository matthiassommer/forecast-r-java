/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.combinationStrategies;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Stock and Watson (2001) A Comparison of Linear and Nonlinear Univariate
 * Models for Forecasting Macroeconomic Time Series
 *
 * @author Matthias Sommer.
 */
class Median extends CombinationStrategy {
    /**
     * Sort forecasts ascending, take middle value and return it as forecast.
     *
     * @return median of forecasts
     */
    public double run() {
        Collections.sort(forecasts);
        final int size = forecasts.size();

        if (size % 2 != 0) {
            return forecasts.get((size - 1) / 2);
        }
        return (forecasts.get(size / 2 - 1) + forecasts.get(size / 2)) / 2;
    }
}
