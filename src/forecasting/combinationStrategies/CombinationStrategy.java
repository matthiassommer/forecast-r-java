package forecasting.combinationStrategies;

import forecasting.forecastMethods.TimeSeriesStorage;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Contains the neccesary data for the several combination strategies.
 * <p>
 * Created by oc6admin on 27.02.2016.
 */
public abstract class CombinationStrategy {
    /**
     * Forecasts made by the activated forecast methods.
     */
    protected List<Double> forecasts;
    List<Double> weights;
    /**
     * Time step the forecasts were made.
     */
    protected float time;
    /**
     * Part of the whole time series the forecasts were based on.
     */
    protected TimeSeriesStorage lastTimeseries;
    // For logging of weights.
    PrintStream psWeights;
    private DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public List<Double> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Double> forecasts) {
        this.forecasts = forecasts;
    }

    void setWeights(List<Double> weights) {
        this.weights = weights;
    }

    void setLastTimeseries(TimeSeriesStorage lastTimeseries) {
        this.lastTimeseries = lastTimeseries;
    }

    public abstract double run();

    void logWeights(List<Double> weights) {
        String output = "";
        for (double weight : weights) {
            output += "," + df.format(weight);
        }

        if (output.length() > 1)
            psWeights.println(output.substring(1));
    }

    /**
     * Normalise weights, that they sum up to one. Biggest error results in
     * lowest weight.
     *
     * @param weights to normalise
     */
    protected void normaliseWeightsSumToOne(@NotNull List<Double> weights) {
        if (weights.size() == 1) {
            weights.set(0, 1.0);
            return;
        }

        float sum = 0;
        for (double weight : weights) {
            sum += weight * weight;
        }

        int size = weights.size();
        for (int i = 0; i < size; i++) {
            double oldWeight = weights.get(i);
            double newWeight = (1 - oldWeight * oldWeight / sum) / (size - 1);
            weights.set(i, newWeight);
        }
    }

    /**
     * Sums the weighted predictions up. Special case when all weights are
     * equal: simple mean average.
     *
     * @param forecasts
     * @param weights   to weight forecasts
     * @return weighted sum of predictions
     */
    protected double simpleWeightedSum(@NotNull List<Double> forecasts, @NotNull List<Double> weights) {
        double sumOfWeightedForecasts = 0;
        for (int i = 0; i < forecasts.size(); i++) {
            if (!Double.isNaN(forecasts.get(i))) {
                sumOfWeightedForecasts += forecasts.get(i) * weights.get(i);
            }
        }
        return sumOfWeightedForecasts;
    }

    /**
     * Removes invalid entries, when weight is <=0 or NaN.
     */
    protected void removeInvalidEntries(@NotNull Iterable<Double> forecasts, @NotNull List<Double> weights) {
        Iterator<Double> it = forecasts.iterator();
        int i = 0;

        while (it.hasNext()) {
            double weight = weights.get(i);
            double forecast = it.next();
            if (Double.isNaN(forecast) || weight < 0 || Double.isNaN(weight) || Double.isInfinite(weight)) {
                it.remove();
                weights.remove(i);
            } else {
                i++;
            }
        }
    }

    void initWeightPrinter() {
        try {
            this.psWeights = new PrintStream(new FileOutputStream(this.getClass().getSimpleName() + "_weights.txt"), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
