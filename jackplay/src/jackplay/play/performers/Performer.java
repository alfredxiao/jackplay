package jackplay.play.performers;


import jackplay.bootstrap.PlayGround;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;
import jackplay.javassist.NotFoundException;
import jackplay.play.PlayException;

import static jackplay.javassist.bytecode.AccessFlag.ABSTRACT;
import static jackplay.javassist.bytecode.AccessFlag.NATIVE;

public interface Performer {
    CtClass perform(CtClass aClass) throws Exception;

    default CtMethod findMethod(CtClass ctClass, PlayGround pg) throws Exception {
        CtMethod[] methods;
        methods = ctClass.getDeclaredMethods(pg.methodShortName);
        for (CtMethod m : methods) {
            if (m.getLongName().equals(pg.methodFullName)) {

                int flags = m.getMethodInfo().getAccessFlags();

                if ((flags & NATIVE) == NATIVE
                        || (flags & ABSTRACT) == ABSTRACT) {
                    throw new PlayException("Cannot trace native or abstract method!");
                }

                return m;
            }
        }

        throw new PlayException("Cannot find method:" + pg.methodFullName);
    }
}
