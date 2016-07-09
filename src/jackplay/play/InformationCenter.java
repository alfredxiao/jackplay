package jackplay.play;

import java.lang.instrument.Instrumentation;

public class InformationCenter {
    public static String loadedClassesAsJson(Instrumentation inst) {
        Class[] classes = inst.getAllLoadedClasses();
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        for (Class clazz : classes) {
            if (!isFirst) builder.append(',');
            builder.append(clazz.getName());
            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }
}
