package forecasting.combinationStrategies.xcsf;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Collection of several utilities (e.g. quick matrix operations without
 * mem-alloc) and convenience methods.
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
public class XCSFUtils {

    /**
     * Multiplies <code>srcMatrix</code> (<code>n</code> <code>n</code>) with
     * <code>srcVector</code> (length <code>n</code>) and puts the results into
     * <code>destination</code> (length <code>n</code>), i.e.
     * <p>
     * <pre>
     * destination = srcMatrix * srcVector
     * </pre>
     * <p>
     * Note that this method does no checks (null or length) for performance
     * reasons and needs no extra memory.
     *
     * @param srcMatrix   the source quadratic matrix of size <code>n</code> *
     *                    <code>n</code>.
     * @param srcVector   the source vector of length <code>n</code>.
     * @param destination the destination vector of length <code>n</code>.
     * @param n           the size of the arrays to be multiplied.
     */
    public static void multiply(double[][] srcMatrix, double[] srcVector,
                                double[] destination, int n) {
        for (int i = 0; i < n; i++) {
            destination[i] = srcMatrix[i][0] * srcVector[0];
            for (int j = 1; j < n; j++) {
                destination[i] += srcMatrix[i][j] * srcVector[j];
            }
        }
    }

    /**
     * Multiplies the extended <code>srcMatrixExt</code> (<code>n+1</code>*
     * <code>n+1</code>) with <code>srcVector</code> (length <code>n</code>),
     * adds the translational component and puts the results into
     * <code>destination</code> (length <code>n</code>), i.e.
     * <p>
     * <pre>
     * destination  = srcMatrixExt * srcVector
     * destination += srcMatrixExt[lastColumn]
     * </pre>
     * <p>
     * Note that this method does no checks (null or length) for performance
     * reasons and needs no extra memory.
     *
     * @param srcMatrixExt the extended source quadratic matrix of size <code>n+1</code> *
     *                     <code>n+1</code>.
     * @param srcVector    the source vector of length <code>n</code>.
     * @param destination  the destination vector of length <code>n</code>.
     * @param n            the size of the arrays to be multiplied.
     */
    public static void multiplyExtended(double[][] srcMatrixExt,
                                        double[] srcVector, double[] destination, int n) {
        for (int i = 0; i < n; i++) {
            destination[i] = srcMatrixExt[i][0] * srcVector[0];
            for (int j = 1; j < n; j++) {
                destination[i] += srcMatrixExt[i][j] * srcVector[j];
            }
            // translation
            destination[i] += srcMatrixExt[i][n];
        }
    }

    /**
     * Multiplies the quadratic matrix <code>srcA</code> with the quadratic
     * matrix <code>srcB</code> and puts the results into
     * <code>destination</code>, i.e.
     * <p>
     * <pre>
     * destination = srcA * srcB
     * </pre>
     * <p>
     * Note that this method does no checks (null or length) for performance
     * reasons and needs no extra memory.
     *
     * @param srcA        the first quadratic source matrix of size <code>n</code> *
     *                    <code>n</code>.
     * @param srcB        the second quadratic source matrix of size <code>n</code> *
     *                    <code>n</code>.
     * @param destination the quadratic destination matrix of size <code>n</code> *
     *                    <code>n</code>.
     * @param n           the size of the arrays to be multiplied.
     */
    public static void multiply(double[][] srcA, double[][] srcB,
                                double[][] destination, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                destination[i][j] = srcA[i][0] * srcB[0][j];
                for (int k = 1; k < n; k++) {
                    destination[i][j] += srcA[i][k] * srcB[k][j];
                }
            }
        }
    }

    /**
     * Copies the quadratic matrix <code>source</code> (<code>n</code>*
     * <code>n</code>) into <code>destination</code>.
     * <p>
     * Note that this method does no checks (null or length) for performance
     * reasons.
     *
     * @param source      quadratic source matrix of size <code>n</code> <code>n</code>.
     * @param destination quadratic destination matrix of size <code>n</code>*
     *                    <code>n</code>.
     * @param n           the size of the arrays to be multiplied.
     */
    public static void copyMatrix(double[][] source, double[][] destination,
                                  int n) {
        for (int i = 0; i < n; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, n);
        }
    }

    /**
     * Faster implementation of the equals method for two double arrays.
     * <p>
     * This method is used to avoid multiple activity-, matching- or
     * prediction-calculations.
     * <p>
     * Note that this method does no length checks for performance reasons.
     *
     * @param arr1 the first array.
     * @param arr2 the second array.
     * @return true, if the arrays contain the same values.
     * @see Arrays#equals(double[] a, double[] a2)
     */
    public static boolean arrayEquals(double[] arr1, double[] arr2) {
        if (arr1 == arr2) {
            return true;
        } else if (arr1 == null || arr2 == null) {
            return false;
        }
        // don't compare length!
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false; // fast fail
            }
        }
        return true;
    }

    /**
     * Copies the <code>source</code> to <code>destination</code>.
     *
     * @param source      the source file.
     * @param destination the destination file (created, if it does not exists).
     */
    public static void fileCopy(File source, File destination) {
        try {
            FileChannel srcChannel = new FileInputStream(source).getChannel();
            FileChannel dstChannel = new FileOutputStream(destination)
                    .getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to copy " + source.getName() + " to "
                    + destination.getName() + ".");
        }
    }

    /**
     * Determines the num first objects, where an object may contain multiple
     * copies specified in nums. Routine determines at least the num first
     * entries, if the entry at the boundary contains more than 1 (specified in
     * nums), it will still be included.
     *
     * @param votes The sorting criterion (largest first)
     * @param nums  The number of entries each entry represents.
     * @param objs  The objects sorting according to the criteria
     * @param size  The number of actual entries in the arrays.
     * @param num   The numer of largest objects put to the front
     * @return The number of first entries that make up the num largest entries.
     */
    public static int putNumFirstObjectsFirst(double[] votes, int[] nums,
                                              Object[] objs, int size, int num) {
        putNumFirstRec(votes, nums, objs, 0, size - 1, num);
        int numsum = 0;
        for (int i = 0; i < size; i++) {
            numsum += nums[i];
            if (numsum >= num) {
                return i + 1;
            }
        }
        return size;
    }

    /**
     * Puts the objects with the highest votes first. An object objs[i] is
     * regarded as nums[i] identical objects. Does NOT sort but simply puts the
     * first num highest objects first in the array (in a generally random
     * order). Algorithms works in linear time (length of array) on average!
     *
     * @param votes An array of votes of the corresponding objects
     * @param nums  The number of identical objects each object entry represents
     * @param objs  The actual array of objects.
     * @param begin The first object in the array considered in the procedure.
     * @param end   The last object considered in the procedure.
     * @param num   The number of objects to be put first.
     */
    private static void putNumFirstRec(double[] votes, int[] nums,
                                       Object[] objs, int begin, int end, int num) {
        if (num <= 0 || begin >= end) {
            return;
        }
        int helpN;
        double help = 0;
        Object helpO;
        double pivot = votes[end];
        int pivotN = nums[end];
        Object pivotO = objs[end];
        if (end - begin > 20) { // choose pivot out of 3 if long array
            // remains
            int pos0 = begin
                    + (int) (XCSFUtils.Random.uniRand() * (end + 1 - begin));
            int pos1 = begin
                    + (int) (XCSFUtils.Random.uniRand() * (end + 1 - begin));
            int pos2 = begin
                    + (int) (XCSFUtils.Random.uniRand() * (end + 1 - begin));
            int posChosen = 0;
            if (votes[pos2] < votes[pos1]) {
                if (votes[pos1] < votes[pos0]) {
                    posChosen = pos1;
                } else if (votes[pos0] < votes[pos2]) {
                    posChosen = pos2;
                } else {
                    posChosen = pos0;
                }
            } else {
                if (votes[pos2] < votes[pos0]) {
                    posChosen = pos2;
                } else if (votes[pos0] < votes[pos1]) {
                    posChosen = pos1;
                } else {
                    posChosen = pos0;
                }
            }
            // flip(posChosen, end)
            pivot = votes[posChosen];
            pivotN = nums[posChosen];
            pivotO = objs[posChosen];
            votes[posChosen] = votes[end];
            nums[posChosen] = nums[end];
            objs[posChosen] = objs[end];
            votes[end] = pivot;
            nums[end] = pivotN;
            objs[end] = pivotO;
        }
        int i = begin;
        int numSize = 0;
        for (int j = begin; j < end; j++) {
            if (pivot <= votes[j]) {
                // exchange elements - remember numSize on the left
                help = votes[j];
                votes[j] = votes[i];
                votes[i] = help;
                helpN = nums[j];
                nums[j] = nums[i];
                nums[i] = helpN;
                helpO = objs[j];
                objs[j] = objs[i];
                objs[i] = helpO;
                numSize += nums[i];
                i++;
            }
        }
        votes[end] = votes[i];
        nums[end] = nums[i];
        objs[end] = objs[i];
        votes[i] = pivot;
        nums[i] = pivotN;
        objs[i] = pivotO;
        if (num < numSize) {
            // more than numSize on the left - solve that part
            putNumFirstRec(votes, nums, objs, begin, i - 1, num);
        } else if (num < numSize + nums[i]) {
            // done including the pivotelement
            return;
        } else {
            // still need to get more from the right - solve that part
            putNumFirstRec(votes, nums, objs, i + 1, end, num - numSize
                    - nums[i]);
        }
    }

    /**
     * This class is used to communicate with a GnuPlot instance. See
     * http://www.gnuplot.info/ for details about GnuPlot.
     * <p>
     * Support for
     * <ul>
     * <li>Windows (executable: C:/Programme/gnuplot/bin/pgnuplot.exe)
     * <li>Linux (executable: gnuplot)
     * </ul>
     * <p>
     * Example Code:
     * <p>
     * <pre>
     * GnuPlotConsole gnuplot = new GnuPlotConsole();
     * gnuplot.writeln(&quot;set xrange[0:1];set yrange[0:1]&quot;);
     * gnuplot.writeln(&quot;set isosamples 20,20&quot;);
     * gnuplot.writeln(&quot;set contour; set surface&quot;);
     * gnuplot.writeln(&quot;splot sin(4 * pi * (x+y)) title 'sinus(4 PI (x+y))'&quot;);
     * </pre>
     *
     * @author Patrick O. Stalph
     */
    public static class GnuPlotConsole {
        /**
         * This static String can be specified, if the default doesn't work.
         */
        static String gnuPlotExecutable = null;
        final static String NOT_INSTALLED = "notinstalled";

        // default executables for windows and linux
        private final static String[][] EXECUTABLE = {{"Linux", "gnuplot"}, //
                {"Windows", "C:/Program Files (x86)/gnuplot/bin/gnuplot.exe"} //
        };

        // communication channel
        private PrintStream console;

        /**
         * Default constructor executes GnuPlot and establishes the
         * communication channel via a <code>PrintStream</code>.
         *
         * @throws IOException           if the system fails to execute gnuplot.
         * @throws IllegalStateException if gnuplot is not installed.
         */
        public GnuPlotConsole() throws IOException, IllegalStateException {
            Process p;
            if (gnuPlotExecutable == null) {
                // default case
                String os = System.getProperty("os.name").toLowerCase();
                for (String[] exec : EXECUTABLE) {
                    if (os.contains(exec[0].toLowerCase())) {
                        p = Runtime.getRuntime().exec(exec[1]);
                        this.console = new PrintStream(p.getOutputStream());
                        return;
                    }
                }
                System.err.println("Sorry, your operating system (" + os
                        + ") is not supported.");
                System.err.println("If you don't have GnuPlot installed, "
                        + "specify 'gnuplotExecutable = notInstalled'.");
                System.err.println("Otherwise specify the GnuPlot "
                        + "executable for your system.");
                throw new IllegalStateException("Failed to execute GnuPlot.");
            } else if (gnuPlotExecutable.toLowerCase().equals(NOT_INSTALLED)) {
                // not installed
                throw new IllegalStateException("GnuPlot is not installed.");
            } else {
                // exe is specified
                p = Runtime.getRuntime().exec(gnuPlotExecutable);
                this.console = new PrintStream(p.getOutputStream());
            }
        }

        /**
         * Sends the given command to GnuPlot. Multiple commands can be
         * seperated with a semicolon.
         *
         * @param command the command.
         */
        public void writeln(String command) {
            if (this.console == null) {
                return; // ignore error
            }
            this.console.println(command);
            this.console.flush();
        }
    }

    /**
     * Implementation of a random number generator. We're not trusting the
     * java.util.Random class :)
     *
     * @author Martin Butz
     */
    public static class Random {

        /**
         * Constant for the random number generator (modulus of PMMLCG = 2^31
         * -1).
         */
        private final static long _M = 2147483647;

        /**
         * Constant for the random number generator (default = 16807).
         */
        private final static long _A = 16807;

        /**
         * Constant for the random number generator (=_M/_A).
         */
        private final static long _Q = _M / _A;

        /**
         * Constant for the random number generator (=_M mod _A).
         */
        private final static long _R = _M % _A;

        /**
         * The current random number value in long format.
         */
        private static long seed = 101;

        /**
         * Sets a random seed in order to randomize the pseudo random generator.
         *
         * @param s the seed to set.
         */
        public static void setSeed(long s) {
            seed = s;
        }

        /**
         * Returns the current random number generator seed value
         *
         * @return The RNG seed value.
         */
        public static long getSeed() {
            return seed;
        }

        /**
         * Returns a random number between zero and one.
         *
         * @return the current random number
         */
        public static double uniRand() {
            long hi = seed / _Q;
            long lo = seed % _Q;
            long test = _A * lo - _R * hi;

            if (test > 0)
                seed = test;
            else
                seed = test + _M;

            return (double) (seed) / _M;
        }

        /**
         * Indicates if another normaly distributed random number has already
         * been generated.
         */
        private static boolean haveUniNum = false;

        /**
         * A generated uniformly distributed random number
         */
        private static double uniNum = 0;

        /**
         * Returns a normally distributed random number with mean 0 and standard
         * deviation 1.
         *
         * @return A random number - normally distributed.
         */
        static double normRand() {
            if (haveUniNum) {
                haveUniNum = false;
                return uniNum;
            } else {
                double x1, x2, w;
                do {
                    x1 = 2.0 * uniRand() - 1.0;
                    x2 = 2.0 * uniRand() - 1.0;
                    w = x1 * x1 + x2 * x2;
                } while (w >= 1.0);

                w = Math.sqrt((-2.0 * Math.log(w)) / w);
                uniNum = x1 * w;
                haveUniNum = true;
                return x2 * w;
            }
        }
    }
}
