package jackplay;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MethodLoggingPlayer implements ClassFileTransformer {

    String targetClassName;
    String[] targetMethodNames;
    public MethodLoggingPlayer(String className, String[] methodNames) {
        System.out.println("transformer created!!!!");
        this.targetClassName = className;
        this.targetMethodNames = methodNames;
    }

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        // System.out.println(className);
        if (className.equals(this.targetClassName)) {

            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(targetClassName);
                for (CtMethod m : cc.getDeclaredMethods()) {
                    m.addLocalVariable("elapsedTime", CtClass.longType);
                    m.insertBefore("elapsedTime = System.currentTimeMillis();" +
                            "System.out.println(\">> Entering " + m.getName() + "()\");");
                    m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
                            + "System.out.println(\"<< Quiting " + m.getName() + "()\\n * elapsedTime(ms): \" + elapsedTime);}");
                }
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return byteCode;
    }


}
