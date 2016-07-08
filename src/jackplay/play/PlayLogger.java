package jackplay.play;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public class PlayLogger {
    static List<String> logHistory = new LinkedList<String>();

    public static void logArguments(String name, String longName, Object[] args) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("(");
        boolean firstArgument = true;
        for (Object arg : args) {
            if (!firstArgument) builder.append(", ");
            if (null == arg) {
                builder.append("null");
            } else {
                builder.append(arg.toString());
            }

            firstArgument = false;
        }
        builder.append(")");

        logHistory.add(builder.toString());
    }

    public static void logReturn(String name, String longName, long elapsed) {
        StringBuffer builder = new StringBuffer(name);
        builder.append(name).append("() returns, elapsed ").append(elapsed).append(" ms");
        logHistory.add(builder.toString());
    }

    public static void logResult(String name, String longName, Object result, long elapsed) {
        StringBuffer builder = new StringBuffer(name);
        builder.append("() => ").append(result).append(" elapsed ").append(elapsed).append(" ms");
        logHistory.add(builder.toString());
    }

    public static void logException(String name, String longName, Throwable t) {
        StringBuffer builder = new StringBuffer(name);
        builder.append(name).append("() throws! ").append(t.getClass().getName()).append(" : ").append(t.getMessage());
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        builder.append(errors);
        logHistory.add(builder.toString());
    }

    public static String getLogHistoryAsJson() {
        return logHistory.toString();
    }
}
