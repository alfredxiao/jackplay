package jackplay;

import java.lang.instrument.*;

public class AgentJack {
    public static void premain(String agentArgs, Instrumentation inst) {
        JackLogger.log("running AgentJack with arguments:" + agentArgs);

        JackOptions options = JackOptions.initialise(agentArgs);
        JackLogger.initialise(options);

        Player player = new Player(options, inst);

        DaemonJack daemonJack = new DaemonJack(options, inst, player);
        daemonJack.start();
    }
}
