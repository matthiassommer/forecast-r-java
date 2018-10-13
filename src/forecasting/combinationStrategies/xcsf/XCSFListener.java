package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.xcsf.classifier.Classifier;

/**
 * This interface can be implemented to observe <code>Population</code> and
 * <code>MatchSet</code> changes in <code>XCSF</code>.
 * 
 * @author Patrick O. Stalph
 */
public interface XCSFListener {

	/**
	 * This method indicates a population and/or matchset change. This method is
	 * called once per iteration of <code>XCSF</code>.
	 * 
	 * @param iteration
	 *            the current iteration of xcsf.
	 * @param population
	 *            the current population.
	 * @param matchSet
	 *            the current matchset.
	 * @param state
	 *            the current state.
	 * @param performance
	 *            the performance for the current experiment.
	 */
	void stateChanged(int iteration, Classifier[] population,
					  Classifier[] matchSet, StateDescriptor state, double[][] performance);
}
