/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.combinationStrategies;

import forecasting.forecastMethods.AbstractForecastMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Each individual weight is interpreted as the probability that its respective forecast will perform
 * the best (in the smallest absolute error sense) on the next occasion. Each probability is
 * estimated as the fraction of occurrences in which its respective forecasting model has
 * performed the best in the past.
 * <p>
 * A Bayesian approach to the linear combination of forecasts, Bunn, 1975
 *
 * @author Matthias Sommer.
 */
class Outperformance extends CombinationStrategy {
    /**
     * Map stores the number of times each forecastMethod had the overall lowest forecast error in the last runs timesteps.
     */
    @NotNull
    private final Map<AbstractForecastMethod, Integer> outperformanceWeights;
    /**
     * Maximum number of the last stored winner counts.
     */
    private final int runs = 10;
    /**
     * Moving window of the last runs best AbstractForecastMethods.
     */
    private final List<AbstractForecastMethod> forecastWinners = new ArrayList<>(runs);
    private double bestWeight = Double.MAX_VALUE;
    @Nullable
    private AbstractForecastMethod bestForecastMethod;

    Outperformance() {
        this.outperformanceWeights = new HashMap<>();
    }

    void addForecastMethod(final AbstractForecastMethod forecastMethod) {
        this.outperformanceWeights.put(forecastMethod, 0);
    }

    public double run() {
        List<Double> weights = getWeights();
        if (weights.isEmpty()) {
            return Double.NaN;
        }

        removeInvalidEntries(forecasts, weights);
        if (weights.isEmpty()) {
            return Double.NaN;
        }

        return simpleWeightedSum(forecasts, weights);
    }

    @NotNull
    private List<Double> getWeights() {
        if (this.bestForecastMethod == null) {
            return Collections.emptyList();
        }

        updateOutperformanceMap(this.bestForecastMethod);
        if (this.forecastWinners.size() < this.runs) {
            return Collections.emptyList();
        }

        return calculateOutperformanceWeights();
    }

    @NotNull
    private List<Double> calculateOutperformanceWeights() {
        List<Double> weights = new ArrayList<>(this.outperformanceWeights.size());
        for (double count : this.outperformanceWeights.values()) {
            weights.add((1 + count) / (this.outperformanceWeights.size() + this.runs));
        }

        logWeights(weights);

        return weights;
    }

    private void updateOutperformanceMap(AbstractForecastMethod forecastMethod) {
        this.outperformanceWeights.computeIfPresent(forecastMethod, (p, count) -> count + 1);
        this.forecastWinners.add(forecastMethod);

        while (this.forecastWinners.size() > runs) {
            AbstractForecastMethod oldest = this.forecastWinners.remove(0);
            this.outperformanceWeights.computeIfPresent(oldest, (p, count) -> count - 1);
        }
    }

    void reset() {
        this.bestForecastMethod = null;
        this.bestWeight = Double.MAX_VALUE;
    }

    void update(double weight, AbstractForecastMethod forecastMethod) {
        if (weight < this.bestWeight) {
            this.bestForecastMethod = forecastMethod;
            this.bestWeight = weight;
        }
    }
}
