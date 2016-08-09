package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import jackplay.bootstrap.Options;
import jackplay.bootstrap.TraceKeeper;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;

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

    synchronized void defineClass(String className) throws Exception {
        Class clazz = Class.forName(className);
        if (inst.isModifiableClass(clazz) && inst.isRetransformClassesSupported()) {
            leadPerformer.setClassToPlay(clazz);
            try {
                inst.retransformClasses(clazz);

                if (!leadPerformer.getExceptionsDuringPerformance().isEmpty()) {
                    StringBuilder allMessage = new StringBuilder();
                    StringBuilder allStackTrace = new StringBuilder();
                    boolean isFirst = true;
                    for (Exception e : leadPerformer.getExceptionsDuringPerformance()) {
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
            } catch(VerifyError ve) {
                Logger.error(ve);
                String msg = "can't verify class" + className + ", will reset its method body (while keep tracing if any)";
                pm.removeClassFromProgram(Genre.METHOD_REDEFINE, className);
                inst.retransformClasses(clazz);
                throw new Exception(msg);
            }
        } else {
            throw new Exception("class not modifiable:" + className);
        }
    }
}
