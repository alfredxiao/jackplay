package jackplay.play;

import jackplay.JackOptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class PlayLogger {
    static List<LogEntry> logHistory = new LinkedList<LogEntry>();
    static int logLimit;

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

        addToHistory(LogEntryType.MethodEntry, builder.toString());
    }

    private static void addToHistory(LogEntryType type, String log) {
        while (logHistory.size() >= logLimit) {
            logHistory.remove(logHistory.size() - 1);
        }
        LogEntry entry = new LogEntry(type, log);
        logHistory.add(0, entry);
    }

    public static void logReturn(String name, String longName, long elapsed) {
        StringBuilder builder = new StringBuilder(name);
        builder.append(name).append("() returns, elapsed ").append(elapsed).append(" ms");
        addToHistory(LogEntryType.MethodReturns, builder.toString());
    }

    public static void logResult(String name, String longName, Object result, long elapsed) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("()").append(" elapsed ").append(elapsed).append(" ms ").append(" => ").append(result);
        addToHistory(LogEntryType.MethodReturns, builder.toString());
    }

    public static void logException(String name, String longName, Throwable t) {
        StringBuilder builder = new StringBuilder(name);
        builder.append(name).append("() throws! ").append(t.getClass().getName()).append(" : ").append(t.getMessage());
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        builder.append(errors);
        addToHistory(LogEntryType.MethodThrowsException, builder.toString());
    }

    public static String getLogHistoryAsJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        for (LogEntry entry : logHistory) {
            if (!isFirst) builder.append(',');
            builder.append("{");
            builder.append("\"type\":\"").append(entry.type.toString()).append("\",");
            builder.append("\"when\":\"").append(formatDate(entry.when)).append("\",");
            builder.append("\"log\":\"").append(entry.log).append("\"");
            builder.append("}");
            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static String formatDate(Date when) {
        return formatter.format(when);
    }

    public static void initialise(JackOptions options) {
        logLimit = options.logLimit();
    }
}

class LogEntry{
    Date when;
    String log;
    LogEntryType type;

    public LogEntry(LogEntryType type, String log) {
        this.when = new Date();
        this.log = log;
        this.type = type;
    }
}

enum LogEntryType {
    MethodEntry,
    MethodReturns,
    MethodThrowsException
}