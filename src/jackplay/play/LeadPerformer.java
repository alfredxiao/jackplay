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

    public LeadPerformer(Composer composer) {
        this.composer = composer;
    }

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;
        List<Performer> performers = composer.findPerformers(className);
        JackLogger.debug("className asked to transform:" + className);
        JackLogger.debug(("performers:" + performers));

        try {
            JackLogger.debug("size of bytecode before transform:" + byteCode.length);

            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.get(className);
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
