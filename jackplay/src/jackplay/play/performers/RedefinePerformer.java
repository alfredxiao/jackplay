package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.PlayGround;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;
import jackplay.play.InfoCenter;

import static jackplay.javassist.bytecode.AccessFlag.NATIVE;

public class RedefinePerformer implements jackplay.play.performers.Performer {
    private final PlayGround playGround;
    private final String newSource;

    public RedefinePerformer(PlayGround playGround, String newSource) {
        this.playGround = playGround;
        this.newSource = newSource;
    }

    @Override
    public CtClass perform(CtClass aClass) throws Exception {
        Logger.debug("performing redefinition for method:" + playGround.methodFullName);

        CtMethod method = InfoCenter.locateMethod(playGround, playGround.methodFullName, playGround.methodShortName);
        if ((method.getMethodInfo().getAccessFlags() & NATIVE) == NATIVE) {
            throw new Exception("Cannot redefine native method!");
        }

        method.setBody(newSource);

        return aClass;
    }

    public PlayGround getPlayGround() {
        return playGround;
    }
}
