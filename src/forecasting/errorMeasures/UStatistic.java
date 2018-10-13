package forecasting.errorMeasures;

/**
 * Theil’s U statistic is a relative accuracy measure that compares the forecasted results with the results of forecasting with minimal historical data.
 * It also squares the deviations to give more weight to large errors and to exaggerate errors, which can help eliminate methods with large errors.
 * <p>
 * <1: The forecasting technique is better than guessing.
 * 1: The forecasting technique is about as good as guessing.
 * >1: The forecasting technique is worse than guessing.
 * <p>
 * https://docs.oracle.com/cd/E40248_01/epm.1112/cb_statistical/frameset.htm?ch07s02s03s04.html
 * <p>
 * Created by oc6admin on 04.03.2016.
 */
public class UStatistic extends AbstractErrorMeasure {
    @Override
    public double run(double[] observations, double[] forecasts) {
        double sum1 = 0;
        double sum2 = 0;

        for (int i = 1; i < forecasts.length; i++) {
            double previous = observations[i - 1];
            double actual = observations[i];

            if (!Double.isNaN(forecasts[i]) && !Double.isNaN(actual) && previous != 0.0) {
                sum1 += Math.pow((forecasts[i] - actual) / previous, 2);
                sum2 += Math.pow((actual - previous) / previous, 2);
            }
        }

        return Math.sqrt(sum1 / sum2);
    }
}
