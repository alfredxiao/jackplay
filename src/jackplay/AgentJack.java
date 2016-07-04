package jackplay;

import java.lang.instrument.*;
import java.util.*;

public class AgentJack {
   public static void premain(String agentArgs, Instrumentation inst) {
       JackLogger.log("running AgentJack with arguments:" + agentArgs);

       JackOptions options = JackOptions.initialise(agentArgs);
       JackLogger.initialise(options);

       DaemonJack daemonJack = new DaemonJack(options, inst);
       daemonJack.start();
   }
}
