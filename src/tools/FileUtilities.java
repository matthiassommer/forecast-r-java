package tools;

import java.io.*;

public abstract class FileUtilities {
    /**
     * Kopiert eine Datei.
     *
     * @param src   Quelldatei
     * @param dest  Zieldatei
     * @param force Falls true, wird eine eventuell bestehende Datei
     *              �berschrieben.
     * @throws IOException
     */
    public static void copyFile(final File src, final File dest, final boolean force) throws IOException {
        final int bufSize = 500;
        if (dest.exists()) {
            if (force) {
                dest.delete();
            } else {
                throw new IOException("Cannot overwrite existing file: " + dest);
            }
        }

        final byte[] buffer = new byte[bufSize];
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            while (true) {
                final int read = in.read(buffer);
                if (read == -1) {
                    // -1 bedeutet EOF
                    break;
                }
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        }
    }

    public static void createNewFile(final String filename) {
        try {
            File newFile = new File(filename);
            String absolutePathToFile = newFile.getAbsolutePath();

            // create folder
            File parent = newFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create folder: " + parent.getAbsolutePath());
            }

            if (newFile.exists()) {
                newFile.delete();
            }
            newFile.createNewFile();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void writeStringToFileAppending(final File file, final String text) {
        writeToFile(file, text, true);
    }

    public static void writeStringToFile(final File file, final String text) {
        writeToFile(file, text, false);
    }

    private static void writeToFile(final File f, final String text, final boolean append) {
        try {
            final FileWriter fstream = new FileWriter(f, append);
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write(text + System.getProperty("line.separator"));
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
