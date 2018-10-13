package tests;

import forecasting.RServeConnection;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RMultithreadTest {
    @Test
    public void testMultiThread() throws RserveException, REXPMismatchException, InterruptedException {
        Runnable r = () -> {
            String output = null;
            try {
                System.out.println("one");
                RConnection conn = RServeConnection.getConnection();
                output = conn.eval("R.version.string").asString();
            } catch (REXPMismatchException | RserveException e) {
                System.err.println(e.toString());
            }
            System.out.println("one " + output);
        };

        Runnable r2 = () -> {
            String output = null;
            try {
                System.out.println("two");
                RConnection conn = RServeConnection.getConnection();
                output = conn.eval("R.version.string").asString();
            } catch (REXPMismatchException | RserveException e) {
                System.err.println(e.toString());
            }
            System.out.println("two" + output);
        };

        new Thread(r).start();
        Thread.sleep(10);
        new Thread(r2).start();

        Thread.sleep(8000);
    }
}
