package jackplay.play;

import jackplay.bootstrap.Genre;
import static jackplay.bootstrap.Genre.*;
import static jackplay.play.MetadataFailureCause.ReferencedClassDefFoundError;
import static jackplay.play.MetadataFailureCause.Unknown;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.PlayGround;
import jackplay.play.performers.Performer;
import jackplay.play.performers.RedefinePerformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class InfoCenter {

    Instrumentation inst;
    ProgramManager pm;
    final static ClassComparator classComparator = new ClassComparator();
    final static MethodComparator methodComparator = new MethodComparator();
    private Options options;
    private Map<String, MetadataFailureCause> classesFailedMetadataLoading = new ConcurrentHashMap<>();

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

    public List<Class> findLoadedModifiableClass(String className) {
        List<Class> classes = this.getAllLoadedModifiableClasses();
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
                        if (!parameterTypeMatches(pg.parameterList.get(i), paramClasses[i])) {
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

    private boolean parameterTypeMatches(String parameterType, Class paramClass) {
        return parameterType.equals(paramClass.getCanonicalName())
                || parameterType.equals(paramClass.getName());
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

    public List<Map<String, String>> getAllLoadedMethods() throws Exception {
        List<Map<String, String>> loadedMethods = new ArrayList<>();
        List<Class> classes = getAllLoadedModifiableClasses();

        for (Class clazz : classes) {
            try {
                loadedMethods.addAll(getAllLoadedMethods(clazz));
            } catch(NoClassDefFoundError ncdf) {
                if (!classesFailedMetadataLoading.containsKey(clazz.getName())) {
                    classesFailedMetadataLoading.put(clazz.getName(), ReferencedClassDefFoundError);
                }
            } catch(Throwable t) {
                if (!classesFailedMetadataLoading.containsKey(clazz.getName())) {
                    classesFailedMetadataLoading.put(clazz.getName(), Unknown);
                }
            }
        }

        return loadedMethods;
    }

    private List<Map<String, String>> getAllLoadedMethods(Class clazz) {

        List<Map<String, String>> loadedMethods = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, methodComparator);
        for (Method m : methods) {
            if (!hasMethodBody(m)) continue;

            PlayGround pg = new PlayGround(getMethodFullName(m));
            Map<String, String> loadedMethod = new HashMap<>();
            loadedMethod.put("classFullName", pg.classFullName);
            loadedMethod.put("methodFullName", pg.methodFullName);
            loadedMethod.put("methodLongName", pg.methodLongName);
            loadedMethod.put("returnType", getFriendlyClassName(m.getReturnType()));
            RedefinePerformer performer = (RedefinePerformer) pm.existingPerformer(METHOD_REDEFINE,
                    pg.classFullName,
                    pg.methodFullName);
            if (performer != null) {
                loadedMethod.put("newBody", performer.getNewBody());
            }

            loadedMethods.add(loadedMethod);
        }

        return loadedMethods;
    }

    private String getMethodFullName(Method m) {
        StringBuilder builder = new StringBuilder();
        builder.append(m.getDeclaringClass().getName());
        builder.append('.');
        builder.append(m.getName());
        builder.append('(');
        boolean isFirstParam = true;
        for (Class paramClass : m.getParameterTypes()) {
            if (!isFirstParam) builder.append(',');
            builder.append(getFriendlyClassName(paramClass));
            isFirstParam = false;
        }
        builder.append(')');

        return builder.toString();
    }

    private String getFriendlyClassName(Class clazz) {
        String canonicalName = clazz.getCanonicalName();
        return canonicalName == null ? clazz.getName() : canonicalName;
    }

    public boolean hasMethodBody(Method method) {
        int flags = method.getModifiers();
        return !Modifier.isNative(flags) && !Modifier.isAbstract(flags);
    }

    private List<Class> getAllLoadedModifiableClasses() {
        List<Class> modifiableClasses = new ArrayList<>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, classComparator);

        for (Class clazz : classes) {
            String packageName = (clazz.getPackage() == null) ? "" : clazz.getPackage().getName();

            if (inst.isModifiableClass(clazz) &&
                    this.isClassTypeAccessible(clazz) &&
                    this.isClassTypeSupported(clazz) &&
                    options.packageAllowed(packageName)) {

                modifiableClasses.add(clazz);
            }
        }

        return modifiableClasses;
    }

    private boolean isClassTypeAccessible(Class clazz) {
        return !Modifier.isPrivate(clazz.getModifiers());
    }

    private boolean isClassTypeSupported(Class clazz) {
        return !clazz.isInterface()
                && !clazz.isAnnotation()
                && !clazz.isArray();
    }

    public Map<Genre, Map<String, Map<String, Performer>>> getCurrentProgram() {
        return pm.copyOfCurrentProgram();
    }

}

enum MetadataFailureCause {
//    PackageIsBlocked,
//    ClassIsInterface,
//    ClassIsAnnotation,
//    ClassIsArray,
//    ClassIsPrivate,
//    ClassNotModifiable,
    ReferencedClassDefFoundError,
    Unknown
}