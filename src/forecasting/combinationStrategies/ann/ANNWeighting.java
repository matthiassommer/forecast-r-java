/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.combinationStrategies.ann;

import forecasting.DefaultForecastParameters;
import forecasting.combinationStrategies.CombinationStrategy;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.quick.QuickPropagation;
import tools.LimitedQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses an Artificial Neural Network to find the optimal weights for the combination of forecasts.
 * A simple feed forward network with one hidden layer.
 * Receives the forecast values from all forecastMethods who made forecasts and returns the combined forecast value.
 * <p>
 * nonlinear combination.
 * <p>
 * Data has to be normalised to [0;1] because of the sigmoid function to work properly!
 *
 * @author Matthias Sommer.
 */
public class ANNWeighting extends CombinationStrategy {
    /**
     * Number of values needed for the training set.
     */
    private final int capacity = 300;
    private LimitedQueue<MLDataPair> trainingSet;
    private BasicNetwork network;
    private boolean isANNTrained = false;
    private int hiddenNeurons = 7;
    private List<Double> lastForecasts = new ArrayList<>();

    public ANNWeighting() {
        this.network = ANNFactory.TYPES.FEED_FORWARD.create(DefaultForecastParameters.DEFAULT_FORECAST_METHODS.size(), hiddenNeurons, 1);
        trainingSet = new LimitedQueue<>(capacity);
    }

    public double run() {
        if (forecasts.size() < DefaultForecastParameters.DEFAULT_FORECAST_METHODS.size()) {
            return Double.NaN;
        } else if (forecasts.contains(Double.NaN)) {
            return Double.NaN;
        }

        if (!this.trainingSet.isFull()) {
            if (!lastForecasts.isEmpty()) {
                makeInputAndIdeal();
            }
            lastForecasts = this.forecasts;
        }

        if (!this.isANNTrained && this.trainingSet.isFull()) {
            trainNetwork();
            this.isANNTrained = true;

            // force re-training after certain intervals (defined by the capacity)
          //  this.trainingSet.clear();
        }

        if (this.isANNTrained) {
            return getForecast();
        }
        return Double.NaN;
    }

    private void makeInputAndIdeal() {
        MLData input = new BasicMLData(this.lastForecasts.size());
        for (int i = 0; i < this.lastForecasts.size(); i++) {
            input.setData(i, lastForecasts.get(i));
        }

        MLData ideal = new BasicMLData(1);
        ideal.setData(0, lastTimeseries.getLastValue());

        this.trainingSet.add(new BasicMLDataPair(input, ideal));
    }

    public double getForecast() {
        MLData input = new BasicMLData(this.forecasts.size());
        for (int i = 0; i < this.forecasts.size(); i++) {
            input.setData(i, forecasts.get(i));
        }

        MLData output = this.network.compute(input);
        return output.getData(0);
    }

    public void trainNetwork() {
        Propagation train = new QuickPropagation(network, new BasicMLDataSet(trainingSet));

        int epoch = 0;
        do {
            train.iteration();
            epoch++;
        } while (train.getError() > 0.001 && epoch < 200);
    }
}
