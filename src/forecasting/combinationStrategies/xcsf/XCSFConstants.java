package forecasting.combinationStrategies.xcsf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Makes available all kinds of XCSF constants (static).
 * 
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class XCSFConstants {
	// booleans for loading and saving the population
	static boolean savePopulation = false;
	static boolean loadPopulation = false;

	// if true, the performance will be plotted during the run
	static boolean plotPerformance = false;

	// General Experimental Setup Parameters
	/***************************************************************************
	 * The file name that is used to create the output folder.
	 */
	public static String outputFolder = "logs";

	/**
	 * If the settings and experimental results should be written out after all
	 * runs.
	 */
	static boolean doWriteOutput = true;

	/**
	 * If the final popualtion should be written out after each run.
	 */
	static boolean doWritePopulation = false;

	/**
	 * Specifies the number of investigated experiments.
	 */
	public static int numberOfExperiments = 30;

	/**
	 * The number of run instances that should be averaged in the performance
	 * evaluation.
	 */
	public static int averageExploitTrials = 75;

	// XCSF Parameters
	/***************************************************************************
	 * The number of learning iterations in one experiment.
	 */
	public static int maxLearningIterations = 0;

	/**
	 * Specifies the maximal number of micro-classifiers in the population. In
	 * the multiplexer problem this value is often set to 400, 800, 1600 in the
	 * 6, 11, 20 multiplexer resp..
	 */
	public static int maxPopSize = 200;

	/**
	 * The accuracy factor (decrease) in inaccurate classifiers. Default: 1
	 */
	public static double alpha = 1;

	/**
	 * The learning rate for updating fitness, prediction error, and action set
	 * size estimate in XCS's classifiers. Default: .1
	 */
	public static double beta = 0.1;

	/**
	 * The fraction of the mean fitness of the population below which the
	 * fitness of a classifier may be considered in its vote for deletion.
	 * Default: .1
	 */
	public static double delta = 0.1;

	/**
	 * Specifies the minimum size of a condition part Default: 0
	 */
	public static double minConditionStretch = 0;

	/**
	 * Specifies the variation in size of randomly created conditions, ofter
	 * referred to as r0. Default: 1
	 */
	public static double coverConditionRange = 0.5;

	// Matching and Compaction
	/***************************************************************************
	 * The number of explore problems after which compaction starts.
	 */
	static int startCompaction = (int) (maxLearningIterations * 1.1);

	/**
	 * The compaction type. 0 = condensation, normal matching 1 = condensation,
	 * closest classifier matching 2 = greedy compaction plus condensation,
	 * normal matching 3 = greedy compaction plus condensation and closest
	 * classifier matching Default: 1
	 */
	static int compactionType = 0;

	/**
	 * Specifies if the num closest classifiers should be considered in the
	 * match set. Otherwise, normal threshold matching applies Default: false
	 */
	static boolean doNumClosestMatch = false;

	/**
	 * The number of closest classifiers that will be included in the match set
	 * if doNumCloestMatch is set to true; Default: 20
	 */
	public static int numClosestMatch = 20;

	// Recursive Least Squares Prediction
	/***************************************************************************
	 * The initialization vector of the diagonal in the inverse covariance
	 * matrix for RLS-based predictions. Default: 1000
	 */
	public static double rlsInitScaleFactor = 1000;

	/**
	 * Forget rate for RLS Danger: small values may lead to instabilities!
	 * Default: 1
	 */
	public static double lambdaRLS = 1;

	/**
	 * If set, then after the specified number of iterations, all gain matrizes
	 * are reset according to the initial scale factor. Default: starts with
	 * compaction.
	 * 
	 * @see XCSFConstants#rlsInitScaleFactor
	 */
	static int resetRLSPredictionsAfterSteps = startCompaction;

	/**
	 * The offset value that is added in real-valued prediction for the
	 * prediction generation (added to the input values).
	 */
	public static double predictionOffsetValue = 1;

	// Classifier error and fitness parameters
	/***************************************************************************
	 * Specifies the exponent in the power function for the fitness evaluation.
	 * Default: 5
	 */
	public static double nu = 5;

	/**
	 * The error threshold under which the accuracy of a classifier is set to
	 * one. Default: .01 (1% of value range)
	 */
	public static double epsilon_0 = 0.01;

	/**
	 * The factor (reduction) of the prediction error when generating an
	 * offspring classifier. Default: 1
	 */
	static double predictionErrorReduction = 1.0;

	/**
	 * The factor (reduction) of the fitness when generating an offspring
	 * classifier. Default: .1
	 */
	static double fitnessReduction = 0.1;

	/**
	 * The initial prediction error value when generating a new classifier (e.g
	 * in covering). Default: 0
	 */
	public static double predictionErrorIni = 0.0;

	/**
	 * The initial fitness value when generating a new classifier (e.g in
	 * covering). Default: .01
	 */
	public static double fitnessIni = 0.01;

	// Evolution Parameters
	/***************************************************************************
	 * The threshold for the GA application. Default: 50.
	 */
	static double theta_GA = 50;

	/**
	 * Choice of selection type 0 = proportionate selection; ]0,1] = tournament
	 * selection (set-size proportional) Default: .4 (tournament selection)
	 */
	static double selectionType = 0.4;

	/**
	 * The probability of mutating one allele, often termed mu, (and the action)
	 * in an offspring classifier. Default: .05
	 */
	static double pM = 0.05;

	/**
	 * The probability to apply crossover to the offspring, often termed chi.
	 * Default: 1
	 */
	static double pX = 1;

	/**
	 * Specified the threshold over which the fitness of a classifier may be
	 * considered in its deletion probability. Default: 20
	 */
	public static int theta_del = 20;

	/**
	 * The experience of a classifier required to be a subsumer. Default: 20
	 */
	public static int theta_sub = 20;

	/**
	 * Specifies if GA subsumption should be executed. Default: true
	 */
	static boolean doGASubsumption = true;

	// Random Number Generator
	/***************************************************************************
	 * The initialization of the pseudo random generator. Must be at least one
	 * and smaller than 2147483647. Will be used only if "doRandomize" is set to
	 * false.
	 */
	static long initialSeed = 101;

	/**
	 * Specifies if the seed should be randomized (based on the current
	 * milliseconds of the computer time)
	 */
	static boolean doRandomize = false;

	/***************************************************************************
	 * Trys to set the properties from the specified <code>filename</code>.
	 * 
	 * @param filename
	 *            The name (relative to start directory or absolute) to the
	 *            parameter file.
	 * @throws IOException
	 *             if the file could not be loaded.
	 */
	public static void load(String filename) throws IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(filename));

		String val = getProperty(prop, "outputFolder");
		if (val != null)
			outputFolder = val;
		val = getProperty(prop, "maxLearningIterations");
		if (val != null)
			maxLearningIterations = Integer.parseInt(val);
		val = getProperty(prop, "numberOfExperiments");
		if (val != null)
			numberOfExperiments = Integer.parseInt(val);
		val = getProperty(prop, "averageExploitTrials");
		if (val != null)
			averageExploitTrials = Integer.parseInt(val);

		val = getProperty(prop, "doWritePopulation");
		if (val != null)
			doWritePopulation = Boolean.valueOf(val);
		val = getProperty(prop, "doWriteOutput");
		if (val != null)
			doWriteOutput = Boolean.valueOf(val);

		val = getProperty(prop, "maxPopSize");
		if (val != null)
			maxPopSize = Integer.parseInt(val);
		val = getProperty(prop, "alpha");
		if (val != null)
			alpha = Double.parseDouble(val);
		val = getProperty(prop, "beta");
		if (val != null)
			beta = Double.parseDouble(val);
		val = getProperty(prop, "delta");
		if (val != null)
			delta = Double.parseDouble(val);

		val = getProperty(prop, "startCompaction");
		if (val != null)
			startCompaction = Integer.parseInt(val);
		val = getProperty(prop, "compactionType");
		if (val != null)
			compactionType = Integer.parseInt(val);
		val = getProperty(prop, "doNumClosestMatch");
		if (val != null)
			doNumClosestMatch = Boolean.valueOf(val);
		val = getProperty(prop, "numClosestMatch");
		if (val != null)
			numClosestMatch = Integer.parseInt(val);

		val = getProperty(prop, "coverConditionRange");
		if (val != null)
			coverConditionRange = Double.parseDouble(val);
		val = getProperty(prop, "minConditionStretch");
		if (val != null)
			minConditionStretch = Double.parseDouble(val);

		val = getProperty(prop, "rlsInitScaleFactor");
		if (val != null)
			rlsInitScaleFactor = Double.parseDouble(val);
		val = getProperty(prop, "lambdaRLS");
		if (val != null)
			lambdaRLS = Double.parseDouble(val);
		val = getProperty(prop, "resetRLSPredictionsAfterSteps");
		if (val != null) {
			if (val.toLowerCase().equals("startcompaction")) {
				resetRLSPredictionsAfterSteps = startCompaction;
			} else {
				resetRLSPredictionsAfterSteps = Integer.parseInt(val);
			}
		}
		val = getProperty(prop, "predictionOffsetValue");
		if (val != null)
			predictionOffsetValue = Double.parseDouble(val);

		val = getProperty(prop, "nu");
		if (val != null)
			nu = Double.parseDouble(val);
		val = getProperty(prop, "epsilon_0");
		if (val != null)
			epsilon_0 = Double.parseDouble(val);
		val = getProperty(prop, "predictionErrorReduction");
		if (val != null)
			predictionErrorReduction = Double.parseDouble(val);
		val = getProperty(prop, "fitnessReduction");
		if (val != null)
			fitnessReduction = Double.parseDouble(val);
		val = getProperty(prop, "predictionErrorIni");
		if (val != null)
			predictionErrorIni = Double.parseDouble(val);
		val = getProperty(prop, "fitnessIni");
		if (val != null)
			fitnessIni = Double.parseDouble(val);

		val = getProperty(prop, "theta_GA");
		if (val != null)
			theta_GA = Double.parseDouble(val);
		val = getProperty(prop, "selectionType");
		if (val != null)
			selectionType = Double.parseDouble(val);
		val = getProperty(prop, "pM");
		if (val != null)
			pM = Double.parseDouble(val);
		val = getProperty(prop, "pX");
		if (val != null)
			pX = Double.parseDouble(val);
		val = getProperty(prop, "theta_del");
		if (val != null)
			theta_del = Integer.parseInt(val);
		val = getProperty(prop, "theta_sub");
		if (val != null)
			theta_sub = Integer.parseInt(val);
		val = getProperty(prop, "doGASubsumption");
		if (val != null)
			doGASubsumption = Boolean.valueOf(val);

		val = getProperty(prop, "initialSeed");
		if (val != null)
			initialSeed = Long.parseLong(val);
		val = getProperty(prop, "doRandomize");
		if (val != null)
			doRandomize = Boolean.valueOf(val);
	}

	/**
	 * Checks the value.
	 * 
	 * @param p
	 *            the properties.
	 * @param property
	 *            the property name.
	 * @return true, if value != null
	 */
	private static String getProperty(Properties p, String property) {
		String value = p.getProperty(property);
		if (value == null) {
			System.err.println("Failed to load property '" + property + "'");
		}
		return value;
	}
}
