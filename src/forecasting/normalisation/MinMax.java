package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

/**
 * Normalises a time series to the value range normalizedLow to normalizedHigh.
 */
public class MinMax implements NormalisationInterface {
    private double actualLow;
    private double actualHigh;
    private double normalizedHigh = 1.0;
    private double normalizedLow = 0.0;

    public MinMax(@NotNull double[] timeSeries) {
        actualLow = getMinimum(timeSeries);
        actualHigh = getMaximum(timeSeries);
    }
    public MinMax(double actualLow, double actualHigh) {
        this.actualHigh = actualHigh;
        this.actualLow = actualLow;
    }

    public MinMax() {
    }

    public void setNormalizedHigh(double normalizedHigh) {
        this.normalizedHigh = normalizedHigh;
    }

    public void setNormalizedLow(double normalizedLow) {
        this.normalizedLow = normalizedLow;
    }

    public double getActualHigh() {
        return actualHigh;
    }

    @NotNull
    @Override
    public double[] normalize(@NotNull double[] timeSeries) {
        double[] normalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            normalizedTimeSeries[i] = (timeSeries[i] - actualLow) / (actualHigh - actualLow) * (normalizedHigh - normalizedLow) + normalizedLow;
        }

        return normalizedTimeSeries;
    }

    @NotNull
    @Override
    public double[] denormalize(@NotNull double[] timeSeries) {
        double[] denormalizedTimeSeries = new double[timeSeries.length];

        for (int i = 0; i < timeSeries.length; i++) {
            denormalizedTimeSeries[i] = ((actualLow - actualHigh) * timeSeries[i] - normalizedHigh * actualLow + actualHigh * normalizedLow) /
                    (normalizedLow - normalizedHigh);
        }

        return denormalizedTimeSeries;
    }

    public double getMinimum(@NotNull double[] timeSeries) {
        double min = Double.MAX_VALUE;
        for (double dataPoint : timeSeries) {
            if (dataPoint < min) {
                min = dataPoint;
            }
        }
        return min;
    }

    public double getMaximum(@NotNull double[] timeSeries) {
        double max = 0;
        for (double dataPoint : timeSeries) {
            if (dataPoint > max) {
                max = dataPoint;
            }
        }
        return max;
    }
}
