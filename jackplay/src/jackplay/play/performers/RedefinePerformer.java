package jackplay.play.performers;

import jackplay.JackplayLogger;
import jackplay.bootstrap.Site;
import jackplay.javassist.CtClass;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtMethod;


public class RedefinePerformer implements jackplay.play.performers.Performer {
    private final Site site;
    private final String newBody;

    public RedefinePerformer(Site site, String newBody) {
        this.site = site;
        this.newBody = newBody;
    }

    @Override
    public CtClass perform(ClassPool cp, CtClass aClass, String mode) throws Exception {
        JackplayLogger.debug("redefinePerformer", "[" + mode + "] starts redefining method:" + site.methodFullName);

        CtMethod method = this.findMethod(aClass, site);

        method.setBody(newBody);
        JackplayLogger.info("redefinePerformer", "finished redefining method:" + site.methodFullName);

        return aClass;
    }

    public String getNewBody() {
        return newBody;
    }
}
