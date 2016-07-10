package jackplay.play;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InformationCenter {
    final static ClassComparator classComparator = new ClassComparator();
    final static MethodComparator methodComparator = new MethodComparator();

    private static List<CtClass> modifiableClasses(Instrumentation inst) throws Exception {
        List<CtClass> modifiableClasses = new ArrayList<CtClass>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, classComparator);

        ClassPool cp = ClassPool.getDefault();

        for (Class clazz : classes) {
            if (inst.isModifiableClass(clazz) && isClassOfInterest(clazz)) {

                CtClass cc = cp.get(clazz.getName());
                modifiableClasses.add(cc);
            }
        }

        return modifiableClasses;
    }

    private static boolean isClassOfInterest(Class clazz) {
        return !clazz.isInterface()
                && !clazz.isAnnotation()
                && !clazz.isArray()
                && !clazz.getName().startsWith("java.")
                && !clazz.getName().startsWith("jdk.internal.")
                && !clazz.getName().startsWith("sun.")
                && !clazz.getName().startsWith("com.sun.")
                && !clazz.getName().startsWith("javassist.")
                && !clazz.getName().startsWith("jackplay.");
    }

    public static String loadedClassesAsJson(Instrumentation inst) throws Exception {
        List<CtClass> classes = modifiableClasses(inst);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;

        for (CtClass clazz : classes) {
            CtMethod[] methods = clazz.getDeclaredMethods();
            Arrays.sort(methods, methodComparator);
            for (CtMethod m : methods) {
                if (!isFirst) builder.append(',');
                builder.append("{\"targetName\":\"").append(m.getLongName()).append("\"}");
            }

            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }
}

class ClassComparator implements Comparator<Class> {

    @Override
    public int compare(Class o1, Class o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

class MethodComparator implements Comparator<CtMethod> {

    @Override
    public int compare(CtMethod o1, CtMethod o2) {
        return o1.getLongName().compareTo(o2.getLongName());
    }
}