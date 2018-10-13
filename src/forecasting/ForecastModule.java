/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting;

import forecasting.combinationStrategies.CombinationModule;
import forecasting.combinationStrategies.Strategies;
import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.ForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import forecasting.forecastMethods.arima.ARIMA;
import org.jetbrains.annotations.NotNull;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Acts as the connector between an external module and this library.
 *
 * @author Matthias Sommer
 */
public class ForecastModule {
    @NotNull
    private final CombinationModule combinationModule;
    /**
     * Stores a moving window of averaged values over a certain time span.
     */
    @NotNull
    private final TimeSeriesStorage timeSeriesStorage;
    @NotNull
    private final Collection<AbstractForecastMethod> forecastMethods;
    /**
     * Stores the latest calculated individual forecasts.
     */
    private List<Double> forecasts;
    private ForecastMethodEvaluator combinedForecastEvaluator;

    /**
     * Initialises the forecast module, the combination method and the forecast methods to be used.
     */
    public ForecastModule() {
        DefaultForecastParameters.readPropertyFile("");

        this.forecastMethods = new ArrayList<>(DefaultForecastParameters.DEFAULT_FORECAST_METHODS.size());
        this.forecasts = new ArrayList<>(DefaultForecastParameters.DEFAULT_FORECAST_METHODS.size());

        this.combinationModule = new CombinationModule();

        this.timeSeriesStorage = new TimeSeriesStorage();

        initForecastMethods();
        this.combinedForecastEvaluator = new ForecastMethodEvaluator();

        int capacity = ForecastMethod.getMaxDataPoints(DefaultForecastParameters.DEFAULT_FORECAST_METHODS);
        this.timeSeriesStorage.setCapacity(capacity);
    }

    /**
     * Add a value to forecast storages of forecastMethods and accumulated forecast timeSeries.
     *
     * @param timeStep current time step of the simulation
     * @param value    current actual sensor value
     */
    public final void addValue(float timeStep, double value) {
        this.timeSeriesStorage.addValue(value);

        if (DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY == Strategies.XCSF) {
            this.combinationModule.rewardForXCSF(timeStep, value);
        }
    }

    public final void addValueToEvaluators(float timeStep, double value) {
        for (AbstractForecastMethod forecastMethod : this.forecastMethods) {
            forecastMethod.addActualValueToEvaluator(timeStep, value);
        }
        this.combinedForecastEvaluator.addActualValueToPair(timeStep, value);
    }

    public List<Double> getForecasts() {
        return forecasts;
    }

    /**
     * Get a forecast of traffic data for the next cycle time where an adaptation of the current TLC is
     * possible.
     *
     * @param time             the current simulation time horizon
     * @param horizon          horizon for which we want the forecast
     * @param timestepForecast the time the forecast is made for
     * @return predicted traffic data
     */
    public final double combinedForecast(final float time, final int horizon, final float timestepForecast) {
        resetOutperformanceStrategy();

        // only forecasts != NaN except Outperformance
        List<Double> forecasts = new ArrayList<>(this.forecastMethods.size());
        List<Double> weights = new ArrayList<>(this.forecastMethods.size());
        // all forecasts
        this.forecasts = new ArrayList<>(this.forecastMethods.size());

        for (AbstractForecastMethod forecastMethod : this.forecastMethods) {
            double forecast = runForecastMethod(forecastMethod, horizon);
            this.forecasts.add(forecast);

            ForecastMethodEvaluator forecastEvaluator = forecastMethod.getEvaluator();
            int dataPointsForForecast = ForecastMethod.getMaxDataPoints(DefaultForecastParameters.DEFAULT_FORECAST_METHODS);

            if (DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY == Strategies.OUTPERFORMANCE) {
                forecasts.add(forecast);
                forecastEvaluator.addForecast(timestepForecast, forecast, horizon, forecastMethod.getSublistOfTimeSeries(dataPointsForForecast));

                double weight = forecastEvaluator.lastAbsoluteError();
                combinationModule.updateOutperformance(weight, forecastMethod);
            }
            // forecast is valid
            else if (!Double.isNaN(forecast)) {
                forecasts.add(forecast);
                forecastEvaluator.addForecast(timestepForecast, forecast, horizon, forecastMethod.getSublistOfTimeSeries(dataPointsForForecast));

                switch (DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY) {
                    case FORECAST_ERROR:
                        double weight = forecastEvaluator.getMASE();
                        weights.add(Math.abs(weight));
                        break;
                    case OPTIMALWEIGHTS:
                        double forecastError = forecastEvaluator.lastAbsoluteError();
                        if (!Double.isNaN(forecastError)) {
                            this.combinationModule.updateOptimalWeights(forecastMethod, forecastError);
                        } else {
                            forecasts.remove(forecasts.size() - 1);
                        }
                        break;
                }
            }
        }

        if (forecasts.isEmpty()) {
            return Double.NaN;
        }

        double combinedForecast = this.combinationModule.getCombinedForecast(forecasts, weights, time, timeSeriesStorage);

        this.combinedForecastEvaluator.addForecast(timestepForecast, combinedForecast, horizon, Collections.emptyList());

        //if combined forecast is NaN --> Fallback to simple average
       /* if (Double.isNaN(combinedForecast)) {
            double forecastSum = 0;
            for (double forecast : forecasts) {
                forecastSum += forecast;
            }
            return forecastSum / (double) forecasts.size();
        }*/

        return combinedForecast;
    }

    private double runForecastMethod(AbstractForecastMethod forecastMethod, int horizon) {
        try {
            return forecastMethod.runForecast(horizon);
        } catch (@NotNull REngineException | REXPMismatchException e) {
            System.err.println(forecastMethod.getClass().getSimpleName() + " - " + e.getMessage()
                    + "\t time series: " + this.timeSeriesStorage.getValues());
            return Double.NaN;
        }
    }

    private void resetOutperformanceStrategy() {
        if (DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY == Strategies.OUTPERFORMANCE) {
            this.combinationModule.resetOutperformance();
        }
    }

    public double combinedForecastError() {
        return this.combinedForecastEvaluator.getMASE();
    }

    private void initForecastMethods() {
        List<ForecastMethod> forecastMethods = DefaultForecastParameters.DEFAULT_FORECAST_METHODS;

        for (int i = 0; i < forecastMethods.size(); i++) {
            ForecastMethod method = forecastMethods.get(i);
            int size = DefaultForecastParameters.FORECAST_METHOD_DATA_POINTS.get(i);
            AbstractForecastMethod forecastMethod = method.create(this.timeSeriesStorage, size);

            if (forecastMethod instanceof ARIMA) {
                ((ARIMA) forecastMethod).setP(DefaultForecastParameters.getP(i));
                ((ARIMA) forecastMethod).setD(DefaultForecastParameters.getD(i));
                ((ARIMA) forecastMethod).setQ(DefaultForecastParameters.getQ(i));
            }

            this.forecastMethods.add(forecastMethod);

            if (DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY == Strategies.OUTPERFORMANCE) {
                this.combinationModule.addMethodForOutperformance(forecastMethod);
            }
        }
    }
}
