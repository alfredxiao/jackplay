package jackplay.play.performers;

import jackplay.Logger;
import jackplay.play.Composer;
import jackplay.play.Performer;
import javassist.ClassPool;
import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;

public class LeadPerformer implements ClassFileTransformer {
    Composer composer;
    private Class classToPlay;
    private List<Exception> exceptionsDuringPerformance;

    public LeadPerformer(Composer composer) {
        this.composer = composer;
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String clsName = classBeingRedefined.getName();
        Logger.debug("Composer said class to perform:" + classToPlay.getName());
        Logger.debug(".transform() is called with class:" + clsName);
        if (classBeingRedefined != classToPlay) {
            Logger.debug("ignore a class not of interest");
            return classfileBuffer;
        } else {
            byte[] byteCode = classfileBuffer;
            List<Performer> performers = composer.findPerformers(clsName);
            Logger.debug(("performers:" + performers));

            try {
                Logger.debug("size of bytecode before transform:" + byteCode.length);

                List<Exception> exceptions = new LinkedList<Exception>();
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(clsName);
                for (Performer performer : performers) {
                    try {
                        cc = performer.perform(cc);
                    } catch (Exception e) {
                        // markdown what exception happened and continue with next performer
                        exceptions.add(e);
                        // tell program manager to remove this performer
                    }
                }
                this.setExceptionsDuringPerformance(exceptions);

                byteCode = cc.toBytecode();
                cc.detach();

                Logger.debug("size of bytecode after transform:" + byteCode.length);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return byteCode;
        }
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
