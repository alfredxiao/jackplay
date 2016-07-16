package jackplay;

public class JackLogger {
    static boolean log = true;
    static boolean debug = true;

    public static void log(Object msg) {
        if (log) {
            System.out.println("jackplay[info]: " + ((null == msg) ? "" : msg.toString()));
        }
    }

    public static void error(Throwable t) {
        System.out.println("jackplay[error]: " + ((null == t) ? "NULL Throwable" : ("class: " + t.getClass().getName() + ", message : " + t.getMessage())));
        if (null != t) t.printStackTrace();
    }

    public static void debug(Object msg) {
        if (debug) {
            System.out.println("jackplay[debug]: " + ((null == msg) ? "" : msg.toString()));
        }
    }

    public static void initialise(JackOptions options) {
        log = options.log();
        debug = options.debug();
    }
}
