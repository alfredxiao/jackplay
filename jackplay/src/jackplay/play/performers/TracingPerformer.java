package jackplay.play.performers;

import jackplay.Logger;
import jackplay.play.domain.PlayGround;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;

public class TracingPerformer implements Performer {
    String methodFullName;
    String methodShortName;
    PlayGround playGround;

    public TracingPerformer(PlayGround playGround) {
        this.playGround = playGround;
        this.methodFullName = playGround.methodFullName;
        this.methodShortName = playGround.methodShortName;
    }

    @Override
    public CtClass perform(CtClass aClass) throws Exception {
        Logger.debug("logging method:" + methodFullName);
        CtMethod method = playGround.locateMethod();

        method.addLocalVariable("_elapsed$", CtClass.longType);
        method.insertBefore(logMethodStarting(method));

        CtClass throwableType = ClassPool.getDefault().get("java.lang.Throwable");
        method.addCatch(logMethodException(method), throwableType);

        boolean isVoid = "void".equals(method.getReturnType().getName());
        if (isVoid) {
            method.insertAfter(logMethodReturn(method));
        } else {
            method.insertAfter(logMethodResult(method));
        }
        return aClass;

        // tried to introduce a local variable - PlayKeeper object instead of above approach
        // too tricky to work around limitation around try/catch/finally
        // ref: https://issues.jboss.org/browse/JASSIST-232
    }

    private static String ELAPSED_TIME_START = "_elapsed$ = System.currentTimeMillis();";
    private static String logMethodStarting(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayKeeper.traceArguments(\"%2$s\", \"%3$s\", $args);",
                             ELAPSED_TIME_START, m.getName(), m.getLongName());
    }

    private static String ELAPSED_TIME_END = "_elapsed$ = System.currentTimeMillis() - _elapsed$;";
    private static String logMethodReturn(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayKeeper.traceReturn(\"%2$s\", \"%3$s\", _elapsed$);",
                ELAPSED_TIME_END, m.getName(), m.getLongName());
    }
    private static String logMethodResult(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayKeeper.traceResult(\"%2$s\", \"%3$s\", $_, _elapsed$);",
                ELAPSED_TIME_END, m.getName(), m.getLongName());
    }

    private static String logMethodException(CtMethod m) {
        return String.format("{ jackplay.play.PlayKeeper.traceException(\"%1$s\", \"%2$s\", $e); throw $e; }",
                             m.getName(), m.getLongName());
    }
}
