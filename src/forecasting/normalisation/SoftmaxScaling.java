package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Michael on 28.09.2015.
 */
public class SoftmaxScaling implements NormalisationInterface {
    @NotNull
    @Override
    public double[] normalize(@NotNull double[] timeSeries) {
        double[] normalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            normalizedTimeSeries[i] = 1.0 / (1 + Math.exp(-timeSeries[i]));
        }

        return normalizedTimeSeries;
    }

    @NotNull
    @Override
    public double[] denormalize(@NotNull double[] timeSeries) {
        double[] denormalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            denormalizedTimeSeries[i] = Math.log(timeSeries[i]) - Math.log(1 - timeSeries[i]);
        }

        return denormalizedTimeSeries;
    }
}
