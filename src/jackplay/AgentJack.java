package jackplay;

import jackplay.play.Composer;

import java.lang.instrument.*;

public class AgentJack {
    public static void premain(String agentArgs, Instrumentation inst) {
        JackLogger.log("running AgentJack with arguments:" + agentArgs);

        JackOptions options = JackOptions.optionsMergedWithDefaults(agentArgs);
        JackLogger.initialise(options);

        Composer composer = new Composer(options, inst);

        DaemonJack daemonJack = new DaemonJack(options, inst, composer);
        daemonJack.start();
    }
}
