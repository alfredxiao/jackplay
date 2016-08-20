package jackplay.play;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.bootstrap.TraceLog;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;


public class InfoCenter {

    Instrumentation inst;
    ProgramManager pm;
    final static ClassComparator classComparator = new ClassComparator();
    final static MethodComparator methodComparator = new MethodComparator();
    private Options options;

    public void init(Instrumentation inst, ProgramManager pm, Options options) {
        this.inst = inst;
        this.pm = pm;
        this.options = options;
    }

    public Map<String, Object> getServerSettings() {
        Map<String, Object> serverSettings = new HashMap<>();
        serverSettings.put("traceLogLimit", options.traceLogLimit());
        serverSettings.put("autoSuggestLimit", options.autoSuggestLimit());

        return serverSettings;
    }

    public void updateOption(String key, Object value) {
        options.updateOption(key, value.toString());
    }

    public List<Class> findLoadedClasses(String className) {
        Class[] classes = this.inst.getAllLoadedClasses();
        List<Class> matched = new LinkedList<>();
        for (Class clz : classes) {
            if (clz.getName().equals(className)) {
                matched.add(clz);
            }
        }

        return matched;
    }

    public Method findMatchingMethod(Class clazz, PlayGround pg) {
        if (clazz.getName().equals(pg.classFullName)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(pg.methodShortName)
                        && method.getParameterCount() == pg.parameterList.size()) {

                    Class[] paramClasses = method.getParameterTypes();
                    boolean parametersMatched = true;
                    for (int i=0; i<method.getParameterCount(); i++) {
                        if (!paramClasses[i].getCanonicalName().equals(pg.parameterList.get(i))) {
                            parametersMatched = false;
                            break;
                        }
                    }

                    if (parametersMatched) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    static class ClassComparator implements Comparator<Class> {
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    static class MethodComparator implements Comparator<Method> {
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public List<Map<String, String>> getLoadedMethods() throws Exception {
        List<Map<String, String>> loadedMethods = new ArrayList<>();
        List<Class> classes = getAllLoadedModifiableClasses();

        for (Class clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            Arrays.sort(methods, methodComparator);
            for (Method m : methods) {
                if (!hasMethodBody(m)) continue;

                PlayGround pg = new PlayGround(getMethodFullName(m));
                Map<String, String> loadedMethod = new HashMap<>();
                loadedMethod.put("classFullName", pg.classFullName);
                loadedMethod.put("methodFullName", pg.methodFullName);
                loadedMethod.put("methodLongName", pg.methodLongName);
                loadedMethod.put("returnType", m.getReturnType().getName());

                loadedMethods.add(loadedMethod);
            }
        }

        return loadedMethods;
    }

    private String getMethodFullName(Method m) {
        StringBuilder builder = new StringBuilder();
        builder.append(m.getDeclaringClass().getCanonicalName());
        builder.append('.');
        builder.append(m.getName());
        builder.append('(');
        boolean isFirstParam = true;
        for (Class paramClass : m.getParameterTypes()) {
            if (!isFirstParam) builder.append(',');
            builder.append(paramClass.getCanonicalName());
            isFirstParam = false;
        }
        builder.append(')');

        return builder.toString();
    }

    public boolean hasMethodBody(Method method) {
        int flags = method.getModifiers();
        return (flags & Modifier.NATIVE) == 0
                && (flags & Modifier.ABSTRACT) == 0;
    }

    private List<Class> getAllLoadedModifiableClasses() throws Exception {
        List<Class> modifiableClasses = new ArrayList<>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, classComparator);

        for (Class clazz : classes) {
            String packageName = (clazz.getPackage() == null) ? "" : clazz.getPackage().getName();

            if (inst.isModifiableClass(clazz) &&
                    this.isClassTypeSupported(clazz) &&
                    options.packageAllowed(packageName)) {

                modifiableClasses.add(clazz);
            }
        }

        return modifiableClasses;
    }

    private boolean isClassTypeSupported(Class clazz) {
        return !clazz.isInterface()
                && !clazz.isAnnotation()
                && !clazz.isArray();
    }

    public List<Map<String, Object>> getTraceLogs() {
        Iterator<TraceLog> it = TraceKeeper.copyTraceLogs().iterator();
        List<Map<String, Object>> listOfLogs = new ArrayList<>();

        while (it.hasNext()) {
            try {
                TraceLog traceLog = it.next();

                Map<String, Object> map = new HashMap<>();
                map.put("when", formatDate(traceLog.when));
                map.put("tracePoint", traceLog.tracePoint.toString());
                map.put("classFullName", traceLog.pg.classFullName);
                map.put("methodShortName", traceLog.pg.methodShortName);
                map.put("uuid", traceLog.uuid);
                map.put("threadId", traceLog.threadId);
                map.put("threadName", traceLog.threadName);
                map.put("arguments", traceLog.arguments);
                map.put("returnedValue", traceLog.returnedValue);
                map.put("exceptionStackTrace", traceLog.exceptionStackTrace);
                map.put("elapsed", traceLog.elapsed);
                map.put("argumentsCount", traceLog.argumentsCount);

                listOfLogs.add(map);
            } catch(ConcurrentModificationException e) {}
        }

        return listOfLogs;
    }

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static String formatDate(Date when) {
        return formatter.format(when);
    }

}
