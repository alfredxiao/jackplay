package jackplay.play.performers;

import jackplay.Logger;
import jackplay.bootstrap.PlayGround;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;
import static jackplay.javassist.bytecode.AccessFlag.*;
import jackplay.play.InfoCenter;

import java.rmi.server.ExportException;

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
        CtMethod method = InfoCenter.locateMethod(playGround, playGround.methodFullName, playGround.methodShortName);
        if ((method.getMethodInfo().getAccessFlags() & NATIVE) == NATIVE) {
            throw new Exception("Cannot trace native method!");
        }

        method.addLocalVariable("_elapsed$", CtClass.longType);
        method.addLocalVariable("_uuid$", ClassPool.getDefault().get("java.lang.String"));
        method.insertBefore(traceMethodEntry(method));

        CtClass throwableType = ClassPool.getDefault().get("java.lang.Throwable");
        method.addCatch(traceMethodThrowingException(method), throwableType);

        boolean isVoid = "void".equals(method.getReturnType().getName());
        if (isVoid) {
            method.insertAfter(traceMethodReturningVoid(method));
        } else {
            method.insertAfter(traceMethodReturningResult(method));
        }
        return aClass;

        // tried to introduce a local variable - TraceKeeper object instead of static invocation
        // too tricky to work around limitation around try/catch/finally
        // ref: https://issues.jboss.org/browse/JASSIST-232
    }

    private static String ELAPSED_TIME_START = "_elapsed$ = System.currentTimeMillis();";
    private static String DECLARE_UUID = "_uuid$ = java.util.UUID.randomUUID().toString();";
    private static String traceMethodEntry(CtMethod m) {
        return String.format("%1$s ; %2$s; jackplay.bootstrap.TraceKeeper.enterMethod(\"%3$s\", $args, _uuid$);",
                             ELAPSED_TIME_START, DECLARE_UUID, m.getLongName());
    }

    private static String ELAPSED_TIME_END = "_elapsed$ = System.currentTimeMillis() - _elapsed$;";
    private static String traceMethodReturningVoid(CtMethod m) {
        return String.format("%1$s ; jackplay.bootstrap.TraceKeeper.returnsVoid(\"%2$s\", $args.length, _uuid$, _elapsed$);",
                ELAPSED_TIME_END, m.getLongName());
    }
    private static String traceMethodReturningResult(CtMethod m) {
        return String.format("%1$s ; jackplay.bootstrap.TraceKeeper.returnsResult(\"%2$s\", $args.length, $_, _uuid$, _elapsed$);",
                ELAPSED_TIME_END, m.getLongName());
    }

    private static String traceMethodThrowingException(CtMethod m) {
        return String.format("{ jackplay.bootstrap.TraceKeeper.throwsException(\"%1$s\", $args.length, $e); throw $e; }",
                             m.getLongName());
    }
}
