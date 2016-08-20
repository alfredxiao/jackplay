package jackplay;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.TraceKeeper;
import jackplay.play.*;
import jackplay.play.performers.LeadPerformer;
import jackplay.web.BoxOffice;

import java.lang.instrument.*;

public class Theatre {
    static Theatre theatre;

    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;
    BoxOffice boxOffice;
    InfoCenter infoCenter;
    Jack jack;

    public Theatre(Options options, Instrumentation inst, ProgramManager pm,
                   LeadPerformer leadPerformer, BoxOffice boxOffice,
                   InfoCenter infoCenter, Jack jack) {
        this.options = options;
        this.inst = inst;
        this.pm = pm;
        this.leadPerformer = leadPerformer;
        this.boxOffice = boxOffice;
        this.infoCenter = infoCenter;
        this.jack = jack;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        Logger.info("jack-agent", "running JackPlay with arguments:" + (agentArgs == null ? "(no args, resort to default)" : agentArgs));
        Logger.debug("jack-agent", "Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        Logger.debug("jack-agent", "Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());

        Options options = Options.optionsMergedWithDefaults(agentArgs);
        Logger.info("jack-agent", "After web server is started, point your browser to " +
                    (options.https() ? "https" : "http") +
                    "://yourserver:" + options.port());

        Logger.init(options);
        TraceKeeper.init(options);

        ProgramManager pm = new ProgramManager();
        LeadPerformer leader = new LeadPerformer();
        BoxOffice boxOffice = new BoxOffice();
        InfoCenter infoCenter = new InfoCenter();
        Jack jack = new Jack();

        theatre = new Theatre(options, inst, pm, leader, boxOffice, infoCenter, jack);
        theatre.init();
        theatre.prepare();
        theatre.start();
    }

    private void init() {
        leadPerformer.init(pm);
        infoCenter.init(inst, pm, options);
        jack.init(inst, options, pm, infoCenter, leadPerformer);
        boxOffice.init(options, jack, infoCenter);
    }

    private void prepare() {
        pm.addTraces(options.defaultTraceAsArray());
    }

    private void start() {
        this.boxOffice.start();
    }
}
