package jackplay.bootstrap;

import static jackplay.bootstrap.TracePoint.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class TraceKeeper {
    private static List<TraceLog> traceLogs = new LinkedList<>();
    private static Options options;

    private synchronized static List<TraceLog> copyTraceLogs() {
        List<TraceLog> copyList = new ArrayList<>(traceLogs.size());
        copyList.addAll(traceLogs);

        return copyList;
    }

    public static List<Map<String, Object>> getTraceLogs() {
        Iterator<TraceLog> it = TraceKeeper.copyTraceLogs().iterator();
        List<Map<String, Object>> listOfLogs = new ArrayList<>();

        while (it.hasNext()) {
            try {
                TraceLog traceLog = it.next();

                Map<String, Object> map = new HashMap<>();
                map.put("when", Options.formatDate(traceLog.when));
                map.put("tracePoint", traceLog.tracePoint.toString());
                map.put("classFullName", traceLog.pg.classFullName);
                map.put("methodShortName", traceLog.pg.methodShortName);
                map.put("uuid", traceLog.uuid);
                map.put("threadId", traceLog.threadId);
                map.put("threadName", traceLog.threadName);
                map.put("arguments", traceLog.arguments);
                map.put("returnedValue", traceLog.returnedValue);
                map.put("returningVoid", traceLog.returningVoid);
                map.put("exceptionStackTrace", traceLog.exceptionStackTrace);
                map.put("elapsed", traceLog.elapsed);
                map.put("argumentsCount", traceLog.argumentsCount);

                listOfLogs.add(map);
            } catch(ConcurrentModificationException e) {}
        }

        return listOfLogs;
    }

    private synchronized static void addTraceLog(TraceLog entry) {
        while (traceLogs.size() >= options.traceLogLimit()) {
            traceLogs.remove(traceLogs.size() - 1);
        }

        traceLogs.add(0, entry);
    }

    public static void enterMethod(String methodFullName, Object[] args, String uuid) {
        TraceLog entry = new TraceLog(MethodEntry, new PlayGround(methodFullName), uuid);
        if (null == args || args.length == 0) {
            entry.arguments = null;
        } else {
            entry.arguments = new String[args.length];
            entry.argumentsCount = args.length;
            for (int i=0; i<args.length; i++) {
                entry.arguments[i] = objectToString(args[i]);
            }
        }

        addTraceLog(entry);
    }

    public static void returnsVoid(String methodFullName, int argsLen, String uuid, long elapsed) {
        TraceLog entry = new TraceLog(MethodReturns, new PlayGround(methodFullName), uuid);
        entry.elapsed = elapsed;
        entry.argumentsCount = argsLen;
        entry.returningVoid = true;

        addTraceLog(entry);
    }

    public static void returnsResult(String methodFullName, int argsLen, Object result, String uuid, long elapsed) {
        TraceLog entry = new TraceLog(MethodReturns, new PlayGround(methodFullName), uuid);
        entry.elapsed = elapsed;
        entry.argumentsCount = argsLen;
        entry.returnedValue = objectToString(result);

        addTraceLog(entry);
    }

    public static void returnsResult(String methodFullName, int argsLen, boolean result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Boolean.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, byte result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Byte.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, short result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Short.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, int result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Integer.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, long result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Long.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, float result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Float.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, char result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Character.valueOf(result), uuid, elapsed);
    }

    public static void returnsResult(String methodFullName, int argsLen, double result, String uuid, long elapsed) {
        returnsResult(methodFullName, argsLen, Double.valueOf(result), uuid, elapsed);
    }

    public static void throwsException(String methodFullName, int argsLen, Throwable t) {
        TraceLog correspondingMethodEntryLog = findCorrespondingEntryLog(methodFullName, Thread.currentThread().getId());

        long elapsed = -1;
        String uuid;
        if (correspondingMethodEntryLog != null) {
            elapsed = System.currentTimeMillis() - correspondingMethodEntryLog.whenAsTimeMs;
            uuid = correspondingMethodEntryLog.uuid;
        } else {
            // this is for the client/browser's purpose
            uuid = "corresponding_uuid_lost_" + java.util.UUID.randomUUID().toString();
        }

        TraceLog entry = new TraceLog(MethodThrowsException, new PlayGround(methodFullName), uuid);
        entry.elapsed = elapsed;
        entry.argumentsCount = argsLen;
        entry.exceptionStackTrace = throwableToString(t);

        addTraceLog(entry);
    }

    private static TraceLog findCorrespondingEntryLog(String methodFullName, long threadId) {
        try {
            for (TraceLog entry : traceLogs) {
                if (entry.threadId == threadId && entry.pg.methodFullName.equals(methodFullName)) {
                    return entry;
                }
            }
        } catch(ConcurrentModificationException cme) {}

        return null;
    }

    public static void init(Options opts) {
        options = opts;
    }

    public synchronized static void clearLogHistory() {
        traceLogs.clear();
    }

    private static String objectToString(Object obj) {
        return objectToString(obj, true);
    }

    private static String objectToString(Object obj, boolean squareBracket) {
        if (null == obj) return null;

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
        } else {
            builder.append(obj);
        }

        return builder.toString();
    }

    public static String throwableToString(Throwable t) {
        if (null == t) {
            return null;
        } else {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            return stackTrace.toString();
        }
    }
}



