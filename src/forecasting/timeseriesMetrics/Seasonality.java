package forecasting.timeseriesMetrics;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * Detect seasonality in a time series.
 * http://robjhyndman.com/hyndsight/detecting-seasonality/
 * <p>
 * p<0.05 --> the additional seasonal component is significant
 */
public class Seasonality extends TimeseriesMetric {
    @Override
    public double run(double[] input) {
        try {
            rConnection.assign("input", input);
            rConnection.voidEval("fit1 <- ets(input)");
            rConnection.voidEval("fit2 <- ets(input,model=\"ANN\")");
            rConnection.voidEval("deviance <- 2*c(logLik(fit1) - logLik(fit2))");
            rConnection.voidEval("df <- attributes(logLik(fit1))$df - attributes(logLik(fit2))$df");
            return rConnection.eval("1-pchisq(deviance,df)").asDouble();
        } catch (REngineException | REXPMismatchException e) {
            System.err.println("Seasonality: " + e.getMessage());
            return Double.NaN;
        }
    }
}
