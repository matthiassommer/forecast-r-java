package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.xcsf.XCSFUtils.GnuPlotConsole;
import forecasting.combinationStrategies.xcsf.classifier.Classifier;
import tools.FileUtilities;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class encapsulates all performance related methods and offers methods to
 * write performance files.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class PerformanceEvaluator {
    /**
     * Description strings for the performance output.
     */
    public final static String[] HEADER = {"Iteration", "Error",
            "Macro Classifier", "Micro Classifier", "Matchset Macro Cl.",
            "MatchSet Micro Cl.", "Pred.Error", "Fitness",
            "Generality", "Experience", "SetSizeEstimate",
            "Timestamp", "Individual Error"};
    private boolean verbose;
    private double[][] predictionError;
    private int[] matchSetSize;
    private int[] matchSetNumerositySum;
    private int experiment;
    private double[][][] avgPerformance;
    private static DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));

    /**
     * Default constructor.
     *
     * @param verbose determines if the performance should be printed to
     *                <code>System.out</code>.
     */
    PerformanceEvaluator(boolean verbose) {
        this.verbose = verbose;
        this.predictionError = new double[XCSFConstants.averageExploitTrials][];
        this.matchSetSize = new int[XCSFConstants.averageExploitTrials];
        this.matchSetNumerositySum = new int[XCSFConstants.averageExploitTrials];
        this.experiment = -1;
        this.avgPerformance = new double[XCSFConstants.numberOfExperiments][XCSFConstants.maxLearningIterations
                / XCSFConstants.averageExploitTrials][];
    }

    /**
     * Returns the performance of the current experiment. Note that this method
     * returns the complete array, even if not filled with values!
     * <p>
     * You will find <code>null</code> entries in the last dimension, if the
     * performance is not evaluated, yet.
     *
     * @return performance array of the current experiment.
     */
    double[][] getCurrentExperimentPerformance() {
        return this.avgPerformance[this.experiment];
    }

    /**
     * Indicates, that the next experiment has begun.
     */
    void nextExperiment() {
        this.experiment++;
    }

    /**
     * Evaluates the performance.
     *
     * @param population             the current population.
     * @param matchSet               the current matchset.
     * @param iteration              the current iteration.
     * @param noiselessFunctionValue the current noiseless functionvalue, if available.
     * @param functionPrediction     the xcsf prediction.
     */
    void evaluate(Population population, MatchSet matchSet, int iteration,
                  double[] noiselessFunctionValue, double[] functionPrediction) {
        // averaging for error, matchset size/numerositysum
        double error[] = new double[functionPrediction.length];
        for (int i = 0; i < functionPrediction.length; i++) {
            error[i] = Math.abs(noiselessFunctionValue[i] - functionPrediction[i]);
        }
        int index = iteration % XCSFConstants.averageExploitTrials;
        this.predictionError[index] = error;
        this.matchSetSize[index] = matchSet.size();
        this.matchSetNumerositySum[index] = 0;
        for (Classifier cl : matchSet) {
            this.matchSetNumerositySum[index] += cl.getNumerosity();
        }
        if (index == 0) {
            if (iteration > 0) {
                double[] performance = evaluatePerformance(population);
                this.avgPerformance[experiment][iteration / XCSFConstants.averageExploitTrials - 1] = performance;
                if (verbose) {
                    System.out.println(performanceToString(iteration, performance));
                }
            } else if (verbose) {
                System.out.println(getHeader(false));
            }
        }
    }

    /**
     * Writes the average performance (averaged over all experiments) to the
     * given <code>file</code>.
     *
     * @param file the file.
     */
    void writeAvgPerformance(String file) {
        int experiments = this.avgPerformance.length;
        int rows = this.avgPerformance[0].length;
        int length = this.avgPerformance[0][0].length;
        double[][] mean = new double[rows][];
        double[][] variance = new double[rows][];

        for (int row = 0; row < rows; row++) {
            mean[row] = new double[length];
            variance[row] = new double[length];

            for (int i = 0; i < length; i++) {
                // calculate mean & variance for performance[exp][row]
                for (int exp = 0; exp < experiments; exp++) {
                    mean[row][i] += avgPerformance[exp][row][i] / experiments;
                }
                for (int exp = 0; exp < experiments; exp++) {
                    double dif = avgPerformance[exp][row][i] - mean[row][i];
                    variance[row][i] += (dif * dif) / (experiments - 1.0);
                }
                variance[row][i] = Math.sqrt(variance[row][i]);
            }
        }

        // write to file
        try {
            FileUtilities.createNewFile(file + experiment + ".txt");
            FileWriter fw = new FileWriter(file + experiment + ".txt");
            fw.write("# data pairs: mean and variance" + XCSF.LINE_SEPERATOR);
            fw.write(getHeader(true) + System.getProperty("line.separator"));
            for (int row = 0; row < rows; row++) {
                String str = meanAndVarianceToString((row + 1) * XCSFConstants.averageExploitTrials, mean[row], variance[row]);
                fw.write(str + XCSF.LINE_SEPERATOR);
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create plot
        try {
            GnuPlotConsole c = new GnuPlotConsole();
            c.writeln("set xlabel 'time steps of time series'");
            c.writeln("set ylabel 'forecast error, # macro classifiers'");
            c.writeln("set logscale y");
            c.writeln("set mytics 10");
            c.writeln("set style data errorlines");
            c.writeln("set term postscript eps enhanced");
            c.writeln("set out '" + file + experiment + ".eps'");
            c.writeln("plot '" + file + experiment + ".txt"
                    + "' using 1:2:2:($2+$3) title 'forecast error' pt 1,"
                    + " '' using 1:4:4:($4+$5) title '# classifiers' pt 2");
            c.writeln("save '" + file + experiment + ".plt'");
            c.writeln("exit");
        } catch (IllegalStateException e) {
            // ignore - gnuplot not installed
        } catch (IOException e) {
            System.err.println("Failed to execute GnuPlot.");
        }

    }

    /**
     * Evaluates the performance array for the given <code>Population</code>.
     *
     * @param population the population to evaluate.
     * @return a double array containing several performance values.
     */
    private double[] evaluatePerformance(Population population) {
        int outputDim = this.predictionError[0].length;

        // ---[ avg prediction error, matchSetSize & -numerositySum ]---
        double[] avgPredError = new double[outputDim];
        double avgMatchSetSize = 0;
        double avgMatchSetNumerositySum = 0;

        for (int expl = 0; expl < this.predictionError.length; expl++) {
            for (int dim = 0; dim < outputDim; dim++) {
                if (predictionError[expl] != null) {
                    avgPredError[dim] += this.predictionError[expl][dim];
                }
            }
            avgMatchSetSize += this.matchSetSize[expl];
            avgMatchSetNumerositySum += this.matchSetNumerositySum[expl];
        }

        for (int dim = 0; dim < outputDim; dim++) {
            avgPredError[dim] /= XCSFConstants.averageExploitTrials;
        }

        avgMatchSetSize /= XCSFConstants.averageExploitTrials;
        avgMatchSetNumerositySum /= XCSFConstants.averageExploitTrials;

        // ---[ calculate values ]---
        double[] performance = new double[11 + outputDim];
        // 1) avg prediction error (sum over all dim)
        for (int dim = 0; dim < outputDim; dim++) {
            performance[0] += avgPredError[dim];
        }
        // 2) population size
        performance[1] = population.size();
        // 3) population numerositySum
        int popNumerositySum = 0;
        for (Classifier cl : population) {
            popNumerositySum += cl.getNumerosity();
        }
        performance[2] = popNumerositySum;
        // 4) avg matchSetSize
        performance[3] = avgMatchSetSize;
        // 5) avg matchSetNumerositySum
        performance[4] = avgMatchSetNumerositySum;
        // 6-11) population performance
        performance[5] = 0;
        performance[6] = 0;
        performance[7] = 0;
        performance[8] = 0;
        performance[9] = 0;

        for (Classifier cl : population) {
            // 6) predictionerror
            performance[5] += cl.getPredictionError() * cl.getNumerosity();
            // 7) fitness
            performance[6] += cl.getFitness();
            // 8) generality
            performance[7] += cl.getGenerality() * cl.getNumerosity();
            // 9) experience
            performance[8] += cl.getExperience() * cl.getNumerosity();
            // 10) setSizeEstimate
            performance[9] += cl.getSetSizeEstimate() * cl.getNumerosity();
            // 11) timeStamp
            performance[10] += cl.getTimestamp() * cl.getNumerosity();
        }

        performance[5] /= popNumerositySum;
        performance[6] /= popNumerositySum;
        performance[7] /= popNumerositySum;
        performance[8] /= popNumerositySum;
        performance[9] /= popNumerositySum;
        performance[10] /= popNumerositySum;

        // 12-?) avg prediction error per dimension
        System.arraycopy(avgPredError, 0, performance, 11, outputDim);
        return performance;
    }

    /**
     * Returns the header for performance files.
     *
     * @param deviation indicates, if the deviation is included.
     * @return the header for performance files.
     */
    private static String getHeader(boolean deviation) {
        String header = "#" + HEADER[0];
        for (int i = 1; i < HEADER.length; i++) {
            header += "\t" + HEADER[i];
            if (deviation) {
                header += "[mean]\t[dev.]";
            }
        }
        return header;
    }

    /**
     * Creates a nice <code>String</code> from the given array.
     *
     * @param iteration   the current iteration.
     * @param performance the performance.
     * @return a nice formatted string.
     */
    private static String performanceToString(int iteration, double[] performance) {
        String str = iteration + "";
        for (double p : performance) {
            str += "\t" + p;
        }
        return str;
    }

    /**
     * Creates a nice <code>String</code> to store performance mean and
     * variance.
     *
     * @param iteration the current iteration.
     * @param mean      the mean of the performance.
     * @param variance  the sample variance of the performance.
     * @return a nice formatted string.
     */
    private static String meanAndVarianceToString(int iteration, double[] mean,
                                                  double[] variance) {
        String str = iteration + "";
        for (int i = 0; i < mean.length; i++) {
            str += "\t" + df.format(mean[i]) + "\t" + df.format(variance[i]);
        }
        return str;
    }
}
