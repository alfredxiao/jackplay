package jackplay.play;

import jackplay.JackLogger;
import javassist.CtClass;
import javassist.CtMethod;

public class LoggingPerformer implements Performer {
    String methodName;

    public LoggingPerformer(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public CtClass play(CtClass aClass) throws Exception {
        CtMethod m = aClass.getDeclaredMethod(methodName);
        JackLogger.debug("running transform for class " + aClass.getSimpleName() + "." + m.getName());
        m.addLocalVariable("elapsedTime", CtClass.longType);
        m.insertBefore("elapsedTime = System.currentTimeMillis();" +
                "System.out.println(\">> Entering " + m.getName() + "()\");");
        m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
                + "System.out.println(\"<< Quiting " + m.getName() + "()\\n * elapsedTime(ms): \" + elapsedTime);}");
        return aClass;
    }
}
