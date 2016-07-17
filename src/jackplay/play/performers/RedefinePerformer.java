package jackplay.play.performers;

import jackplay.Logger;
import jackplay.play.domain.PlayGround;
import javassist.CtClass;
import javassist.CtMethod;

public class RedefinePerformer implements jackplay.play.performers.Performer {
    String methodLongName;
    String methodShortName;
    String newSource;

    public RedefinePerformer(PlayGround playGround, String newSource) {
        this.methodLongName = playGround.methodLongName;
        this.methodShortName = playGround.methodShortName;
        this.newSource = newSource;
    }

    @Override
    public CtClass perform(CtClass aClass) throws Exception {
        Logger.debug("redefining method:" + methodLongName);
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
