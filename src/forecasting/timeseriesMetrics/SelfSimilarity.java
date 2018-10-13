package forecasting.timeseriesMetrics;

import org.rosuda.REngine.RList;

/**
 * long term memory of a time series / long range dependency between values / long range correlation.
 * <p>
 * value~0.5 -> random
 * value-->0 -> anti-persistent: many high/low switches (-->seasonaility/periodicty?)
 * value-->1 -> persistent: most likely a high will follow on a high and a low on a low (-->trend?)
 * <p>
 * Take the average of the available methods.
 */
public class SelfSimilarity extends TimeseriesMetric {
    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);

            //remove NA values, otherwise we might get no result
            RList rList = rConnection.eval("hurstexp(input)").asList();

            double hurst = 0;
            String hurstMethods[] = {"Hs", "Hrs", "He", "Hal"};
            int i = 0;
            while (i < hurstMethods.length) {
                hurst += rList.at(hurstMethods[i]).asDouble();
                i++;
            }
            return hurst / hurstMethods.length;
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
