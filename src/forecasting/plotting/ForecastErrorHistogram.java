package forecasting.plotting;

import evaluation.EvaluationParameters;
import forecasting.RServeConnection;
import org.apache.commons.lang3.ArrayUtils;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import tools.MathFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * To check whether the forecast errors are normally distributed with mean zero,
 * we plot a histogram of the forecast errors, with an overlaid normal curve that has
 * mean zero and the same standard deviation as the distribution of forecast errors.
 * <p>
 * R throws an exception if the vector of forecast errors is too short!
 * <p>
 * https://a-little-book-of-r-for-time-series.readthedocs.org/en/latest/src/timeseries.html
 * <p>
 * Created by oc6admin on 26.02.2016.
 */
public abstract class ForecastErrorHistogram {
    public static double run(double[] timeSeries, double[] forecasts, String pdfPath) {
        String path = EvaluationParameters.WORKING_DIRECTORY + "\\Rscripts\\ForecastErrorHistogram.R";
        path = path.replace("\\", "/");
        String newPdfPath = pdfPath + "_ForecastErrorHistogram.pdf";

        try {
            RConnection rConnection = RServeConnection.getConnection();

            rConnection.assign("path", path);
            rConnection.assign("mypath", newPdfPath);
            double[] errors = getForecastErrors(timeSeries, forecasts);

            rConnection.assign("forecastErrors", errors);
            rConnection.voidEval("source(path)");
            rConnection.voidEval("plotForecastErrors(forecastErrors, mypath)");

            return MathFunctions.mean(errors);
        } catch (REngineException e) {
            System.err.println(e.getMessage());
        }
        return Double.NaN;
    }

    private static double[] getForecastErrors(double[] timeSeries, double[] forecasts) {
        List<Double> errors = new ArrayList<>();
        for (int i = 0; i < forecasts.length; i++) {
            double error = timeSeries[i] - forecasts[i];
            if (!Double.isNaN(error)) {
                errors.add(error);
            }
        }

        Double[] ds = errors.toArray(new Double[errors.size()]);
        return ArrayUtils.toPrimitive(ds);
    }
}
