package jackplay.core.performers;

import jackplay.JackplayLogger;
import jackplay.model.Site;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;

public class TracingPerformer implements Performer {
    private final Site site;

    public TracingPerformer(Site site) {
        this.site = site;
    }

    @Override
    public CtClass perform(ClassPool cp, CtClass aClass, String mode) throws Exception {
        JackplayLogger.debug("tracingPerformer", "[" + mode + "] starts tracing for method:" + site.methodFullName);

        CtMethod method = this.findMethod(aClass, site);

        method.addLocalVariable("_jackplay_elapsed$", CtClass.longType);
        method.addLocalVariable("_jackplay_uuid$", cp.get("java.lang.String"));
        method.insertBefore(traceMethodEntrance(method));

        CtClass throwableType = cp.get("java.lang.Throwable");
        method.addCatch(traceMethodThrowingException(method), throwableType);

        boolean isVoid = "void".equals(method.getReturnType().getName());
        if (isVoid) {
            method.insertAfter(traceMethodReturningVoid(method));
        } else {
            method.insertAfter(traceMethodReturningResult(method));
        }

        JackplayLogger.info("tracingPerformer", "finished tracing for method:" + site.methodFullName);
        return aClass;

        // tried to introduce a local variable - Keeper object instead of static invocation
        // too tricky to work around limitation around try/catch/finally
        // ref: https://issues.jboss.org/browse/JASSIST-232
    }

    private static String ELAPSED_TIME_START = "_jackplay_elapsed$ = System.currentTimeMillis();";
    private static String DECLARE_UUID = "_jackplay_uuid$ = java.util.UUID.randomUUID().toString();";
    private static String traceMethodEntrance(CtMethod m) {
        return String.format("%1$s ; %2$s; jackplay.core.Keeper.entersMethod(\"%3$s\", $args, _jackplay_uuid$);",
                             ELAPSED_TIME_START, DECLARE_UUID, m.getLongName());
    }

    private static String ELAPSED_TIME_END = "_jackplay_elapsed$ = System.currentTimeMillis() - _jackplay_elapsed$;";
    private static String traceMethodReturningVoid(CtMethod m) {
        return String.format("%1$s ; jackplay.core.Keeper.returnsVoid(\"%2$s\", $args.length, _jackplay_uuid$, _jackplay_elapsed$);",
                ELAPSED_TIME_END, m.getLongName());
    }
    private static String traceMethodReturningResult(CtMethod m) {
        return String.format("%1$s ; jackplay.core.Keeper.returnsResult(\"%2$s\", $args.length, $_, _jackplay_uuid$, _jackplay_elapsed$);",
                ELAPSED_TIME_END, m.getLongName());
    }

    private static String traceMethodThrowingException(CtMethod m) {
        return String.format("{ jackplay.core.Keeper.throwsException(\"%1$s\", $args.length, $e); throw $e; }",
                             m.getLongName());
    }
}