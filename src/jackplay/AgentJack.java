package jackplay;

import jackplay.play.Composer;

import java.lang.instrument.*;

public class AgentJack {
    public static void premain(String agentArgs, Instrumentation inst) {
        JackLogger.log("running AgentJack with arguments:" + agentArgs);
        JackLogger.debug("Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        JackLogger.debug("Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());

        JackOptions options = JackOptions.optionsMergedWithDefaults(agentArgs);
        JackLogger.initialise(options);

        Composer composer = new Composer(options, inst);

        DaemonJack daemonJack = new DaemonJack(options, inst, composer);
        daemonJack.start();
    }
}
