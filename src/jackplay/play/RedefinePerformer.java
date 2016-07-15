package jackplay.play;

import jackplay.JackLogger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class RedefinePerformer implements Performer {
    String methodLongName;
    String methodShortName;
    String newSource;

    public RedefinePerformer(PlayGround playGround, String newSource) {
        this.methodLongName = playGround.methodLongName;
        this.methodShortName = playGround.methodShortName;
        this.newSource = newSource;
    }

    @Override
    public CtClass play(CtClass aClass) throws Exception {
        JackLogger.debug("redefining method:" + methodLongName);
        CtMethod method = findMethodByLongName(aClass);

        method.setBody(newSource);

        return aClass;
    }

    private CtMethod findMethodByLongName(CtClass aClass) throws Exception {
        CtMethod[] methods = aClass.getDeclaredMethods(methodShortName);
        for (CtMethod m : methods) {
            if (m.getLongName().equals(methodLongName)) {
                return m;
            }
        }

        throw new RuntimeException("method " + methodLongName + " not found in class " + aClass.getName());
    }
}
