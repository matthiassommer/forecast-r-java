package forecasting.dataSources;

import com.Ostermiller.util.CSVParser;
import tools.FileUtilities;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads a time series. Converts it to a double array.
 */
public abstract class TimeSeriesLoader {
    public static final String TIME_SERIES_REPOSITORY = "timeseries\\";
    public static final String TRAFFIC_FOLDER = TIME_SERIES_REPOSITORY + "TrafficData\\";
    public static final String[] TRAFFIC_FILES = {"16_2010", "26_2010", "54_2010", "147_2010", "176_2010", "276_2010",
            "378_2010", "562_2010", "645_2010", "777_2010", "805_2010", "894_2010", "999_2010", "1090_2010",
            "1234_2010", "1398_2010", "1444_2010", "1500_2010", "1550_2010", "1653_2010", "1812_2010", "1915_2010",
            "2000_2010", "2100_2010", "2200_2010", "2300_2010", "2400_2010", "2600_2010", "2900_2010", "3000_2010"};


    public static double[] readTimeSeriesfromCSV(String path) {
        String csvFile = Paths.get("..", path).toAbsolutePath().toString();
        BufferedReader br = null;
        String line;

        double[] timeSeries = null;
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(csvFile));
            lnr.skip(Long.MAX_VALUE);
            timeSeries = new double[lnr.getLineNumber()];
            lnr.close();

            br = new BufferedReader(new FileReader(csvFile));

            int i = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String value = line.split(",")[1];
                timeSeries[i] = Double.parseDouble(value);
                i++;
            }
        } catch (NumberFormatException | IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        return timeSeries;
    }

    /**
     * Reads a time series from a csv and writes it in reversed order back to this file.
     */
    public static void reverseTimseries() {
        String csvFile = "C:\\Users\\oc6admin\\workspace\\OTC\\Forecasting\\Zeitreihen\\BARB-MSCI_D\\BARB-MSCI_D.csv";
        BufferedReader br = null;
        String line;
        List<String> reverse = new ArrayList<>();

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                reverse.add(line);
            }
        } catch (NumberFormatException | IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        Collections.reverse(reverse);
        try {
            for (String aReverse : reverse) {
                FileUtilities.createNewFile(csvFile + ".txt");
                PrintStream psout = new PrintStream(new FileOutputStream(csvFile + ".txt", true), true);
                psout.println(aReverse);
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void removeInvalidLines() {
        String csvFile = "C:\\Users\\oc6admin\\workspace\\OTC\\Forecasting\\Zeitreihen\\FRED-RIFSPPFAAD15NB\\RIFSPPFAAD15NB.csv";
        BufferedReader br = null;
        String line;
        List<String> entries = new ArrayList<>();

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                try {
                    Double value = Double.parseDouble(line.split(",")[1]);
                    entries.add(line);
                } catch (NumberFormatException e) {
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            for (String aReverse : entries) {
                FileUtilities.createNewFile(csvFile + ".txt");
                PrintStream psout = new PrintStream(new FileOutputStream(csvFile + ".txt", true), true);
                psout.println(aReverse);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static double[] loadTimeSeriesFromCSV(final String filePath) {
        File file = new File(filePath);
        double[] data = new double[0];

        try {
            CSVParser parser = new CSVParser(new FileInputStream(file));
            parser.changeDelimiter(',');

            String[][] lines = parser.getAllValues();

            data = new double[lines.length - 1];
            for (int i = 0; i < data.length; i++) {
                if (lines[i].length != 0) {
                    data[i] = Double.parseDouble(lines[i + 1][1]);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return data;
    }
}
