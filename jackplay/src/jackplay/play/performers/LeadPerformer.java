package jackplay.play.performers;

import jackplay.bootstrap.Genre;
import jackplay.javassist.NotFoundException;
import jackplay.play.PlayException;
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

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String className = classNameWithSlash.replace('/', '.');
        Map<Genre, Map<String, Performer>> agenda = pm.agendaForClass(className);
        if (agenda.isEmpty()) {
            // ignore it
            return classfileBuffer;
        } else {
            // this class is on the program menu
            byte[] byteCode;
            CtClass cc = null;

            try {
                cc = cp.get(className);

                cc = performAgenda(agenda.get(METHOD_REDEFINE), cc);
                cc = performAgenda(agenda.get(METHOD_TRACE), cc);

                byteCode = cc.toBytecode();
            } catch(Exception e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                if (cc != null) cc.detach();
            }

            return byteCode;
        }
    }
}
