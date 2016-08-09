package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Options;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

// singleton
public class Composer {
    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;

    public void wireUp(Options options, Instrumentation inst, ProgramManager pm, LeadPerformer leadPerformer) {
        this.inst = inst;
        this.options = options;
        this.pm = pm;
        this.leadPerformer = leadPerformer;
        this.inst.addTransformer(this.leadPerformer, true);
    }

    synchronized void performPlay(String className) throws Exception {
        Class c = Class.forName(className);
        if (c.getName().equals(className)) {
            if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
                leadPerformer.setClassToPlay(c);
                try {
                    inst.retransformClasses(c);
                } catch(VerifyError ve) {
                    Logger.error(ve);
                    Logger.info("can't verify a class, will reset its method body (while keep tracing if any)");
                    UndoClassRedefinition(c);
                    // todo: remove redefine performers of this class
                }

                if (!leadPerformer.getExceptionsDuringPerformance().isEmpty()) {
                    // todo: get all errors and return to client
                    leadPerformer.getExceptionsDuringPerformance().get(0).printStackTrace();
                    throw new Exception("error in performing class redefinition: " + leadPerformer.getExceptionsDuringPerformance().get(0).getMessage());
                }
            } else {
                throw new Exception("class not modifiable:" + className);
            }
        }
    }

    private void UndoClassRedefinition(Class c) throws UnmodifiableClassException {
        pm.removeRedefinitions(c.getName());
        inst.retransformClasses(c);
    }

}
