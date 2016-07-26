package jackplay.play;

import jackplay.play.domain.Genre;
import jackplay.play.domain.PlayGround;
import jackplay.play.performers.Performer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.instrument.Instrumentation;
import java.util.*;

public class InformationCenter {
    static ProgramManager pm;
    final static ClassComparator classComparator = new ClassComparator();
    final static MethodComparator methodComparator = new MethodComparator();

    private static List<CtClass> modifiableClasses(Instrumentation inst) throws Exception {
        List<CtClass> modifiableClasses = new ArrayList<CtClass>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, classComparator);

        ClassPool cp = ClassPool.getDefault();

        for (Class clazz : classes) {
            if (inst.isModifiableClass(clazz) && isClassOfInterest(clazz)) {

                try {
                    CtClass cc = cp.get(clazz.getName());
                    modifiableClasses.add(cc);
                } catch(NotFoundException nfe) {
                }
            }
        }

        return modifiableClasses;
    }

    private static boolean isClassOfInterest(Class clazz) {
        return !clazz.isInterface()
                && !clazz.isAnnotation()
                && !clazz.isArray()
//                && !clazz.getName().startsWith("java.")
//                && !clazz.getName().startsWith("jdk.internal.")
//                && !clazz.getName().startsWith("sun.")
//                && !clazz.getName().startsWith("com.sun.")
//                && !clazz.getName().startsWith("javassist.")
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

                PlayGround pg = new PlayGround(m.getLongName());
                builder.append("{");
                builder.append("\"classFullName\":\"").append(pg.classFullName).append("\"").append(",");
                builder.append("\"methodLongName\":\"").append(pg.methodLongName).append("\"").append(",");
                builder.append("\"methodFullName\":\"").append(pg.methodFullName).append("\"").append(",");
                builder.append("\"returnType\":\"").append(m.getReturnType().getName()).append("\"");
                builder.append("}");
                isFirst = false;
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static String programAsJson(Instrumentation inst) {
        return ObjectAsJson(pm.program);
    }

    private static String ObjectAsJson(Map<Genre, Map<String, Map<String, Performer>>> program) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        Set<Genre> genres = program.keySet();
        boolean isFirstGenre = true;
        for (Genre genre : genres) {
            if (!isFirstGenre) builder.append(",");
            builder.append("\"").append(genre.toString()).append("\":{");

            Map<String, Map<String, Performer>> classMap = program.get(genre);
            Set<String> classes = classMap.keySet();
            boolean isFirstClass = true;
            for (String cls : classes) {
                if (!isFirstClass) builder.append(",");
                builder.append("\"").append(cls).append("\":{");

                Map<String, Performer> methodMap = classMap.get(cls);
                Set<String> methods = methodMap.keySet();
                boolean isFirstMethod = true;
                for (String m : methods) {
                    if (!isFirstMethod) builder.append(",");
                    builder.append("\"").append(m).append("\":");
                    builder.append("\"").append(methodMap.get(m)).append("\"");
                    isFirstMethod = false;
                }
                builder.append("}");
                isFirstClass = false;
            }

            builder.append("}");
            isFirstGenre = false;
        }

        builder.append("}");
        return builder.toString();
    }

    public static void init(ProgramManager pm1) {
        pm = pm1;
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