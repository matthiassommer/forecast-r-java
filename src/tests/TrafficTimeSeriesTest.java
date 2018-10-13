package tests;

import forecasting.DefaultForecastParameters;
import forecasting.ForecastModule;
import forecasting.combinationStrategies.Strategies;
import forecasting.dataSources.TimeSeriesLoader;
import forecasting.forecastMethods.ForecastMethod;
import forecasting.normalisation.MinMax;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import tools.FileUtilities;
import tools.MathFunctions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * evaluation.Evaluation class to evaluate several forecast combination strategies.
 * <p>
 * Available metrics: MAE, MASE, Percentage better, U-measure, Conf. interval.
 * <p>
 * Data: stock market and traffic flow (30 files each)
 *
 * @author Michael Vogt.
 */
public class TrafficTimeSeriesTest {
    private final String EVALUATION_FOLDER = "..\\Evaluation\\TimeSeriesTest\\";
    private static PrintStream psBoxplot;
    /**
     * Lists to save error measures for various time steps.
     */
    private final ArrayList<ArrayList<Double>> maeErrors = new ArrayList<>();
    private final ArrayList<ArrayList<Double>> maseErrors = new ArrayList<>();
    private final ArrayList<Double> uMeasure = new ArrayList<>();
    private final ArrayList<ArrayList<Double>> allForecastErrors = new ArrayList<>();

    private int meanOfMaximumValues = 0;

    @BeforeClass
    public static void setup() {
        DefaultForecastParameters.DEFAULT_FORECAST_METHODS = Arrays.asList(
                ForecastMethod.CUBICSPLINE,
                ForecastMethod.CROSTON,
                ForecastMethod.ES
        );
    }

    @Test
    public final void simpleAverage() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.SIMPLE_AVERAGE;
    }

    @Test
    public final void optimalWeights() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.OPTIMALWEIGHTS;
    }

    @Test
    public final void annWeighting() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.ANN_WEIGHTING;
    }

    @Test
    public final void median() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.MEDIAN;
    }

    @Test
    public final void forecastError() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.FORECAST_ERROR;
    }

    @Test
    public final void outperformance() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.OUTPERFORMANCE;
    }

    @Test
    public final void xcsf() {
        DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY = Strategies.XCSF;
    }

    @After
    public void run() {
        meanOfMaximumValues = getMaxMean(TimeSeriesLoader.TRAFFIC_FILES);

        initBoxPlotPrinter();

        // one run for every file / time series
        for (String filename : TimeSeriesLoader.TRAFFIC_FILES) {
            ArrayList<Double> mae = new ArrayList<>();
            ArrayList<Double> mase = new ArrayList<>();
            ArrayList<Double> forecastErrors = new ArrayList<>();

            SummaryStatistics last500Errors = new SummaryStatistics();
            SummaryStatistics theilU1 = new SummaryStatistics();
            SummaryStatistics theilU2 = new SummaryStatistics();

            double[] timeSeries = TimeSeriesLoader.loadTimeSeriesFromCSV("..\\" + TimeSeriesLoader.TRAFFIC_FOLDER + filename + ".csv");

            MinMax normalisation = new MinMax(timeSeries);
            double maximumDataPoint = normalisation.getActualHigh();

            ForecastModule forecastModule = new ForecastModule();

            for (int i = 0; i < 300; i++) {
                forecastModule.addValue(i, timeSeries[i]);
                forecastModule.addValueToEvaluators(i, timeSeries[i]);
                double forecast = forecastModule.combinedForecast(i, 1, i + 1);

                //compute absolute error, U-Statistic nominator and denominator
                if ((!Double.isNaN(forecast)) && i + 1 < timeSeries.length) {
                    double absoluteError = (Math.abs((forecast) - timeSeries[i + 1])) / maximumDataPoint;

                    if (timeSeries[i] != 0) {
                        theilU1.addValue(Math.pow((forecast - timeSeries[i + 1]) / timeSeries[i], 2));
                        theilU2.addValue(Math.pow((timeSeries[i + 1] - timeSeries[i]) / timeSeries[i], 2));
                    }

                    forecastErrors.add(absoluteError);
                    last500Errors.addValue(absoluteError);
                }

                if (Double.isNaN(forecast)) {
                    forecastErrors.add(Double.NaN);
                }

                if (last500Errors.getN() == 500) {
                    mae.add(last500Errors.getMean());
                    // mase.add(forecastModule.getForecastError());
                    last500Errors.clear();
                }
            }

            allForecastErrors.add((ArrayList<Double>) forecastErrors.clone());
            maeErrors.add((ArrayList<Double>) mae.clone());
            maseErrors.add((ArrayList<Double>) mase.clone());
            uMeasure.add(Math.sqrt(theilU1.getSum() / theilU2.getSum()));

            printAverageMAEandMASE(mae, mase);
        }

        exportPercentageBetter();
        printMAE();
        printMASE();
        printUMeasure();

        psBoxplot.close();
    }

    private void initBoxPlotPrinter() {
        try {
            String path = EVALUATION_FOLDER + DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY + "_boxPlotData.txt";
            FileUtilities.createNewFile(path);
            psBoxplot = new PrintStream(new FileOutputStream(path), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        psBoxplot.println("MAE \t MASE");
    }

    private void printAverageMAEandMASE(ArrayList<Double> mae, ArrayList<Double> mase) {
        //build average MAE from all 500 time steps
        float maeSum = 0;
        for (double f : mae) {
            maeSum += f * meanOfMaximumValues;
        }
        maeSum /= mae.size();

        //build average MASE from all 500 time steps
        float maseSum = 0;
        for (double f : mase) {
            maseSum += f;
        }
        maseSum /= mase.size();

        //print MAE and MASE
        psBoxplot.println(maeSum + "\t" + maseSum);
    }

    /**
     * export MASE, Std. Dev., Conf. Int., every 500 time steps.
     */
    private void printMASE() {
        try {
            String path = EVALUATION_FOLDER + DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY + "_MASE.txt";
            FileUtilities.createNewFile(path);
            PrintStream psMASE = new PrintStream(new FileOutputStream(path), true);
            psMASE.println("MASE \t std. dev. \t [95]confInt. low \t [95]confInt. high \t [99]confInt. low \t [99]confInt. high");


            SummaryStatistics maseErrorData = new SummaryStatistics();
            int length2 = getMinLength(maseErrors);
            for (int i = 0; i < length2; i++) {
                for (List<Double> ts : maseErrors) {
                    maseErrorData.addValue(ts.get(i));
                }
                double mean = maseErrorData.getMean();
                double stdDev = maseErrorData.getStandardDeviation();
                double[] confInterval95 = MathFunctions.getConfidenceInterval(0.05, (int) maseErrorData.getN(), stdDev, mean);
                double[] confInterval99 = MathFunctions.getConfidenceInterval(0.01, (int) maseErrorData.getN(), stdDev, mean);
                psMASE.println(mean + "\t" + stdDev + "\t" + confInterval95[0] + "\t" + confInterval95[1] + "\t" + confInterval99[0] + "\t" + confInterval99[1]);
                maseErrorData.clear();
            }
            psMASE.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * export MAE, Std. Dev., Conf. Int., every 500 time steps.
     */
    private void printMAE() {
        try {
            String path = EVALUATION_FOLDER + DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY + "_performance500Steps.txt";
            FileUtilities.createNewFile(path);
            PrintStream psMAE = new PrintStream(new FileOutputStream(path), true);
            psMAE.println("iteration \t error \t std. dev. \t [95]confInt. low \t [95]confInt. high \t [99]confInt. low \t [99]confInt. high");

            SummaryStatistics maeErrorData = new SummaryStatistics();
            int length = getMinLength(maeErrors);
            for (int i = 0; i < length; i++) {
                for (List<Double> ts : maeErrors) {
                    maeErrorData.addValue(ts.get(i) * meanOfMaximumValues);
                }

                double mean = maeErrorData.getMean();
                double stdDev = maeErrorData.getStandardDeviation();
                double[] confInterval95 = MathFunctions.getConfidenceInterval(0.05, (int) maeErrorData.getN(), stdDev, mean);
                double[] confInterval99 = MathFunctions.getConfidenceInterval(0.01, (int) maeErrorData.getN(), stdDev, mean);
                psMAE.println((i * 500 + 500) + "\t" + mean + "\t" + stdDev + "\t" + confInterval95[0] + "\t" + confInterval95[1] + "\t" + confInterval99[0] + "\t" + confInterval99[1]);
                maeErrorData.clear();
            }
            psMAE.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printUMeasure() {
        try {
            String path = EVALUATION_FOLDER + DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY + "_UStat.txt";
            FileUtilities.createNewFile(path);
            PrintStream psUMEasure = new PrintStream(new FileOutputStream(path), true);
            psUMEasure.println("U-Statistic");

            uMeasure.forEach(psUMEasure::println);
            psUMEasure.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * export forecast errors for every time step (for percentage better comparison).
     */
    private void exportPercentageBetter() {
        try {
            String path = EVALUATION_FOLDER + DefaultForecastParameters.FORECAST_COMBINATION_STRATEGY + ".txt";
            FileUtilities.createNewFile(path);
            PrintStream psPB = new PrintStream(new FileOutputStream(path), true);

            SummaryStatistics forecastData = new SummaryStatistics();
            int length1 = getMinLength(allForecastErrors);
            for (int i = 0; i < length1; i++) {
                for (List<Double> errors : allForecastErrors) {
                    forecastData.addValue(errors.get(i) * meanOfMaximumValues);
                }
                double mean = forecastData.getMean();
                psPB.println(mean);
                forecastData.clear();
            }
            psPB.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Ignore
    @Test
    public void percentageBetter() {
        PercentageBetter.run("logs\\");
    }

    private int getMinLength(List<ArrayList<Double>> allForecasts) {
        int min = Integer.MAX_VALUE;
        for (List<Double> allForecast : allForecasts) {
            if (allForecast.size() < min) {
                min = allForecast.size();
            }
        }
        return min;
    }

    /**
     * Get the average of all maximum values.
     *
     * @param files
     * @return average maximum value
     */
    private int getMaxMean(String[] files) {
        int max = 0;
        for (String file : files) {
            double[] ts = TimeSeriesLoader.loadTimeSeriesFromCSV("..\\" + TimeSeriesLoader.TRAFFIC_FOLDER + file + ".csv");
            MinMax normalisation = new MinMax(ts);
            max += normalisation.getActualHigh();
        }
        max /= files.length;
        return max;
    }
}
