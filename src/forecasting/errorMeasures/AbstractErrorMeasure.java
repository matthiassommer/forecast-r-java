package forecasting.errorMeasures;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class AbstractErrorMeasure {
    public PrintStream printStream;

    /**
     * Calculate the forecast error.
     *
     * @param observations actual values
     * @param forecasts    forecast values
     * @return forecast error
     */
    public abstract double run(double[] observations, double[] forecasts);

    public void initStream(String filename) {
        try {
            printStream = new PrintStream(new FileOutputStream(filename), true);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
