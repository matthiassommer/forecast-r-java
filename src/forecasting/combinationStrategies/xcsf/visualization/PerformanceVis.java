package forecasting.combinationStrategies.xcsf.visualization;

import forecasting.combinationStrategies.xcsf.PerformanceEvaluator;
import forecasting.combinationStrategies.xcsf.StateDescriptor;
import forecasting.combinationStrategies.xcsf.XCSFConstants;
import forecasting.combinationStrategies.xcsf.XCSFListener;
import forecasting.combinationStrategies.xcsf.classifier.Classifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Visualization of the performance.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class PerformanceVis extends JFrame implements XCSFListener,
		ActionListener {

	final static Color[] COLORS = { Color.RED, Color.GREEN, Color.BLUE,
			Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK, Color.YELLOW };

	private PerformancePanel panel;
	private JCheckBox[] checkBoxes;
	private JLabel[] checkBoxLabels;
	private GraphIcon[] checkBoxGraphs;

	/**
	 * Default constructor.
	 */
	public PerformanceVis() {
		super("Performance");
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// create contentpane
		super.setContentPane(this.createContentPane());
		// layout
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int xSize = (int) (0.5 * screen.getWidth());
		int ySize = (int) (0.45 * screen.getHeight());
		this.setPreferredSize(new Dimension(xSize, ySize));
		this.setLocation(0, 0);
		super.pack();
		super.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		int labelIndex = 0;
		int colorIndex = 0;
		boolean[] paint = new boolean[this.checkBoxes.length];
		for (int i = 0; i < PerformanceEvaluator.HEADER.length - 1; i++) {
			if (this.checkBoxes[i].isSelected()) {
				paint[i] = true;
				this.checkBoxLabels[labelIndex]
						.setText(PerformanceEvaluator.HEADER[i + 1]);
				this.checkBoxGraphs[labelIndex]
						.setGraphColor(COLORS[colorIndex]);

				labelIndex++;
				colorIndex = (colorIndex + 1) % COLORS.length;
			} else {
				paint[i] = false;
			}
		}
		for (; labelIndex < this.checkBoxLabels.length; labelIndex++) {
			this.checkBoxLabels[labelIndex].setText("");
			this.checkBoxGraphs[labelIndex].unsetGraphColor();
		}
		this.panel.setPaint(paint);
		this.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xcsf.listener.XCSFListener#stateChanged(int, java.util.Vector,
	 * java.util.Vector, xcsf.StateDescriptor)
	 */
	public void stateChanged(int iteration, Classifier[] population,
			Classifier[] matchSet, StateDescriptor state, double[][] performance) {
		if (iteration % XCSFConstants.averageExploitTrials != 0
				|| iteration == 0) {
			return;
		}
		double[][] p = new double[iteration
				/ XCSFConstants.averageExploitTrials][];
		for (int i = 0; i < performance.length; i++) {
			if (performance[i] != null) {
				p[i] = new double[performance[i].length + 1];
				p[i][0] = i * XCSFConstants.averageExploitTrials;
				for (int k = 0; k < performance[i].length; k++) {
					p[i][k + 1] = performance[i][k];
				}
			} else {
				break;
			}
		}
		this.panel.setValues(p);
		this.repaint();
	}

	/**
	 * Creates the contentpane.
	 * 
	 * @return the contentpane
	 */
	private JPanel createContentPane() {
		JPanel cp = new JPanel(new BorderLayout());

		// center: performance graphs
		this.panel = new PerformancePanel();
		cp.add(this.panel, BorderLayout.CENTER);

		// west: graph selection buttons
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		JPanel keyPanel = new JPanel();
		keyPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = 1;
		this.checkBoxes = new JCheckBox[PerformanceEvaluator.HEADER.length - 1];
		this.checkBoxLabels = new JLabel[PerformanceEvaluator.HEADER.length - 1];
		this.checkBoxGraphs = new GraphIcon[PerformanceEvaluator.HEADER.length - 1];
		for (int i = 0; i < this.checkBoxes.length; i++) {
			// activate avgError, numPopMacroCl, numMatchMacroCl, avgGenerality
			this.checkBoxes[i] = new JCheckBox(
					PerformanceEvaluator.HEADER[i + 1], i < 2 || i == 3
							|| i == 7);
			this.checkBoxes[i].addActionListener(this);
			gbc.gridy++;
			gbc.ipadx = 30;
			buttonPanel.add(this.checkBoxes[i], gbc);
			gbc.ipadx = 5;
			// add name labels
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.insets = new Insets(0, 5, 0, 5);
			this.checkBoxLabels[i] = new JLabel(
					PerformanceEvaluator.HEADER[i + 1]);
			this.checkBoxLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
			keyPanel.add(this.checkBoxLabels[i], gbc);
			// add graph labels
			gbc.gridx++;
			gbc.weightx = 0;
			gbc.insets = new Insets(0, 0, 0, 0);
			this.checkBoxGraphs[i] = new GraphIcon();
			keyPanel.add(this.checkBoxGraphs[i], gbc);
			gbc.gridx = 0;
		}

		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel east = new JPanel(new BorderLayout());
		east.setBackground(Color.WHITE);
		keyPanel.setBackground(Color.WHITE);
		east.add(keyPanel, BorderLayout.NORTH);
		east.add(buttonPanel, BorderLayout.SOUTH);
		cp.add(east, BorderLayout.EAST);
		cp.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		// initial setting of paint
		this.actionPerformed(null);
		return cp;
	}

	/**
	 * PerformancePanel extends JPanel to draw the graphs.
	 * 
	 * @author Patrick Stalph
	 */
	private static class PerformancePanel extends JPanel {
		private final static int[] X_OFFSET = { 50, 20 };
		private final static int[] Y_OFFSET = { 30, 10 };
		private final static int X_TICS = 10;

		private double xMinValue, xMaxValue, yMinValue, yMaxValue;
		private int xMin, xMax, yMin, yMax;

		private boolean[] paint;
		private double[][] values;

		/**
		 * Sets the values to be painted.
		 * 
		 * @param values
		 */
		void setValues(double[][] values) {
			int length1 = values.length;
			int length2 = paint.length;
			this.values = new double[length1][];
			for (int i = 0; i < length1; i++) {
				this.values[i] = new double[length2 + 1];
				this.values[i][0] = values[i][0];
				for (int k = 1; k <= length2; k++) {
					// set logscale y :)
					this.values[i][k] = Math.log10(values[i][k]);
				}
			}
			this.initMinMaxValues();
		}

		/**
		 * Sets the boolean array indicating which indices are drawn.
		 * 
		 * @param paint
		 *            the array to indicate which indices are drawn.
		 */
		void setPaint(boolean[] paint) {
			this.paint = paint;
			if (this.values != null) {
				this.initMinMaxValues();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			// panel min/max
			this.xMax = this.getWidth() - X_OFFSET[1];
			this.yMax = this.getHeight() - Y_OFFSET[0];
			this.xMin = X_OFFSET[0];
			this.yMin = Y_OFFSET[1];
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			if (this.values == null) {
				return;
			}
			g.setColor(Color.BLACK);
			this.drawAxis(g);
			int colorIndex = 0;
			for (int i = 0; i < this.paint.length; i++) {
				if (this.paint[i]) {
					g.setColor(COLORS[colorIndex]);
					colorIndex = (colorIndex + 1) % COLORS.length;
					this.drawValues(i + 1, g);
					// last item is individual error. Draw all remaining values
					if (i == PerformanceEvaluator.HEADER.length - 2) {
						for (int k = i + 2; k < this.values[0].length; k++) {
							this.drawValues(k, g);
						}
					}
				}
			}
		}

		/**
		 * Draws the x- and y-axis.
		 * 
		 * @param g
		 *            the graphics.
		 */
		private void drawAxis(Graphics g) {
			int xLow = translateX(xMinValue);
			int yLow = translateY(yMinValue);
			int xHigh = translateX(xMaxValue);
			int yHigh = translateY(yMaxValue);
			// ---[ x-axis ]---
			for (int i = 0; i <= X_TICS; i++) {
				double percent = (double) i / (double) X_TICS;
				int x = xLow + (int) (percent * (xHigh - xLow));
				int it = (int) (percent * XCSFConstants.maxLearningIterations);
				String text = (it <= 1000) ? it + "" : it / 1000 + "k";
				g.drawString(text, x - text.length() * 4, yLow + Y_OFFSET[0]
						- 5);
				g.drawLine(x, yLow, x, yLow - 5);
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(x, yLow - 5, x, yHigh);
				g.setColor(Color.BLACK);
			}
			g.drawLine(xLow, yLow, xHigh, yLow);

			// ---[ y-axis (log_10 scaled) ]---
			int exp = (int) yMaxValue + 1;
			int y;
			while (exp > yMinValue) {
				exp--;
				for (int i = 9; i > 0; i--) {
					y = translateY(exp + Math.log10(i));
					if (y >= yMin && y <= yMax) {
						if (i == 1) {
							// main tic (10 pixel) + text
							y = translateY(exp);
							String text = "";
							if (exp >= 0 && exp <= 3) {
								text = "" + (int) Math.pow(10, exp);
							} else if (exp > -4 && exp < 0) {
								text = "" + Math.pow(10, exp);
								// grr... remove trailing zeros
								while (text.endsWith("0")) {
									text = text.substring(0, text.length() - 1);
								}
							} else {
								text = "10^" + exp;
							}
							g.drawString(text, xLow - X_OFFSET[0] + 5, y + 5);
							if (y != yLow) {
								g.drawLine(xLow, y, xLow + 10, y);
								g.setColor(Color.LIGHT_GRAY);
								g.drawLine(xLow + 10, y, xHigh, y);
								g.setColor(Color.BLACK);
							}
						} else {
							// small tic
							g.drawLine(xLow, y, xLow + 5, y);
						}
					}
				}
			}
			g.drawLine(xLow, yLow, xLow, yHigh);
		}

		/**
		 * Draws the values at the given index.
		 * 
		 * @param index
		 *            the index.
		 * @param g
		 *            the graphics.
		 */
		private void drawValues(int index, Graphics g) {
			int x1 = this.translateX(this.values[0][0]);
			int y1 = this.translateY(this.values[0][index]);
			for (int i = 1; i < this.values.length; i++) {
				int x2 = this.translateX(this.values[i][0]);
				int y2 = this.translateY(this.values[i][index]);
				// draw line
				if (x1 < x2) {
					g.drawLine(x1, y1, x2, y2);
				}
				// shift
				x1 = x2;
				y1 = y2;
			}
		}

		/**
		 * Initializes min- and max-values for x/y translation methods.
		 * 
		 * @see #translateX(double)
		 * @see #translateY(double)
		 */
		private void initMinMaxValues() {
			// function min/max
			this.xMinValue = 0;
			this.xMaxValue = XCSFConstants.maxLearningIterations;
			this.yMinValue = Double.MAX_VALUE;
			this.yMaxValue = Double.MIN_VALUE;
			for (double[] row : this.values) {
				for (int i = 0; i < this.paint.length; i++) {
					if (paint[i]) {
						if (row[i + 1] > yMaxValue) {
							yMaxValue = row[i + 1];
						}
						if (row[i + 1] < yMinValue) {
							yMinValue = row[i + 1];
						}
					}
				}
			}
			this.yMaxValue = (int) this.yMaxValue + 1;
			this.yMinValue = (int) this.yMinValue - 1;
		}

		/**
		 * Translates the given x value to panel x-coordinate.
		 * 
		 * @param x
		 *            the x coordinate.
		 * @return the transformed x coordinate for painting.
		 */
		private int translateX(double x) {
			// translate to [0:1]
			double tmp = (x - xMinValue) / (xMaxValue - xMinValue);
			// translate to [offset:panel.width-offset]
			return (int) (xMin + (xMax - xMin) * tmp);
		}

		/**
		 * Translates the given y value to panel y-coordinate.
		 * 
		 * @param y
		 *            the y coordinate.
		 * @return the transformed y coordinate for painting.
		 */
		private int translateY(double y) {
			// translate to [0:1] and invert
			double tmp = 1.0 - (y - yMinValue) / (yMaxValue - yMinValue);
			// translate to [offset:panel.height-offset]
			return (int) (yMin + (yMax - yMin) * (tmp));
		}
	}

	/**
	 * GraphIcon extends JLabel. Simply draws a horizontal line using the
	 * forground color.
	 * 
	 * @author Patrick Stalph
	 */
	private static class GraphIcon extends JLabel {
		private Color graphColor;

		GraphIcon() {
			super();
			super.setPreferredSize(new Dimension(30, 3));
			super.setOpaque(false);
			super.setBackground(Color.WHITE);
			this.graphColor = null;
		}

		void setGraphColor(Color c) {
			this.graphColor = c;
		}

		/**
		 * Unsets the color (prevents line drawing).
		 */
		void unsetGraphColor() {
			this.graphColor = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			if (this.graphColor == null) {
				return;
			}
			g.setColor(this.graphColor);
			int h = this.getHeight() / 2;
			g.drawLine((int) (this.getWidth() * 0.1), h,
					(int) (this.getWidth() * 0.9), h);
		}
	}
}
