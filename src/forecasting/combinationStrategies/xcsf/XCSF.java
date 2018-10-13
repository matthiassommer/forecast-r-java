package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.CombinationStrategy;
import forecasting.combinationStrategies.xcsf.classifier.Classifier;
import forecasting.combinationStrategies.xcsf.visualization.ConditionVis;
import forecasting.combinationStrategies.xcsf.visualization.PerformanceVis;
import forecasting.combinationStrategies.xcsf.visualization.PredictionVis;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Vector;


public class XCSF extends CombinationStrategy {
    /**
     * This filename is used to temporarily store the prediction approximation.
     *
     * @see PredictionVis
     */
    public final static String APPROXIMATION_FILE = "approximation.temp";

    final static String LINE_SEPERATOR = System.getProperty("line.separator");

    // xcsf ini & output files
    private final static String VISUALIZATION_FILE = "src\\forecasting\\combinationStrategies\\xcsf\\xcsf_visualization.ini";

    // used for all runs
    private static Vector<XCSFListener> listener = new Vector<>();

    public PerformanceEvaluator getPerformanceEvaluator() {
        return performanceEvaluator;
    }

    private PerformanceEvaluator performanceEvaluator;

    // re-created for every run
    private Population population;
    private MatchSet matchSet;
    private EvolutionaryComp evolutionaryComponent;

    private double[] situation;
    private double[] functionPrediction;

    public XCSF() {
        //load a saved population
        if (XCSFConstants.loadPopulation) {
            try {
                FileInputStream fileInputStream = new FileInputStream(XCSFConstants.outputFolder + "\\XCSF_population.ser");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                this.population = (Population) objectInputStream.readObject();
                objectInputStream.close();
            } catch (java.io.IOException | ClassNotFoundException IOException) {
                this.population = new Population();
            }
        } else {
            this.population = new Population();
        }

        this.performanceEvaluator = new PerformanceEvaluator(false);
        try {
            initialize();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        resetForNextExperiment();
    }

    /**
     * Convert and normalise the feature vector for XCSF.
     *
     * @return normalised feature vector
     */
    private double[] generateSituation() {
        if (forecasts == null) {
            return new double[0];
        }

        double[] situation = new double[forecasts.size()];
        for (int i = 0; i < forecasts.size(); i++) {
            situation[i] = forecasts.get(i);
        }
        return situation;
    }

    /**
     * Loads the <code>XCSFConstants</code>, the array to
     * evaluate, the default visualization and initializes the output directory,
     * if necessary.
     *
     * @return the functions to evaluate.
     * @throws Exception if the functions could not be loaded.
     */
    private void initialize() throws Exception {
        // load default visualization listeners
        try {
            if (XCSFConstants.plotPerformance) {
                loadDefaultListeners(2);
                System.out.println("done  " + listener.size() + " listener(s) registered");
                for (XCSFListener l : listener) {
                    System.out.println(" * " + l.getClass().getSimpleName());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load '" + VISUALIZATION_FILE + "'");
        }

        // init output directory
        if (XCSFConstants.doWriteOutput || XCSFConstants.doWritePopulation) {
            File outputDirectory = new File(XCSFConstants.outputFolder);
            System.out.print("initializing output directory...");
            if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
                System.err.println("Failed to create output directory: '" + XCSFConstants.outputFolder + "'");
                throw new IOException("Could not create directory: " + outputDirectory.getAbsolutePath());
            }
            XCSFConstants.outputFolder = outputDirectory.getAbsolutePath();
            System.out.println(" * " + outputDirectory.getName());
        }

        // initial seed for random number generator
        System.out.print("initializing random number generator...");
        if (XCSFConstants.doRandomize) {
            XCSFUtils.Random.setSeed(11 + (System.currentTimeMillis()) % 100000);
        } else {
            XCSFUtils.Random.setSeed(XCSFConstants.initialSeed);
        }
        System.out.println("done  " + (XCSFConstants.doRandomize ? "rnd-" : "")
                + "seed(" + XCSFConstants.initialSeed + ")");
    }

    /**
     * Reads the <code>XCSF.VISUALIZATION_FILE</code> and registers the
     * appropriate listeners, if the doVisualization flag is set.
     *
     * @param dimension the function input space dimension.
     * @throws IOException if the file could not be loaded.
     */
    private void loadDefaultListeners(int dimension) throws IOException {
        FileInputStream in = new FileInputStream(VISUALIZATION_FILE);
        Properties p = new Properties();
        p.load(in);

        boolean doVisualization = Boolean.parseBoolean(p.getProperty("doVisualization"));
        String gnuplotExe = p.getProperty("gnuplotExecutable");
        if (!gnuplotExe.toLowerCase().equals("default")) {
            XCSFUtils.GnuPlotConsole.gnuPlotExecutable = gnuplotExe;
        }

        if (doVisualization) {
            ConditionVis.slowMotion = Boolean.parseBoolean(p.getProperty("slowMotion"));
            ConditionVis.visualizationSteps = Integer.parseInt(p.getProperty("updateVisualizationSteps"));
            ConditionVis.visualizedConditionSize = Float.parseFloat(p.getProperty("relativeVisualizedConditionSize"));
            ConditionVis.visualizationTransparency = Float.parseFloat(p.getProperty("visualizationTransparencyDegree"));
            ConditionVis.visualizationDelay = Integer.parseInt(p.getProperty("visualizationDelay"));
            /*
             * Add the listeners. PerformanceVis for all dimensions, no
			 * preconditions. ConditionVis for 2D (always) and 3D, if Java3D is
			 * installed. PredictionVis for 2D, if GnuPlot is installed.
			 */
            listener.add(new PerformanceVis());
            if (dimension == 2) {
                listener.add(new ConditionVis());
                if (XCSFUtils.GnuPlotConsole.gnuPlotExecutable == null
                        || !XCSFUtils.GnuPlotConsole.gnuPlotExecutable.toLowerCase()
                        .equals(XCSFUtils.GnuPlotConsole.NOT_INSTALLED)) {
                    try {
                        listener.add(new PredictionVis());
                    } catch (IllegalStateException e) {
                        System.err.print("GnuPlot is not installed! ");
                    } catch (IOException e) {
                        System.err.print("Failed to execute GnuPlot! ");
                    }
                }
            } else if (dimension == 3) {
                // check for Java3D
                try {
                    XCSF.class.getClassLoader().loadClass("com.sun.j3d.utils.universe.SimpleUniverse");
                    listener.add(new ConditionVis());
                } catch (ClassNotFoundException e) {
                    System.err.print("Java3D is not installed! ");
                }
            }
        }
        in.close();
    }

    /**
     * Update the chosen classifiers in the match set.
     *
     * @param timeStep
     * @param value    payoff / rewardForXCSF
     */
    public void receiveReward(float timeStep, double value) {
        if (situation != null) {
            StateDescriptor state = new StateDescriptor(situation, new double[]{value});
            this.matchSet.setState(state);

            this.performanceEvaluator.evaluate(this.population, this.matchSet, (int) timeStep, new double[]{value}, functionPrediction);

            this.matchSet.updateClassifiers();
            this.evolutionaryComponent.evolve(this.population, this.matchSet, state, (int) timeStep);

            callListeners((int) timeStep, state);
        }
    }

    public double run() {
        this.situation = generateSituation();
        // 1) get next problem instance
        StateDescriptor state = new StateDescriptor(situation, new double[]{mean(situation)});

        // 2) match & cover, if necessary
        this.matchSet.match(state, this.population);
        this.matchSet.ensureStateCoverage(this.population, (int) getTime());

        // 3) evaluate performance
        functionPrediction = this.matchSet.getAveragePrediction();

        // reset rls prediction at next iteration?
        if (getTime() + 1 == XCSFConstants.resetRLSPredictionsAfterSteps) {
            for (Classifier cl : this.population) {
                cl.getRlsPrediction().resetGainMatrix();
            }
        }

        // start compaction at next iteration?
        if (getTime() + 1 == XCSFConstants.startCompaction) {
            this.evolutionaryComponent.setCondensation(true);
            if (XCSFConstants.compactionType % 2 == 1) {
                // type 1 & 3
                this.matchSet.setNumClosestMatching(true);
            }
            if (XCSFConstants.compactionType >= 2) {
                // type 2, 3
                this.population.applyGreedyCompaction();
            }
        }
        return functionPrediction[0];
    }

    /**
     * Informs all listeners about the changes.
     *
     * @param iteration the current iteration.
     * @param state     the current state.
     */
    private void callListeners(int iteration, StateDescriptor state) {
        if (listener.isEmpty()) {
            return;
        }
        // prevent side effects from missbehaving listeners
        Classifier[] populationCopy = this.population.shallowCopy();
        Classifier[] matchsetCopy = this.matchSet.shallowCopy();
        double[][] performance = this.performanceEvaluator
                .getCurrentExperimentPerformance();
        for (XCSFListener l : listener) {
            l.stateChanged(iteration, populationCopy, matchsetCopy, state,
                    performance);
        }
    }

    public Population getPopulation() {
        return this.population;
    }

    private double mean(double[] situation) {
        double sum = 0;
        for (double forecast : situation) {
            sum += forecast;
        }
        return sum / situation.length;
    }

    public void resetForNextExperiment() {
        this.population = new Population();
        this.matchSet = new MatchSet(XCSFConstants.doNumClosestMatch);
        this.evolutionaryComponent = new EvolutionaryComp();
        this.performanceEvaluator.nextExperiment();
    }

    public void finishAndwriteOut(int timeStep) {
        // store performance, prediction plot and population screenshot
        if (XCSFConstants.doWriteOutput && timeStep == XCSFConstants.maxLearningIterations) {
            this.performanceEvaluator.writeAvgPerformance(XCSFConstants.outputFolder + File.separator + "XCSF_performance");
        }

        //save the population
        if (XCSFConstants.savePopulation && timeStep == ((XCSFConstants.maxLearningIterations / XCSFConstants.averageExploitTrials) * XCSFConstants.averageExploitTrials) + 1) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(XCSFConstants.outputFolder + File.separator + "XCSF_population.ser");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(this.population);
                objectOutputStream.close();
            } catch (IOException IOException) {
                System.err.print("Population cannot be saved!");
            }
        }
    }
}

