package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Michael on 28.09.2015.
 */
public class Exponential implements NormalisationInterface {
    private double a;
    private double b;

    public Exponential(double a, double b) {
        this.a = a;
        this.b = b;
    }

    @NotNull
    @Override
    public double[] normalize(@NotNull double[] timeSeries) {
        double[] normalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            double numerator = Math.exp(a * timeSeries[i]) - 1;
            double denominator = b + Math.exp(a * timeSeries[i]);
            normalizedTimeSeries[i] = numerator / denominator;
        }

        return normalizedTimeSeries;
    }

    @NotNull
    @Override
    public double[] denormalize(@NotNull double[] timeSeries) {
        double[] denormalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            double numerator = Math.log((timeSeries[i] * b + 1) / (1 - timeSeries[i]));
            denormalizedTimeSeries[i] = numerator / a;
        }

        return denormalizedTimeSeries;
    }
}
