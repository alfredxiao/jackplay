package jackplay.play;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class LoggingPerformer implements Performer {
    String methodName;

    public LoggingPerformer(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public CtClass play(CtClass aClass) throws Exception {
        CtMethod method = aClass.getDeclaredMethod(methodName);

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

        // tried to introduce a local variable - PlayLogger object instead of above approach
        // too tricky to work around limitation around try/catch/finally
        // ref: https://issues.jboss.org/browse/JASSIST-232
    }

    private static String ELAPSED_TIME_START = "_elapsed$ = System.currentTimeMillis();";
    private static String logMethodStarting(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayLogger.logArguments(\"%2$s\", \"%3$s\", $args);",
                             ELAPSED_TIME_START, m.getName(), m.getLongName());
    }

    private static String ELAPSED_TIME_END = "_elapsed$ = System.currentTimeMillis() - _elapsed$;";
    private static String logMethodReturn(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayLogger.logReturn(\"%2$s\", \"%3$s\", _elapsed$);",
                ELAPSED_TIME_END, m.getName(), m.getLongName());
    }
    private static String logMethodResult(CtMethod m) {
        return String.format("%1$s ; jackplay.play.PlayLogger.logResult(\"%2$s\", \"%3$s\", $_, _elapsed$);",
                ELAPSED_TIME_END, m.getName(), m.getLongName());
    }

    private static String logMethodException(CtMethod m) {
        return String.format("{ jackplay.play.PlayLogger.logException(\"%1$s\", \"%2$s\", $e); throw $e; }",
                             m.getName(), m.getLongName());
    }
}
