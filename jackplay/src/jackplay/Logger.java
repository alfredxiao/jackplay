package jackplay;

import jackplay.bootstrap.Options;

public class Logger {
    static String logLevel = "info";

    public static void info(Object msg) {
        if ("info".equalsIgnoreCase(logLevel) || "debug".equalsIgnoreCase(logLevel)) {
            System.out.println("jackplay[info]: " + ((null == msg) ? "" : msg.toString()));
        }
    }

    public static void error(Object msg) {
        System.out.println("jackplay[error]: " + ((null == msg) ? "" : msg.toString()));
    }

    public static void error(String who, Throwable t) {
        System.out.println("jackplay[error][" + who + "]: " + ((null == t) ? "NULL Throwable" : ("class: " + t.getClass().getName() + ", message : " + t.getMessage())));
        if (null != t) t.printStackTrace();
    }

    public static void debug(Object msg) {
        if ("debug".equalsIgnoreCase(logLevel)) {
            System.out.println("jackplay[debug]: " + ((null == msg) ? "" : msg.toString()));
        }
    }

    public static void init(Options options) {
        logLevel = options.logLevel();
    }
}
