package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.xcsf.classifier.Classifier;
import forecasting.combinationStrategies.xcsf.classifier.ConditionHyperellipsoid;
import forecasting.combinationStrategies.xcsf.classifier.RLSPrediction;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class encapsulates the evolutionary process of xcsf, i.e. selection,
 * crossover, mutation and insertion/subsumption.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
class EvolutionaryComp {
    private final double LOWER_BOUND = 0.0;
    private final double UPPER_BOUND = 1.0;
    private final double LOWER_ROTATION_BOUND = 0;
    private final double UPPER_ROTATION_BOUND = 2 * Math.PI;
    private boolean doCondensation;
    private int selectionSize;

    EvolutionaryComp() {
        this.doCondensation = false;
        this.selectionSize = 2;
    }

    /**
     * Starts the evolutionary process.
     *
     * @param population the current population.
     * @param matchSet   the matchset to evolve.
     * @param state      the state, this <code>matchSet</code> matched.
     * @param iteration  the current iteration.
     */
    void evolve(Population population, MatchSet matchSet,
                StateDescriptor state, int iteration) {
        // calculate some derived values
        double avgTimestampSum = 0.0;
        double fitnessSum = 0.0;
        int numerositySum = 0;
        for (Classifier cl : matchSet) {
            fitnessSum += cl.getFitness();
            avgTimestampSum += cl.getTimestamp() * cl.getNumerosity();
            numerositySum += cl.getNumerosity();
        }
        avgTimestampSum /= numerositySum;
        // Don't do a GA if the theta_GA threshold is not reached, yet
        if (iteration - avgTimestampSum < XCSFConstants.theta_GA) {
            return;
        }

        // update timestamp of the matchset
        for (Classifier cl : matchSet) {
            cl.setTimestamp(iteration);
        }

        // ---[ selection ]---
        Vector<Classifier> parents = selection(matchSet, fitnessSum);
        Vector<Classifier> offspring = new Vector<>(parents.size());
        for (Classifier cl : parents) {
            /*
			 * note that this is not really a clone: experience = 0, numerosity
			 * = 1, fitness = old.fitness / old.numerosity
			 */
            offspring.add(cl.clone());
        }

        if (!this.doCondensation) {
            crossoverAndMutation(offspring);
        }

        insertion(offspring, parents, matchSet, population, state);
    }

    /**
     * Sets the condensation flag.
     */
    void setCondensation(boolean value) {
        this.doCondensation = value;
    }

    /**
     * Selection routine.
     *
     * @param matchSet   the matchset to select from.
     * @param fitnessSum the sum of fitnesses of the <code>matchSet</code>.
     * @return the selected <code>Classifier</code> objects.
     */
    private Vector<Classifier> selection(MatchSet matchSet, double fitnessSum) {
        Vector<Classifier> selection = new Vector<>(this.selectionSize);
        if (XCSFConstants.selectionType == 0) {
            // roulette Wheel Selection
            for (int i = 0; i < this.selectionSize; i++) {
                Classifier cl = rouletteWheelSelection(matchSet, fitnessSum);
                selection.add(cl);
            }
        } else {
            // tournament selection
            for (int i = 0; i < this.selectionSize; i++) {
                Classifier cl = tournamenSelection(matchSet);
                selection.add(cl);
            }
        }
        return selection;
    }

    /**
     * Crossover and mutation routine.
     *
     * @param offspring the list of <code>Classifier</code> objects to cross and/or
     *                  mutate.
     */
    private void crossoverAndMutation(Vector<Classifier> offspring) {
        // select classifier pairs. start from end of vector
        int index = offspring.size() - 1;
        while (index > 0) { // two indices left: 0 & 1
            Classifier cl1 = offspring.get(index--);
            Classifier cl2 = offspring.get(index--);
            boolean changed1 = false, changed2 = false;
            // reduce fitness and predictionError
            cl1.scaleFitness(XCSFConstants.fitnessReduction);
            cl2.scaleFitness(XCSFConstants.fitnessReduction);
            cl1.scalePredictionError(XCSFConstants.predictionErrorReduction);
            cl2.scalePredictionError(XCSFConstants.predictionErrorReduction);
            if (XCSFConstants.pX <= 1.0) {
                // always mutation, crossover with probability pX
                if (XCSFUtils.Random.uniRand() < XCSFConstants.pX) {
                    changed1 = changed2 = uniformCrossover(cl1, cl2);
                }
                changed1 |= mutateCondition(cl1.getCondition());
                changed2 |= mutateCondition(cl2.getCondition());
            } else {
                // pX in (1,2] then either crossover or mutation but not both!
                if (XCSFUtils.Random.uniRand() < XCSFConstants.pX - 1.0) {
                    changed1 = changed2 = uniformCrossover(cl1, cl2);
                } else {
                    changed1 |= mutateCondition(cl1.getCondition());
                    changed2 |= mutateCondition(cl2.getCondition());
                }
            }
            if (changed1) {
                cl1.getCondition().recalculateTransformationMatrix();
            }
            if (changed2) {
                cl2.getCondition().recalculateTransformationMatrix();
            }
        }
        // mutate last classifier without crossover, if size is odd
        if (index == 0) {
            Classifier cl = offspring.firstElement();
            cl.scaleFitness(XCSFConstants.fitnessReduction);
            cl.scalePredictionError(XCSFConstants.predictionErrorReduction);
            if (mutateCondition(cl.getCondition())) {
                cl.getCondition().recalculateTransformationMatrix();
            }
        }
    }

    /**
     * Insertion routine.
     *
     * @param offspring  the offspring to insert.
     * @param parents    the parents of the given <code>offspring</code> (same order!).
     * @param matchSet   the matchSet.
     * @param population the population to insert into.
     * @param state      the machted state.
     */
    private void insertion(Vector<Classifier> offspring, Vector<Classifier> parents,
                           MatchSet matchSet, Population population, StateDescriptor state) {
        // don't exceed population.maxSize
        int numerositySum = 0;
        for (Classifier cl : population) {
            numerositySum += cl.getNumerosity();
        }
        int toDelete = numerositySum + offspring.size() - XCSFConstants.maxPopSize;
        if (toDelete > 0) {
            population.deleteWorstClassifiers(toDelete);
        }

        // insert new (matching) classifiers
        if (XCSFConstants.doGASubsumption) {
            // subsumption
            for (int i = 0; i < offspring.size(); i++) {
                Classifier cl = offspring.get(i);
                if (cl.doesMatch(state)) {
                    subsumeClassifier(cl, parents, population, matchSet);
                } else {
                    insertClassifier(cl, population, matchSet, false);
                }
            }
        } else {
            // no subsumption
            for (Classifier cl : offspring) {
                insertClassifier(cl, population, matchSet, cl.doesMatch(state));
            }
        }
    }

    /**
     * Inserts the <code>cl</code> into the given population.
     *
     * @param cl         the classifier to insert.
     * @param population the population.
     * @param matchSet   the matchset.
     * @param doesMatch  true, if the classifier matches.
     */
    private void insertClassifier(Classifier cl, Population population,
                                         MatchSet matchSet, boolean doesMatch) {
        if (doesMatch) {
            // 1. doesMatch && matchSet.getIdenticalCl != null
            Classifier identical = matchSet.findIdenticalCondition(cl.getCondition());
            if (identical != null) {
                identical.addNumerosity(1);
                return;
            }
        } else {
            // 2. !doesMatch && pop.getIdentical != null
            Classifier identical = population.findIdenticalCondition(cl.getCondition());
            if (identical != null) {
                identical.addNumerosity(1);
                return;
            }
        }
        // 3. no identical classifier found: simply add the new one
        matchSet.add(cl);
        population.add(cl);
    }

    /**
     * Tries to subsume the given offspring Classifier. <li>1) subsumption by a
     * parent <li>2) subsumption by any classifier of the matchset <li>3) no
     * subsumption possible, just add it
     *
     * @param offspring  the classifier to subsume.
     * @param parents    the parents at this iteration of the evolution (not
     *                   necessarily the parents of the offspring classifier).
     * @param population the population.
     * @param matchSet   the matchset.
     */
    private void subsumeClassifier(Classifier offspring,
                                          Vector<Classifier> parents, Population population, MatchSet matchSet) {
        // 1) check parents
        for (Classifier clP : parents) {
            if (clP.canSubsume() && clP.isMoreGeneral(offspring)) {
                clP.addNumerosity(1);
                return;
            }
        }
        // 2) check matchSet
        Vector<Classifier> choices = new Vector<>();
        for (Classifier cl : matchSet) {
            if (cl.canSubsume() && cl.isMoreGeneral(offspring)) {
                choices.addElement(cl);
            }
        }
        if (choices.size() > 0) {
            int choice = (int) (XCSFUtils.Random.uniRand() * choices.size());
            choices.elementAt(choice).addNumerosity(1);
            return;
        }
        // 3) If no subsumer was found, add the classifier to the population
        insertClassifier(offspring, population, matchSet, true);
    }

    /**
     * Applies uniform crossover to <code>cl1</code> and <code>cl2</code>.
     *
     * @param cl1 the first classifier.
     * @param cl2 the second classifier.
     * @return true, if the transformation matrix needs to be recalculated.
     */
    private boolean uniformCrossover(Classifier cl1, Classifier cl2) {
        // ---[ fitness & predictionError ]---
        double avgPredictionError = (cl1.getPredictionError() + cl2
                .getPredictionError()) / 2.0;
        cl1.setPredictionError(avgPredictionError);
        cl2.setPredictionError(avgPredictionError);
        double avgfitness = (cl1.getFitness() + cl2.getFitness()) / 2.0;
        cl1.setFitness(avgfitness);
        cl2.setFitness(avgfitness);

        // ---[ predictions ]---
        RLSPrediction rls1 = cl1.getRlsPrediction();
        RLSPrediction rls2 = cl2.getRlsPrediction();
        double[][] coef1 = rls1.getCoefficients();
        double[][] coef2 = rls2.getCoefficients();
        int predLength = coef1.length;
        for (int p = 0; p < predLength; p++) {
            for (int i = 0; i < coef1[p].length; i++) {
                double avg = coef1[p][i] + coef2[p][i];
                avg /= 2.0;
                coef1[p][i] = avg;
                coef2[p][i] = avg;
            }
        }
        rls1.setCoefficients(coef1);
        rls2.setCoefficients(coef2);

        // ---[ conditions ]---
        ConditionHyperellipsoid che1 = cl1.getCondition();
        ConditionHyperellipsoid che2 = cl2.getCondition();
        // center
        double[] center1 = che1.getCenter();
        double[] center2 = che2.getCenter();
        for (int i = 0; i < center1.length; i++) {
            if (XCSFUtils.Random.uniRand() < 0.5) {
                flip(i, center1, center2);
            }
        }
        // stretch
        boolean changed = false;
        double[] stretch1 = che1.getStretch();
        double[] stretch2 = che2.getStretch();
        for (int i = 0; i < stretch1.length; i++) {
            if (XCSFUtils.Random.uniRand() < 0.5) {
                changed = true;
                flip(i, stretch1, stretch2);
            }
        }
        // angles
        double[] angles1 = che1.getAngles();
        double[] angles2 = che2.getAngles();
        for (int i = 0; i < angles1.length; i++) {
            if (XCSFUtils.Random.uniRand() < 0.5) {
                changed = true;
                flip(i, angles1, angles2);
            }
        }
        return changed;
    }

    /**
     * Mutates the given <code>condition</code>.
     *
     * @param condition the condition to mutate.
     * @return true, if the transformation matrix needs to be recalculated.
     */
    private boolean mutateCondition(ConditionHyperellipsoid condition) {
        double[] center = condition.getCenter();
        double[] stretch = condition.getStretch();
        double[] angles = condition.getAngles();
        int n = center.length;

        boolean changed = false;
        // first, mutate the center: stay in old ellipsoid
        double[] mutation = new double[n];
        for (int i = 0; i < n; i++) {
            if (XCSFUtils.Random.uniRand() < XCSFConstants.pM) {
                changed = true;
                if (XCSFUtils.Random.uniRand() < 0.5) {
                    mutation[i] = XCSFUtils.Random.uniRand();
                } else {
                    mutation[i] = -XCSFUtils.Random.uniRand();
                }
            }
        }
        if (changed) {
            double sqDist = condition
                    .calculateRelativeSquaredDistance(mutation);
            if (sqDist > 1) {
                // scale with random factor
                double factor = XCSFUtils.Random.uniRand() / Math.sqrt(sqDist);
                for (int i = 0; i < n; i++) {
                    mutation[i] *= factor;
                }
            }
            for (int i = 0; i < n; i++) {
                center[i] += mutation[i];
                // stay in bounds
                if (center[i] < LOWER_BOUND) {
                    center[i] = LOWER_BOUND;
                } else if (center[i] > UPPER_BOUND) {
                    center[i] = UPPER_BOUND;
                }
            }
        }

        // second, mutate the stretch: uni-rnd between 50% and 200% of stretch
        changed = false;
        for (int i = 0; i < n; i++) {
            if (XCSFUtils.Random.uniRand() < XCSFConstants.pM) {
                changed = true;
                double rnd = 1.0;
                if (XCSFUtils.Random.uniRand() < 0.5) {
                    // enlarge (up to twice the stretch)
                    rnd += XCSFUtils.Random.uniRand();
                } else {
                    // shrink (down to half the stretch)
                    rnd -= 0.5 * XCSFUtils.Random.uniRand();
                }
                // rnd [0.5 : 2]
                stretch[i] *= rnd; // no bound check necessary
            }
        }

        // mutate angles
        for (int i = 0; i < angles.length; i++) {
            if (XCSFUtils.Random.uniRand() < XCSFConstants.pM) {
                changed = true;
                // max rotation: 45°
                double change = XCSFUtils.Random.uniRand() * Math.PI * 0.25;
                angles[i] += (XCSFUtils.Random.uniRand() < 0.5) ? change : -1.0
                        * change;
                // stay in (human readable) bounds, though not necessary
                if (angles[i] < LOWER_ROTATION_BOUND) {
                    angles[i] += 2 * Math.PI; // +360°
                } else if (angles[i] > UPPER_ROTATION_BOUND) {
                    angles[i] -= 2 * Math.PI; // -360°
                }
            }
        }
        return changed;
    }

    /**
     * Selects one classifier using roulette wheel selection according to the
     * fitnesses of the classifiers.
     *
     * @param matchSet the matchset.
     * @param fitSum   the sum of fitnesses in the matchset.
     * @return the selected classifier.
     */
    private Classifier rouletteWheelSelection(MatchSet matchSet,
                                              double fitSum) {
        double choiceP = XCSFUtils.Random.uniRand() * fitSum;
        Iterator<Classifier> it = matchSet.iterator();
        Classifier cl = it.next();
        double sum = cl.getFitness();
        while (choiceP > sum) {
            cl = it.next();
            sum += cl.getFitness();
        }
        return cl;
    }

    /**
     * Selects a classifier using set-size proportionate tournament selection
     *
     * @param matchSet the matchset.
     * @return the selected classifier.
     */
    private Classifier tournamenSelection(MatchSet matchSet) {
        Classifier winner = null;
        double bestFitness = 0;
        while (winner == null) {
            for (Classifier cl : matchSet) {
                for (int j = 0; j < cl.getNumerosity(); j++) {
                    if (XCSFUtils.Random.uniRand() < XCSFConstants.selectionType) {
                        double microFitness = cl.getFitness() / cl.getNumerosity();
                        if (winner == null || microFitness > bestFitness) {
                            winner = cl;
                            bestFitness = microFitness;
                            // move to next classifier in set.
                            break;
                        }
                    }
                }
            }
        }
        return winner;
    }

    /**
     * Flip the array position.
     */
    private void flip(int pos, double[] arr1, double[] arr2) {
        double tmp = arr1[pos];
        arr1[pos] = arr2[pos];
        arr2[pos] = tmp;
    }
}
