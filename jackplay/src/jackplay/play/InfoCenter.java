package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import static jackplay.play.MetadataFailureCause.ReferencedClassDefFoundError;
import static jackplay.play.MetadataFailureCause.Unknown;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.PlayGround;
import jackplay.play.performers.Performer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InfoCenter {

    private final static Comparator<Class> CLASS_COMPARATOR = new ClassComparatorByName();
    private final static Comparator<Method> METHOD_COMPARATOR = new MethodComparatorByName();
    private Instrumentation inst;
    private ProgramManager pm;
    private Options options;
    private Map<String, MetadataFailureCause> metadataInaccessibleClasses = new ConcurrentHashMap<>();

    public void init(Instrumentation inst, ProgramManager pm, Options options) {
        this.inst = inst;
        this.pm = pm;
        this.options = options;
    }

    public Map<String, Object> getConfigurableOptions() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("traceLogLimit", options.traceLogLimit());
        settings.put("autoSuggestLimit", options.autoSuggestLimit());
        settings.put("intervalSyncTraceLogs", options.intervalSyncTraceLogs());
        settings.put("intervalSyncModifiableMethods", options.intervalSyncModifiableMethods());

        return settings;
    }

    public void configOption(String key, Object value) {
        options.updateOption(key, value.toString());
    }

    public List<Class> findLoadedModifiableClasses(String className) {
        return this.allModifiableClasses().stream()
                .filter(clz -> clz.getName().equals(className)).collect(Collectors.toList());
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

    public Map<String, Map<String, String>> allModifiableMethods() throws Exception {
        List<Class> classes = allModifiableClasses();

        Map<String, Map<String, String>> loadedMethods = new TreeMap<>();

        for (Class clazz : classes) {
            try {
                loadedMethods.put(clazz.getName(), modifiableMethodsInClass(clazz));
            } catch(NoClassDefFoundError ncdf) {
                if (!metadataInaccessibleClasses.containsKey(clazz.getName())) {
                    metadataInaccessibleClasses.put(clazz.getName(), ReferencedClassDefFoundError);
                    Logger.error("infocenter", ncdf);
                }
            } catch(Throwable t) {
                if (!metadataInaccessibleClasses.containsKey(clazz.getName())) {
                    metadataInaccessibleClasses.put(clazz.getName(), Unknown);
                    Logger.error("infocenter", t);
                }
            }
        }

        return loadedMethods;
    }

    private Map<String, String> modifiableMethodsInClass(Class clazz) {

        Map<String, String> loadedMethods = new TreeMap<>();

        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, METHOD_COMPARATOR);

        for (Method m : methods) {
            if (!hasMethodBody(m)) continue;

            PlayGround pg = new PlayGround(getMethodFullName(m));
            loadedMethods.put(pg.methodShortNameWithSignature, getFriendlyClassName(m.getReturnType()));
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

    private List<Class> allModifiableClasses() {
        List<Class> modifiableClasses = new ArrayList<>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, CLASS_COMPARATOR);

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
        try {
            clazz.getName();
            clazz.getCanonicalName();
            return true;
        } catch(Throwable t) {
            return false;
        }
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

class ClassComparatorByName implements Comparator<Class> {
    public int compare(Class o1, Class o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

class MethodComparatorByName implements Comparator<Method> {
    public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
