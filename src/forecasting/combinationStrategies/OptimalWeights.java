/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.combinationStrategies;

import Jama.Matrix;
import forecasting.forecastMethods.AbstractForecastMethod;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Optimal weights: The linear weights are calculated to minimise the error variance of the
 * combination (assuming unbiasedness for each individual forecast). The weights sum to one.
 * <p>
 * Optimal (adaptive) with independence assumption: The estimate of S is restricted to
 * be diagonal, comprising just the individual forecast error variances.
 * <p>
 * Optimal (adaptive) with restricted weights: no individual weight can be outside the interval [0,1].
 * <p>
 * Review of Guidelines for the Use of Combined Forecasts
 * Bates & Granger (1969)
 * <p>
 * Vector of weights w = (S^-1 * E) (E'*S^-1*E)^-1
 * <p>
 * InTech-How_to_provide_accurate_and_robust_traffic_forecasts_practically 3.2.1ff
 *
 * @author Matthias Sommer.
 */
class OptimalWeights extends CombinationStrategy {
    /**
     * Maps each forecast method with a list of its latest forecast errors.
     */
    @NotNull
    private final Map<AbstractForecastMethod, List<Double>> forecastErrorMap;
    /**
     * Number of entries needed for the average forecast error calculation.
     */
    private final int averageCount = 5;

    OptimalWeights() {
        this.forecastErrorMap = new HashMap<>();
    }

    void addForecastMethod(final AbstractForecastMethod forecastMethod) {
        this.forecastErrorMap.putIfAbsent(forecastMethod, new ArrayList<>(averageCount));
    }

    void addForecastError(AbstractForecastMethod forecastMethod, double error) {
        List<Double> errors = this.forecastErrorMap.get(forecastMethod);
        errors.add(error);

        while (errors.size() > this.averageCount) {
            errors.remove(0);
        }
    }

    private void removeEmptyForecastMethods() {
        Iterator<Map.Entry<AbstractForecastMethod, List<Double>>> it = this.forecastErrorMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<AbstractForecastMethod, List<Double>> entry = it.next();
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
    }

    public double run() {
        List<Double> weights = getWeights();
        if (!weights.isEmpty()) {
            logWeights(weights);
            return simpleWeightedSum(forecasts, weights);
        }
        return Double.NaN;
    }

    @NotNull
    private List<Double> getWeights() {
        removeEmptyForecastMethods();
        if (this.forecastErrorMap.size() < 2) {
            return Collections.emptyList();
        }

        int size = this.forecastErrorMap.size();
        double[][] forecastErrors = getDiagonalErrorMatrix(size);
        Matrix errorMatrix = new Matrix(forecastErrors);

        double determinante = errorMatrix.det();
        if (determinante == 0 || Double.isNaN(determinante)) {
            return Collections.emptyList();
        }

        Matrix inverseErrorMatrix = errorMatrix.inverse();

        Matrix unitVector = createUnitMatrix(size, 1);
        Matrix unitMatrix = createUnitMatrix(1, size);

        Matrix m = inverseErrorMatrix.times(unitVector);
        Matrix n = unitMatrix.times(inverseErrorMatrix).times(unitVector);

        Matrix weights = m.times(n.inverse());

        List<Double> weightsList = new ArrayList<>(weights.getColumnDimension());
        for (int j = 0; j < weights.getRowDimension(); j++) {
            weightsList.add(weights.get(j, 0));
        }
        return weightsList;
    }

    @NotNull
    private Matrix createUnitMatrix(int cols, int rows) {
        double[][] identity = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                identity[j][i] = 1;
            }
        }
        return new Matrix(identity);
    }

    /**
     * The estimate of S in (1) is restricted to be diagonal, comprising just the individual forecast error variances.
     * --> Werte nur auf Hauptdiagonale, sonst alles 0.
     * <p>
     * Um die Unabhängigkeitsannahme fallen zu lassen, müssen die einzelnen Covariancen multipliziert werden:
     * M(1,1)=M(0,1)*M(1,0) usw.
     *
     * @param size n*n, n being the number of forecast models
     * @return forecast covariance matrix
     */
    @NotNull
    private double[][] getDiagonalErrorMatrix(final int size) {
        double[][] forecastErrors = new double[size][size];

        int k = 0;
        for (Map.Entry<AbstractForecastMethod, List<Double>> entry : this.forecastErrorMap.entrySet()) {
            List<Double> errors = entry.getValue();

            // calculate the quadratic sum of all forecasts errors of method i
            double sum = 0;
            for (double error : errors) {
                sum += FastMath.pow(error, 2);
            }

            forecastErrors[k][k] = sum;
            k++;
        }

        return forecastErrors;
    }
}
