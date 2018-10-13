package forecasting.plotting;

import evaluation.EvaluationParameters;
import forecasting.RServeConnection;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * Plots a decomposed time series with season, trend, and random components.
 * <p>
 * https://www.otexts.org/fpp/6/5
 * <p>
 */
public abstract class DecomposeTimeSeries {
    /**
     * “Seasonal and TrendStationary decomposition using Loess” (STL) decomposition.
     * http://cs.wellesley.edu/~cs315/Papers/stl%20statistical%20model.pdf
     *
     * @param timeSeries to analyze
     * @param pdfPath    path where the pdf should be stored
     */
    public static void run(double[] timeSeries, String pdfPath) {
        String path = EvaluationParameters.WORKING_DIRECTORY + "\\Rscripts\\DecomposeTimeSeries.R";
        path = path.replace("\\", "/");
        String newPdfPath = pdfPath + "_decompose.pdf";

        try {
            RConnection rConnection = RServeConnection.getConnection();

            rConnection.assign("path", path);
            rConnection.assign("mypath", newPdfPath);
            rConnection.assign("timeSeries", timeSeries);

            rConnection.voidEval("source(path)");
            rConnection.voidEval("decomposeTimeseries(timeSeries, mypath)");
        } catch (REngineException e) {
            System.err.println(e.getMessage());
        }
    }
}
