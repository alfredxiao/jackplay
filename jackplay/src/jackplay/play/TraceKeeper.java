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

    private static void addToHistory(TraceTriggerPoint type, String methodFullName, String log, String uuid) {
        addToHistory(type, methodFullName, log, 0, uuid);
    }

    private synchronized static void addToHistory(TraceTriggerPoint type, String methodFullName, String log, long elapsed, String uuid) {
        while (logHistory.size() >= traceLogLimit) {
            logHistory.remove(logHistory.size() - 1);
        }

        TraceLog entry = new TraceLog(type, new PlayGround(methodFullName), log, uuid, elapsed);
        logHistory.add(0, entry);
    }

    public static void enterMethod(String methodFullName, Object[] args, String uuid) {
        String argsAsString = (args == null || args.length == 0) ? "" : ((args.length == 1) ? objectToString(args[0]) : objectToString(args, false));
        addToHistory(TraceTriggerPoint.MethodEntry, methodFullName, argsAsString, uuid);
    }

    public static void returnsVoid(String methodFullName, long elapsed, String uuid) {
        addToHistory(TraceTriggerPoint.MethodReturns, methodFullName, "returns", elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, Object result, long elapsed, String uuid) {
        addToHistory(TraceTriggerPoint.MethodReturns, methodFullName, objectToString(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, boolean result, long elapsed, String uuid) {
        returnsResult(methodFullName, Boolean.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, byte result, long elapsed, String uuid) {
        returnsResult(methodFullName, Byte.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, short result, long elapsed, String uuid) {
        returnsResult(methodFullName, Short.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, int result, long elapsed, String uuid) {
        returnsResult(methodFullName, Integer.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, long result, long elapsed, String uuid) {
        returnsResult(methodFullName, Long.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, float result, long elapsed, String uuid) {
        returnsResult(methodFullName, Float.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, char result, long elapsed, String uuid) {
        returnsResult(methodFullName, Character.valueOf(result), elapsed, uuid);
    }

    public static void returnsResult(String methodFullName, double result, long elapsed, String uuid) {
        returnsResult(methodFullName, Double.valueOf(result), elapsed, uuid);
    }

    public static void throwsException(String methodFullName, Throwable t) {
        addToHistory(TraceTriggerPoint.MethodThrowsException, methodFullName, objectToString(t), 0, null);
    }

    public static void init(Options options) {
        traceLogLimit = options.traceLogLimit();
    }

    public synchronized static void clearLogHistory() {
        logHistory.clear();
    }

    private static String objectToString(Object obj) {
        return objectToString(obj, true);
    }

    private static String objectToString(Object obj, boolean squareBracket) {
        if (null == obj) return "null";

        StringBuilder builder = new StringBuilder();

        if (obj instanceof String) {
            builder.append("\"").append(obj).append("\"");
        } else if (obj.getClass().equals(Character.class)) {
            builder.append("\'").append(obj).append("\'");
        } else if (obj.getClass().isArray()) {
            builder.append(squareBracket ? "[" : "(");

            boolean isFirst = true;
            Object[] values = (Object[]) obj;
            for (Object value : values) {
                if (!isFirst) builder.append(",");
                builder.append(objectToString(value, squareBracket));
                isFirst = false;
            }
            builder.append(squareBracket ? "]" : ")");
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



