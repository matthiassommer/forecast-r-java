package forecasting.combinationStrategies.xcsf.visualization;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import forecasting.combinationStrategies.xcsf.StateDescriptor;
import forecasting.combinationStrategies.xcsf.XCSFConstants;
import forecasting.combinationStrategies.xcsf.XCSFListener;
import forecasting.combinationStrategies.xcsf.XCSF;
import forecasting.combinationStrategies.xcsf.classifier.Classifier;
import forecasting.combinationStrategies.xcsf.classifier.ConditionHyperellipsoid;

import javax.imageio.ImageIO;
import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Implements the <code>XCSFListener</code> interface to provide a population
 * visualization. There is one <code>JPanel</code> for 2D conditions and one for
 * 3D. The latter is used for higher dimensions too, showing the first three
 * dimensions.
 * 
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class ConditionVis extends JFrame implements XCSFListener, ChangeListener, ActionListener {
	private final static int MIN_STEPS = 1;
	private final static int MAX_STEPS = 1000;
	private final static int MIN_DELAY = 0;
	private final static int MAX_DELAY = 5000;

	/**
	 * The number of steps to wait, until the population is visualized.
	 */
	public static int visualizationSteps = XCSFConstants.averageExploitTrials;

	/**
	 * The delay in milliseconds after the population is visualized. Note, that
	 * the visualization is NOT synchronized with xcsf, since the event
	 * dispatched thread calls the paint(Graphics) method! Therefore it is
	 * possible, that the population or some classifiers change during paint()
	 * calls. If slowMotion is true, the listener/xcsf thread is blocked, until
	 * paint() returns.
	 */
	public static int visualizationDelay = 50;

	/**
	 * A scaling factor for the conditions.
	 */
	public static float visualizedConditionSize = 0.2f;

	/**
	 * The transparency of the conditions.
	 */
	public static float visualizationTransparency = 0.2f;

	/**
	 * This flag indicates, if the matching should be visualized. If this flag
	 * is set, the population is visualized every step and
	 * {@link #visualizationSteps} is ignored! Additionally the xcsf thread is
	 * blocked, until the paint() method returns.
	 */
	public static boolean slowMotion = false;

	// sliders & slowmode-button
	private JSlider slider1;
	private JSlider slider2;
	private JSlider slider3;
	private JSlider slider4;
	private JToggleButton slowModeButton;
	private int fastMotionSteps;
	private JPanel clVis;
	private JLabel labelText;

	// used for simple synchronization
	boolean isPainting = false;

	// to be visualized
	Classifier[] currentPopulation;
	StateDescriptor currentState;

	public ConditionVis() {
		super("Condition Visualization");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int xSize = (int) (0.5 * screen.getWidth());
		int ySize = (int) (0.8 * screen.getHeight());
		this.setBounds(0, 0, xSize, ySize);
		this.setLocation((int) screen.getWidth() - xSize, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xcsf.listener.XCSFListener#stateChanged(int, java.util.Vector,
	 * java.util.Vector, xcsf.StateDescriptor)
	 */
	public void stateChanged(int iteration, Classifier[] population,
			Classifier[] matchSet, StateDescriptor state, double[][] performance) {
		if (this.clVis == null) {
			// first call
			int dim = population[0].getCondition().getCenter().length;
			if (dim < 2 || dim > 2) {
				// invalid dimension
				System.out.println("illegal dim: " + dim);
				return;
			}
			init(dim);
		} else if (iteration % visualizationSteps != 0 && !slowMotion) {
			return;
		}
		this.currentPopulation = population;
		this.currentState = state;
		this.labelText.setText("Iteration: " + iteration
				+ ", MacroClassifiers: " + population.length);

		// repaint call, wait & delay
		try {
			this.isPainting = true;
			this.repaint();
			if (ConditionVis.slowMotion) {
				// dirty sync for painting
				while (this.isPainting) {
					Thread.sleep(50);
					/*
					 * XXX infinite loop, if paint is not called. This happens,
					 * when the window is not visible for any reason. On the
					 * other hand, it doesn't make any sense to start slowmode
					 * and hide the frame...
					 */
				}
			}
			// sleep for at least 100ms to support gui repaints
			// in the eventDispatchedThread
			Thread.sleep(100 + visualizationDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		ConditionVis.slowMotion = this.slowModeButton.isSelected();
		if (slowMotion) {
			fastMotionSteps = visualizationSteps;
			this.slider1.setValue(1);
		} else {
			if (fastMotionSteps > 1) {
				visualizationSteps = fastMotionSteps;
				this.slider1.setValue(fastMotionSteps);
			}
		}
		this.slider1.setEnabled(!ConditionVis.slowMotion);
		this.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
			return;
		}
		if (e.getSource().equals(this.slider1)) {
			ConditionVis.visualizationSteps = this.slider1.getValue();
		} else if (e.getSource().equals(this.slider2)) {
			ConditionVis.visualizationDelay = this.slider2.getValue();
		} else if (e.getSource().equals(this.slider3)) {
			ConditionVis.visualizedConditionSize = this.slider3.getValue() / 100.0f;
		} else if (e.getSource().equals(this.slider4)) {
			ConditionVis.visualizationTransparency = this.slider4.getValue() / 100.0f;
		}
		this.repaint();
	}

	/**
	 * Creates a nice screenshot of the given <code>population</code>.
	 * 
	 * @param population
	 *            the population to visualize.
	 * @param filename
	 *            the filename.
	 */
	public static void screenshot(Classifier[] population, String filename) {
		BufferedImage screenshot = null;
		int dim = population[0].getCondition().getCenter().length;
		if (dim == 2) {
			// 2D - create image using the paint method.
			ConditionVis cvis = new ConditionVis();
			cvis.init(2);
			cvis.currentPopulation = population;
			int width = cvis.clVis.getWidth();
			int height = cvis.clVis.getHeight();
			screenshot = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = screenshot.getGraphics();
			cvis.clVis.paint(g);
			cvis.setVisible(false);
			cvis.dispose();
		} else if (dim == 3) {
			try {
				XCSF.class.getClassLoader().loadClass(
						"com.sun.j3d.utils.universe.SimpleUniverse");
			} catch (ClassNotFoundException e) {
				return; // java3D not available.
			}
			// 3D - workaround: create a screenshot.
			// XXX screenshot captures screensavers or blankscreens too
			try {
				// temporarily change setting
				boolean slomo = slowMotion;
				slowMotion = false; // avoid drawing matchset
				java.awt.Robot robot = new java.awt.Robot();
				ConditionVis cvis = new ConditionVis();
				cvis.setLocation(0, 0);
				cvis.isPainting = true;
				cvis.stateChanged(XCSFConstants.maxLearningIterations,
						population, null, null, null);
				while (cvis.isPainting) {
					Thread.sleep(100);
				}
				cvis.requestFocus();
				screenshot = robot.createScreenCapture(new Rectangle(cvis.getBounds()));

				// reset
				cvis.setVisible(false);
				cvis.dispose();
				slowMotion = slomo;
			} catch (AWTException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// dimension not supported
			return;
		}

		// store the screenshot
		try {
			ImageIO.write(screenshot, "png", new File(filename + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialized the GUI.
	 * 
	 * @param dimension
	 *            the dimension of the conditions to visualize.
	 */
	private void init(int dimension) {
		// bottom
		this.labelText = new JLabel("Iteration 0, Current Average Error 1",
				SwingConstants.CENTER);
		this.labelText.setBorder(BorderFactory.createEtchedBorder());
		this.add(this.labelText, BorderLayout.SOUTH);

		// top
		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = 1;
		// slowmode button
		slowModeButton = new JToggleButton("Show Matchset",
				ConditionVis.slowMotion);
		slowModeButton.addActionListener(this);
		c.gridheight = 2;
		top.add(slowModeButton, c);
		c.gridheight = 1;
		// slider 1
		c.gridx++;
		if (visualizationSteps > MAX_STEPS) {
			visualizationSteps = MAX_STEPS;
		} else if (visualizationSteps < MIN_STEPS) {
			visualizationSteps = MIN_STEPS;
		}
		this.slider1 = new JSlider(MIN_STEPS, MAX_STEPS,
				ConditionVis.visualizationSteps);
		slider1.setMajorTickSpacing(MAX_STEPS - MIN_STEPS);
		slider1.setMinorTickSpacing((MAX_STEPS - MIN_STEPS + 1) / 10);
		slider1.setPaintTicks(true);
		slider1.setPaintLabels(true);
		slider1.setEnabled(!slowMotion);
		slider1.addChangeListener(this);
		top.add(new JLabel("Visualization Steps", SwingConstants.CENTER), c);
		c.gridy++;
		top.add(this.slider1, c);
		// slider 2
		c.gridy = 0;
		c.gridx++;
		if (visualizationDelay > MAX_DELAY) {
			visualizationDelay = MAX_DELAY;
		} else if (visualizationDelay < MIN_DELAY) {
			visualizationDelay = MIN_DELAY;
		}
		this.slider2 = new JSlider(MIN_DELAY, MAX_DELAY,
				ConditionVis.visualizationDelay);
		slider2.setMajorTickSpacing((MAX_DELAY - MIN_DELAY) / 2);
		slider2.setMinorTickSpacing((MAX_DELAY - MIN_DELAY) / 10);
		slider2.setPaintLabels(true);
		slider2.setPaintTicks(true);
		slider2.addChangeListener(this);
		top.add(new JLabel("Visualization Delay (ms)", SwingConstants.CENTER),
				c);
		c.gridy++;
		top.add(this.slider2, c);
		// slider 3
		c.gridy = 0;
		c.gridx++;
		if (visualizedConditionSize > 1f) {
			visualizedConditionSize = 1f;
		} else if (visualizedConditionSize < 0f) {
			visualizedConditionSize = 0f;
		}
		this.slider3 = new JSlider(0, 100,
				(int) (ConditionVis.visualizedConditionSize * 100));
		slider3.setMajorTickSpacing(50);
		slider3.setMinorTickSpacing(10);
		slider3.setPaintLabels(true);
		slider3.setPaintTicks(true);
		slider3.addChangeListener(this);
		top.add(new JLabel("Condition Size (%)", SwingConstants.CENTER), c);
		c.gridy++;
		top.add(this.slider3, c);
		// slider 4
		if (dimension == 2) {
			c.gridy = 0;
			c.gridx++;
			if (visualizationTransparency > 1f) {
				visualizationTransparency = 1f;
			} else if (visualizationTransparency < 0f) {
				visualizationTransparency = 0f;
			}
			this.slider4 = new JSlider(0, 100,
					(int) (ConditionVis.visualizationTransparency * 100));
			slider4.setMajorTickSpacing(50);
			slider4.setMinorTickSpacing(10);
			slider4.setPaintLabels(true);
			slider4.setPaintTicks(true);
			slider4.addChangeListener(this);
			top.add(new JLabel("Transparency (%)", SwingConstants.CENTER), c);
			c.gridy++;
			top.add(this.slider4, c);
		}
		top.setBorder(BorderFactory.createEtchedBorder());
		this.add(top, BorderLayout.NORTH);

		// center
		if (dimension == 2) {
			this.clVis = new Neuron2DVisualization();
		} else if (dimension > 2) {
			this.clVis = new Neuron3DVisualization();
		} else {
			throw new IllegalArgumentException(
					"Illegal ConditionVisualization dimension: " + dimension);
		}
		this.add(this.clVis, BorderLayout.CENTER);

		this.setVisible(true);
	}

	/**
	 * Realizes a classifier visualiztation in 2D. Creats a JPanel within which
	 * the classifiers are drawn. Supports panel resizing.
	 * 
	 * @author Patrick Stalph
	 */
	private class Neuron2DVisualization extends JPanel {
		private int offset; // top/bottom/left/right offset for painting
		private int range; // vertical & horizontal range for painting

		/**
		 * Overriden paint method to visualize the 2D-ConditionHyperEllipsoids.
		 * <p>
		 * Note that this method is running from the event dispatched thread.
		 * Therefore the population can change (and will!) during painting,
		 * because the event dispatched thread is running with low priority. If
		 * slowMotion is true, the xcsf-thread will wait, until paint returns -
		 * kind of a dirty syncronization.
		 * 
		 * @param g
		 *            the graphics object
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			if (currentPopulation == null) {
				return;
			}
			this.updateScaleAndOffset();
			Graphics2D g2 = (Graphics2D) g;
			// light gray background
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawRect(0, 0, this.getWidth(), this.getHeight());
			// white background for input space
			g2.setColor(Color.WHITE);
			g2.fillRect(offset, offset, range, range);
			// enable antialiasing & transparency
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					1.f - visualizationTransparency));

			if (!slowMotion) {
				// fast view: paint all cl's with fitness color
				for (int i = 0; i < currentPopulation.length; i++) {
					Classifier cl = currentPopulation[i];
					double fit = cl.getFitness();
					fit = Math.pow(fit, .3);
					g2.setColor(new Color((int) (255 * (.8 - .8 * fit)),
							(int) (255 * (.8 - .8 * fit)), 255));
					g2.fill(createShape(cl.getCondition()));
				}
			} else {
				// slow mode: paint non-matching cl's with fitness color
				// and matching cl's with border & activity color
				for (int i = 0; i < currentPopulation.length; i++) {
					Classifier cl = currentPopulation[i];
					ConditionHyperellipsoid con = cl.getCondition();
					if (!cl.doesMatch(currentState)) {
						// use fitness for coloring
						double fit = cl.getFitness();
						fit = Math.pow(fit, .3);
						g2.setColor(new Color((int) (255 * (.8 - .8 * fit)),
								(int) (255 * (.8 - .8 * fit)), 255));
						g2.fill(createShape(con));
					}
				}
				// paint matchset
				for (int i = 0; i < currentPopulation.length; i++) {
					Classifier cl = currentPopulation[i];
					ConditionHyperellipsoid con = cl.getCondition();
					if (cl.doesMatch(currentState)) {
						// con.doesMatch: draw border + use activity for color
						float act = (float) cl.getActivity(currentState);
						g2.setColor(new Color(0.0f, 1.0f, 0.0f, act));
						Shape shape = createShape(con);
						g2.fill(shape);
						g2.setColor(new Color(0.0f, 0.0f, 0.0f, act));
						g2.draw(shape);
					}
				}
				// paint condition input
				int x = translateX(currentState.getConditionInput()[0]);
				int y = translateY(currentState.getConditionInput()[1]);
				g2.setColor(Color.RED);
				g2.drawLine(x - 5, y - 5, x + 5, y + 5);
				g2.drawLine(x + 5, y - 5, x - 5, y + 5);
			}
			// release pseudo lock
			isPainting = false;
		}

		/**
		 * Recalculate x/y offset and x/y range.
		 */
		private void updateScaleAndOffset() {
			range = Math.min(this.getWidth(), this.getHeight());
			offset = (int) (.05 * range);
			range -= (2. * offset);

		}

		/**
		 * Translates the given x-coordinate for painting.
		 * 
		 * @param x
		 *            the x coordinate with 0 <= x <= 1
		 * @return the x painting coordinate
		 */
		private int translateX(double x) {
			return (int) (offset + (x * range));
		}

		/**
		 * Translates the given y-coordinate for painting.
		 * 
		 * @param y
		 *            the y coordinate with 0 <= x <= 1
		 * @return the y painting coordinate
		 */
		private int translateY(double y) {
			return (int) (offset + ((1.0 - y) * range));
		}

		/**
		 * Creates the shape for the given condition.
		 * 
		 * @param con
		 *            the ellipsoidal condition.
		 * @return the shape object used for 2D-painting
		 */
		private Shape createShape(ConditionHyperellipsoid con) {
			double stretchX = visualizedConditionSize * con.getStretch()[0] * 2
					* this.getWidth() / this.range;
			double stretchY = visualizedConditionSize * con.getStretch()[1] * 2
					* this.getWidth() / this.range;
			Ellipse2D ellipse = new Ellipse2D.Double(-.5 * stretchX * range,
					-.5 * stretchY * range, stretchX * range, stretchY * range);
			// rotate
			AffineTransform at = AffineTransform.getRotateInstance(-con
					.getAngles()[0]);
			Shape ellRot = at.createTransformedShape(ellipse);
			// set position (invert y because y.top is 0
			// and y.bottom is this.getHeight())
			at.setToTranslation(offset + con.getCenter()[0] * range, offset
					+ (1.0 - con.getCenter()[1]) * range);
			return at.createTransformedShape(ellRot);
		}
	}

	/**
	 * Realizes a classifier visualiztation in 3D. Uses Java3D. Supports
	 * rotating, zooming, and moving the shown population structure using the
	 * left, middle, and right mouse buttons, respectively.
	 * <p>
	 * XXX transparency doesn't work online
	 * <p>
	 * XXX crazy mem alloc.
	 * 
	 * @author Patrick Stalph
	 */
	private class Neuron3DVisualization extends JPanel {
		Canvas3D canvas3d = null;
		private int prevSize;
		private SimpleUniverse univ = null;
		private BranchGroup scene = null;
		private TransformGroup mouseTrans = null;
		private BranchGroup clBranchGroup = null;

		// condition array
		private Primitive[] condObjects;
		// transform3D for every condition (rotation, stretch and translation)
		private Transform3D[] t3d;
		// the transformgroup for every condition
		private TransformGroup[] tgs;
		// Set up colors
		private final Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		private final Color3f blue = new Color3f(0.3f, 0.3f, 0.8f);
		private final Color3f specular = new Color3f(0.7f, 0.7f, 0.7f);
		private final Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		private final Color3f red = new Color3f(0.7f, .15f, .15f);

		Neuron3DVisualization() {
			super.setLayout(new GridLayout(1, 1));
			canvas3d = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
			super.add(canvas3d);

			// Create a simple scene and attach it to the virtual universe
			scene = this.createBaseSceneGraph();
			condObjects = new Primitive[XCSFConstants.maxPopSize];
			t3d = new Transform3D[XCSFConstants.maxPopSize];
			tgs = new TransformGroup[XCSFConstants.maxPopSize];
			createClassifiercondObjects();
			scene.compile();
			univ = new SimpleUniverse(canvas3d);

			// This will move the ViewPlatform back a bit so the
			// objects in the scene can be viewed.
			univ.getViewingPlatform().setNominalViewingTransform();

			// Allow Java3D to optimize the scene graph.
			univ.addBranchGraph(scene);
			prevSize = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			if (currentPopulation == null) {
				return;
			}
			// the inverse transformation of a 3D ellipsoid (3*3 matrix)
			// put into a lenght 16 array (used as 4*4 matrix)
			// i.e. indices 1-3, 4-6, 8-10
			double[] trans = new double[16];
			// translational components (indices: 3,7,11,12,13,14,15) unset
			trans[15] = 1.0; // all zero except 15 (diagonal element)

			int i = 0;
			for (; i < currentPopulation.length; i++) {
				if (visualizedConditionSize > 0) {
					Classifier cl = currentPopulation[i];
					ConditionHyperellipsoid con = cl.getCondition();

					// set color
					if (slowMotion && cl.doesMatch(currentState)) {
						// matching classifier but not offspring
						Color3f col = new Color3f(.2f, 1f, 1f);
						Color3f colLight = new Color3f(.8f, 1f, 1f);
						condObjects[i].getAppearance().setMaterial(
								new Material(col, black, col, colLight, 1.0f));
					} else {
						double fit = cl.getFitness();
						fit = Math.pow(fit, .1);
						Color3f col = new Color3f((float) (.8 - .8 * fit),
								(float) (.8 - .8 * fit), (float) (.9));
						Color3f colLight = new Color3f((float) (.9 - .6 * fit),
								(float) (.9 - .6 * fit), 1f);
						condObjects[i].getAppearance().setMaterial(
								new Material(col, black, col, colLight, 1.0f));
					}

					double[][] invTransMatrix = con.getTransform();
					// set stretch & rotation
					trans[0] = invTransMatrix[0][0] * visualizedConditionSize;
					trans[1] = invTransMatrix[0][1] * visualizedConditionSize;
					trans[2] = invTransMatrix[0][2] * visualizedConditionSize;
					// second row
					trans[4] = invTransMatrix[1][0] * visualizedConditionSize;
					trans[5] = invTransMatrix[1][1] * visualizedConditionSize;
					trans[6] = invTransMatrix[1][2] * visualizedConditionSize;
					// third row
					trans[8] = invTransMatrix[2][0] * visualizedConditionSize;
					trans[9] = invTransMatrix[2][1] * visualizedConditionSize;
					trans[10] = invTransMatrix[2][2] * visualizedConditionSize;
					// translation - 0.5
					trans[3] = invTransMatrix[0][3] - 0.5;
					trans[7] = invTransMatrix[1][3] - 0.5;
					trans[11] = invTransMatrix[2][3] - 0.5;
					trans[15] = 1.0;
					t3d[i].set(trans);
					try {
						tgs[i].setTransform(t3d[i]);
					} catch (Exception e) {
						System.out.println("Transform did not work" + t3d[i]
								+ " Error:" + e);
						Vector3d v3ds2 = new Vector3d(0, 0, 0);
						t3d[i].set(v3ds2);
						t3d[i].setScale(v3ds2);
						tgs[i].setTransform(t3d[i]);
					}
				} else {
					// size 0
					Vector3d v3ds2 = new Vector3d(0, 0, 0);
					t3d[i].set(v3ds2);
					t3d[i].setScale(v3ds2);
					tgs[i].setTransform(t3d[i]);
				}

			}

			// remove previous classifiers by setting their condObjects to zero
			// scale.
			for (; i < prevSize; i++) {
				Vector3d v3ds = new Vector3d(0, 0, 0);
				t3d[i].set(v3ds);
				t3d[i].setScale(v3ds);
				tgs[i].setTransform(t3d[i]);
			}
			prevSize = currentPopulation.length;
			isPainting = false;
		}

		/**
		 * Creates a complete set of condObjects and transformation nodes
		 * However, the condObjects are initially of size 0.
		 */
		private void createClassifiercondObjects() {
			for (int i = 0; i < condObjects.length; i++) {
				Appearance ap = new Appearance();
				ap.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
				ap.setTransparencyAttributes(new TransparencyAttributes(
						TransparencyAttributes.FASTEST,
						visualizationTransparency));
				ap.setMaterial(new Material(blue, black, blue, white, 1.0f));

				// Create a ball to demonstrate textures
				int primflags = Primitive.GENERATE_NORMALS
						+ Primitive.GENERATE_TEXTURE_COORDS;
				condObjects[i] = new Sphere(1.0f, primflags, ap);
				condObjects[i]
						.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY);
				condObjects[i].setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

				t3d[i] = new Transform3D();
				t3d[i].setScale(new Vector3d(0., 0., 0.));

				Vector3d v3dt = new Vector3d(.5, .5, .5);
				t3d[i].setTranslation(v3dt);

				tgs[i] = new TransformGroup(t3d[i]);
				tgs[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				tgs[i].addChild(condObjects[i]);

				mouseTrans.addChild(tgs[i]);
			}
		}

		private BranchGroup createBaseSceneGraph() {
			// Create the root of the branching graph of 3-D objects
			BranchGroup objRoot = new BranchGroup();

			// creating a background
			BoundingSphere boundingSphere = new BoundingSphere(new Point3d(0.0,
					0.0, 0.0), 1000.0);

			Background background = new Background(specular);//backgroundTexture.
																// getImage());
			background.setApplicationBounds(boundingSphere);
			objRoot.addChild(background);

			TransformGroup baseTrans = new TransformGroup();
			baseTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			mouseTrans = new TransformGroup();
			mouseTrans.setCapability(Group.ALLOW_CHILDREN_WRITE);
			mouseTrans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			mouseTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			clBranchGroup = new BranchGroup();
			clBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
			clBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			clBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

			// Create encompassing cube
			Appearance cca = new Appearance();
			cca.setTexture(null);
			cca.setTransparencyAttributes(new TransparencyAttributes(
					TransparencyAttributes.FASTEST, .9f));
			cca.setMaterial(new Material(black, black, black, white, 1.0f));
			Box cc = new Box(.55f, .55f, .55f, cca);
			clBranchGroup.addChild(cc);

			createCoordinateSystem();

			mouseTrans.addChild(clBranchGroup);
			baseTrans.addChild(mouseTrans);
			objRoot.addChild(baseTrans);

			Color3f light1Color = new Color3f(.6f, .6f, .6f);
			Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
			DirectionalLight light1 = new DirectionalLight(light1Color,
					light1Direction);
			light1.setInfluencingBounds(boundingSphere);
			objRoot.addChild(light1);

			// Set up the ambient light
			Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);
			AmbientLight ambientLightNode = new AmbientLight(ambientColor);
			ambientLightNode.setInfluencingBounds(boundingSphere);
			objRoot.addChild(ambientLightNode);

			// Create the rotate behavior node
			MouseRotate behaviorRot = new MouseRotate();
			behaviorRot.setTransformGroup(mouseTrans);

			baseTrans.addChild(behaviorRot);
			behaviorRot.setSchedulingBounds(boundingSphere);

			// Create the translate behavior node
			MouseTranslate behaviorTrans = new MouseTranslate();
			behaviorTrans.setTransformGroup(mouseTrans);

			baseTrans.addChild(behaviorTrans);
			behaviorTrans.setSchedulingBounds(boundingSphere);

			// Create wheel zoom behavior
			MouseWheelZoom behaviorWZoom = new MouseWheelZoom();
			behaviorWZoom.setTransformGroup(mouseTrans);

			baseTrans.addChild(behaviorWZoom);
			behaviorWZoom.setSchedulingBounds(boundingSphere);

			return objRoot;
		}

		private void createCoordinateSystem() {
			// Create X axis
			LineArray axisXLines = new LineArray(2, GeometryArray.COORDINATES);
			clBranchGroup.addChild(new Shape3D(axisXLines));
			axisXLines.setCoordinate(0, new Point3f(-1.0f, 0.0f, 0.0f));
			axisXLines.setCoordinate(1, new Point3f(1.0f, 0.0f, 0.0f));

			// Create Y axis
			LineArray axisYLines = new LineArray(2, GeometryArray.COORDINATES
					| GeometryArray.COLOR_3);
			clBranchGroup.addChild(new Shape3D(axisYLines));
			axisYLines.setCoordinate(0, new Point3f(0.0f, -1.0f, 0.0f));
			axisYLines.setCoordinate(1, new Point3f(0.0f, 1.0f, 0.0f));

			// Create Z axis with arrow
			Point3f z1 = new Point3f(0.0f, 0.0f, -1.0f);
			Point3f z2 = new Point3f(0.0f, 0.0f, 1.0f);
			LineArray axisZLines = new LineArray(10, GeometryArray.COORDINATES
					| GeometryArray.COLOR_3);
			clBranchGroup.addChild(new Shape3D(axisZLines));

			axisZLines.setCoordinate(0, z1);
			axisZLines.setCoordinate(1, z2);
			axisZLines.setCoordinate(2, z2);
			axisZLines.setCoordinate(3, new Point3f(0.1f, 0.1f, 0.9f));
			axisZLines.setCoordinate(4, z2);
			axisZLines.setCoordinate(5, new Point3f(-0.1f, 0.1f, 0.9f));
			axisZLines.setCoordinate(6, z2);
			axisZLines.setCoordinate(7, new Point3f(0.1f, -0.1f, 0.9f));
			axisZLines.setCoordinate(8, z2);
			axisZLines.setCoordinate(9, new Point3f(-0.1f, -0.1f, 0.9f));
			for (int i = 0; i < axisZLines.getValidVertexCount(); i++)
				axisZLines.setColor(i, red);

			// now write the coordinates (X,Y,Z) to the sides
			Appearance textap = new Appearance();
			textap.setTexture(null);
			textap.setMaterial(new Material(black, black, black, black, 1.0f));
			Font3D f3d = new Font3D(new Font("Arial", Font.BOLD, 10),
					new FontExtrusion());
			double textScale = .004;

			// Write "X"
			Text3D t100 = new Text3D(f3d, "X", new Point3f(0f, 0f, 0f));
			// positive axis
			Shape3D s100 = new Shape3D(t100, textap);
			Transform3D trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(.55f, 0f, 0f));
			TransformGroup tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(.55f, 0f, 0f));
			trans.setRotation(new Quat4d(Math.PI * .66, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(.55f, 0f, 0f));
			trans.setRotation(new Quat4d(-Math.PI * .66, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			// negative axis
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(-.55f, 0f, 0f));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(-.55f, 0f, 0f));
			trans.setRotation(new Quat4d(Math.PI * .66, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(-.55f, 0f, 0f));
			trans.setRotation(new Quat4d(-Math.PI * .66, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);

			// Write "Y" - positive axis
			t100 = new Text3D(f3d, "Y", new Point3f(0f, 0f, 0f));
			// positiv
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0.55f, 0f));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0.55f, 0f));
			trans.setRotation(new Quat4d(0, .66 * Math.PI, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0.55f, 0f));
			trans.setRotation(new Quat4d(0, -.66 * Math.PI, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			// negative axis
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, -0.55f, 0f));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, -0.55f, 0f));
			trans.setRotation(new Quat4d(0, .66 * Math.PI, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, -0.55f, 0f));
			trans.setRotation(new Quat4d(0, -.66 * Math.PI, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);

			// write "Z"
			t100 = new Text3D(f3d, "Z", new Point3f(0f, 0f, 0f));
			// positive axis
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, 0.55f));
			trans.setRotation(new Quat4d(0, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, 0.55f));
			trans.setRotation(new Quat4d(0, 0, .66 * Math.PI, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, 0.55f));
			trans.setRotation(new Quat4d(0, 0, -.66 * Math.PI, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			// negative axis
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, -0.55f));
			trans.setRotation(new Quat4d(0, 0, 0, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, -0.55f));
			trans.setRotation(new Quat4d(0, 0, .66 * Math.PI, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
			s100 = new Shape3D(t100, textap);
			trans = new Transform3D();
			trans.setScale(new Vector3d(textScale, textScale, textScale));
			trans.setTranslation(new Vector3f(0f, 0f, -0.55f));
			trans.setRotation(new Quat4d(0, 0, -.66 * Math.PI, 1));
			tg = new TransformGroup(trans);
			tg.addChild(s100);
			mouseTrans.addChild(tg);
		}
	}
}
