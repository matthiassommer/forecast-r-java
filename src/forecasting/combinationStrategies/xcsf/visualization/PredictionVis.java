package forecasting.combinationStrategies.xcsf.visualization;

import forecasting.combinationStrategies.xcsf.*;
import forecasting.combinationStrategies.xcsf.XCSFUtils.GnuPlotConsole;
import forecasting.combinationStrategies.xcsf.classifier.Classifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * Implements a time-consuming listener to visualize the current prediction of
 * xcsf. GnuPlot is used to plot the 3D graph.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class PredictionVis implements XCSFListener {
	// 21 tics, interval [0:1] - if computed via for-loop: inaccurate!
	private final static double[] TICS = { 0, .05, .1, .15, .2, .25, .3, .35,
			.4, .45, .5, .55, .6, .65, .7, .75, .8, .85, .9, .95, 1 };

	// initial gnuplot commands
	private final static String[] GNUPLOT_CMD = { "set grid", //
			"set xrange[0:1]", //
			"set yrange[0:1]", //
			"set xlabel 'x'", //
			"set ylabel 'y'", //
			"set zlabel 'f'", //
			"set style data lines", //
			"set contour", //
			"set surface", //
			"set hidden3d", //
			"set dgrid3d " + TICS.length + "," + TICS.length, //
	};

	private GnuPlotConsole console;

	/**
	 * Default constructor.
	 * 
	 * @throws IllegalStateException
	 *             if gnuplot is not installed.
	 * @throws IOException
	 *             if the system fails to execute gnuplot.
	 */
	public PredictionVis() throws IllegalStateException, IOException {
		this.console = new GnuPlotConsole();
		for (String cmd : GNUPLOT_CMD) {
			this.console.writeln(cmd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xcsf.listener.XCSFListener#stateChanged(int, java.util.Vector,
	 * java.util.Vector, xcsf.StateDescriptor)
	 */
	public void stateChanged(int iteration, Classifier[] population,
			Classifier[] matchSet, StateDescriptor state, double[][] performance) {
		if (iteration % ConditionVis.visualizationSteps != 0 || state.getConditionInput().length != 2) {
			return;
		}
		// create 2D samples and plot
		write2dIsosamples(population, new File(XCSF.APPROXIMATION_FILE));
		this.console.writeln("set title 'iteration " + iteration + "'");
		this.console.writeln("splot '" + XCSF.APPROXIMATION_FILE
				+ "' title 'prediction'");
	}

	/**
	 * Creates a eps plot using the given File.
	 * 
	 * @param population
	 *            the population.
	 * @param filename
	 *            the filename.
	 * @param title
	 *            the title of the plot.
	 */
	public static void epsPlot(Classifier[] population, String filename,
			String title) {
		if (population[0].getCondition().getCenter().length != 2) {
			// not supported
			return;
		}
		try {
			GnuPlotConsole c = new GnuPlotConsole();
			for (String cmd : GNUPLOT_CMD) {
				c.writeln(cmd);
			}
			write2dIsosamples(population, new File(XCSF.APPROXIMATION_FILE));
			c.writeln("set term postscript eps enhanced");
			c.writeln("set out '" + filename + ".eps'");
			c.writeln("set title '" + title + "'");
			c.writeln("splot '" + XCSF.APPROXIMATION_FILE
					+ "' title 'prediction'");
			c.writeln("save '" + filename + ".plt'");
			c.writeln("exit");
			XCSFUtils.fileCopy(new File(XCSF.APPROXIMATION_FILE), new File(
					filename + ".dat"));
		} catch (IllegalStateException e) {
			// gnuplot not installed - ignore
		} catch (IOException e) {
			System.err.println("Failed to execute GnuPlot.");
		}
	}

	/**
	 * Calculates isosamples for use with gnuplot.
	 * 
	 * @param population
	 *            the population.
	 * @return isosamples 3D Array[xTics][yTics][funcX, funcY, funcVal]
	 */
	private static double[][][] get2dIsosamples(Classifier[] population) {
		int l = TICS.length;
		double[][][] samples = new double[l][l][3];
		for (int x = 0; x < l; x++) {
			for (int y = 0; y < l; y++) {
				double[] param = { TICS[x], TICS[y] };
				double value = getApproximation(population, param)[0];
				samples[x][y][0] = TICS[x];
				samples[x][y][1] = TICS[y];
				samples[x][y][2] = value;
			}
		}
		return samples;
	}

	/**
	 * Writes the isosamples to the given File f.
	 * 
	 * @param population
	 *            the population
	 * @param f
	 *            the file to write.
	 */
	private static void write2dIsosamples(Classifier[] population, File f) {
		double[][][] samples = get2dIsosamples(population);
		try {
			FileWriter writer = new FileWriter(f);
			for (double[][] element : samples) {
				for (int y = 0; y < samples[0].length; y++) {
					double funcX = element[y][0];
					double funcY = element[y][1];
					double funcVal = element[y][2];
					writer
							.write(funcX + "\t" + funcY + "\t" + funcVal
									+ "\r\n");
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the approximated function output for the given function input.
	 * 
	 * @param population
	 *            the classifier population.
	 * @param param
	 *            the function input.
	 * @return the function output.
	 */
	private static double[] getApproximation(Classifier[] population,
			double[] param) {
		StateDescriptor state = new StateDescriptor(param, null);
		Vector<Classifier> matchSet = createMatchSet(population, state);
		return getAveragePrediction(matchSet, state);
	}

	/**
	 * Creates the matchSet for a given population and StateDescriptor. If no
	 * classifier matches the regular way, num-closest-matching is used.
	 * 
	 * @param population
	 *            the population.
	 * @param state
	 *            the state to be matched.
	 * @return a list of <code>Classifier</code> objects.
	 */
	private static Vector<Classifier> createMatchSet(Classifier[] population,
			StateDescriptor state) {
		Vector<Classifier> matchSet = new Vector<Classifier>();
		// regular matching
		for (int i = 0; i < population.length; i++) {
			Classifier cl = population[i];
			if (cl.doesMatch(state)) {
				matchSet.add(cl);
			}
		}
		// ensure matching (match with closest classifiers)
		if (matchSet.isEmpty()) {
			// ---[ numClostest matching ]---
			int size = population.length;
			Classifier[] clSetHelp = new Classifier[size];
			int[] nums = new int[size];
			double[] votes = new double[size];
			for (int i = 0; i < size; i++) {
				Classifier cl = population[i];
				votes[i] = cl.getActivity(state);
				nums[i] = cl.getNumerosity();
				clSetHelp[i] = cl;
			}
			int firstNum = XCSFUtils.putNumFirstObjectsFirst(votes, nums,
					clSetHelp, clSetHelp.length, XCSFConstants.numClosestMatch);
			// now add the classifiers to this match set.
			for (int i = 0; i < firstNum; i++) {
				matchSet.add(clSetHelp[i]);
			}
		}
		return matchSet;
	}

	/**
	 * Calculates the average prediction for the given <code>matchSet</code>.
	 * 
	 * @param matchSet
	 *            the matchset.
	 * @param state
	 *            the matched state.
	 * @return the prediction of the <code>matchSet</code>.
	 */
	private static double[] getAveragePrediction(Vector<Classifier> matchSet,
			StateDescriptor state) {
		int n = matchSet.firstElement().predict(state).length;
		double fitnessSum = 0;
		double[] avgPrediction = new double[n];
		for (Classifier cl : matchSet) {
			double[] prediction = cl.predict(state);
			double fitness = cl.getFitness();
			fitnessSum += fitness;
			for (int i = 0; i < n; i++) {
				avgPrediction[i] += prediction[i] * fitness;
			}
		}
		for (int i = 0; i < n; i++) {
			avgPrediction[i] /= fitnessSum;
		}
		return avgPrediction;
	}
}
