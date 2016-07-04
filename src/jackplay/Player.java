package jackplay;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Player {
    JackOptions options;
    Instrumentation inst;

    public Player(JackOptions options, Instrumentation inst) {
        this.options = options;
        this.inst =  inst;
    }
    public void play(PlayCategory category, String className, String[] methodNames) throws Exception {
        // query existing transfomer
        // if need to add new transformer
        inst.addTransformer(new MethodLoggingPlayer(className, methodNames), true);
        Class c = Class.forName(className);
        if  (inst.isModifiableClass(c)) {
            inst.retransformClasses(c);
        } else {
            throw new Exception("class not modifiable:" + className);
        }
    }

}
