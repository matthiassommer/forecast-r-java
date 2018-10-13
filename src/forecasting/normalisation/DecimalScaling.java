package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Michael on 28.09.2015.
 */
public class DecimalScaling implements NormalisationInterface {
    private double p;

    public DecimalScaling(@NotNull double[] timeSeries) {
        double maximumDataPoint = getMaximumDataPoint(timeSeries);
        this.p = Double.toString(maximumDataPoint).length();
    }

    @NotNull
    @Override
    public double[] normalize(@NotNull double[] timeSeries) {
        double[] normalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            normalizedTimeSeries[i] = timeSeries[i] / Math.pow(10, p);
        }

        return normalizedTimeSeries;
    }

    @NotNull
    @Override
    public double[] denormalize(@NotNull double[] timeSeries) {
        double[] denormalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            denormalizedTimeSeries[i] = timeSeries[i] * Math.pow(10, p);
        }

        return denormalizedTimeSeries;
    }

    private double getMaximumDataPoint(@NotNull double[] timeSeries) {
        double max = 0;
        for (double dataPoint : timeSeries) {
            if (dataPoint > max) {
                max = dataPoint;
            }
        }
        return max;
    }
}
