/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods;

import forecasting.forecastMethods.arima.*;
import forecasting.forecastMethods.other.MeanForecast;
import forecasting.forecastMethods.other.RandomWalkForecast;
import forecasting.forecastMethods.smoothing.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * This enumeration lists all available forecast methods.
 *
 * @author Matthias Sommer
 */
public enum ForecastMethod {
    RANDOMWALK() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new RandomWalkForecast(timeSeries, observations);
        }
    }, MEAN() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new MeanForecast(timeSeries, observations);
        }
    }, MOVINGAVERAGE() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new MovingAverage(timeSeries, observations);
        }
    }, DOUBLEMOVINGAVERAGE() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new DoubleMovingAverage(timeSeries, observations);
        }
    }, ES() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new ExponentialSmoothing(timeSeries, observations);
        }
    }, DES() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new DoubleExponentialSmoothing(timeSeries, observations);
        }
    }, SEASONALEXPONENTIALSMOOTHING() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new SeasonalExponentialSmoothing(timeSeries, observations);
        }
    }, DOUBLESEASONALHOLTWINTERS() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new DoubleSeasonalHoltWinters(timeSeries, observations);
        }
    }, CUBICSPLINE() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new CubicSpline(timeSeries, observations);
        }
    }, CROSTON() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new Croston(timeSeries, observations);
        }
    }, TBATS() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new TBATS(timeSeries, observations);
        }
    }, BATS() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new BATS(timeSeries, observations);
        }
    }, ETS() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new ETS(timeSeries, observations);
        }
    }, SARIMA() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new Sarima(timeSeries, observations);
        }
    }, ARIMA() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new ARIMA(timeSeries, observations);
        }
    }, ARIMA101() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new ARIMA101(timeSeries, observations);
        }
    }, AUTOARIMA() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new AUTOARIMA(timeSeries, observations);
        }
    }, ARFIMA() {
        @NotNull
        public AbstractForecastMethod create(TimeSeriesStorage timeSeries, int observations) {
            this.minDataPoints = observations;
            return new ARFIMA(timeSeries, observations);
        }
    };

    int minDataPoints = 0;

    /**
     * Retrieve the biggest timeSeries size over all active forecast methods.
     * Size of the moving window should be as large as the timeSeries of the forecastMethod
     * having the largest size of historic values for prediction.
     *
     * @param forecastMethods the forecast methods
     * @return timeSeries size
     */
    public static int getMaxDataPoints(@NotNull Collection<ForecastMethod> forecastMethods) {
        if (forecastMethods.isEmpty()) {
            return 0;
        }

        int storageSize = 0;
        for (ForecastMethod method : forecastMethods) {
            storageSize = Math.max(method.minDataPoints, storageSize);
        }
        return storageSize;
    }

    @NotNull
    public abstract AbstractForecastMethod create(TimeSeriesStorage timeSeries, int size);
}
