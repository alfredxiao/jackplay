package jackplay.play;

import jackplay.Options;
import jackplay.play.domain.PlayGround;
import jackplay.play.domain.TraceLog;
import jackplay.play.domain.TraceTriggerPoint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class TraceKeeper {
    static List<TraceLog> logHistory = new CopyOnWriteArrayList<>();
    static int traceLogLimit;

    private static void addToHistory(TraceTriggerPoint type, String methodFullName, String log) {
        addToHistory(type, methodFullName, log, 0);
    }

    private synchronized static void addToHistory(TraceTriggerPoint type, String methodFullName, String log, long elapsed) {
        while (logHistory.size() >= traceLogLimit) {
            logHistory.remove(logHistory.size() - 1);
        }

        TraceLog entry = new TraceLog(type, new PlayGround(methodFullName), log, elapsed);
        logHistory.add(0, entry);
    }

    public static void enterMethod(String methodFullName, Object[] args) {
        addToHistory(TraceTriggerPoint.MethodEntry, methodFullName, objectToString(args));
    }

    public static void returnsVoid(String methodFullName, long elapsed) {
        addToHistory(TraceTriggerPoint.MethodReturns, methodFullName, "returns", elapsed);
    }

    public static void returnsResult(String methodFullName, Object result, long elapsed) {
        addToHistory(TraceTriggerPoint.MethodReturns, methodFullName, objectToString(result), elapsed);
    }

    public static void returnsResult(String methodFullName, boolean result, long elapsed) {
        returnsResult(methodFullName, Boolean.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, byte result, long elapsed) {
        returnsResult(methodFullName, Byte.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, short result, long elapsed) {
        returnsResult(methodFullName, Short.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, int result, long elapsed) {
        returnsResult(methodFullName, Integer.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, long result, long elapsed) {
        returnsResult(methodFullName, Long.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, float result, long elapsed) {
        returnsResult(methodFullName, Float.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, char result, long elapsed) {
        returnsResult(methodFullName, Character.valueOf(result), elapsed);
    }

    public static void returnsResult(String methodFullName, double result, long elapsed) {
        returnsResult(methodFullName, Double.valueOf(result), elapsed);
    }

    public static void throwsException(String methodFullName, Throwable t) {
        addToHistory(TraceTriggerPoint.MethodThrowsException, methodFullName, objectToString(t));
    }

    public static void init(Options options) {
        traceLogLimit = options.traceLogLimit();
    }

    public synchronized static void clearLogHistory() {
        logHistory.clear();
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
        } else if (obj instanceof Throwable) {
            StringWriter stackTrace = new StringWriter();
            ((Throwable) obj).printStackTrace(new PrintWriter(stackTrace));
            builder.append(stackTrace);
        } else {
            builder.append(obj);
        }

        return builder.toString();
    }
}



