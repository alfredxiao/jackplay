package jackplay;

import jackplay.bootstrap.Options;

import java.io.*;

public class JackplayLogger {
    private static String logLevel = "info";
    private static String logFile = null;
    private static final long FILE_SIZE_LIMIT = 100 * 1024 * 1024;  // 100M as maximum log file size

    private final static String TEMPLATE = "jackplay[%2$s][%1$s][%3$s]: %4$s";

    private static void write(String level, String who, Object msg) {
        String logLine = String.format(TEMPLATE, level, now(), who, messagify(msg));

        File file =  fineWritableFile(logFile);
        if (file != null) {
            PrintWriter out = null;
            try {
                boolean append = file.length() < FILE_SIZE_LIMIT;
                if (!append) System.out.println(String.format(TEMPLATE, "error", now(), "logger", "log file being truncated!"));

                out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, append)));
                out.write(logLine);
                out.write('\n');
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
                System.out.println(logLine);
                if (out != null) out.close();
            }
        } else {
            System.out.println(logLine);
        }
    }

    private static File fineWritableFile(String logFile) {
        if (logFile == null || logFile.trim().length() == 0) return null;

        File file = new File(logFile.trim());

        try {
            return (file.exists() || file.createNewFile())
                     && file.canWrite()
                   ? file : null;
        } catch (IOException ioe) {
            return null;
        }
    }

    private static String now() {
        return Options.formatNow();
    }

    private static String messagify(Object msg) {
        return (null == msg) ? "" : msg.toString();
    }

    public static void info(String who, Object msg) {
        if ("info".equalsIgnoreCase(logLevel) || "debug".equalsIgnoreCase(logLevel)) {
            write("info", who, msg);
        }
    }

    public static void error(String who, Object msg) {
        write("error", who, msg);
    }

    public static void error(String who, Throwable t) {
        write("error", who, (null == t) ? "null Throwable" : ("class: " + t.getClass().getName() + ", message : " + t.getMessage()));

        if (null != t) t.printStackTrace();
    }

    public static void debug(String who, Object msg) {
        if ("debug".equalsIgnoreCase(logLevel)) {
            write("debug", who, msg);
        }
    }

    public static void init(Options options) {
        logLevel = options.logLevel();
        logFile = options.logFile();
    }
}
