/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods.smoothing;

import forecasting.forecastMethods.AbstractForecastMethod;
import forecasting.forecastMethods.TimeSeriesStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * Exponential smoothing state space model. The methodology is fully automatic.
 * The only required argument for ets is the time series. The model is chosen automatically if not specified.
 * <p>
 * Hyndman, R.J., Akram, Md., and Archibald, B. (2008) "The admissible parameter space for exponential smoothing models". Annals of Statistical Mathematics, 60(2), 407�426.
 * <p>
 * Created by alexandermartel on 11.08.15.
 */
public class ETS extends AbstractForecastMethod {
    /**
     * if false automatic, else use damped trend
     */
    private final Boolean damped = null;
    /**
     * "N"=none, "A"=additive, "M"=multiplicative and "Z"=automatically selected"
     */
    @NotNull
    private String errorType = "Z";
    @NotNull
    private String trendType = "Z";
    @NotNull
    private String seasonType = "Z";
    /**
     * data smoothing factor, 0 < alpha < 1. If null, its estimated.
     */
    @Nullable
    private Float alpha = null;
    /**
     * trend smoothing factor, 0 < beta < 1, beta < alpha. If null, its estimated.
     */
    @Nullable
    private Float beta = null;
    /**
     * seasonal smoothing factor. If null, its estimated.
     */
    @Nullable
    private Float gamma = null;
    /**
     * damping parameter, dampens the trend, so that it approaches a constant value (used to reduce 'over-forecasting')
     */
    @Nullable
    private Float phi = null;

    public ETS(TimeSeriesStorage storage, int minObservations) {
        super(storage);
        this.minObservations = minObservations;
    }

    @Override
    public double runForecast(int timeStep) throws REngineException, REXPMismatchException {
        if (this.timeSeries.getSize() >= minObservations) {
            super.injectTimeSeries();

            //holt is a wrapper function for forecast(ets(<holt params>))
            StringBuilder sb = new StringBuilder();

            sb.append("forecast(ets(input,");
            String model = "\"" + errorType + trendType + seasonType + "\"";
            sb.append(model);

            if (damped == null) {
                sb.append(", damped=NULL");
            } else if (damped) {
                sb.append(", damped=TRUE");
            } else {
                sb.append(", damped=FALSE");
            }

            if (alpha != null) {
                sb.append(", alpha=").append(alpha);
            }

            if (beta != null) {
                sb.append(", beta=").append(beta);
            }

            if (gamma != null) {
                sb.append(", gamma=").append(gamma);
            }

            if (phi != null) {
                sb.append(", phi=").append(phi);
            }

            sb.append("), h=").append(timeStep).append(")");

            return runForecast(sb.toString(), timeStep);
        }

        return Double.NaN;
    }
}
