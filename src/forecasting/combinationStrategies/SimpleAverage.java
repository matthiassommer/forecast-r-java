package forecasting.combinationStrategies;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oc6admin on 03.03.2016.
 */
class SimpleAverage extends CombinationStrategy {
    public double run() {
        double forecastSum = 0;
        for (double forecast : forecasts) {
            forecastSum += forecast;
        }
        return forecastSum / (double) forecasts.size();
    }
}
