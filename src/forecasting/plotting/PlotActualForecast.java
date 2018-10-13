package forecasting.plotting;

import evaluation.EvaluationParameters;
import forecasting.RServeConnection;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * Plots the forecasts and the actual time series as a simple curve.
 * <p>
 * Forecasts and time series have to be of equal length.
 */
public abstract class PlotActualForecast {
    public static void run(double[] timeSeries, double[] forecasts, String pdfPath) {
        String path = EvaluationParameters.WORKING_DIRECTORY + "\\Rscripts\\ActualForecast.R";
        path = path.replace("\\", "/");
        String newPdfPath = pdfPath + "_ActualForecast.pdf";

        try {
            RConnection rConnection = RServeConnection.getConnection();

            rConnection.assign("path", path);
            rConnection.assign("mypath", newPdfPath);
            rConnection.assign("timeseries", timeSeries);
            rConnection.assign("forecasts", forecasts);

            rConnection.voidEval("source(path)");
            rConnection.voidEval("plotActualForecast(timeseries, forecasts, mypath)");
        } catch (REngineException e) {
            System.err.println(e.getMessage());
        }
    }
}
