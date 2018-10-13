/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.combinationStrategies;

import forecasting.DefaultForecastParameters;
import forecasting.combinationStrategies.xcsf.XCSF;
import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Executes the chosen combination strategy for the combination of forecasts (ensemble forecasting).
 */
public class CombinationModule {
    private CombinationStrategy strategy;

    /**
     * Initialise the chosen combination strategy.
     */
    public CombinationModule() {
        this.strategy = DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY.create();
        this.strategy.initWeightPrinter();
    }

    public void rewardForXCSF(float timeStep, double value) {
        ((XCSF) strategy).receiveReward(timeStep, value);
    }

    public void resetOutperformance() {
        ((Outperformance) this.strategy).reset();
    }

    public void updateOutperformance(double weight, AbstractForecastMethod forecastMethod) {
        ((Outperformance) this.strategy).update(weight, forecastMethod);
    }

    public void updateOptimalWeights(AbstractForecastMethod forecastMethod, double forecastError) {
        ((OptimalWeights) this.strategy).addForecastMethod(forecastMethod);
        ((OptimalWeights) this.strategy).addForecastError(forecastMethod, forecastError);
    }

    /**
     * Calculate a combined forecast based on the chosen method, the forecasts and their respective weights.
     *
     * @param forecasts  list of forecast values
     * @param weights    list of combination weights
     * @param time       step
     * @param timeSeries moving window of actual values
     * @return combined forecast
     */
    public double getCombinedForecast(@NotNull List<Double> forecasts, @NotNull List<Double> weights,
                                      float time, TimeSeriesStorage timeSeries) {
        strategy.setForecasts(forecasts);
        strategy.setWeights(weights);
        strategy.setTime(time);
        strategy.setLastTimeseries(timeSeries);

        if (strategy instanceof XCSF) {
            // not yet enough forecasts available
            if (forecasts.size() != DefaultForecastParameters.DEFAULT_FORECAST_METHODS.size()) {
                return Double.NaN;
            }
        }

        return strategy.run();
    }

    public void addMethodForOutperformance(AbstractForecastMethod method) {
        ((Outperformance) this.strategy).addForecastMethod(method);
    }
}