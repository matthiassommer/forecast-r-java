package forecasting.normalisation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Michael on 28.09.2015.
 */
public interface NormalisationInterface {
    @NotNull
    double[] normalize(double[] timeSeries);

    @NotNull
    double[] denormalize(double[] timeSeries);
}
