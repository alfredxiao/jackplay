package jackplay.play.performers;

import jackplay.play.Composer;
import jackplay.play.Theatre;
import jackplay.play.ProgramManager;
import jackplay.play.domain.Genre;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LeadPerformer implements ClassFileTransformer {
    Composer composer;
    ProgramManager pm;
    private Class classToPlay;
    private List<Exception> exceptionsDuringPerformance;

    public void init(Theatre theatre) {
        this.composer = theatre.getComposer();
        this.pm = theatre.getProgramManager();
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String clsName = classBeingRedefined.getName();
        if (classBeingRedefined != classToPlay) {
            return classfileBuffer;
        } else {
            byte[] byteCode = classfileBuffer;

            List<Performer> allPerformers = findAllPerformers(clsName);

            try {
                List<Exception> exceptions = new LinkedList<Exception>();
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(clsName);
                for (Performer performer : allPerformers) {
                    try {
                        cc = performer.perform(cc);
                    } catch (Exception e) {
                        // markdown what exception happened and continue with next performer
                        exceptions.add(e);
                        // tell program manager to remove this performer
                        if (performer instanceof  RedefinePerformer) {
                            RedefinePerformer redefPerf = (RedefinePerformer) performer;
                            pm.removeRedefinition(clsName, redefPerf.methodFullName);
                        }
                    }
                }
                this.setExceptionsDuringPerformance(exceptions);

                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return byteCode;
        }
    }

    private List<Performer> findAllPerformers(String clsName) {
        List<Performer> allPerformers = new LinkedList<Performer>();

        Collection<Performer> redefiningPerformers = pm.findPerformers(Genre.METHOD_REDEFINE, clsName);
        if (redefiningPerformers != null) allPerformers.addAll(redefiningPerformers);

        Collection<Performer> tracingPerformers = pm.findPerformers(Genre.METHOD_LOGGING, clsName);
        if (tracingPerformers != null) allPerformers.addAll(tracingPerformers);

        return allPerformers;
    }

    public void setClassToPlay(Class classToPlay) {
        this.classToPlay = classToPlay;
    }

    public void setExceptionsDuringPerformance(List<Exception> exceptionsDuringPerformance) {
        this.exceptionsDuringPerformance = exceptionsDuringPerformance;
    }

    public List<Exception> getExceptionsDuringPerformance() {
        return this.exceptionsDuringPerformance;
    }
}
