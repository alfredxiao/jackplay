package jackplay.play;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MethodLoggingPlayer implements ClassFileTransformer {
    PlayUnit unit;

    public MethodLoggingPlayer(PlayUnit unit) {
        this.unit = unit;
    }

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        if (className.equals(unit.className)) {

            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(unit.className);
                CtMethod m = cc.getDeclaredMethod(unit.methodName);
                m.addLocalVariable("elapsedTime", CtClass.longType);
                m.insertBefore("elapsedTime = System.currentTimeMillis();" +
                        "System.out.println(\">> Entering " + m.getName() + "()\");");
                m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
                        + "System.out.println(\"<< Quiting " + m.getName() + "()\\n * elapsedTime(ms): \" + elapsedTime);}");
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return byteCode;
    }


}
