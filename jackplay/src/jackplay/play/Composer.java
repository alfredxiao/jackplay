package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import jackplay.bootstrap.Options;
import jackplay.bootstrap.TraceKeeper;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;
import java.util.LinkedList;
import java.util.List;

// singleton
public class Composer {
    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;

    public void init(Options options, Instrumentation inst, ProgramManager pm, LeadPerformer leadPerformer) {
        this.inst = inst;
        this.options = options;
        this.pm = pm;
        this.leadPerformer = leadPerformer;
        this.inst.addTransformer(this.leadPerformer, true);
    }

    synchronized void retransformByClassName(String className) throws Exception {
        List<Class> classes = findClassesByName(className);

        if (classes.isEmpty()) {
            try {
                Class clazz = Class.forName(className);
                classes.add(clazz);
            } catch(ClassNotFoundException e) {
                Logger.error("Cannot retransform class " + className + ", because no Class found to be matching this name");
                return;
            }
        }

        boolean multipleClassesFound = classes.size() > 1;

        for (Class clazz : classes) {
            retransformClass(className, multipleClassesFound, clazz);
        }
    }

    private void retransformClass(String className, boolean multipleClassesFound, Class clazz) throws Exception {
        if (inst.isModifiableClass(clazz) && inst.isRetransformClassesSupported()) {
            leadPerformer.setClassToRetransform(clazz);
            try {
                String classloaderInfo = (multipleClassesFound ? " whose classloader is " + clazz.getClassLoader() : "");
                Logger.debug("composer attempting to retransform class:" + className + classloaderInfo);
                inst.retransformClasses(clazz);
                Logger.debug("composer finished retransforming class:" + className + classloaderInfo);

                List<Exception> exceptions = leadPerformer.getExceptionsDuringPerformance();
                if (!exceptions.isEmpty()) {
                    rethrowPerformerExceptions(className, exceptions);
                }
            } catch (VerifyError ve) {
                Logger.error(ve);
                String msg = "can't verify class" + className + ", will reset its method body (while keep tracing if any)";
                pm.removeClassFromProgram(Genre.METHOD_REDEFINE, className);
                inst.retransformClasses(clazz);
                throw new Exception(msg);
            } finally {
                leadPerformer.setClassToRetransform(null);
            }
        } else {
            throw new Exception("class not modifiable:" + className);
        }
    }

    private void rethrowPerformerExceptions(String className, List<Exception> exceptions) throws Exception {
        StringBuilder allMessage = new StringBuilder();
        StringBuilder allStackTrace = new StringBuilder();
        boolean isFirst = true;
        for (Exception e : exceptions) {
            if (!isFirst) allMessage.append(" | ");
            if (!isFirst) allStackTrace.append("\n\n");

            allMessage.append(e.getMessage());
            allStackTrace.append(TraceKeeper.throwableToString(e));

            isFirst = false;
        }

        String errorMessage = "error in performing redefinition for class " + className + ": " + allMessage.toString();
        Logger.error(errorMessage);
        Logger.error(allStackTrace.toString());
        throw new Exception(errorMessage);
    }

    private List<Class> findClassesByName(String className) {
        Class[] classes = this.inst.getAllLoadedClasses();
        List<Class> matched = new LinkedList<>();
        for (Class clz : classes) {
            if (clz.getName().equals(className)) {
                matched.add(clz);
            }
        }

        return matched;
    }
}
