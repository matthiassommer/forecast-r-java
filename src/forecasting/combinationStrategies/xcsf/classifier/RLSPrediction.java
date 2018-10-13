package forecasting.combinationStrategies.xcsf.classifier;

import forecasting.combinationStrategies.xcsf.XCSFConstants;
import forecasting.combinationStrategies.xcsf.XCSFUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Linear prediction using recursive least squares.
 * 
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class RLSPrediction implements Cloneable, Serializable {
	// arrays for temporary timeSeries to avoid mem alloc.
	private static double[] tmpExtendedPredInput;
	private static double[] tmpGainArray;
	private static double[][] tmpMatrix1;
	private static double[][] tmpMatrix2;

	private int inputLength; // dimension of function input + 1 (offset)
	private int predictionLength; // dimension of function output
	private double[][] coefficients; // coefficients of the linear fit
	private double[][] gainMatrix; // some kind of magic matrix
	private double[] prediction; // array, to avoid mem-alloc

	/**
	 * Default constructor with given inputLength and the functionValue (used as
	 * initial prediction).
	 * 
	 * @param inputLength
	 *            the length of prediction input (without offset)
	 * @param initialPrediction
	 *            the initial prediction value.
	 */
	RLSPrediction(int inputLength, double[] initialPrediction) {
		this.inputLength = inputLength + 1;
		this.predictionLength = initialPrediction.length;
		this.coefficients = new double[this.predictionLength][this.inputLength];
		this.gainMatrix = new double[this.inputLength][this.inputLength];
		this.prediction = new double[this.predictionLength];
		// init coefficients
		for (int p = 0; p < this.predictionLength; p++) {
			// first coefficient is the offset
			if (XCSFConstants.predictionOffsetValue > 0) {
				this.coefficients[p][0] = initialPrediction[p];
			} else {
				this.coefficients[p][0] = 0;
			}
			for (int i = 1; i < this.inputLength; i++) {
				this.coefficients[p][i] = 0;
			}
		}
		// init gainMatrix
		initializeGainMatrix();

		// create temporary arrays
		if (tmpGainArray == null) {
			tmpGainArray = new double[this.inputLength];
			tmpExtendedPredInput = new double[this.inputLength];
			tmpMatrix1 = new double[this.inputLength][this.inputLength];
			tmpMatrix2 = new double[this.inputLength][this.inputLength];
		}
	}

	/**
	 * Private empty constructor for efficient cloning.
	 */
	private RLSPrediction() {
		// empty
	}

	/**
	 * Generates the prediction using the given <code>predInput</code>.
	 * 
	 * @param predInput
	 *            the prediction input.
	 * @return the prediction of the given <code>predInput</code>.
	 */
	double[] predict(double[] predInput) {
		for (int p = 0; p < this.predictionLength; p++) {
			// first coefficient is offset
			this.prediction[p] = this.coefficients[p][0] * XCSFConstants.predictionOffsetValue;
			// multiply other coefficients with the prediction input
			for (int i = 1; i < this.inputLength; i++) {
				this.prediction[p] += this.coefficients[p][i] * predInput[i - 1];
			}
		}
		return this.prediction;
	}

	/**
	 * Updates this prediction using the provided Vector for the generation of
	 * the prediction.
	 * 
	 * @param predInput
	 *            the prediction input.
	 * @param functionValue
	 *            the actual function value for the given <code>predInput</code>
	 */
	void updatePrediction(double[] predInput, double[] functionValue) {
		initializeTempVariables(this.inputLength);
		extendPredictionInput(predInput);
		// tmpArray = gainMatrix * extendedPredInput
		XCSFUtils.multiply(this.gainMatrix, tmpExtendedPredInput, tmpGainArray, this.inputLength);

		double divisor = XCSFConstants.lambdaRLS;
		for (int i = 0; i < this.inputLength; i++) {
			divisor += tmpExtendedPredInput[i] * tmpGainArray[i];
		}
		for (int i = 0; i < this.inputLength; i++) {
			tmpGainArray[i] /= divisor;
		}

		// update coefficients using the error (functionValue - prediction)
		// Note, that this.prediction is up to date at the moment!
		for (int p = 0; p < this.predictionLength; p++) {
			double error = functionValue[p] - this.prediction[p];
			for (int i = 0; i < this.inputLength; i++) {
				this.coefficients[p][i] += error * tmpGainArray[i];
			}
		}

		// update gainMatrix:
		// gainMatrix = (I - rank1(helpV1, diffStateExt)) * gainMatrix
		for (int i = 0; i < this.inputLength; i++) {
			for (int j = 0; j < this.inputLength; j++) {
				double tmp = tmpGainArray[i] * tmpExtendedPredInput[j];
				if (i == j) {
					tmpMatrix1[i][j] = 1.0 - tmp;
				} else {
					tmpMatrix1[i][j] = -tmp;
				}
			}
		}
		XCSFUtils.multiply(tmpMatrix1, this.gainMatrix, tmpMatrix2,
				this.inputLength);
		for (int row = 0; row < this.inputLength; row++) {
			for (int col = 0; col < this.inputLength; col++) {
				this.gainMatrix[row][col] = tmpMatrix2[row][col]
						/ XCSFConstants.lambdaRLS;
			}
		}
	}

	/**
	 * Resets the gain matrix.
	 */
	public void resetGainMatrix() {
		for (int i = 0; i < this.inputLength; i++) {
			this.gainMatrix[i][i] += XCSFConstants.rlsInitScaleFactor;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public RLSPrediction clone() {
		RLSPrediction clone = new RLSPrediction();
		// cloned fields
		clone.inputLength = this.inputLength;
		clone.predictionLength = this.predictionLength;
		clone.coefficients = new double[this.predictionLength][this.inputLength];
		for (int p = 0; p < this.predictionLength; p++) {
			for (int i = 0; i < this.inputLength; i++) {
				clone.coefficients[p][i] = this.coefficients[p][i];
			}
		}
		// modified fields
		clone.prediction = new double[this.predictionLength];
		clone.gainMatrix = new double[this.inputLength][this.inputLength];
		clone.initializeGainMatrix();
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		//in=" + (this.inputLength - 1) + ",out="+ this.predictionLength + "
		String s = "prediction{coef=";
		for (double[] coeff : this.coefficients) {
			s += Arrays.toString(coeff);
		}
		s += ", gain=";
		for (double[] row : this.gainMatrix) {
			s += Arrays.toString(row);
		}
		return s + "}";
	}

	/**
	 * Returns the coefficients of this linear prediction.
	 */
	public double[][] getCoefficients() {
		return this.coefficients;
	}

	/**
	 * Sets the coefficients of this linear prediction.
	 */
	public void setCoefficients(double[][] coefficients) {
		this.coefficients = coefficients;
	}

	/**
	 * Initializes the gainMatrix for this RLS-prediction.
	 */
	private void initializeGainMatrix() {
		for (int row = 0; row < this.gainMatrix.length; row++) {
			for (int col = 0; col < this.gainMatrix.length; col++) {
				this.gainMatrix[row][col] = (row != col) ? 0
						: XCSFConstants.rlsInitScaleFactor;
			}
		}
	}

	/**
	 * Extends the given vector with a leading 1 (see
	 * {@link XCSFConstants#predictionOffsetValue}) and stores the extended
	 * array in the static temporary array <code>extendedPredInput</code>.
	 * 
	 * @param predInput
	 *            the prediction input.
	 */
	private static void extendPredictionInput(double[] predInput) {
		tmpExtendedPredInput[0] = XCSFConstants.predictionOffsetValue;
		System.arraycopy(predInput, 0, tmpExtendedPredInput, 1, predInput.length);
	}

	private void initializeTempVariables(int inputLength){
		// create temporary arrays
		if (tmpGainArray == null) {
			tmpGainArray = new double[this.inputLength];
			tmpExtendedPredInput = new double[this.inputLength];
			tmpMatrix1 = new double[this.inputLength][this.inputLength];
			tmpMatrix2 = new double[this.inputLength][this.inputLength];
		}
	}
}
