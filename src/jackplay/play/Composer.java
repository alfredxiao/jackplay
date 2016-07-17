package jackplay.play;

import jackplay.Logger;
import jackplay.Options;
import jackplay.play.domain.Genre;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

// singleton
public class Composer {
    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;

    public void init(Opera opera) {
        this.inst = opera.getInstrumentation();
        this.options = opera.getOptions();
        this.pm = opera.getProgramManager();
        this.leadPerformer = opera.getLeadPerformer();
        Logger.debug("add transfomerf....l..");
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
                    Logger.log("can't verify a class, will reset its method body (while keep tracing if any)");
                    UndoClassRedefinition(c);
                    // todo: remove redefine performers of this class
                }

                Logger.debug("leader:" + leadPerformer);
                Logger.debug("getExceptionsDuringPerformance:" + leadPerformer.getExceptionsDuringPerformance());
                if (!leadPerformer.getExceptionsDuringPerformance().isEmpty()) {
                    throw new Exception("error in performing class redefinition", leadPerformer.getExceptionsDuringPerformance().get(0));
                }
            } else {
                throw new Exception("class not modifiable:" + className);
            }
        }
    }

    private void UndoClassRedefinition(Class c) throws UnmodifiableClassException {
        pm.removeRedefinePerformers(c.getName());
        inst.retransformClasses(c);
    }

}
