package forecasting.timeseriesMetrics;

import forecasting.RServeConnection;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * For alternative="greater", the alternative hypothesis is that method 2 is more accurate than method 1 on a 95% confidence level.
 */
public class PairedtTest {
    public static String calculate(double[] a, double[] b) throws REngineException, REXPMismatchException {
        RConnection rConnection = RServeConnection.getConnection();
        rConnection.assign("tTest.a", a);
        rConnection.assign("tTest.b", b);

        StringBuilder sb = new StringBuilder();
        sb.append("t.test(tTest.a, tTest.b, alt='greater', paired=TRUE, conf.level=0.95)");
        rConnection.voidEval("result <- " + sb.toString());

        return rConnection.eval("paste(capture.output(print(result)),collapse=\"\\n\")").asString();
    }
}
