package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import jackplay.play.ProgramManager;
import static jackplay.bootstrap.Genre.*;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

public class LeadPerformer implements ClassFileTransformer {
    ProgramManager pm;
    ClassPool cp;

    public void init(ProgramManager pm) {
        this.pm = pm;
        this.cp = ClassPool.getDefault();
    }

    private CtClass performAgenda(Map<String, Performer> performerMap, CtClass cc) throws Exception {
        CtClass beingPlayed = cc;

        if (performerMap != null) {
            for (Performer performer : performerMap.values()) {
                beingPlayed = performer.perform(beingPlayed);
            }
        }

        return beingPlayed;
    }

    private boolean isAgendaEmpty(Map<Genre, Map<String, Performer>> agenda) {
        return agenda == null || ((agenda.get(METHOD_TRACE) == null || agenda.get(METHOD_TRACE).isEmpty())
                && (agenda.get(METHOD_REDEFINE) == null || agenda.get(METHOD_REDEFINE).isEmpty()));
    }

    public void rehearsal(Class clazz) throws Exception {
        String className = clazz.getCanonicalName();
        Map<Genre, Map<String, Performer>> agenda = pm.agendaForClass(className);

        if (!isAgendaEmpty(agenda)) {
            Logger.debug("leadPerformer starting rehearsal on class:" + className);
            CtClass cc = null;

            try {
                cc = performAsPerAgenda(className, agenda);
                Logger.debug("leadPerformer finished rehearsal on class:" + className);
            } finally {
                if (cc != null) cc.detach();
            }
        }
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String className = classNameWithSlash.replace('/', '.');
        Map<Genre, Map<String, Performer>> agenda = pm.agendaForClass(className);

        if (isAgendaEmpty(agenda)) {
            if (classBeingRedefined == null) {
                return classfileBuffer;
            } else {
                CtClass cc = null;
                try {
                    cc = cp.get(className);
                    return cc.toBytecode();
                } catch(Exception e) {
                    Logger.error("leadPerfomer", e);
                    return classfileBuffer;
                } finally {
                    if (cc != null) cc.detach();
                }

            }
        } else {
            Logger.debug("leadPerformer found agenda for class:" + className);
            // this class is on the program menu
            byte[] byteCode;
            CtClass cc = null;

            try {
                Logger.debug("leadPerformer starting retransform class:" + className);
                cc = performAsPerAgenda(className, agenda);
                Logger.debug("leadPerformer finished retransform class:" + className);

                byteCode = cc.toBytecode();
            } catch(Exception e) {
                Logger.error("leadPerfomer", e);
                byteCode = classfileBuffer;
            } finally {
                if (cc != null) cc.detach();
            }

            return byteCode;
        }
    }

    private CtClass performAsPerAgenda(String className, Map<Genre, Map<String, Performer>> agenda) throws Exception {
        CtClass cc;
        cc = cp.get(className);

        cc = performAgenda(agenda.get(METHOD_REDEFINE), cc);
        cc = performAgenda(agenda.get(METHOD_TRACE), cc);
        return cc;
    }
}
