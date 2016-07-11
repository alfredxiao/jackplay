package jackplay.play;

import jackplay.JackLogger;
import javassist.ClassPool;
import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

public class LeadPerformer implements ClassFileTransformer {
    Composer composer;
    private Class classToPlay;

    public LeadPerformer(Composer composer) {
        this.composer = composer;
    }

    public byte[] transform(ClassLoader loader, String classNameWithSlash, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String clsName = classBeingRedefined.getName();
        JackLogger.debug("Composer said class to play:" + classToPlay.getName());
        JackLogger.debug(".transform() is called with class:" + clsName);
        if (classBeingRedefined != classToPlay) {
            JackLogger.debug("ignore a class not of interest");
            return classfileBuffer;
        } else {
            byte[] byteCode = classfileBuffer;
            List<Performer> performers = composer.findPerformers(clsName);
            JackLogger.debug(("performers:" + performers));

            try {
                JackLogger.debug("size of bytecode before transform:" + byteCode.length);

                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(clsName);
                for (Performer performer : performers) {
                    cc = performer.play(cc);
                }

                byteCode = cc.toBytecode();
                cc.detach();

                JackLogger.debug("size of bytecode after transform:" + byteCode.length);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return byteCode;
        }
    }

    public void setClassToPlay(Class classToPlay) {
        this.classToPlay = classToPlay;
    }
}
