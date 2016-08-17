package jackplay.play.performers;

import jackplay.Logger;
import jackplay.play.Composer;
import jackplay.play.ProgramManager;
import jackplay.bootstrap.Genre;
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
    private Class classToRetransform;
    private List<Exception> exceptionsDuringPerformance;

    public void init(Composer composer, ProgramManager pm) {
        this.composer = composer;
        this.pm = pm;
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if (null == classBeingRedefined) return classfileBuffer;

        String clsName = classBeingRedefined.getName();
        if (!clsName.equals(classToRetransform.getName())) {
            return classfileBuffer;
        } else {
            byte[] byteCode = classfileBuffer;

            List<Performer> allPerformers = findAllPerformers(clsName);
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = null;

            try {
                cc =  cp.get(clsName);
                List<Exception> exceptions = new LinkedList<>();

                Logger.debug("leadperformer transforming class:" + clsName);
                for (Performer performer : allPerformers) {
                    try {
                        cc = performer.perform(cc);
                    } catch (Exception e) {
                        // markdown what exception happened and continue with next performer
                        exceptions.add(e);
                        // tell program manager to remove this bad performer
                        if (performer instanceof  RedefinePerformer) {
                            RedefinePerformer redefPerformer = (RedefinePerformer) performer;
                            pm.removeMethodRedefinition(clsName, redefPerformer.getPlayGround().methodFullName);
                        }
                    }
                }
                this.setExceptionsDuringPerformance(exceptions);

                byteCode = cc.toBytecode();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (cc != null) cc.detach();
            }

            return byteCode;
        }
    }

    private List<Performer> findAllPerformers(String clsName) {
        List<Performer> allPerformers = new LinkedList<Performer>();

        Collection<Performer> redefiningPerformers = pm.findPerformers(Genre.METHOD_REDEFINE, clsName);
        if (redefiningPerformers != null) allPerformers.addAll(redefiningPerformers);

        Collection<Performer> tracingPerformers = pm.findPerformers(Genre.METHOD_TRACE, clsName);
        if (tracingPerformers != null) allPerformers.addAll(tracingPerformers);

        return allPerformers;
    }

    public void setClassToRetransform(Class classToPlay) {
        this.classToRetransform = classToPlay;
    }

    public void setExceptionsDuringPerformance(List<Exception> exceptionsDuringPerformance) {
        this.exceptionsDuringPerformance = exceptionsDuringPerformance;
    }

    public List<Exception> getExceptionsDuringPerformance() {
        return this.exceptionsDuringPerformance;
    }
}
