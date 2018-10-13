package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Created by Michael on 28.09.2015.
 */
public class Median implements NormalisationInterface {
    private double median;

    public Median(@NotNull double[] timeSeries) {
        this.median = getMedianDataPoint(timeSeries);
    }

    @NotNull
    @Override
    public double[] normalize(@NotNull double[] timeSeries) {
        double[] normalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            normalizedTimeSeries[i] = (timeSeries[i] / median);
        }

        return normalizedTimeSeries;
    }

    @NotNull
    @Override
    public double[] denormalize(@NotNull double[] timeSeries) {
        double[] denormalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            denormalizedTimeSeries[i] = (timeSeries[i] * median);
        }

        return denormalizedTimeSeries;
    }

    public double getMedianDataPoint(@NotNull double[] timeSeries) {
        double[] sortedData = new double[timeSeries.length];
        System.arraycopy(timeSeries, 0, sortedData, 0, timeSeries.length);
        Arrays.sort(sortedData);

        int middle = sortedData.length / 2;
        if (sortedData.length % 2 == 0) {
            double left = sortedData[middle - 1];
            double right = sortedData[middle];
            return (left + right) / 2;
        } else {
            return sortedData[middle];
        }
    }
}
