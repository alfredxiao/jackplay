package jackplay;

import jackplay.model.Options;
import jackplay.model.TraceKeeper;
import jackplay.core.*;
import jackplay.core.performers.Transformer;
import jackplay.web.BoxOffice;
import static jackplay.JackplayLogger.*;

import java.lang.instrument.*;

public class JackplayAgent {
    static Jack jack;
    static InfoCenter infoCenter;

    public static void premain(String agentArgs, Instrumentation inst) {
        Options options = Options.asOptions(agentArgs);
        JackplayLogger.init(options);
        TraceKeeper.init(options);

        info("jack-agent", "running JackPlay with arguments:" + (agentArgs == null ? "there are no args, fallback to defaults." : agentArgs));
        debug("jack-agent", "Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        debug("jack-agent", "Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());
        info("jack-agent", "After web server is started, point your browser to " +
                    (options.https() ? "https" : "http") +
                    "://yourserver:" + options.port());

        Registry pm = new Registry(options);
        Transformer leader = new Transformer(pm);
        infoCenter = new InfoCenter(options, inst, pm);
        jack = new Jack(inst, options, pm, infoCenter, leader);
        BoxOffice boxOffice = new BoxOffice(options, jack, infoCenter);

        boxOffice.start();
    }
}
