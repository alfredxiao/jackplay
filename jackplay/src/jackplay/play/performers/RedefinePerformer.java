package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.PlayGround;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;


public class RedefinePerformer implements jackplay.play.performers.Performer {
    private final PlayGround playGround;
    private final String newSource;

    public RedefinePerformer(PlayGround playGround, String newSource) {
        this.playGround = playGround;
        this.newSource = newSource;
    }

    @Override
    public CtClass perform(CtClass aClass) throws Exception {
        Logger.debug("redefinePerformer performing redefinition for method:" + playGround.methodFullName);

        CtMethod method = this.findMethod(aClass, playGround);

        method.setBody(newSource);
        Logger.debug("redefinePerformer performed redefinition for method:" + playGround.methodFullName);

        return aClass;
    }
}
