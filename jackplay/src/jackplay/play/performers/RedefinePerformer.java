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
    public CtClass perform(CtClass aClass, String mode) throws Exception {
        Logger.debug("redefinePerformer", "[" + mode + "] starts redefining method:" + playGround.methodFullName);

        CtMethod method = this.findMethod(aClass, playGround);

        method.setBody(newSource);
        Logger.info("redefinePerformer", "finished redefining method:" + playGround.methodFullName);

        return aClass;
    }
}
