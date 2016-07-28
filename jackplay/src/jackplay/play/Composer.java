package jackplay.play;

import jackplay.Logger;
import jackplay.Options;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

// singleton
public class Composer {
    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;

    public void init(Theatre theatre) {
        this.inst = theatre.getInstrumentation();
        this.options = theatre.getOptions();
        this.pm = theatre.getProgramManager();
        this.leadPerformer = theatre.getLeadPerformer();
        this.inst.addTransformer(leadPerformer, true);
    }

    void performPlay(String className) throws Exception {
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
