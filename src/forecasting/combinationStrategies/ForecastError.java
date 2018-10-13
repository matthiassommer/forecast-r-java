package forecasting.combinationStrategies;

/**
 * Created by oc6admin on 03.03.2016.
 */
class ForecastError extends CombinationStrategy {
    public double run() {
        removeInvalidEntries(forecasts, weights);
        if (!weights.isEmpty()) {
            normaliseWeightsSumToOne(weights);
            return simpleWeightedSum(forecasts, weights);
        }
        return Double.NaN;
    }
}
