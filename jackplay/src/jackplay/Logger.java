package jackplay;

import jackplay.bootstrap.Options;

public class Logger {
    static String logLevel = "info";

    private final static String TEMPLATE = "jackplay[%2$s][%1$s][%3$s]: %4$s";

    private static void write(String level, String who, Object msg) {
        System.out.println(String.format(TEMPLATE, level, now(), who, messagify(msg)));
    }

    private static String now() {
        return Options.formateNow();
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
    }
}
