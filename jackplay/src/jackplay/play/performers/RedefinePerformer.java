package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.PlayGround;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;

import static jackplay.javassist.bytecode.AccessFlag.NATIVE;

public class RedefinePerformer implements jackplay.play.performers.Performer {
    String methodFullName;
    String methodShortName;
    String newSource;

    public RedefinePerformer(PlayGround playGround, String newSource) {
        this.methodFullName = playGround.methodFullName;
        this.methodShortName = playGround.methodShortName;
        this.newSource = newSource;
    }

    @Override
    public CtClass perform(CtClass aClass) throws Exception {
        Logger.debug("redefining method:" + methodFullName);
        CtMethod method = findMethodByLongName(aClass);
        if ((method.getMethodInfo().getAccessFlags() & NATIVE) == NATIVE) {
            throw new Exception("Cannot redefine native method!");
        }

        method.setBody(newSource);

        return aClass;
    }

    private CtMethod findMethodByLongName(CtClass aClass) throws Exception {
        CtMethod[] methods = aClass.getDeclaredMethods(methodShortName);
        for (CtMethod m : methods) {
            if (m.getLongName().equals(methodFullName)) {
                return m;
            }
        }

        throw new RuntimeException("method " + methodFullName + " not found in class " + aClass.getName());
    }
}
