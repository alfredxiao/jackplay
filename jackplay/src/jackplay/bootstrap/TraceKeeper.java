package jackplay.bootstrap;

import static jackplay.bootstrap.Site.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.*;

public class TraceKeeper {
    private static List<Trace> traces = new LinkedList<>();
    private static Options options;

    private TraceKeeper() {}

    private synchronized static List<Trace> copyTraceLogs() {
        List<Trace> copyList = new ArrayList<>(traces.size());
        copyList.addAll(traces);

        return copyList;
    }

    public static List<Map<String, Object>> getTraces() {
        Iterator<Trace> it = TraceKeeper.copyTraceLogs().iterator();
        List<Map<String, Object>> listOfLogs = new ArrayList<>();

        while (it.hasNext()) {
            try {
                Trace trace = it.next();

                Map<String, Object> map = new HashMap<>();
                map.put("when", Options.formatDate(trace.when));
                map.put("site", trace.site.toString());
                map.put("classFullName", trace.pg.classFullName);
                map.put("methodShortName", trace.pg.methodShortName);
                map.put("uuid", trace.uuid);
                map.put("threadId", trace.threadId);
                map.put("threadName", trace.threadName);
                map.put("arguments", trace.arguments);
                map.put("returnedValue", trace.returnedValue);
                map.put("returningVoid", trace.returningVoid);
                map.put("exceptionStackTrace", trace.exceptionStackTrace);
                map.put("elapsed", trace.elapsed);
                map.put("argumentsCount", trace.argumentsCount);

                listOfLogs.add(map);
            } catch(ConcurrentModificationException ignore) {}
        }

        return listOfLogs;
    }

    private synchronized static void addTraceLog(Trace entry) {
        while (traces.size() >= options.traceLogLimit()) {
            traces.remove(traces.size() - 1);
        }

        traces.add(0, entry);
    }

    public static void enterMethod(String methodFullName, Object[] args, String uuid) {
        try {
            Trace entry = new Trace(MethodEntrance, new PlayGround(methodFullName), uuid);
            if (null == args || args.length == 0) {
                entry.arguments = null;
            } else {
                entry.arguments = new String[args.length];
                entry.argumentsCount = args.length;
                for (int i = 0; i < args.length; i++) {
                    entry.arguments[i] = objectToString(args[i]);
                }
            }

            addTraceLog(entry);
        } catch(Throwable ignore) {}
    }

    public static void returnsVoid(String methodFullName, int argsLen, String uuid, long elapsed) {
        try {
            Trace entry = new Trace(MethodExit, new PlayGround(methodFullName), uuid);
            entry.elapsed = elapsed;
            entry.argumentsCount = argsLen;
            entry.returningVoid = true;

            addTraceLog(entry);
        } catch(Throwable ignore) {}
    }

    public static void returnsResult(String methodFullName, int argsLen, Object result, String uuid, long elapsed) {
        try {
            Trace entry = new Trace(MethodExit, new PlayGround(methodFullName), uuid);
            entry.elapsed = elapsed;
            entry.argumentsCount = argsLen;
            entry.returnedValue = objectToString(result);

            addTraceLog(entry);
        } catch(Throwable ignore) {}
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
        try {
            Trace correspondingEntranceLog = findCorrespondingEntryLog(methodFullName, Thread.currentThread().getId());

            long elapsed = -1;
            String uuid;
            if (correspondingEntranceLog != null) {
                elapsed = System.currentTimeMillis() - correspondingEntranceLog.whenAsTimeMs;
                uuid = correspondingEntranceLog.uuid;
            } else {
                // this is for the client/browser's purpose
                uuid = "corresponding_uuid_lost_" + java.util.UUID.randomUUID().toString();
            }

            Trace entry = new Trace(MethodThrowsException, new PlayGround(methodFullName), uuid);
            entry.elapsed = elapsed;
            entry.argumentsCount = argsLen;
            entry.exceptionStackTrace = throwableToString(t);

            addTraceLog(entry);
        } catch(Throwable ignore) {}
    }

    private static Trace findCorrespondingEntryLog(String methodFullName, long threadId) {
        try {
            for (Trace entry : traces) {
                if (entry.threadId == threadId && entry.pg.methodFullName.equals(methodFullName)) {
                    return entry;
                }
            }
        } catch(ConcurrentModificationException ignore) {}

        return null;
    }

    public static void init(Options opts) {
        options = opts;
    }

    public synchronized static void clearLogHistory() {
        traces.clear();
    }

    private static String objectToString(Object obj) {
        return objectToString(obj, true);
    }

    private static String objectToString(Object obj, boolean squareBracket) {
        if (null == obj) return null;

        StringBuilder builder = new StringBuilder();

        if (obj instanceof String) {
            String str = (String) obj;
            int lenToPrint = options.traceStringLength();
            String strTraced = str.length() > lenToPrint ? str.substring(0, lenToPrint) : str;
            if (lenToPrint < str.length()) {
                strTraced += "..." + (str.length() - lenToPrint) + " more characters...";
            }

            builder.append("\"").append(strTraced).append("\"");
        } else if (obj.getClass().equals(Character.class)) {
            builder.append("\'").append(obj).append("\'");
        } else if (obj.getClass().isArray()) {
            builder.append(squareBracket ? "[" : "(");

            int arrayLen = Array.getLength(obj);
            int lenToPrint = Math.min(options.traceArrayLength(), arrayLen);
            boolean isFirst = true;

            Class componentType = obj.getClass().getComponentType();
            for (int i=0; i<lenToPrint; i++) {
                if (!isFirst) builder.append(", ");
                Object element = Array.get(obj, i);

                if (componentType.isPrimitive()) {
                    builder.append(element);
                } else {
                    builder.append(objectToString(element, squareBracket));
                }

                isFirst = false;
            }

            if (lenToPrint < arrayLen) {
                builder.append(", ..").append(arrayLen - lenToPrint).append(" more..");
            }

            builder.append(squareBracket ? "]" : ")");
        } else {
            builder.append(obj);
        }

        return builder.toString();
    }

    private static String throwableToString(Throwable t) {
        if (null == t) {
            return null;
        } else {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            return stackTrace.toString();
        }
    }
}



