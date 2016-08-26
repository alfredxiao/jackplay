package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.PlayGround;
import jackplay.javassist.CtClass;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtMethod;


public class RedefinePerformer implements jackplay.play.performers.Performer {
    private final PlayGround playGround;
    private final String newBody;

    public RedefinePerformer(PlayGround playGround, String newBody) {
        this.playGround = playGround;
        this.newBody = newBody;
    }

    @Override
    public CtClass perform(ClassPool cp, CtClass aClass, String mode) throws Exception {
        Logger.debug("redefinePerformer", "[" + mode + "] starts redefining method:" + playGround.methodFullName);

        CtMethod method = this.findMethod(aClass, playGround);

        method.setBody(newBody);
        Logger.info("redefinePerformer", "finished redefining method:" + playGround.methodFullName);

        return aClass;
    }

    public String getNewBody() {
        return newBody;
    }
}
