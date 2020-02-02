package jackplay.core.performers;

import jackplay.Logger;
import jackplay.model.Category;
import jackplay.javassist.NotFoundException;
import jackplay.core.Registry;
import static jackplay.model.Category.*;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

public class Transformer implements ClassFileTransformer {
    Registry pm;
    public boolean transformSuccess = true;
    public Throwable transformFailure = null;

    final static String STAGING_MODE = "STAGING";

    public Transformer(Registry pm) {
        this.pm = pm;
    }

    private CtClass performAgenda(ClassPool cp, Map<String, Performer> performerMap, CtClass cc, String mode) throws Exception {
        CtClass beingPlayed = cc;

        if (performerMap != null) {
            for (Performer performer : performerMap.values()) {
                beingPlayed = performer.perform(cp, beingPlayed, mode);
            }
        }

        return beingPlayed;
    }

    private boolean isAgendaEmpty(Map<Category, Map<String, Performer>> agenda) {
        return agenda == null || ((agenda.get(TRACE) == null || agenda.get(TRACE).isEmpty())
                && (agenda.get(REDEFINE) == null || agenda.get(REDEFINE).isEmpty()));
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String className = classNameWithSlash.replace('/', '.');
        Map<Category, Map<String, Performer>> agenda = pm.agendaForClass(className);

        ClassPool cp = new ClassPool(true);

        if (isAgendaEmpty(agenda)) {
            if (classBeingRedefined == null) {
                return null;
            } else {
                // we are here if we undo plays for a class
                CtClass cc = null;
                try {
                    cc = getCtClass(cp, classBeingRedefined, classfileBuffer);
                    this.transformSuccess = true;
                    return cc.toBytecode();
                } catch(Exception e) {
                    Logger.error("leadPerfomer", e);
                    this.transformSuccess = true;
                    return null;
                } finally {
                    if (cc != null) cc.detach();
                }
            }
        } else {
            Logger.debug("leadPerformer", "found agenda for class:" + className);
            // this class is on the program menu
            byte[] byteCode;
            CtClass cc = null;

            try {
                Logger.debug("leadPerformer", "starts retransform class:" + className);
                cc = performAsPerAgenda(cp, classBeingRedefined, classfileBuffer, agenda, STAGING_MODE);
                Logger.debug("leadPerformer", "finished retransform class:" + className);

                byteCode = cc.toBytecode();
                this.transformSuccess = true;
            } catch(Throwable t) {
                Logger.error("leadPerfomer", t);
                byteCode = null;
                this.transformSuccess = false;
                this.transformFailure = t;
            } finally {
                if (cc != null) cc.detach();
            }

            return byteCode;
        }
    }

    private CtClass performAsPerAgenda(ClassPool cp, Class clazz, byte[] bytes, Map<Category, Map<String, Performer>> agenda, String mode) throws Exception {
        CtClass cc = getCtClass(cp, clazz, bytes);

        cc = performAgenda(cp, agenda.get(REDEFINE), cc, mode);
        cc = performAgenda(cp, agenda.get(TRACE), cc, mode);
        return cc;
    }

    private CtClass getCtClass(ClassPool cp, Class clazz, byte[] bytes) throws Exception {
        try {
            if (clazz == null) {
                // class loaded the first time, not being redefined
                return cp.makeClass(new ByteArrayInputStream(bytes));
            } else {
                return cp.get(clazz.getName());
            }
        } catch(NotFoundException e) {
            // class loaded by a custom class loader
            // cp.appendClassPath(new ClassClassPath(clazz));
            // cp.insertClassPath(new LoaderClassPath(clazz.getClassLoader()));
            // above two lines does not work it work
            return cp.makeClass(new ByteArrayInputStream(bytes));
        }
    }
}
