package forecasting;

import org.apache.commons.lang3.SystemUtils;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to call R from Java via Rserve.
 *
 * @author Matthias Sommer.
 */
public class RServeConnection {
    private static final String PATH_TO_R = SystemUtils.IS_OS_UNIX ? "R" : "C:\\Program Files\\R\\R-3.5.1\\bin\\x64\\R.exe";
    private static final String HOST = "localhost";
    /**
     * Unique identifier to use for variable names in R.
     */
    private static final AtomicInteger nextVarId = new AtomicInteger(1);
    private static int PORT = 6311;
    // maps thread-ids to R-threads
    private static Map<Long, RConnection> threads;
    // Holds the main Rserve process. Can be used to shutdown the main process.
    // private static List<Process> rServeProcesses;

    /**
     * Returns a globally unique variable name.
     *
     * @return unique id
     */
    public static synchronized String getUniqueVarname() {
        return String.format("var_%s", nextVarId.getAndIncrement());
    }

    public static synchronized RConnection getConnection() {
        if (threads == null) {
            threads = new HashMap<>();
        }
        /*if (rServeProcesses == null) {
            rServeProcesses = new ArrayList<>(2);
        }*/

        // Multithreading
        // Unix: no problem, one Rserve instance can serve mulitple calls.
        // Windows: Rserve can't create a seperate process by forking the current process.
        // --> create a new Rserve process for each thread (listening on a different port),
        // a new Rserve connection on the corresponding port has to be established as well.
        long threadId = Thread.currentThread().getId();
        if (threads.containsKey(threadId) || (SystemUtils.IS_OS_UNIX && !threads.isEmpty())) {
            if (SystemUtils.IS_OS_UNIX) {
                return (RConnection) (threads.values().toArray())[0];
            }
            return threads.get(threadId);
        } else {
            try {
                System.out.println("Start Rserve on port " + PORT);

                createRserveProcess(PORT);
                RConnection connection = new RConnection(HOST, PORT);
                init(connection);
                threads.put(threadId, connection);

                PORT++;

                return connection;
            } catch (RserveException e) {
                System.err.println("Rserve: " + e.getMessage());
                tearDown();
                return null;
            } catch (IOException e) {
                System.err.println("Cannot start Rserve: " + e.getMessage());
                tearDown();
                return null;
            }
        }
    }

    private static synchronized void createRserveProcess(int port) throws IOException {
        String cmd = PATH_TO_R + " -e " + "\"library(Rserve);Rserve(port=" + port + ")\"";
        //  rServeProcesses.add(Runtime.getRuntime().exec(cmd));
        Runtime.getRuntime().exec(cmd);
    }

    public static void tearDown() {
        try {
            for (RConnection connection : threads.values()) {
                connection.close();
                connection.shutdown();
                connection.detach();
            }
           // rServeProcesses.forEach(Process::destroy);
        } catch (RserveException e1) {
            System.err.println("Error closing Rserve threads: " + e1.getMessage());
        }
    }

    /**
     * Load the necessary R packages.
     * TODO: check if they are still all necessary
     *
     * @param connection
     * @throws RserveException
     */
    private static synchronized void init(RConnection connection) throws RserveException {
        // find local package repositories
        connection.voidEval("library.path <- cat(.libPaths()[1:1])");

        //load packages
        connection.voidEval("library(\"stats\", lib.loc = library.path)");
        connection.voidEval("library(\"forecast\", lib.loc = library.path)");
        connection.voidEval("library(\"bfast\", lib.loc = library.path)");
        connection.voidEval("library(\"FKF\", lib.loc = library.path)");
        connection.voidEval("library(\"fNonlinear\", lib.loc = library.path)");
        connection.voidEval("library(\"moments\", lib.loc = library.path)");
        connection.voidEval("library(\"pracma\", lib.loc = library.path)");
        connection.voidEval("library(\"ggplot2\", lib.loc = library.path)");
        connection.voidEval("library(\"RColorBrewer\", lib.loc = library.path)");
        connection.voidEval("library(\"tseries\", lib.loc = library.path)");
        connection.voidEval("library(\"fma\", lib.loc = library.path)");
        connection.voidEval("library(\"tsoutliers\", lib.loc = library.path)");
    }
}
