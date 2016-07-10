package jackplay.play;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class InformationCenter {
    private static List<Class> modifiableClasses(Instrumentation inst) {
        List<Class> modifiableClasses = new ArrayList<Class>();
        Class[] classes = inst.getAllLoadedClasses();
        for (Class clazz : classes) {
            if (inst.isModifiableClass(clazz)
                    && !clazz.isInterface()
                    && !clazz.isAnnotation()
                    && !clazz.isArray()
                    && !clazz.getName().startsWith("java.")
                    && !clazz.getName().startsWith("jdk.internal.")
                    && !clazz.getName().startsWith("sun.")
                    && !clazz.getName().startsWith("com.sun.")
                    && !clazz.getName().startsWith("javassist.")) {
                modifiableClasses.add(clazz);
            }
        }

        return modifiableClasses;
    }
    public static String loadedClassesAsJson(Instrumentation inst) {
        List<Class> classes = modifiableClasses(inst);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        for (Class clazz : classes) {
            if (!isFirst) builder.append(',');

            builder.append("{\"targetName\":\"").append(clazz.getName()).append("\"}");
            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }
}
