package jackplay.play;

import jackplay.Options;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class PlayKeeper {
    static List<PlayEntry> logHistory = new CopyOnWriteArrayList<PlayEntry>();
    static int playBookSize;

    public static void traceArguments(String name, String longName, Object[] args) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("(");
        boolean firstArgument = true;
        for (Object arg : args) {
            if (!firstArgument) builder.append(", ");
            builder.append(objectToString(arg));

            firstArgument = false;
        }
        builder.append(")");

        addToHistory(PlayEntryType.MethodEntry, builder.toString());
    }

    private synchronized static void addToHistory(PlayEntryType type, String log) {
        while (logHistory.size() >= playBookSize) {
            logHistory.remove(logHistory.size() - 1);
        }
        PlayEntry entry = new PlayEntry(type, log);
        logHistory.add(0, entry);
    }

    public static void traceReturn(String name, String longName, long elapsed) {
        StringBuilder builder = new StringBuilder(name);
        builder.append(name).append("() returns, elapsed ").append(elapsed).append(" ms");
        addToHistory(PlayEntryType.MethodReturns, builder.toString());
    }

    public static void traceResult(String name, String longName, Object result, long elapsed) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("()").append(" elapsed ").append(elapsed).append(" ms ").append(" => ").append(objectToString(result));
        addToHistory(PlayEntryType.MethodReturns, builder.toString());
    }

    private static String objectToString(Object obj) {
        if (null == obj) return "null";

        StringBuilder builder = new StringBuilder();

        if (obj instanceof String) {
            builder.append("\"").append(obj).append("\"");
        } else if (obj.getClass().equals(Character.class)) {
            builder.append("\'").append(obj).append("\'");
        } else if (obj.getClass().isArray()) {
            builder.append("[");

             boolean isFirst = true;
            Object[] values = (Object[]) obj;
            for (Object value : values) {
                if (!isFirst) builder.append(",");
                builder.append(objectToString(value));
                isFirst = false;
            }
            builder.append("]");
        } else {
            builder.append(obj);
        }

        return builder.toString();
    }

    public static void traceResult(String name, String longName, boolean result, long elapsed) {
        traceResult(name, longName, Boolean.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, byte result, long elapsed) {
        traceResult(name, longName, Byte.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, short result, long elapsed) {
        traceResult(name, longName, Short.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, int result, long elapsed) {
        traceResult(name, longName, Integer.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, long result, long elapsed) {
        traceResult(name, longName, Long.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, float result, long elapsed) {
        traceResult(name, longName, Float.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, char result, long elapsed) {
        traceResult(name, longName, Character.valueOf(result), elapsed);
    }

    public static void traceResult(String name, String longName, double result, long elapsed) {
        traceResult(name, longName, Double.valueOf(result), elapsed);
    }

    public static void traceException(String name, String longName, Throwable t) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("() throws an exception! ");
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        builder.append(errors);
        addToHistory(PlayEntryType.MethodThrowsException, builder.toString());
    }

    public static String getLogHistoryAsJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        Iterator<PlayEntry> it = logHistory.iterator();
        while (it.hasNext()) {
            PlayEntry entry = it.next();
            if (!isFirst) builder.append(',');
            builder.append("{");
            builder.append("\"type\":\"").append(entry.type.toString()).append("\",");
            builder.append("\"when\":\"").append(formatDate(entry.when)).append("\",");
            builder.append("\"log\":").append(escape(entry.log));
            builder.append("}");
            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }

    private static String escape(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static String formatDate(Date when) {
        return formatter.format(when);
    }

    public static void init(Options options) {
        playBookSize = options.playBookSize();
    }

    public synchronized static void clearLogHistory() {
        logHistory.clear();
    }
}

class PlayEntry {
    Date when;
    String log;
    PlayEntryType type;

    public PlayEntry(PlayEntryType type, String log) {
        this.when = new Date();
        this.log = log;
        this.type = type;
    }
}

enum PlayEntryType {
    MethodEntry,
    MethodReturns,
    MethodThrowsException
}