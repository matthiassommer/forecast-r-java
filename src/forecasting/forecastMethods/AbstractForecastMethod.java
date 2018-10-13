/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.forecastMethods;

import forecasting.ForecastMethodEvaluator;
import forecasting.RServeConnection;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.util.List;

/**
 * Interface for forecast methods.
 * <p>
 * https://cran.r-project.org/web/packages/forecast/forecast.pdf
 *
 * @author Matthias Sommer
 */
public abstract class AbstractForecastMethod {
    /**
     * RCaller interface, used to call R from Java
     */
    protected static final RConnection rConnection = RServeConnection.getConnection();
    /**
     * Confidence interval for the forecast.
     */
    protected final int predictionConfidenceLowerLevel = 80;
    protected final int predictionConfidenceUpperLevel = 95;
    /**
     * The unique identifier for this forecastMethod.
     */
    private final int identifier;
    /**
     * Stores the last recorded values.
     */
    protected TimeSeriesStorage timeSeries;
    /**
     * Number of observations the method needs to make a forecast.
     */
    protected int minObservations;
    private ForecastMethodEvaluator evaluator;

    /**
     * Constructor for the {@link AbstractForecastMethod}.
     */
    protected AbstractForecastMethod(TimeSeriesStorage timeSeries) {
        this.identifier = this.hashCode();
        this.timeSeries = timeSeries;

        evaluator = new ForecastMethodEvaluator();
    }

    public ForecastMethodEvaluator getEvaluator() {
        return evaluator;
    }

    public void addActualValueToEvaluator(float timeStep, double value) {
        this.evaluator.addActualValueToPair(timeStep, value);
    }

    public final int getUniqueIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the last {@code size} entries of the {@code TimeSeriesStorage} object.
     *
     * @return {@code size} entries of the {@code TimeSeriesStorage}.
     */
    @NotNull
    public final List<Double> getSublistOfTimeSeries(int numberEntries) {
        if (numberEntries > this.timeSeries.getSize()) {
            return this.timeSeries.getValues();
        }

        List<Double> timeSeries = this.timeSeries.getValues();
        return timeSeries.subList(timeSeries.size() - numberEntries, timeSeries.size());
    }

    /**
     * Runs a forecast for a time step further into the future. A value of 1
     * refers to the next time step t + 1.
     * <p>
     * A horizon value of 1 equals a one-step forecast.
     *
     * @param horizon to predict into the future
     * @return forecast
     */
    public abstract double runForecast(int horizon) throws REngineException, REXPMismatchException;

    public int getTimeSeriesLength() {
        return this.timeSeries.getSize();
    }

    /**
     * Pass the input (the time series to forecast) to R.
     *
     * @throws REngineException
     */
    protected void injectTimeSeries() throws REngineException {
        try {
            List<Double> sublist = getSublistOfTimeSeries(this.minObservations);
            Double[] timeSeries = sublist.toArray(new Double[sublist.size()]);
            rConnection.assign("input", ArrayUtils.toPrimitive(timeSeries));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Runs a forecast method in R with the forecast package.
     *
     * @param parameters string resembles the R code
     * @param timeStep   to make a forecast
     * @return the forecast for the given timestep
     * @throws RserveException
     * @throws REXPMismatchException
     */
    protected double runForecast(String parameters, int timeStep) throws RserveException, REXPMismatchException {
        RList rList = rConnection.eval(parameters).asList();

        double[] mean = rList.at("mean").asDoubles();
        return mean[timeStep - 1];
    }
}
